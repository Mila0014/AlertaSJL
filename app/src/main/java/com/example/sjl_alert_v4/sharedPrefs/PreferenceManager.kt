package com.example.sjl_alert_v4.sharedPrefs

import android.content.Context
import android.content.SharedPreferences

class PreferenceManager(context: Context) {

    // crear el archivo de preferencias del usuario
    private val prefs: SharedPreferences = context.getSharedPreferences("UserSettings", Context.MODE_PRIVATE)

    // get & set variable para el modo oscuro
    fun isDarkMode() = prefs.getBoolean("dark_mode", false)
    fun setDarkMode(enable: Boolean) = prefs.edit().putBoolean("dark_mode", enable).apply()

    // get and set para la variable de idioma
    fun getLanguage() = prefs.getString("Language", "es") ?: "es"
    fun setLanguage(lang: String) = prefs.edit().putString("Language", lang).apply()

    // get & set para la variable de tamaño de fuente (float)
    // 16f = tamaño base normal
    fun getFontSize() = prefs.getFloat("font_size", 16f)
    fun setFontSize(size: Float) = prefs.edit().putFloat("font_size", size).apply()

    // get & set para la variable del nombre de usuario (string)
    fun getUserName() = prefs.getString("user_name", "") ?: ""
    fun setUserName(name: String) = prefs.edit().putString("user_name", name).apply()

    // Preferencias adicionales
    fun isEmailNotificationEnabled() = prefs.getBoolean("email_notifications", true)
    fun setEmailNotificationEnabled(enable: Boolean) = prefs.edit().putBoolean("email_notifications", enable).apply()

    fun isCommunityAlertsEnabled() = prefs.getBoolean("community_alerts", true)
    fun setCommunityAlertsEnabled(enable: Boolean) = prefs.edit().putBoolean("community_alerts", enable).apply()

    fun isLocationSharingEnabled() = prefs.getBoolean("location_sharing", true)
    fun setLocationSharingEnabled(enable: Boolean) = prefs.edit().putBoolean("location_sharing", enable).apply()

    // ---- SESIÓN DE USUARIO ----

    // Guardar sesión al hacer login exitoso
    fun guardarSesion(usuarioId: Int, nombre: String, correo: String) {
        prefs.edit()
            .putBoolean("sesion_activa", true)
            .putInt("sesion_usuario_id", usuarioId)
            .putString("sesion_nombre", nombre)
            .putString("sesion_correo", correo)
            .apply()
    }

    // Verificar si hay sesión activa
    fun haySesionActiva() = prefs.getBoolean("sesion_activa", false)

    // Obtener datos de la sesión activa
    fun getSesionUsuarioId() = prefs.getInt("sesion_usuario_id", -1)
    fun getSesionNombre() = prefs.getString("sesion_nombre", "") ?: ""
    fun getSesionCorreo() = prefs.getString("sesion_correo", "") ?: ""

    // Cerrar sesión (logout)
    fun cerrarSesion() {
        prefs.edit()
            .remove("sesion_activa")
            .remove("sesion_usuario_id")
            .remove("sesion_nombre")
            .remove("sesion_correo")
            .apply()
    }

    // ---- BLOQUEO POR INTENTOS FALLIDOS (CP-01.4) ----
    private val MAX_INTENTOS = 3
    private val TIEMPO_BLOQUEO_MS = 5 * 60 * 1000L // 5 minutos en milisegundos

    fun getIntentosFallidos() = prefs.getInt("intentos_fallidos", 0)

    fun registrarIntentoFallido() {
        val intentos = getIntentosFallidos() + 1
        prefs.edit().putInt("intentos_fallidos", intentos).apply()
        if (intentos >= MAX_INTENTOS) {
            prefs.edit().putLong("bloqueo_hasta", System.currentTimeMillis() + TIEMPO_BLOQUEO_MS).apply()
        }
    }

    fun resetearIntentosFallidos() {
        prefs.edit()
            .putInt("intentos_fallidos", 0)
            .remove("bloqueo_hasta")
            .apply()
    }

    fun estaBloqueado(): Boolean {
        val bloqueoHasta = prefs.getLong("bloqueo_hasta", 0L)
        if (bloqueoHasta == 0L) return false
        return if (System.currentTimeMillis() < bloqueoHasta) {
            true
        } else {
            resetearIntentosFallidos()
            false
        }
    }

    fun getTiempoBloqueoRestante(): Long {
        val bloqueoHasta = prefs.getLong("bloqueo_hasta", 0L)
        val restante = bloqueoHasta - System.currentTimeMillis()
        return if (restante > 0) restante else 0L
    }
}