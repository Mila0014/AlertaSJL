package com.example.sjl_alert_v4.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.sjl_alert_v4.modelos.AppDatabase
import com.example.sjl_alert_v4.modelos.toDto
import com.example.sjl_alert_v4.red.RetrofitClient

class SyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val db  = AppDatabase.getInstance(applicationContext)
            val dao = db.incidenciaDao()

            // Obtener todas las incidencias no sincronizadas
            val pendientes = dao.obtenerNoSincronizadas()

            if (pendientes.isEmpty()) return Result.success()

            var todasEnviadas = true

            pendientes.forEach { incidencia ->
                try {
                    val response = RetrofitClient.incidenciaApi.crear(incidencia.toDto())
                    if (response.isSuccessful) {
                        // Marcar como sincronizada en Room
                        dao.marcarSincronizada(incidencia.id)
                    } else {
                        todasEnviadas = false
                    }
                } catch (e: Exception) {
                    todasEnviadas = false
                }
            }

            if (todasEnviadas) Result.success() else Result.retry()

        } catch (e: Exception) {
            Result.retry()
        }
    }
}