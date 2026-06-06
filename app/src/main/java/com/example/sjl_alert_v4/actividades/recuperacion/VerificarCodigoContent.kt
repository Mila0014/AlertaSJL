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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sjl_alert_v4.R
import com.example.sjl_alert_v4.ui.theme.DeepRed
import com.example.sjl_alert_v4.ui.theme.PrimaryBlue
import com.example.sjl_alert_v4.ui.theme.SJL_Alert_v4Theme

@Composable
fun VerificarCodigoContent(
    onVerificar: () -> Unit,
    onReenviar: () -> Unit
) {
    var codigo by remember { mutableStateOf(List(6) { "" }) }

    // Colores del tema para el logo
    val primaryColor = MaterialTheme.colorScheme.primary

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        // LOGO (Igual que en Login y RecuperarContraContent)
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
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "SJL Alerta",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        color = primaryColor,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.5).sp
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Verificar Código",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = PrimaryBlue
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Hemos enviado un código de 6 dígitos a tu correo. Por favor, ingrésalo a continuación para continuar.",
            style = MaterialTheme.typography.bodyLarge.copy(
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Fila de inputs para el código
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(6) { index ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF3F4F6)),
                    contentAlignment = Alignment.Center
                ) {
                    // Simulación de input de un dígito
                    Text(
                        text = codigo[index].ifEmpty { "•" },
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (codigo[index].isEmpty()) Color.LightGray else PrimaryBlue
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onVerificar,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = DeepRed)
        ) {
            Text("Verificar", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "¿No recibiste el código?",
            color = Color.Gray,
            fontSize = 14.sp
        )
        Text(
            text = "Reenviar",
            fontWeight = FontWeight.Bold,
            color = DeepRed,
            modifier = Modifier
                .padding(top = 4.dp)
                .clickable { onReenviar() }
        )

        Spacer(modifier = Modifier.weight(1f))

        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFF3F4F6)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.LightGray)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Seguridad digital SJL • 2026",
            style = MaterialTheme.typography.labelSmall.copy(
                color = Color.LightGray,
                letterSpacing = 1.sp
            )
        )
        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun VerificarCodigoContentPreview() {
    SJL_Alert_v4Theme {
        VerificarCodigoContent(onVerificar = {}, onReenviar = {})
    }
}