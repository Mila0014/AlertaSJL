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
  options: {
    encrypt:                true,   // obligatorio en Azure SQL
    trustServerCertificate: false
  }
};

// Pool de conexión reutilizable
let pool;
async function getPool() {
  if (!pool) pool = await sql.connect(dbConfig);
  return pool;
}

// ── Crear tabla si no existe (se ejecuta al iniciar) ──────────────────────
async function crearTablaIncidencias() {
  try {
    const p = await getPool();
    await p.request().query(`
      IF NOT EXISTS (
        SELECT * FROM sysobjects WHERE name='incidencias' AND xtype='U'
      )
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
    console.log("✅ Tabla incidencias lista");
  } catch (err) {
    console.error("❌ Error creando tabla:", err.message);
  }
}

// ─────────────────────────────────────────────────────────────────────────────
// C — CREATE: POST /api/incidencias
// ─────────────────────────────────────────────────────────────────────────────
app.post("/api/incidencias", async (req, res) => {
  try {
    const { id, tipo, descripcion, ubicacion, latitud, longitud,
            evidencias, imagenUri, fecha, estado, usuarioId } = req.body;

    if (!id || !tipo) {
      return res.status(400).json({ error: "id y tipo son obligatorios" });
    }

    const p = await getPool();
    await p.request()
      .input("id",          sql.NVarChar(50),  id)
      .input("tipo",        sql.NVarChar(100), tipo)
      .input("descripcion", sql.NVarChar(500), descripcion || "")
      .input("ubicacion",   sql.NVarChar(300), ubicacion   || "")
      .input("latitud",     sql.Float,         latitud     ?? null)
      .input("longitud",    sql.Float,         longitud    ?? null)
      .input("evidencias",  sql.NVarChar(sql.MAX), evidencias || "")
      .input("imagenUri",   sql.NVarChar(500), imagenUri   ?? null)
      .input("fecha",       sql.BigInt,        fecha       || Date.now())
      .input("estado",      sql.NVarChar(50),  estado      || "PENDIENTE")
      .input("usuarioId",   sql.Int,           usuarioId   || 0)
      .query(`
        INSERT INTO incidencias
          (id, tipo, descripcion, ubicacion, latitud, longitud,
           evidencias, imagenUri, fecha, estado, usuarioId)
        VALUES
          (@id, @tipo, @descripcion, @ubicacion, @latitud, @longitud,
           @evidencias, @imagenUri, @fecha, @estado, @usuarioId)
      `);

    res.status(201).json({ mensaje: "Incidencia creada", id });
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: err.message });
  }
});

// ─────────────────────────────────────────────────────────────────────────────
// R — READ ALL: GET /api/incidencias
// ─────────────────────────────────────────────────────────────────────────────
app.get("/api/incidencias", async (req, res) => {
  try {
    const p      = await getPool();
    const result = await p.request()
      .query("SELECT * FROM incidencias ORDER BY fecha DESC");
    res.json(result.recordset);
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: err.message });
  }
});

// ─────────────────────────────────────────────────────────────────────────────
// R — READ ONE: GET /api/incidencias/:id
// ─────────────────────────────────────────────────────────────────────────────
app.get("/api/incidencias/:id", async (req, res) => {
  try {
    const p      = await getPool();
    const result = await p.request()
      .input("id", sql.NVarChar(50), req.params.id)
      .query("SELECT * FROM incidencias WHERE id = @id");

    if (result.recordset.length === 0) {
      return res.status(404).json({ error: "Incidencia no encontrada" });
    }
    res.json(result.recordset[0]);
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: err.message });
  }
});

// ─────────────────────────────────────────────────────────────────────────────
// R — READ BY USER: GET /api/incidencias/usuario/:usuarioId
// ─────────────────────────────────────────────────────────────────────────────
app.get("/api/incidencias/usuario/:usuarioId", async (req, res) => {
  try {
    const p      = await getPool();
    const result = await p.request()
      .input("usuarioId", sql.Int, parseInt(req.params.usuarioId))
      .query("SELECT * FROM incidencias WHERE usuarioId = @usuarioId ORDER BY fecha DESC");
    res.json(result.recordset);
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: err.message });
  }
});

// ─────────────────────────────────────────────────────────────────────────────
// U — UPDATE: PUT /api/incidencias/:id
// ─────────────────────────────────────────────────────────────────────────────
app.put("/api/incidencias/:id", async (req, res) => {
  try {
    const { tipo, descripcion, ubicacion, latitud, longitud,
            evidencias, imagenUri, estado } = req.body;

    const p = await getPool();
    const result = await p.request()
      .input("id",          sql.NVarChar(50),  req.params.id)
      .input("tipo",        sql.NVarChar(100), tipo)
      .input("descripcion", sql.NVarChar(500), descripcion || "")
      .input("ubicacion",   sql.NVarChar(300), ubicacion   || "")
      .input("latitud",     sql.Float,         latitud     ?? null)
      .input("longitud",    sql.Float,         longitud    ?? null)
      .input("evidencias",  sql.NVarChar(sql.MAX), evidencias || "")
      .input("imagenUri",   sql.NVarChar(500), imagenUri   ?? null)
      .input("estado",      sql.NVarChar(50),  estado      || "PENDIENTE")
      .query(`
        UPDATE incidencias SET
          tipo        = @tipo,
          descripcion = @descripcion,
          ubicacion   = @ubicacion,
          latitud     = @latitud,
          longitud    = @longitud,
          evidencias  = @evidencias,
          imagenUri   = @imagenUri,
          estado      = @estado
        WHERE id = @id
      `);

    if (result.rowsAffected[0] === 0) {
      return res.status(404).json({ error: "Incidencia no encontrada" });
    }
    res.json({ mensaje: "Incidencia actualizada", id: req.params.id });
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: err.message });
  }
});

// ─────────────────────────────────────────────────────────────────────────────
// D — DELETE: DELETE /api/incidencias/:id
// ─────────────────────────────────────────────────────────────────────────────
app.delete("/api/incidencias/:id", async (req, res) => {
  try {
    const p      = await getPool();
    const result = await p.request()
      .input("id", sql.NVarChar(50), req.params.id)
      .query("DELETE FROM incidencias WHERE id = @id");

    if (result.rowsAffected[0] === 0) {
      return res.status(404).json({ error: "Incidencia no encontrada" });
    }
    res.json({ mensaje: "Incidencia eliminada", id: req.params.id });
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: err.message });
  }
});

// ── Health check ───────────────────────────────────────────────────────────
app.get("/", (req, res) => res.json({ estado: "API SJL Alerta funcionando ✅" }));

// ── Iniciar servidor ───────────────────────────────────────────────────────
const PORT = process.env.PORT || 3000;
app.listen(PORT, async () => {
  console.log(`🚀 Servidor corriendo en puerto ${PORT}`);
  await crearTablaIncidencias();
});
