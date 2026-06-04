package com.example.sjl_alert_v4.modelos

import com.google.gson.annotations.SerializedName

// DTO: modelo que viaja entre la app y la API REST
// Los nombres coinciden exactamente con los campos del JSON de la API
data class IncidenciaDto(
    @SerializedName("id")          val id: String          = "",
    @SerializedName("tipo")        val tipo: String        = "",
    @SerializedName("descripcion") val descripcion: String = "",
    @SerializedName("ubicacion")   val ubicacion: String   = "",
    @SerializedName("latitud")     val latitud: Double?    = null,
    @SerializedName("longitud")    val longitud: Double?   = null,
    @SerializedName("evidencias")  val evidencias: String  = "",
    @SerializedName("imagenUri")   val imagenUri: String?  = null,
    @SerializedName("fecha")       val fecha: Long         = 0L,
    @SerializedName("estado")      val estado: String      = "PENDIENTE",
    @SerializedName("usuarioId")   val usuarioId: Int      = 0
)

// Extensiones para convertir entre DTO y Entity (Room)
fun IncidenciaDto.toEntity() = IncidenciaEntity(
    id          = id,
    tipo        = tipo,
    descripcion = descripcion,
    ubicacion   = ubicacion,
    latitud     = latitud,
    longitud    = longitud,
    evidencias  = evidencias,
    imagenUri   = imagenUri,
    fecha       = fecha,
    estado      = estado,
    usuarioId   = usuarioId
)

fun IncidenciaEntity.toDto() = IncidenciaDto(
    id          = id,
    tipo        = tipo,
    descripcion = descripcion,
    ubicacion   = ubicacion,
    latitud     = latitud,
    longitud    = longitud,
    evidencias  = evidencias,
    imagenUri   = imagenUri,
    fecha       = fecha,
    estado      = estado,
    usuarioId   = usuarioId
)