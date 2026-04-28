package com.example.sjl_alert_v4.ui.theme

import androidx.compose.ui.graphics.Color

// ── Colores base de la marca (no cambian) ─────────────────────────────────────
val PrimaryBlue        = Color(0xFF0D2C54)
val SecondaryBlue      = Color(0xFF1B4965)
val AccentBlue         = Color(0xFF62B6CB)
val LightBlue          = Color(0xFFEAF4F4)
val SoftRed            = Color(0xFFFFD1D1)
val DeepRed            = Color(0xFFA51C30)
val SoftYellow         = Color(0xFFFFF4D1)
val DeepYellow         = Color(0xFFD4A017)
val SoftPurple         = Color(0xFFE8EAF6)
val DeepPurple         = Color(0xFF3F51B5)

// ── Colores del tema CLARO ────────────────────────────────────────────────────
val LightPrimary               = Color(0xFF0D2C54)
val LightOnPrimary             = Color(0xFFFFFFFF)
val LightSecondary             = Color(0xFF1B4965)
val LightBackground            = Color(0xFFF8F9FA)
val LightSurface               = Color(0xFFFFFFFF)
val LightOnBackground          = Color(0xFF191C1E)
val LightOnSurface             = Color(0xFF191C1E)
val LightOnSurfaceVariant      = Color(0xFF43474E)
val LightSurfaceVariant        = Color(0xFFF0F4F7)
val LightOutlineVariant        = Color(0xFFC3C7CF)
val LightSurfaceContainerLow   = Color(0xFFF0F4F7)
val LightSurfaceContainerHigh  = Color(0xFFE2E2E6)
val LightSurfaceContainerHighest = Color(0xFFDDE2F4)
val LightPrimaryContainer      = Color(0xFF004394)
val LightPrimaryFixed          = Color(0xFFD7E2FF)
val LightSecondaryFixed        = Color(0xFFDDE2F9)

// ── Colores del tema OSCURO ───────────────────────────────────────────────────
val DarkPrimary                = Color(0xFF90B4E8)   // azul más claro para contrastar
val DarkOnPrimary              = Color(0xFF0D2C54)
val DarkSecondary              = Color(0xFF62B6CB)
val DarkBackground             = Color(0xFF111827)
val DarkSurface                = Color(0xFF1F2937)
val DarkOnBackground           = Color(0xFFF9FAFB)
val DarkOnSurface              = Color(0xFFF3F4F6)
val DarkOnSurfaceVariant       = Color(0xFF9CA3AF)
val DarkSurfaceVariant         = Color(0xFF374151)
val DarkOutlineVariant         = Color(0xFF4B5563)
val DarkSurfaceContainerLow    = Color(0xFF1F2937)
val DarkSurfaceContainerHigh   = Color(0xFF374151)
val DarkSurfaceContainerHighest = Color(0xFF4B5563)
val DarkPrimaryContainer       = Color(0xFF1E3A5F)
val DarkPrimaryFixed           = Color(0xFF1E3A5F)
val DarkSecondaryFixed         = Color(0xFF1B3A4B)

// ── Aliases estáticos (usados en componentes que no leen el tema dinámico) ────
// Estos se mantienen por compatibilidad pero lo ideal es usar MaterialTheme.colorScheme
val Primary                = LightPrimary
val OnPrimary              = LightOnPrimary
val Secondary              = LightSecondary
val Background             = LightBackground
val PrimaryFixed           = LightPrimaryFixed
val SecondaryFixed         = LightSecondaryFixed
val PrimaryContainer       = LightPrimaryContainer
val OnSurface              = LightOnSurface
val OnSurfaceVariant       = LightOnSurfaceVariant
val OutlineVariant         = LightOutlineVariant
val SurfaceContainerLowest = LightSurface
val SurfaceContainerLow    = LightSurfaceContainerLow
val SurfaceContainerHigh   = LightSurfaceContainerHigh
val SurfaceContainerHighest = LightSurfaceContainerHighest
val BackgroundWhite        = LightBackground