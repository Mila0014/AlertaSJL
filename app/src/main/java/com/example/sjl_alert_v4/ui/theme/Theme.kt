package com.example.sjl_alert_v4.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Density
import androidx.core.view.WindowCompat
import com.example.sjl_alert_v4.sharedPrefs.PreferenceManager

// ── Esquema CLARO ─────────────────────────────────────────────────────────────
private val LightColorScheme = lightColorScheme(
    primary                 = LightPrimary,
    onPrimary               = LightOnPrimary,
    primaryContainer        = LightPrimaryContainer,
    secondary               = LightSecondary,
    background              = LightBackground,
    surface                 = LightSurface,
    onBackground            = LightOnBackground,
    onSurface               = LightOnSurface,
    onSurfaceVariant        = LightOnSurfaceVariant,
    surfaceVariant          = LightSurfaceVariant,
    outline                 = LightOutlineVariant,
    surfaceContainer        = LightSurfaceContainerLow,
    surfaceContainerHigh    = LightSurfaceContainerHigh,
    surfaceContainerHighest = LightSurfaceContainerHighest,
)

// ── Esquema OSCURO ────────────────────────────────────────────────────────────
private val DarkColorScheme = darkColorScheme(
    primary                 = DarkPrimary,
    onPrimary               = DarkOnPrimary,
    primaryContainer        = DarkPrimaryContainer,
    secondary               = DarkSecondary,
    background              = DarkBackground,
    surface                 = DarkSurface,
    onBackground            = DarkOnBackground,
    onSurface               = DarkOnSurface,
    onSurfaceVariant        = DarkOnSurfaceVariant,
    surfaceVariant          = DarkSurfaceVariant,
    outline                 = DarkOutlineVariant,
    surfaceContainer        = DarkSurfaceContainerLow,
    surfaceContainerHigh    = DarkSurfaceContainerHigh,
    surfaceContainerHighest = DarkSurfaceContainerHighest,
)

@Composable
fun SJL_Alert_v4Theme(
    // Parámetros opcionales: si se pasan, se usan directamente (modo reactivo).
    // Si no se pasan, se leen de PreferenceManager (modo legacy / preview).
    darkTheme: Boolean? = null,
    fontSizePreference: Float? = null,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val preferenceManager = PreferenceManager(context)

    // Usa el valor externo si se proporcionó, de lo contrario lee las prefs
    val isDark = darkTheme ?: preferenceManager.isDarkMode()

    // fontScale: 16f es el tamaño base (normal = 1.0f)
    // Ej: 14f → 0.875f (pequeño), 18f → 1.125f (grande), 22f → 1.375f (muy grande)
    val fontSize = fontSizePreference ?: preferenceManager.getFontSize()
    val fontScale = fontSize / 16f

    val colorScheme = if (isDark) DarkColorScheme else LightColorScheme

    // Cambia el color de la barra de estado según el tema
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDark
        }
    }

    // ── Aplica fontScale globalmente a TODAS las pantallas ────────────────────
    // LocalDensity sobreescrito conserva la densidad de pantalla pero ajusta
    // la escala de fuente según la preferencia del usuario.
    val currentDensity = LocalDensity.current
    CompositionLocalProvider(
        LocalDensity provides Density(
            density   = currentDensity.density,
            fontScale = fontScale
        )
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography  = Typography,
            content     = content
        )
    }
}