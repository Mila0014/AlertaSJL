package com.example.sjl_alert_v4.actividades
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import java.util.Locale

object LocaleHelper {

    /**
     * Envuelve el Context con el locale correcto.
     * Se llama desde attachBaseContext() en MainActivity.
     */
    fun wrap(context: Context, languageCode: String): ContextWrapper {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)

        val localizedContext = context.createConfigurationContext(config)
        return ContextWrapper(localizedContext)
    }
}