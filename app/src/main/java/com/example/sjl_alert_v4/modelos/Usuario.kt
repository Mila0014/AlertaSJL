package com.example.sjl_alert_v4.modelos

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "usuarios")
data class Usuario(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nombre: String,
    val apellido: String,
    val dni: String,
    val correo: String,
    val telefono: String,
    val contrasena: String, // Guarda el hash, nunca texto plano
    val direccion: String = "",
    val fechaRegistro: Long = System.currentTimeMillis(),
    val fechaNacimiento: String = ""
)
