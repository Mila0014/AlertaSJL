package com.example.sjl_alert_v4.actividades

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sjl_alert_v4.sharedPrefs.PreferenceManager
import com.example.sjl_alert_v4.ui.theme.*
import kotlinx.coroutines.delay
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.sjl_alert_v4.R

@Composable
fun WelcomePage(
    onContinue: () -> Unit
) {
    val context = LocalContext.current
    val prefManager = remember { PreferenceManager(context) }
    val nombre = remember { prefManager.getSesionNombre() }

    val primaryColor = MaterialTheme.colorScheme.primary
    val backgroundColor = MaterialTheme.colorScheme.background
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant

    // Animación de escala del logo
    val scale = remember { Animatable(0.5f) }
    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
        )
        delay(2500)
        onContinue()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        // Blob decorativo superior
        Box(
            modifier = Modifier
                .size(350.dp)
                .offset(x = 120.dp, y = (-180).dp)
                .background(
                    color = primaryColor.copy(alpha = 0.1f),
                    shape = CircleShape
                )
                .align(Alignment.TopEnd)
        )
        // Blob decorativo inferior
        Box(
            modifier = Modifier
                .size(300.dp)
                .offset(x = (-100).dp, y = 150.dp)
                .background(
                    color = primaryColor.copy(alpha = 0.07f),
                    shape = CircleShape
                )
                .align(Alignment.BottomStart)
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
            // Logo animado
            Image(
                painter = painterResource(id = R.drawable.logo_sjl),
                contentDescription = "Logo SJL Alerta",
                modifier = Modifier
                    .size(120.dp)
                    .scale(scale.value)
                    .clip(CircleShape),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "¡Bienvenido/a,",
                fontSize = 22.sp,
                color = onSurfaceVariantColor,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = nombre.ifBlank { "Vecino" } + "!",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = primaryColor,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Tu seguridad es nuestra prioridad.\nJuntos hacemos de SJL un lugar más seguro.",
                fontSize = 15.sp,
                color = onSurfaceVariantColor,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Indicador de carga
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = primaryColor,
                trackColor = primaryColor.copy(alpha = 0.2f)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Botón para saltar la espera
            TextButton(onClick = onContinue) {
                Text(
                    "Continuar →",
                    color = primaryColor,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
            }
        }
    }
}
