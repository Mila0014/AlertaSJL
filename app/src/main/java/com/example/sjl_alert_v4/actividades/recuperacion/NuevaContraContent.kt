package com.example.sjl_alert_v4.actividades.recuperacion

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sjl_alert_v4.red.NuevaContrasenaRequest
import com.example.sjl_alert_v4.red.RetrofitClient
import com.example.sjl_alert_v4.ui.theme.*
import com.example.sjl_alert_v4.utilidades.SuccessToast
import com.example.sjl_alert_v4.utilidades.ToastData
import kotlinx.coroutines.launch
import java.security.MessageDigest

fun hashSHA256Recup(input: String): String {
    val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
    return bytes.joinToString("") { "%02x".format(it) }
}

@Composable
fun NuevaContraContent(
    correo: String,
    codigo: String,
    onActualizar: () -> Unit
) {
    var nuevaContra            by remember { mutableStateOf("") }
    var confirmarContra        by remember { mutableStateOf("") }
    var contraVisible          by remember { mutableStateOf(false) }
    var confirmarContraVisible by remember { mutableStateOf(false) }
    var isLoading              by remember { mutableStateOf(false) }
    var errorMessage           by remember { mutableStateOf<String?>(null) }
    var toastContra            by remember { mutableStateOf<ToastData?>(null) }
    val scope = rememberCoroutineScope()

    val primaryColor                 = MaterialTheme.colorScheme.primary
    val surfaceColor                 = MaterialTheme.colorScheme.surface
    val onSurfaceColor               = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariantColor        = MaterialTheme.colorScheme.onSurfaceVariant
    val surfaceContainerLowColor     = MaterialTheme.colorScheme.surfaceContainerLow
    val surfaceContainerHighColor    = MaterialTheme.colorScheme.surfaceContainerHigh
    val surfaceContainerHighestColor = MaterialTheme.colorScheme.surfaceContainerHighest
    val primaryContainerColor        = MaterialTheme.colorScheme.primaryContainer

    Box(modifier = Modifier.fillMaxSize()) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Nueva Contraseña",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold, color = primaryColor),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Crea una contraseña segura para proteger tu cuenta.",
                style = MaterialTheme.typography.bodyLarge.copy(color = onSurfaceVariantColor),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )

            Spacer(modifier = Modifier.height(24.dp))

            errorMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(8.dp))
            }

            Card(
                modifier  = Modifier.fillMaxWidth(),
                shape     = RoundedCornerShape(16.dp),
                colors    = CardDefaults.cardColors(containerColor = surfaceColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {

                    Text(
                        "Nueva Contraseña",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.SemiBold, color = onSurfaceColor)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value         = nuevaContra,
                        onValueChange = { nuevaContra = it; errorMessage = null },
                        modifier      = Modifier.fillMaxWidth(),
                        visualTransformation = if (contraVisible) VisualTransformation.None
                        else PasswordVisualTransformation(),
                        trailingIcon  = {
                            IconButton(onClick = { contraVisible = !contraVisible }) {
                                Icon(
                                    if (contraVisible) Icons.Default.VisibilityOff
                                    else Icons.Default.Visibility,
                                    null, tint = onSurfaceVariantColor
                                )
                            }
                        },
                        shape  = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor   = surfaceContainerHighestColor,
                            unfocusedContainerColor = surfaceContainerLowColor,
                            unfocusedBorderColor    = Color.Transparent,
                            focusedBorderColor      = primaryColor,
                            focusedTextColor        = onSurfaceColor,
                            unfocusedTextColor      = onSurfaceColor
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        "Confirmar Contraseña",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.SemiBold, color = onSurfaceColor)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value         = confirmarContra,
                        onValueChange = { confirmarContra = it; errorMessage = null },
                        modifier      = Modifier.fillMaxWidth(),
                        visualTransformation = if (confirmarContraVisible) VisualTransformation.None
                        else PasswordVisualTransformation(),
                        trailingIcon  = {
                            IconButton(onClick = { confirmarContraVisible = !confirmarContraVisible }) {
                                Icon(
                                    if (confirmarContraVisible) Icons.Default.VisibilityOff
                                    else Icons.Default.Visibility,
                                    null, tint = onSurfaceVariantColor
                                )
                            }
                        },
                        shape  = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor   = surfaceContainerHighestColor,
                            unfocusedContainerColor = surfaceContainerLowColor,
                            unfocusedBorderColor    = Color.Transparent,
                            focusedBorderColor      = primaryColor,
                            focusedTextColor        = onSurfaceColor,
                            unfocusedTextColor      = onSurfaceColor
                        )
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = {
                            when {
                                nuevaContra.length < 6 ->
                                    errorMessage = "La contraseña debe tener mínimo 6 caracteres"
                                nuevaContra != confirmarContra ->
                                    errorMessage = "Las contraseñas no coinciden"
                                else -> {
                                    isLoading = true
                                    scope.launch {
                                        try {
                                            val response = RetrofitClient.usuarioApi.nuevaContrasena(
                                                NuevaContrasenaRequest(
                                                    correo          = correo,
                                                    codigo          = codigo,
                                                    nuevaContrasena = hashSHA256Recup(nuevaContra)
                                                )
                                            )
                                            isLoading = false
                                            if (response.isSuccessful) {
                                                toastContra = ToastData(
                                                    icon     = Icons.Default.LockOpen,
                                                    iconBg   = primaryContainerColor,
                                                    iconTint = primaryColor,
                                                    title    = "Contraseña actualizada",
                                                    message  = "Usa tus nuevas credenciales para ingresar."
                                                )
                                            } else {
                                                errorMessage = response.body()?.error
                                                    ?: "Error al actualizar"
                                            }
                                        } catch (e: Exception) {
                                            isLoading = false
                                            errorMessage = "Sin conexión. Intenta de nuevo."
                                        }
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape    = RoundedCornerShape(12.dp),
                        colors   = ButtonDefaults.buttonColors(containerColor = primaryColor),
                        enabled  = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color    = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Text(
                                "Actualizar Contraseña",
                                fontWeight = FontWeight.Bold,
                                fontSize   = 16.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(primaryContainerColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Security,
                    contentDescription = null,
                    tint     = primaryColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text  = "Tus datos están protegidos por el sistema de seguridad encriptado de SJL Alerta Guard.",
                style = MaterialTheme.typography.bodySmall.copy(
                    color     = onSurfaceVariantColor,
                    textAlign = TextAlign.Center),
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Toast anclado arriba
        Box(modifier = Modifier.align(Alignment.TopCenter)) {
            SuccessToast(
                toastData = toastContra,
                onDismiss = {
                    toastContra = null
                    onActualizar()
                }
            )
        }
    }
}