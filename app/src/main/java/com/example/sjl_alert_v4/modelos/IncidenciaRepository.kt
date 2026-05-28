package com.example.sjl_alert_v4.modelos

import com.example.sjl_alert_v4.red.SupabaseClient
import kotlinx.coroutines.flow.Flow

// Repository: capa intermedia entre la UI y los datos
// Guarda en Room (local) Y sincroniza con Azure SQL (remoto)
class IncidenciaRepository(private val dao: IncidenciaDao) {

    // ── CAMINO A: Servidor Node.js (Express) migrado a Supabase
    // private val api = RetrofitClient.incidenciaApi

    // ── CAMINO B: Conexión directa a Supabase REST API (descomenta si usas este camino)
    private val api = SupabaseClient.api

    // ── CREATE ────────────────────────────────────────────────────────────
    suspend fun crear(incidencia: IncidenciaEntity): ResultadoApi {
        return try {
            // 1. Guarda en Room (local) inmediatamente
            dao.insertar(incidencia)
            // 2. Sincroniza con Azure SQL (remoto)
            val response = api.crear(incidencia.toDto())
            if (response.isSuccessful) {
                ResultadoApi.Exito("Incidencia creada en Supabase")
            } else {
                ResultadoApi.Error("Error API: ${response.code()} ${response.message()}")
            }
        } catch (e: Exception) {
            // Si falla la red, igual quedó guardado en Room
            ResultadoApi.Error("Sin conexión. Guardado localmente: ${e.message}")
        }
    }

    // ── READ — obtener por usuario (Flow desde Room) ───────────────────────
    fun obtenerPorUsuario(usuarioId: Int): Flow<List<IncidenciaEntity>> =
        dao.obtenerPorUsuario(usuarioId)

    // ── READ — sincronizar desde Supabase ────────────────────────────────────
    suspend fun sincronizarConSupabase(usuarioId: Int): ResultadoApi {
        return try {
            // [Camino A]:
            // val response = api.obtenerPorUsuario(usuarioId)
            // [Camino B] (Descomenta si usas conexión directa a Supabase):
            val response = api.obtenerPorUsuario("eq.$usuarioId")

            if (response.isSuccessful) {
                response.body()?.forEach { dto ->
                    dao.insertar(dto.toEntity())   // upsert en Room
                }
                ResultadoApi.Exito("Sincronizado con Supabase")
            } else {
                ResultadoApi.Error("Error al sincronizar: ${response.code()}")
            }
        } catch (e: Exception) {
            ResultadoApi.Error("Sin conexión a Supabase: ${e.message}")
        }
    }

    // ── READ — obtener todas desde Supabase (para admin) ─────────────────────
    suspend fun obtenerTodasDesdeSupabase(): ResultadoApi {
        return try {
            val response = api.obtenerTodas()
            if (response.isSuccessful) {
                response.body()?.forEach { dto -> dao.insertar(dto.toEntity()) }
                ResultadoApi.Exito("OK")
            } else {
                ResultadoApi.Error("Error: ${response.code()}")
            }
        } catch (e: Exception) {
            ResultadoApi.Error(e.message ?: "Error desconocido")
        }
    }

    // ── UPDATE ────────────────────────────────────────────────────────────
    suspend fun actualizar(incidencia: IncidenciaEntity): ResultadoApi {
        return try {
            // 1. Actualiza en Room
            dao.insertar(incidencia)   // REPLACE actualiza si ya existe
            // 2. Sincroniza con Azure
            // [Camino A]:
            // val response = api.actualizar(incidencia.id, incidencia.toDto())
            // [Camino B] (Descomenta si usas conexión directa a Supabase):
            val response = api.actualizar("eq.${incidencia.id}", incidencia.toDto())

            if (response.isSuccessful) {
                ResultadoApi.Exito("Actualizado en Supabase")
            } else {
                ResultadoApi.Error("Error API: ${response.code()}")
            }
        } catch (e: Exception) {
            ResultadoApi.Error("Sin conexión. Actualizado localmente: ${e.message}")
        }
    }

    // ── DELETE ────────────────────────────────────────────────────────────
    suspend fun eliminar(incidencia: IncidenciaEntity): ResultadoApi {
        return try {
            // 1. Elimina de Room
            dao.eliminar(incidencia)
            // 2. Elimina de Azure
            // [Camino A]:
            // val response = api.eliminar(incidencia.id)
            // [Camino B] (Descomenta si usas conexión directa a Supabase):
            val response = api.eliminar("eq.${incidencia.id}")

            if (response.isSuccessful) {
                ResultadoApi.Exito("Eliminado de Supabase")
            } else {
                ResultadoApi.Error("Error API: ${response.code()}")
            }
        } catch (e: Exception) {
            ResultadoApi.Error("Sin conexión. Eliminado localmente: ${e.message}")
        }
    }
}

// Resultado genérico para operaciones de la API
sealed class ResultadoApi {
    data class Exito(val mensaje: String) : ResultadoApi()
    data class Error(val mensaje: String) : ResultadoApi()
}