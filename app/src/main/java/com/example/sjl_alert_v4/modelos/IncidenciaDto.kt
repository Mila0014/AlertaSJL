package com.example.sjl_alert_v4.modelos

import com.google.gson.annotations.SerializedName
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.util.*

data class IncidenciaDto(
    @SerializedName("id")          val id: String          = "",
    @SerializedName("tipo")        val tipo: String        = "",
    @SerializedName("descripcion") val descripcion: String = "",
    @SerializedName("ubicacion")   val ubicacion: String   = "",
    @SerializedName("latitud")     val latitud: Double?    = null,
    @SerializedName("longitud")    val longitud: Double?   = null,
    @SerializedName("evidencias")  val evidencias: String  = "",
    @SerializedName("imagenUri")   val imagenUri: String?  = null,
    @SerializedName("fecha")       val fecha: Any?         = null, // ← Any para aceptar Long o String
    @SerializedName("estado")      val estado: String      = "PENDIENTE",
    @SerializedName("usuarioId")   val usuarioId: Int      = 0
) {
    // Convierte fecha a Long sin importar si viene como número o string ISO
    fun getFechaLong(): Long {
        return when (fecha) {
            is Double -> fecha.toLong()
            is Long   -> fecha
            is String -> {
                try {
                    val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                    sdf.timeZone = TimeZone.getTimeZone("UTC")
                    sdf.parse(fecha)?.time ?: System.currentTimeMillis()
                } catch (e: Exception) {
                    try {
                        fecha.toLong()
                    } catch (e2: Exception) {
                        System.currentTimeMillis()
                    }
                }
            }
            else -> System.currentTimeMillis()
        }
    }
}

fun IncidenciaDto.toEntity() = IncidenciaEntity(
    id          = id,
    tipo        = tipo,
    descripcion = descripcion,
    ubicacion   = ubicacion,
    latitud     = latitud,
    longitud    = longitud,
    evidencias  = evidencias,
    imagenUri   = imagenUri,
    fecha       = getFechaLong(),   // ← usa la función que convierte correctamente
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