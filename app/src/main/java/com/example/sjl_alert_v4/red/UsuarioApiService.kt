package com.example.sjl_alert_v4.red

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

// ── DTOs de usuario ────────────────────────────────────────────────────────

data class RegistroRequest(
    @SerializedName("nombre")          val nombre: String,
    @SerializedName("apellido")        val apellido: String,
    @SerializedName("dni")             val dni: String,
    @SerializedName("correo")          val correo: String,
    @SerializedName("telefono")        val telefono: String,
    @SerializedName("contrasena")      val contrasena: String,
    @SerializedName("direccion")       val direccion: String,
    @SerializedName("fechaRegistro")   val fechaRegistro: Long,
    @SerializedName("fechaNacimiento") val fechaNacimiento: String
)

data class LoginRequest(
    @SerializedName("dniOCorreo") val dniOCorreo: String,
    @SerializedName("contrasena") val contrasena: String
)

data class UsuarioResponse(
    @SerializedName("id")       val id: Int,
    @SerializedName("nombre")   val nombre: String,
    @SerializedName("apellido") val apellido: String,
    @SerializedName("dni")      val dni: String,
    @SerializedName("correo")   val correo: String,
    @SerializedName("telefono") val telefono: String,
    @SerializedName("direccion") val direccion: String
)

data class LoginResponse(
    @SerializedName("mensaje") val mensaje: String = "",
    @SerializedName("usuario") val usuario: UsuarioResponse? = null,
    @SerializedName("error")   val error: String = ""
)

data class RegistroResponse(
    @SerializedName("mensaje") val mensaje: String = "",
    @SerializedName("id")      val id: Int = 0,
    @SerializedName("error")   val error: String = ""
)

// ── Interfaz Retrofit ──────────────────────────────────────────────────────
interface UsuarioApiService {

    // Registro: POST /api/usuarios/registro
    @POST("api/usuarios/registro")
    suspend fun registro(@Body request: RegistroRequest): Response<RegistroResponse>

    // Login: POST /api/usuarios/login
    @POST("api/usuarios/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>
}