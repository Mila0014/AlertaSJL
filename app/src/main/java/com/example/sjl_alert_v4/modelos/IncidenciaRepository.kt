package com.example.sjl_alert_v4.modelos

import com.example.sjl_alert_v4.red.RetrofitClient
import kotlinx.coroutines.flow.Flow

// Repository: capa intermedia entre la UI y los datos
// Guarda en Room (local) Y sincroniza con Azure SQL (remoto)
class IncidenciaRepository(private val dao: IncidenciaDao) {

    // ── CAMINO A: Servidor Node.js (Express) en Azure App Service
    private val api = RetrofitClient.incidenciaApi

    // ── CAMINO B: Conexión directa a Supabase REST API
    // private val api = SupabaseClient.api

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
    suspend fun sincronizarConAzure(usuarioId: Int): ResultadoApi {
        return try {
            val response = api.obtenerPorUsuario(usuarioId)
            if (response.isSuccessful) {
                response.body()?.forEach { dto ->
                    dao.insertar(dto.toEntity())
                }
                ResultadoApi.Exito("Sincronizado con Azure")
            } else {
                ResultadoApi.Error("Error al sincronizar: ${response.code()}")
            }
        } catch (e: Exception) {
            ResultadoApi.Error("Sin conexión a Azure: ${e.message}")
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
            dao.insertar(incidencia)
            val response = api.actualizar(incidencia.id, incidencia.toDto())
            if (response.isSuccessful) {
                ResultadoApi.Exito("Actualizado en Azure")
            } else {
                ResultadoApi.Error("Error API: ${response.code()}")
            }
        } catch (e: Exception) {
            ResultadoApi.Error("Sin conexión. Actualizado localmente: ${e.message}")
        }
    }

    suspend fun eliminar(incidencia: IncidenciaEntity): ResultadoApi {
        return try {
            dao.eliminar(incidencia)
            val response = api.eliminar(incidencia.id)
            if (response.isSuccessful) {
                ResultadoApi.Exito("Eliminado de Azure")
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