package com.example.sjl_alert_v4.red

import com.example.sjl_alert_v4.modelos.IncidenciaDto
import retrofit2.Response
import retrofit2.http.*

interface IncidenciaApiService {

    @POST("api/incidencias")
    suspend fun crear(@Body incidencia: IncidenciaDto): Response<IncidenciaDto>

    @GET("api/incidencias")
    suspend fun obtenerTodas(): Response<List<IncidenciaDto>>

    @GET("api/incidencias/usuario/{usuarioId}")
    suspend fun obtenerPorUsuario(@Path("usuarioId") usuarioId: Int): Response<List<IncidenciaDto>>

    @PUT("api/incidencias/{id}")
    suspend fun actualizar(@Path("id") id: String, @Body incidencia: IncidenciaDto): Response<IncidenciaDto>

    @DELETE("api/incidencias/{id}")
    suspend fun eliminar(@Path("id") id: String): Response<Unit>
}