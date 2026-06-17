package com.example.sjl_alert_v4.actividades.recuperacion

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sjl_alert_v4.R
import com.example.sjl_alert_v4.red.RecuperarRequest
import com.example.sjl_alert_v4.red.RetrofitClient
import com.example.sjl_alert_v4.ui.theme.PrimaryBlue
import com.example.sjl_alert_v4.ui.theme.SJL_Alert_v4Theme
import kotlinx.coroutines.launch

@Composable
fun RecuperarContraContent(
    onEnviarCodigo: (String) -> Unit,
    onLoginClick: () -> Unit
) {
    var correo by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    val primaryColor = MaterialTheme.colorScheme.primary

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo_sjl),
                contentDescription = "Logo SJL Alerta",
                modifier = Modifier.size(120.dp),
                contentScale = ContentScale.Fit
            )
            Text(
                text = "SJL Alerta",
                style = MaterialTheme.typography.headlineLarge.copy(
                    color = primaryColor,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.5).sp
                )
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Recuperar Contraseña",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold, color = Color.Black
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Ingresa tu correo electrónico registrado para enviarte un código de verificación.",
            style = MaterialTheme.typography.bodyLarge.copy(
                color = Color.Gray, textAlign = TextAlign.Center
            ),
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Spacer(modifier = Modifier.height(40.dp))

        errorMessage?.let {
            Text(it, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(8.dp))
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "Correo electrónico",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.SemiBold, color = Color.DarkGray
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = correo,
                    onValueChange = { correo = it; errorMessage = null },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("nombre@ejemplo.com") },
                    leadingIcon = {
                        Icon(Icons.Default.Email, contentDescription = null, tint = Color.Gray)
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF3F4F6),
                        unfocusedContainerColor = Color(0xFFF3F4F6),
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = PrimaryBlue
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (correo.isBlank() || !correo.contains("@")) {
                            errorMessage = "Ingresa un correo válido"
                            return@Button
                        }
                        isLoading = true
                        scope.launch {
                            try {
                                val response = RetrofitClient.usuarioApi.recuperar(
                                    RecuperarRequest(correo = correo.trim().lowercase())
                                )
                                isLoading = false
                                if (response.isSuccessful) {
                                    onEnviarCodigo(correo.trim().lowercase())
                                } else {
                                    errorMessage = response.body()?.error ?: "Correo no registrado"
                                }
                            } catch (e: Exception) {
                                isLoading = false
                                errorMessage = "Sin conexión. Intenta de nuevo."
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                    } else {
                        Text("Enviar Código", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Text("¿Recordaste tu contraseña? ", color = Color.Gray)
            Text(
                text = "Inicia sesión",
                fontWeight = FontWeight.Bold,
                color = PrimaryBlue,
                modifier = Modifier.clickable { onLoginClick() }
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            Icon(Icons.Default.VerifiedUser, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Gray)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "SISTEMA DE SEGURIDAD ENCRIPTADO",
                style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray, letterSpacing = 1.sp)
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
    }
}