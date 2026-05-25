package com.example.sjl_alert_v4.modelos

import com.example.sjl_alert_v4.red.RetrofitClient
import kotlinx.coroutines.flow.Flow

// Repository: capa intermedia entre la UI y los datos
// Guarda en Room (local) Y sincroniza con Azure SQL (remoto)
class IncidenciaRepository(private val dao: IncidenciaDao) {

    private val api = RetrofitClient.incidenciaApi

    // ── CREATE ────────────────────────────────────────────────────────────
    suspend fun crear(incidencia: IncidenciaEntity): ResultadoApi {
        return try {
            // 1. Guarda en Room (local) inmediatamente
            dao.insertar(incidencia)
            // 2. Sincroniza con Azure SQL (remoto)
            val response = api.crear(incidencia.toDto())
            if (response.isSuccessful) {
                ResultadoApi.Exito("Incidencia creada en Azure SQL")
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

    // ── READ — sincronizar desde Azure ────────────────────────────────────
    suspend fun sincronizarDesdeAzure(usuarioId: Int): ResultadoApi {
        return try {
            val response = api.obtenerPorUsuario(usuarioId)
            if (response.isSuccessful) {
                response.body()?.forEach { dto ->
                    dao.insertar(dto.toEntity())   // upsert en Room
                }
                ResultadoApi.Exito("Sincronizado con Azure SQL")
            } else {
                ResultadoApi.Error("Error al sincronizar: ${response.code()}")
            }
        } catch (e: Exception) {
            ResultadoApi.Error("Sin conexión a Azure: ${e.message}")
        }
    }

    // ── READ — obtener todas desde Azure (para admin) ─────────────────────
    suspend fun obtenerTodasDesdeAzure(): ResultadoApi {
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
            val response = api.actualizar(incidencia.id, incidencia.toDto())
            if (response.isSuccessful) {
                ResultadoApi.Exito("Actualizado en Azure SQL")
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
            val response = api.eliminar(incidencia.id)
            if (response.isSuccessful) {
                ResultadoApi.Exito("Eliminado de Azure SQL")
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