package com.example.sjl_alert_v4.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Objeto que expone colores dinámicos según el tema activo (claro u oscuro).
 * Úsalo en cualquier Composable así:
 *
 *   AppColors.background
 *   AppColors.surface
 *   AppColors.onSurface
 *   etc.
 *
 * De esta forma todas las pantallas respetan automáticamente el modo oscuro.
 */
object AppColors {

    val primary: Color
        @Composable get() = MaterialTheme.colorScheme.primary

    val onPrimary: Color
        @Composable get() = MaterialTheme.colorScheme.onPrimary

    val primaryContainer: Color
        @Composable get() = MaterialTheme.colorScheme.primaryContainer

    val secondary: Color
        @Composable get() = MaterialTheme.colorScheme.secondary

    val background: Color
        @Composable get() = MaterialTheme.colorScheme.background

    val surface: Color
        @Composable get() = MaterialTheme.colorScheme.surface

    val onBackground: Color
        @Composable get() = MaterialTheme.colorScheme.onBackground

    val onSurface: Color
        @Composable get() = MaterialTheme.colorScheme.onSurface

    val onSurfaceVariant: Color
        @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant

    val surfaceVariant: Color
        @Composable get() = MaterialTheme.colorScheme.surfaceVariant

    val outline: Color
        @Composable get() = MaterialTheme.colorScheme.outline

    val surfaceContainer: Color
        @Composable get() = MaterialTheme.colorScheme.surfaceContainer

    val surfaceContainerHigh: Color
        @Composable get() = MaterialTheme.colorScheme.surfaceContainerHigh

    val surfaceContainerHighest: Color
        @Composable get() = MaterialTheme.colorScheme.surfaceContainerHighest

    val error: Color
        @Composable get() = MaterialTheme.colorScheme.error

    val onError: Color
        @Composable get() = MaterialTheme.colorScheme.onError

    val errorContainer: Color
        @Composable get() = MaterialTheme.colorScheme.errorContainer

    val onErrorContainer: Color
        @Composable get() = MaterialTheme.colorScheme.onErrorContainer
}