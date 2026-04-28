package com.example.sjl_alert_v4.modelos

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface IncidenciaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(incidencia: IncidenciaEntity)

    @Query("SELECT * FROM incidencias WHERE usuarioId = :usuarioId ORDER BY fecha DESC")
    fun obtenerPorUsuario(usuarioId: Int): Flow<List<IncidenciaEntity>>

    @Query("SELECT * FROM incidencias ORDER BY fecha DESC")
    fun obtenerTodas(): Flow<List<IncidenciaEntity>>

    @Query("SELECT * FROM incidencias WHERE id = :id")
    fun obtenerPorId(id: String): Flow<IncidenciaEntity?>

    @Delete
    suspend fun eliminar(incidencia: IncidenciaEntity)

    @Query("UPDATE incidencias SET estado = :estado WHERE id = :id")
    suspend fun actualizarEstado(id: String, estado: String)
}