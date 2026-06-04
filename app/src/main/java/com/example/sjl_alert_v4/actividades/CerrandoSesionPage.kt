package com.example.sjl_alert_v4.actividades

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.Image
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sjl_alert_v4.R
import kotlinx.coroutines.delay

@Composable
fun CerrandoSesionPage(onFinished: () -> Unit) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val backgroundColor = MaterialTheme.colorScheme.background

    // Animación de pulso en el ícono
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val escala by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "escala"
    )

    // Navega al login después de 2 segundos
    LaunchedEffect(Unit) {
        delay(2000)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.systemBarsPadding()
        ) {
            // Logo con animación
            Box(
                modifier = Modifier
                    .scale(escala)
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(primaryColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo_sjl),
                    contentDescription = "Logo SJL",
                    modifier = Modifier.size(60.dp),
                    contentScale = ContentScale.Fit
                )
            }

            Text(
                text = "Cerrando sesión...",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = primaryColor
            )

            Text(
                text = "Hasta pronto 👋",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            CircularProgressIndicator(
                color = primaryColor,
                strokeWidth = 3.dp,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}