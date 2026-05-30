const express = require("express");
const sql     = require("mssql");
const cors    = require("cors");
require("dotenv").config();

const app = express();
app.use(cors());
app.use(express.json());

// ── Configuración Azure SQL ────────────────────────────────────────────────
const dbConfig = {
  user:     process.env.DB_USER,
  password: process.env.DB_PASSWORD,
  server:   process.env.DB_SERVER,
  database: process.env.DB_NAME,
  port:     parseInt(process.env.DB_PORT) || 1433,
  options: { encrypt: true, trustServerCertificate: false }
};

let pool;
async function getPool() {
  if (!pool) pool = await sql.connect(dbConfig);
  return pool;
}

// ── Crear tablas si no existen ─────────────────────────────────────────────
async function crearTablas() {
  try {
    const p = await getPool();

    // Tabla usuarios
    await p.request().query(`
      IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='usuarios' AND xtype='U')
      CREATE TABLE usuarios (
        id              INT IDENTITY(1,1) PRIMARY KEY,
        nombre          NVARCHAR(100) NOT NULL,
        apellido        NVARCHAR(100) NOT NULL,
        dni             NVARCHAR(20)  NOT NULL UNIQUE,
        correo          NVARCHAR(200) NOT NULL UNIQUE,
        telefono        NVARCHAR(20)  DEFAULT '',
        contrasena      NVARCHAR(200) NOT NULL,
        direccion       NVARCHAR(300) DEFAULT '',
        fechaRegistro   BIGINT        DEFAULT 0,
        fechaNacimiento NVARCHAR(20)  DEFAULT ''
      )
    `);

    // Tabla incidencias
    await p.request().query(`
      IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='incidencias' AND xtype='U')
      CREATE TABLE incidencias (
        id          NVARCHAR(50)   PRIMARY KEY,
        tipo        NVARCHAR(100)  NOT NULL,
        descripcion NVARCHAR(500)  DEFAULT '',
        ubicacion   NVARCHAR(300)  DEFAULT '',
        latitud     FLOAT          NULL,
        longitud    FLOAT          NULL,
        evidencias  NVARCHAR(MAX)  DEFAULT '',
        imagenUri   NVARCHAR(500)  NULL,
        fecha       BIGINT         DEFAULT 0,
        estado      NVARCHAR(50)   DEFAULT 'PENDIENTE',
        usuarioId   INT            DEFAULT 0
      )
    `);

    console.log("✅ Tablas listas");
  } catch (err) {
    console.error("❌ Error creando tablas:", err.message);
  }
}

// ═════════════════════════════════════════════════════════════════════════════
// USUARIOS
// ═════════════════════════════════════════════════════════════════════════════

// REGISTRO: POST /api/usuarios/registro
app.post("/api/usuarios/registro", async (req, res) => {
  try {
    const { nombre, apellido, dni, correo, telefono,
            contrasena, direccion, fechaRegistro, fechaNacimiento } = req.body;

    if (!nombre || !dni || !correo || !contrasena) {
      return res.status(400).json({ error: "nombre, dni, correo y contrasena son obligatorios" });
    }

    const p = await getPool();

    // Verificar si ya existe
    const existe = await p.request()
      .input("dni",    sql.NVarChar(20),  dni.trim())
      .input("correo", sql.NVarChar(200), correo.trim().toLowerCase())
      .query("SELECT id, dni, correo FROM usuarios WHERE dni = @dni OR correo = @correo");

    if (existe.recordset.length > 0) {
      const encontrado = existe.recordset[0];
      const campo = encontrado.correo === correo.trim().toLowerCase()
        ? "El correo ya está registrado"
        : "El DNI ya está registrado";
      return res.status(409).json({ error: campo });
    }

    // Insertar usuario
    const result = await p.request()
      .input("nombre",          sql.NVarChar(100), nombre.trim())
      .input("apellido",        sql.NVarChar(100), (apellido || "").trim())
      .input("dni",             sql.NVarChar(20),  dni.trim())
      .input("correo",          sql.NVarChar(200), correo.trim().toLowerCase())
      .input("telefono",        sql.NVarChar(20),  (telefono || "").trim())
      .input("contrasena",      sql.NVarChar(200), contrasena)
      .input("direccion",       sql.NVarChar(300), (direccion || "").trim())
      .input("fechaRegistro",   sql.BigInt,        fechaRegistro || Date.now())
      .input("fechaNacimiento", sql.NVarChar(20),  fechaNacimiento || "")
      .query(`
        INSERT INTO usuarios
          (nombre, apellido, dni, correo, telefono, contrasena, direccion, fechaRegistro, fechaNacimiento)
        OUTPUT INSERTED.id
        VALUES
          (@nombre, @apellido, @dni, @correo, @telefono, @contrasena, @direccion, @fechaRegistro, @fechaNacimiento)
      `);

    const nuevoId = result.recordset[0].id;
    res.status(201).json({ mensaje: "Usuario registrado", id: nuevoId });

  } catch (err) {
    console.error(err);
    res.status(500).json({ error: err.message });
  }
});

// LOGIN: POST /api/usuarios/login
app.post("/api/usuarios/login", async (req, res) => {
  try {
    const { dniOCorreo, contrasena } = req.body;

    if (!dniOCorreo || !contrasena) {
      return res.status(400).json({ error: "dniOCorreo y contrasena son obligatorios" });
    }

    const p = await getPool();

    // Buscar usuario por DNI o correo
    const busqueda = await p.request()
      .input("dniOCorreo", sql.NVarChar(200), dniOCorreo.trim())
      .query(`
        SELECT * FROM usuarios
        WHERE dni = @dniOCorreo OR correo = @dniOCorreo
      `);

    if (busqueda.recordset.length === 0) {
      return res.status(404).json({ error: "Usuario no encontrado" });
    }

    const usuario = busqueda.recordset[0];

    // Verificar contraseña (hash SHA-256)
    if (usuario.contrasena !== contrasena) {
      return res.status(401).json({ error: "Contraseña incorrecta" });
    }

    // Login exitoso — devuelve datos del usuario (sin contraseña)
    res.status(200).json({
      mensaje: "Login exitoso",
      usuario: {
        id:       usuario.id,
        nombre:   usuario.nombre,
        apellido: usuario.apellido,
        dni:      usuario.dni,
        correo:   usuario.correo,
        telefono: usuario.telefono,
        direccion: usuario.direccion
      }
    });

  } catch (err) {
    console.error(err);
    res.status(500).json({ error: err.message });
  }
});

// ═════════════════════════════════════════════════════════════════════════════
// INCIDENCIAS — CRUD completo
// ═════════════════════════════════════════════════════════════════════════════

// CREATE: POST /api/incidencias
app.post("/api/incidencias", async (req, res) => {
  try {
    const { id, tipo, descripcion, ubicacion, latitud, longitud,
            evidencias, imagenUri, fecha, estado, usuarioId } = req.body;

    if (!id || !tipo) return res.status(400).json({ error: "id y tipo son obligatorios" });

    const p = await getPool();
    await p.request()
      .input("id",          sql.NVarChar(50),      id)
      .input("tipo",        sql.NVarChar(100),     tipo)
      .input("descripcion", sql.NVarChar(500),     descripcion || "")
      .input("ubicacion",   sql.NVarChar(300),     ubicacion   || "")
      .input("latitud",     sql.Float,             latitud     ?? null)
      .input("longitud",    sql.Float,             longitud    ?? null)
      .input("evidencias",  sql.NVarChar(sql.MAX), evidencias  || "")
      .input("imagenUri",   sql.NVarChar(500),     imagenUri   ?? null)
      .input("fecha",       sql.BigInt,            fecha       || Date.now())
      .input("estado",      sql.NVarChar(50),      estado      || "PENDIENTE")
      .input("usuarioId",   sql.Int,               usuarioId   || 0)
      .query(`
        INSERT INTO incidencias
          (id,tipo,descripcion,ubicacion,latitud,longitud,evidencias,imagenUri,fecha,estado,usuarioId)
        VALUES
          (@id,@tipo,@descripcion,@ubicacion,@latitud,@longitud,@evidencias,@imagenUri,@fecha,@estado,@usuarioId)
      `);

    res.status(201).json({ mensaje: "Incidencia creada", id });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// READ ALL: GET /api/incidencias
app.get("/api/incidencias", async (req, res) => {
  try {
    const p = await getPool();
    const r = await p.request().query("SELECT * FROM incidencias ORDER BY fecha DESC");
    res.json(r.recordset);
  } catch (err) { res.status(500).json({ error: err.message }); }
});

// READ BY USER: GET /api/incidencias/usuario/:usuarioId
app.get("/api/incidencias/usuario/:usuarioId", async (req, res) => {
  try {
    const p = await getPool();
    const r = await p.request()
      .input("usuarioId", sql.Int, parseInt(req.params.usuarioId))
      .query("SELECT * FROM incidencias WHERE usuarioId = @usuarioId ORDER BY fecha DESC");
    res.json(r.recordset);
  } catch (err) { res.status(500).json({ error: err.message }); }
});

// READ ONE: GET /api/incidencias/:id
app.get("/api/incidencias/:id", async (req, res) => {
  try {
    const p = await getPool();
    const r = await p.request()
      .input("id", sql.NVarChar(50), req.params.id)
      .query("SELECT * FROM incidencias WHERE id = @id");
    if (r.recordset.length === 0) return res.status(404).json({ error: "No encontrada" });
    res.json(r.recordset[0]);
  } catch (err) { res.status(500).json({ error: err.message }); }
});

// UPDATE: PUT /api/incidencias/:id
app.put("/api/incidencias/:id", async (req, res) => {
  try {
    const { tipo, descripcion, ubicacion, latitud, longitud, evidencias, imagenUri, estado } = req.body;
    const p = await getPool();
    const r = await p.request()
      .input("id",          sql.NVarChar(50),      req.params.id)
      .input("tipo",        sql.NVarChar(100),     tipo)
      .input("descripcion", sql.NVarChar(500),     descripcion || "")
      .input("ubicacion",   sql.NVarChar(300),     ubicacion   || "")
      .input("latitud",     sql.Float,             latitud     ?? null)
      .input("longitud",    sql.Float,             longitud    ?? null)
      .input("evidencias",  sql.NVarChar(sql.MAX), evidencias  || "")
      .input("imagenUri",   sql.NVarChar(500),     imagenUri   ?? null)
      .input("estado",      sql.NVarChar(50),      estado      || "PENDIENTE")
      .query(`
        UPDATE incidencias SET
          tipo=@tipo, descripcion=@descripcion, ubicacion=@ubicacion,
          latitud=@latitud, longitud=@longitud, evidencias=@evidencias,
          imagenUri=@imagenUri, estado=@estado
        WHERE id=@id
      `);
    if (r.rowsAffected[0] === 0) return res.status(404).json({ error: "No encontrada" });
    res.json({ mensaje: "Actualizada", id: req.params.id });
  } catch (err) { res.status(500).json({ error: err.message }); }
});

// DELETE: DELETE /api/incidencias/:id
app.delete("/api/incidencias/:id", async (req, res) => {
  try {
    const p = await getPool();
    const r = await p.request()
      .input("id", sql.NVarChar(50), req.params.id)
      .query("DELETE FROM incidencias WHERE id = @id");
    if (r.rowsAffected[0] === 0) return res.status(404).json({ error: "No encontrada" });
    res.json({ mensaje: "Eliminada", id: req.params.id });
  } catch (err) { res.status(500).json({ error: err.message }); }
});

// Health check
app.get("/", (req, res) => res.json({ estado: "API SJL Alerta funcionando ✅" }));

const PORT = process.env.PORT || 3000;
app.listen(PORT, async () => {
  console.log(`🚀 Servidor corriendo en puerto ${PORT}`);
  await crearTablas();
});