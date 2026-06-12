package com.example.sjl_alert_v4.modelos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UsuarioDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertarUsuario(usuario: Usuario): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarOActualizar(usuario: Usuario): Long

    @Query("SELECT * FROM usuarios WHERE dni = :dni OR correo = :correo LIMIT 1")
    suspend fun buscarPorDniOCorreo(dni: String, correo: String): Usuario?

    @Query("SELECT * FROM usuarios WHERE (dni = :dniOCorreo OR correo = :dniOCorreo) AND contrasena = :contrasena LIMIT 1")
    suspend fun login(dniOCorreo: String, contrasena: String): Usuario?

    @Query("SELECT * FROM usuarios WHERE id = :id LIMIT 1")
    suspend fun buscarPorId(id: Int): Usuario?

    @Query("UPDATE usuarios SET telefono = :nuevoTelefono WHERE id = :id")
    suspend fun actualizarTelefono(id: Int, nuevoTelefono: String)

    @Query("UPDATE usuarios SET correo = :nuevoCorreo WHERE id = :id")
    suspend fun actualizarCorreo(id: Int, nuevoCorreo: String)

    @Query("UPDATE usuarios SET contrasena = :nuevaContrasena WHERE id = :id")
    suspend fun actualizarContrasena(id: Int, nuevaContrasena: String)
}