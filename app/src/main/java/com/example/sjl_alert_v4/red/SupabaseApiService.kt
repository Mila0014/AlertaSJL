package com.example.sjl_alert_v4.red

import com.example.sjl_alert_v4.modelos.IncidenciaDto
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface SupabaseApiService {

    @POST("rest/v1/incidencias")
    suspend fun crear(
        @Body incidencia: IncidenciaDto,
    ): Response<ResponseBody>

    @GET("rest/v1/incidencias?select=*")
    suspend fun obtenerTodas(): Response<List<IncidenciaDto>>

    @GET("rest/v1/incidencias?select=*")
    suspend fun obtenerPorUsuario(
        @Query("usuarioId") usuarioIdFilter: String, // Ej: "eq.12"
    ): Response<List<IncidenciaDto>>

    @PATCH("rest/v1/incidencias")
    suspend fun actualizar(
        @Query("id") idFilter: String, // Ej: "eq.uuid"
        @Body incidencia: IncidenciaDto
    ): Response<ResponseBody>

    @DELETE("rest/v1/incidencias")
    suspend fun eliminar(
        @Query("id") idFilter: String // Ej: "eq.uuid"
    ): Response<ResponseBody>
}
