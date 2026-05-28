const express = require("express");
const { Pool } = require("pg");
const cors    = require("cors");
require("dotenv").config();

const app = express();
app.use(cors());
app.use(express.json());

// ── Configuración Supabase / PostgreSQL ──────────────────────────────────────
const poolConfig = process.env.DATABASE_URL
  ? { connectionString: process.env.DATABASE_URL }
  : {
      user:     process.env.DB_USER,
      password: process.env.DB_PASSWORD,
      host:     process.env.DB_SERVER || process.env.DB_HOST,
      database: process.env.DB_NAME,
      port:     parseInt(process.env.DB_PORT) || 5432,
    };

// Habilitar SSL para conexiones remotas (ej: Supabase)
if (
  poolConfig.connectionString &&
  !poolConfig.connectionString.includes("localhost") &&
  !poolConfig.connectionString.includes("127.0.0.1")
) {
  poolConfig.ssl = { rejectUnauthorized: false };
} else if (
  poolConfig.host &&
  poolConfig.host !== "localhost" &&
  poolConfig.host !== "127.0.0.1"
) {
  poolConfig.ssl = { rejectUnauthorized: false };
}

const pool = new Pool(poolConfig);

// ── Crear tabla si no existe (se ejecuta al iniciar) ──────────────────────
async function crearTablaIncidencias() {
  try {
    await pool.query(`
      CREATE TABLE IF NOT EXISTS public.incidencias (
        id          VARCHAR(50)   PRIMARY KEY,
        tipo        VARCHAR(100)  NOT NULL,
        descripcion VARCHAR(500)  DEFAULT '',
        ubicacion   VARCHAR(300)  DEFAULT '',
        latitud     DOUBLE PRECISION NULL,
        longitud    DOUBLE PRECISION NULL,
        evidencias  TEXT          DEFAULT '',
        "imagenUri"   VARCHAR(500)  NULL,
        fecha       BIGINT         DEFAULT 0,
        estado      VARCHAR(50)   DEFAULT 'PENDIENTE',
        "usuarioId"   INT            DEFAULT 0
      )
    `);
    console.log("✅ Tabla incidencias lista en PostgreSQL / Supabase");
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

    const query = `
      INSERT INTO incidencias
        (id, tipo, descripcion, ubicacion, latitud, longitud,
         evidencias, "imagenUri", fecha, estado, "usuarioId")
      VALUES
        ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11)
    `;
    const values = [
      id,
      tipo,
      descripcion || "",
      ubicacion || "",
      latitud ?? null,
      longitud ?? null,
      evidencias || "",
      imagenUri ?? null,
      fecha || Date.now(),
      estado || "PENDIENTE",
      usuarioId || 0
    ];

    await pool.query(query, values);
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
    const result = await pool.query("SELECT * FROM incidencias ORDER BY fecha DESC");
    res.json(result.rows);
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
    const result = await pool.query("SELECT * FROM incidencias WHERE id = $1", [req.params.id]);

    if (result.rows.length === 0) {
      return res.status(404).json({ error: "Incidencia no encontrada" });
    }
    res.json(result.rows[0]);
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
    const result = await pool.query(
      'SELECT * FROM incidencias WHERE "usuarioId" = $1 ORDER BY fecha DESC',
      [parseInt(req.params.usuarioId)]
    );
    res.json(result.rows);
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

    const query = `
      UPDATE incidencias SET
        tipo        = $2,
        descripcion = $3,
        ubicacion   = $4,
        latitud     = $5,
        longitud    = $6,
        evidencias  = $7,
        "imagenUri"   = $8,
        estado      = $9
      WHERE id = $1
    `;
    const values = [
      req.params.id,
      tipo,
      descripcion || "",
      ubicacion || "",
      latitud ?? null,
      longitud ?? null,
      evidencias || "",
      imagenUri ?? null,
      estado || "PENDIENTE"
    ];

    const result = await pool.query(query, values);

    if (result.rowCount === 0) {
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
    const result = await pool.query("DELETE FROM incidencias WHERE id = $1", [req.params.id]);

    if (result.rowCount === 0) {
      return res.status(404).json({ error: "Incidencia no encontrada" });
    }
    res.json({ mensaje: "Incidencia eliminada", id: req.params.id });
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: err.message });
  }
});

// ── Health check ───────────────────────────────────────────────────────────
app.get("/", (req, res) => res.json({ estado: "API SJL Alerta funcionando con Supabase / Postgres ✅" }));

// ── Iniciar servidor ───────────────────────────────────────────────────────
const PORT = process.env.PORT || 3000;
app.listen(PORT, async () => {
  console.log(`🚀 Servidor corriendo en puerto ${PORT}`);
  await crearTablaIncidencias();
});
