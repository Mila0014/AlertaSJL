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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sjl_alert_v4.ui.theme.*

@Composable
fun NuevaContraContent(
    onActualizar: () -> Unit
) {
    var nuevaContra by remember { mutableStateOf("") }
    var confirmarContra by remember { mutableStateOf("") }
    var contraVisible by remember { mutableStateOf(false) }
    var confirmarContraVisible by remember { mutableStateOf(false) }

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
                fontWeight = FontWeight.Bold,
                color = PrimaryBlue
            ),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Start
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Crea una contraseña segura para proteger tu cuenta.",
            style = MaterialTheme.typography.bodyLarge.copy(color = Color.Gray),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Start
        )

        Spacer(modifier = Modifier.height(32.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA).copy(alpha = 0.5f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Nueva Contraseña",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = nuevaContra,
                    onValueChange = { nuevaContra = it },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = if (contraVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { contraVisible = !contraVisible }) {
                            Icon(if (contraVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, null)
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f)
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Fortaleza de la contraseña
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Fortaleza de la contraseña", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text("Media", style = MaterialTheme.typography.labelSmall, color = DeepYellow, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(modifier = Modifier.weight(1f).height(6.dp).clip(CircleShape).background(DeepYellow))
                    Box(modifier = Modifier.weight(1f).height(6.dp).clip(CircleShape).background(DeepYellow))
                    Box(modifier = Modifier.weight(1f).height(6.dp).clip(CircleShape).background(Color.LightGray))
                    Box(modifier = Modifier.weight(1f).height(6.dp).clip(CircleShape).background(Color.LightGray))
                }

                Spacer(modifier = Modifier.height(16.dp))

                RequisitoItem(texto = "Al menos 8 caracteres", cumplido = true)
                RequisitoItem(texto = "Incluye un símbolo o número", cumplido = false)

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Confirmar Contraseña",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = confirmarContra,
                    onValueChange = { confirmarContra = it },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = if (confirmarContraVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { confirmarContraVisible = !confirmarContraVisible }) {
                            Icon(if (confirmarContraVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, null)
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f)
                    )
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = onActualizar,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DeepRed)
                ) {
                    Text("Actualizar Contraseña", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFDDE2F4)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Security, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(24.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Tus datos están protegidos por el sistema de seguridad encriptado de SJL Alerta Guard.",
            style = MaterialTheme.typography.bodySmall.copy(
                color = Color.Gray,
                textAlign = TextAlign.Center
            ),
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun RequisitoItem(texto: String, cumplido: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
        Icon(
            imageVector = if (cumplido) Icons.Default.CheckCircle else Icons.Default.Circle,
            contentDescription = null,
            tint = if (cumplido) PrimaryBlue else Color.LightGray,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(texto, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
    }
}

@Preview(showBackground = true)
@Composable
fun NuevaContraContentPreview() {
    SJL_Alert_v4Theme {
        NuevaContraContent(onActualizar = {})
    }
}