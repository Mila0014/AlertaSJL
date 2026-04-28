package com.example.sjl_alert_v4.modelos

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "incidencias")
data class IncidenciaEntity(
    @PrimaryKey
    val id: String,
    val tipo: String,
    val descripcion: String = "",
    val ubicacion: String = "",
    val latitud: Double? = null,
    val longitud: Double? = null,
    val evidencias: String = "",   // URIs separadas por coma
    val fecha: Long = System.currentTimeMillis(),
    val estado: String = "PENDIENTE",
    val usuarioId: Int = 0
)