package com.example.sjl_alert_v4.actividades.recuperacion

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.example.sjl_alert_v4.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecuperarContraPage(
    onBack: () -> Unit,
    onLoginClick: () -> Unit
) {
    var pasoActual by remember { mutableIntStateOf(1) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "SJL Alerta",
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = PrimaryBlue,
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Regresar",
                            tint = PrimaryBlue
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF8F9FA))
        ) {
            when (pasoActual) {
                1 -> RecuperarContraContent(
                    onEnviarCodigo = { pasoActual = 2 },
                    onLoginClick = onLoginClick
                )
                2 -> VerificarCodigoContent(
                    onVerificar = { pasoActual = 3 },
                    onReenviar = { /* Lógica de reenviar */ }
                )
                3 -> NuevaContraContent(
                    onActualizar = { onLoginClick() }
                )
            }
        }
    }
}