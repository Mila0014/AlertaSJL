package com.example.sjl_alert_v4.modelos

import java.util.UUID

data class Incidencia(
    val id: String = UUID.randomUUID().toString(),
    val tipo: String,
    val descripcion: String = "",
    val ubicacion: String = "",
    val latitud: Double? = null,
    val longitud: Double? = null,
    val evidencias: List<String> = emptyList(), // Lista de URIs
    val fecha: Long = System.currentTimeMillis(),
    val estado: String = "PENDIENTE" // PENDIENTE, EN_PROCESO, RESUELTO, RECHAZADO
)
