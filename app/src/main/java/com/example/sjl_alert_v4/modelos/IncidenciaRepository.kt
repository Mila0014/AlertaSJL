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
            // 1. Guarda en Room inmediatamente con sincronizado = false
            dao.insertar(incidencia)

            // 2. Intenta subir a Azure
            val response = api.crear(incidencia.toDto())
            if (response.isSuccessful) {
                // ✅ Subió a Azure — marcar como sincronizada
                dao.marcarSincronizada(incidencia.id)
                ResultadoApi.Exito("Incidencia creada en Azure")
            } else {
                // ❌ Error API — queda pendiente para SyncWorker
                ResultadoApi.Error("Error API: ${response.code()} ${response.message()}")
            }
        } catch (e: Exception) {
            // ❌ Sin internet — queda en Room con sincronizado=false
            // SyncWorker la subirá cuando vuelva el internet
            ResultadoApi.Error("Sin conexión. Guardado localmente: ${e.message}")
        }
    }

    // ── SINCRONIZAR PENDIENTES ────────────────────────────────────────────
    // Sube a Azure todas las incidencias que quedaron con sincronizado=false
    suspend fun sincronizarPendientes(): ResultadoApi {
        return try {
            val pendientes = dao.obtenerNoSincronizadas()
            if (pendientes.isEmpty()) return ResultadoApi.Exito("Todo sincronizado")

            var enviadas = 0
            pendientes.forEach { incidencia ->
                try {
                    val response = api.crear(incidencia.toDto())
                    if (response.isSuccessful) {
                        dao.marcarSincronizada(incidencia.id)
                        enviadas++
                    }
                } catch (e: Exception) {
                    // Sin internet — se reintentará después
                }
            }
            ResultadoApi.Exito("Sincronizadas $enviadas de ${pendientes.size}")
        } catch (e: Exception) {
            ResultadoApi.Error("Error al sincronizar: ${e.message}")
        }
    }

    // ── READ — obtener por usuario (Flow desde Room) ───────────────────────
    fun obtenerPorUsuario(usuarioId: Int): Flow<List<IncidenciaEntity>> =
        dao.obtenerPorUsuario(usuarioId)

    // ── READ — sincronizar desde Azure ────────────────────────────────────
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

    // ── READ — obtener todas desde Azure ──────────────────────────────────
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
            dao.insertar(incidencia)
            val response = api.actualizar(incidencia.id, incidencia.toDto())
            if (response.isSuccessful) {
                dao.marcarSincronizada(incidencia.id)
                ResultadoApi.Exito("Actualizado en Azure")
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