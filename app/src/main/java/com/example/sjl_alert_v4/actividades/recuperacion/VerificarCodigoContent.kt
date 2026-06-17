package com.example.sjl_alert_v4.actividades.recuperacion

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sjl_alert_v4.R
import com.example.sjl_alert_v4.red.RetrofitClient
import com.example.sjl_alert_v4.red.VerificarCodigoRequest
import com.example.sjl_alert_v4.ui.theme.DeepRed
import com.example.sjl_alert_v4.ui.theme.PrimaryBlue
import kotlinx.coroutines.launch

@Composable
fun VerificarCodigoContent(
    correo: String,
    onVerificar: (String) -> Unit,
    onReenviar: () -> Unit
) {
    var codigo by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    
    val primaryColor = MaterialTheme.colorScheme.primary
    val onBackground = MaterialTheme.colorScheme.onBackground
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant
    val surfaceContainerLowColor = MaterialTheme.colorScheme.surfaceContainerLow
    val surfaceContainerHighColor = MaterialTheme.colorScheme.surfaceContainerHigh
    val surfaceContainerHighestColor = MaterialTheme.colorScheme.surfaceContainerHighest

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Image(
                painter = painterResource(id = R.drawable.logo_sjl),
                contentDescription = "Logo SJL Alerta",
                modifier = Modifier.size(120.dp),
                contentScale = ContentScale.Fit
            )
            Text(
                text = "SJL Alerta",
                style = MaterialTheme.typography.headlineLarge.copy(
                    color = primaryColor, fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.5).sp
                )
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Verificar Código",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, color = primaryColor)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Hemos enviado un código de 6 dígitos a $correo. Ingrésalo a continuación.",
            style = MaterialTheme.typography.bodyLarge.copy(color = onSurfaceVariantColor, textAlign = TextAlign.Center)
        )

        Spacer(modifier = Modifier.height(32.dp))

        errorMessage?.let {
            Text(it, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(8.dp))
        }

        OutlinedTextField(
            value = codigo,
            onValueChange = { if (it.length <= 6) { codigo = it; errorMessage = null } },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Ingresa el código de 6 dígitos") },
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = surfaceContainerHighestColor,
                unfocusedContainerColor = surfaceContainerLowColor,
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = primaryColor,
                focusedTextColor = onSurfaceColor,
                unfocusedTextColor = onSurfaceColor
            )
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (codigo.length != 6) {
                    errorMessage = "El código debe tener 6 dígitos"
                    return@Button
                }
                isLoading = true
                scope.launch {
                    try {
                        val response = RetrofitClient.usuarioApi.verificarCodigo(
                            VerificarCodigoRequest(correo = correo, codigo = codigo.trim())
                        )
                        isLoading = false
                        if (response.isSuccessful) {
                            onVerificar(codigo.trim())
                        } else {
                            errorMessage = response.body()?.error ?: "Código incorrecto"
                        }
                    } catch (e: Exception) {
                        isLoading = false
                        errorMessage = "Sin conexión. Intenta de nuevo."
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
            } else {
                Text("Verificar", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(text = "¿No recibiste el código?", color = onSurfaceVariantColor, fontSize = 14.sp)
        Text(
            text = "Reenviar",
            fontWeight = FontWeight.Bold,
            color = primaryColor,
            modifier = Modifier.padding(top = 4.dp).clickable { onReenviar() }
        )

        Spacer(modifier = Modifier.weight(1f))

        Box(
            modifier = Modifier.size(60.dp).clip(RoundedCornerShape(12.dp)).background(surfaceContainerHighColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = onSurfaceVariantColor)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "SEGURIDAD DIGITAL SJL • 2026",
            style = MaterialTheme.typography.labelSmall.copy(color = onSurfaceVariantColor, letterSpacing = 1.sp)
        )
        Spacer(modifier = Modifier.height(20.dp))
    }
}