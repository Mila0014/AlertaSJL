const express    = require("express");
const sql        = require("mssql");
const cors       = require("cors");
const nodemailer = require("nodemailer");
require("dotenv").config();

const app = express();
app.use(cors());
app.use(express.json());

// ── Mapa temporal de códigos de recuperación (en memoria) ─────────────────
const codigosRecuperacion = new Map();

// ── Configuración de correo Gmail ─────────────────────────────────────────
const transporter = nodemailer.createTransport({
  service: "gmail",
  auth: {
    user: "ingsistemascomp123@gmail.com",
    pass: "cyzv jgnf iwat cbvs"
  }
});

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

app.post("/api/usuarios/registro", async (req, res) => {
  try {
    const { nombre, apellido, dni, correo, telefono,
            contrasena, direccion, fechaRegistro, fechaNacimiento } = req.body;

    if (!nombre || !dni || !correo || !contrasena) {
      return res.status(400).json({ error: "nombre, dni, correo y contrasena son obligatorios" });
    }

    const p = await getPool();

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

app.post("/api/usuarios/login", async (req, res) => {
  try {
    const { dniOCorreo, contrasena } = req.body;

    if (!dniOCorreo || !contrasena) {
      return res.status(400).json({ error: "dniOCorreo y contrasena son obligatorios" });
    }

    const p = await getPool();

    const busqueda = await p.request()
      .input("dniOCorreo", sql.NVarChar(200), dniOCorreo.trim())
      .query("SELECT * FROM usuarios WHERE dni = @dniOCorreo OR correo = @dniOCorreo");

    if (busqueda.recordset.length === 0) {
      return res.status(404).json({ error: "Usuario no encontrado" });
    }

    const usuario = busqueda.recordset[0];

    if (usuario.contrasena !== contrasena) {
      return res.status(401).json({ error: "Contraseña incorrecta" });
    }

    res.status(200).json({
      mensaje: "Login exitoso",
      usuario: {
        id:        usuario.id,
        nombre:    usuario.nombre,
        apellido:  usuario.apellido,
        dni:       usuario.dni,
        correo:    usuario.correo,
        telefono:  usuario.telefono,
        direccion: usuario.direccion
      }
    });

  } catch (err) {
    console.error(err);
    res.status(500).json({ error: err.message });
  }
});

app.get("/api/usuarios", async (req, res) => {
  try {
    const p = await getPool();
    const r = await p.request().query(
      "SELECT id, nombre, apellido, dni, correo, telefono, direccion, fechaRegistro, fechaNacimiento FROM usuarios ORDER BY id DESC"
    );
    res.json(r.recordset);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

app.get("/api/usuarios/:id", async (req, res) => {
  try {
    const p = await getPool();
    const r = await p.request()
      .input("id", sql.Int, parseInt(req.params.id))
      .query("SELECT id, nombre, apellido, dni, correo, telefono, direccion, fechaRegistro, fechaNacimiento FROM usuarios WHERE id = @id");
    if (r.recordset.length === 0) return res.status(404).json({ error: "Usuario no encontrado" });
    res.json(r.recordset[0]);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

app.put("/api/usuarios/:id", async (req, res) => {
  try {
    const { nombre, apellido, dni, correo, telefono, direccion, fechaNacimiento } = req.body;
    const p = await getPool();
    const r = await p.request()
      .input("id",              sql.Int,           parseInt(req.params.id))
      .input("nombre",          sql.NVarChar(100), nombre         || "")
      .input("apellido",        sql.NVarChar(100), apellido       || "")
      .input("dni",             sql.NVarChar(20),  dni            || "")
      .input("correo",          sql.NVarChar(200), (correo || "").toLowerCase())
      .input("telefono",        sql.NVarChar(20),  telefono       || "")
      .input("direccion",       sql.NVarChar(300), direccion      || "")
      .input("fechaNacimiento", sql.NVarChar(20),  fechaNacimiento || "")
      .query(`
        UPDATE usuarios SET
          nombre=@nombre, apellido=@apellido, dni=@dni, correo=@correo,
          telefono=@telefono, direccion=@direccion, fechaNacimiento=@fechaNacimiento
        WHERE id=@id
      `);
    if (r.rowsAffected[0] === 0) return res.status(404).json({ error: "Usuario no encontrado" });
    res.json({ mensaje: "Usuario actualizado", id: req.params.id });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

app.delete("/api/usuarios/:id", async (req, res) => {
  try {
    const p = await getPool();
    const r = await p.request()
      .input("id", sql.Int, parseInt(req.params.id))
      .query("DELETE FROM usuarios WHERE id = @id");
    if (r.rowsAffected[0] === 0) return res.status(404).json({ error: "Usuario no encontrado" });
    res.json({ mensaje: "Usuario eliminado", id: req.params.id });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// ═════════════════════════════════════════════════════════════════════════════
// INCIDENCIAS
// ═════════════════════════════════════════════════════════════════════════════

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

app.get("/api/incidencias", async (req, res) => {
  try {
    const p = await getPool();
    const r = await p.request().query("SELECT * FROM incidencias ORDER BY fecha DESC");
    res.json(r.recordset);
  } catch (err) { res.status(500).json({ error: err.message }); }
});

app.get("/api/incidencias/usuario/:usuarioId", async (req, res) => {
  try {
    const p = await getPool();
    const r = await p.request()
      .input("usuarioId", sql.Int, parseInt(req.params.usuarioId))
      .query("SELECT * FROM incidencias WHERE usuarioId = @usuarioId ORDER BY fecha DESC");
    res.json(r.recordset);
  } catch (err) { res.status(500).json({ error: err.message }); }
});

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

// ═════════════════════════════════════════════════════════════════════════════
// RECUPERACIÓN DE CONTRASEÑA
// ═════════════════════════════════════════════════════════════════════════════

app.post("/api/usuarios/recuperar", async (req, res) => {
  try {
    const { correo } = req.body;
    if (!correo) return res.status(400).json({ error: "Correo es obligatorio" });

    const p = await getPool();
    const r = await p.request()
      .input("correo", sql.NVarChar(200), correo.trim().toLowerCase())
      .query("SELECT id FROM usuarios WHERE correo = @correo");

    if (r.recordset.length === 0) {
      return res.status(404).json({ error: "Correo no registrado" });
    }

    const codigo = Math.floor(100000 + Math.random() * 900000).toString();
    const expira = Date.now() + 10 * 60 * 1000;

    codigosRecuperacion.set(correo.trim().toLowerCase(), { codigo, expira });

    await transporter.sendMail({
      from: '"SJL Alerta" <ingsistemascomp123@gmail.com>',
      to: correo.trim(),
      subject: "Código de recuperación - SJL Alerta",
      html: `
        <div style="font-family:Arial,sans-serif;max-width:480px;margin:auto;padding:24px;border-radius:12px;border:1px solid #eee;">
          <h2 style="color:#1a73e8;">SJL Alerta</h2>
          <p>Hola, recibimos una solicitud para recuperar tu contraseña.</p>
          <p>Tu código de verificación es:</p>
          <div style="font-size:36px;font-weight:bold;letter-spacing:12px;color:#1a73e8;text-align:center;padding:16px;">
            ${codigo}
          </div>
          <p style="color:#888;font-size:13px;">Este código expira en 10 minutos. Si no solicitaste esto, ignora este correo.</p>
        </div>
      `
    });

    res.json({ mensaje: "Código enviado al correo" });
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: err.message });
  }
});

app.post("/api/usuarios/verificar-codigo", async (req, res) => {
  try {
    const { correo, codigo } = req.body;
    if (!correo || !codigo) return res.status(400).json({ error: "Correo y código son obligatorios" });

    const key = correo.trim().toLowerCase();
    const registro = codigosRecuperacion.get(key);

    if (!registro) return res.status(400).json({ error: "No hay código activo para este correo" });
    if (Date.now() > registro.expira) {
      codigosRecuperacion.delete(key);
      return res.status(400).json({ error: "El código ha expirado" });
    }
    if (registro.codigo !== codigo.trim()) {
      return res.status(400).json({ error: "Código incorrecto" });
    }

    res.json({ mensaje: "Código verificado correctamente" });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

app.post("/api/usuarios/nueva-contrasena", async (req, res) => {
  try {
    const { correo, codigo, nuevaContrasena } = req.body;
    if (!correo || !codigo || !nuevaContrasena) {
      return res.status(400).json({ error: "Correo, código y nueva contraseña son obligatorios" });
    }

    const key = correo.trim().toLowerCase();
    const registro = codigosRecuperacion.get(key);

    if (!registro) return res.status(400).json({ error: "No hay código activo" });
    if (Date.now() > registro.expira) {
      codigosRecuperacion.delete(key);
      return res.status(400).json({ error: "El código ha expirado" });
    }
    if (registro.codigo !== codigo.trim()) {
      return res.status(400).json({ error: "Código incorrecto" });
    }

    const p = await getPool();
    const r = await p.request()
      .input("correo",     sql.NVarChar(200), key)
      .input("contrasena", sql.NVarChar(200), nuevaContrasena)
      .query("UPDATE usuarios SET contrasena = @contrasena WHERE correo = @correo");

    if (r.rowsAffected[0] === 0) return res.status(404).json({ error: "Usuario no encontrado" });

    codigosRecuperacion.delete(key);
    res.json({ mensaje: "Contraseña actualizada correctamente" });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// Health check
app.get("/", (req, res) => res.json({ estado: "API SJL Alerta funcionando ✅" }));

const PORT = process.env.PORT || 3000;
app.listen(PORT, async () => {
  console.log(`🚀 Servidor corriendo en puerto ${PORT}`);
  await crearTablas();
});