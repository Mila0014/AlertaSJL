package com.example.sjl_alert_v4.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
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
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val preferenceManager = PreferenceManager(context)

    // Lee la preferencia guardada por el usuario
    val darkTheme = preferenceManager.isDarkMode()

    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    // Cambia el color de la barra de estado según el tema
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}