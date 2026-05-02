package com.example.sjl_alert_v4.actividades

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sjl_alert_v4.R
import com.example.sjl_alert_v4.ui.theme.*

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun DirectorioPreview() {
    SJL_Alert_v4Theme {
        DirectorioPage(
            onBack = {},
            onNavigateToHome = {},
            onNavigateToReports = {},
            onLogout = {},
            onNavigateToSettings = {}
        )
    }
}

data class Contacto(
    val nombre: String,
    val descripcion: String,
    val numero: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val isPrincipal: Boolean = false
)

@Composable
fun DirectorioPage(
    onBack: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToReports: () -> Unit,
    onLogout: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {}
) {
    // ── Lista de contactos con textos localizados ─────────────────────────
    // Al cambiar el idioma, stringResource() retorna automáticamente
    // el texto correcto según el locale activo.
    val contactos = listOf(
        Contacto(
            nombre      = stringResource(R.string.contacto_serenazgo_nombre),
            descripcion = stringResource(R.string.contacto_serenazgo_desc),
            numero      = "(01) 388-1212",
            icon        = Icons.Default.Shield,
            isPrincipal = true
        ),
        Contacto(
            nombre      = stringResource(R.string.contacto_pnp_nombre),
            descripcion = stringResource(R.string.contacto_pnp_desc),
            numero      = "105",
            icon        = Icons.Default.Policy
        ),
        Contacto(
            nombre      = stringResource(R.string.contacto_bomberos_nombre),
            descripcion = stringResource(R.string.contacto_bomberos_desc),
            numero      = "116",
            icon        = Icons.Default.FireTruck
        )
    )

    val backgroundColor       = MaterialTheme.colorScheme.background
    val primaryColor          = MaterialTheme.colorScheme.primary
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant

    Scaffold(
        topBar = {
            TopHeader(
                onLogout = onLogout,
                onNavigateToSettings = onNavigateToSettings
            )
        },
        bottomBar = {
            BottomNavigationBar(
                currentScreen    = "directory",
                onHomeClick      = onNavigateToHome,
                onReportsClick   = onNavigateToReports,
                onDirectoryClick = { }
            )
        },
        containerColor = backgroundColor
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text       = stringResource(R.string.directorio_titulo),
                fontSize   = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color      = primaryColor,
                lineHeight = 38.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text       = stringResource(R.string.directorio_subtitulo),
                fontSize   = 16.sp,
                color      = onSurfaceVariantColor,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier            = Modifier.fillMaxSize()
            ) {
                items(contactos) { contacto -> NewContactCard(contacto) }
                item { Spacer(modifier = Modifier.height(20.dp)) }
            }
        }
    }
}

@Composable
fun NewContactCard(contacto: Contacto) {
    val context = LocalContext.current

    val surfaceColor          = MaterialTheme.colorScheme.surface
    val primaryColor          = MaterialTheme.colorScheme.primary
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant
    val surfaceVariantColor   = MaterialTheme.colorScheme.surfaceVariant

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = surfaceColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {

            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.Top
            ) {
                Box(
                    modifier         = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(SoftRed.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        contacto.icon,
                        contentDescription = null,
                        tint               = DeepRed,
                        modifier           = Modifier.size(28.dp)
                    )
                }
                if (contacto.isPrincipal) {
                    Surface(color = SoftRed.copy(alpha = 0.8f), shape = RoundedCornerShape(12.dp)) {
                        Text(
                            text     = stringResource(R.string.principal),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color    = DeepRed
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Nombre y descripción ya vienen localizados desde la lista
            Text(contacto.nombre,      fontSize = 20.sp, fontWeight = FontWeight.Bold, color = primaryColor)
            Text(contacto.descripcion, fontSize = 14.sp, color = onSurfaceVariantColor)

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    // Limpia el número y abre el marcador del celular
                    val numeroLimpio = contacto.numero
                        .replace(" ", "")
                        .replace("(", "")
                        .replace(")", "")
                        .replace("-", "")
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$numeroLimpio"))
                    context.startActivity(intent)
                },
                modifier       = Modifier.fillMaxWidth(),
                shape          = RoundedCornerShape(12.dp),
                colors         = ButtonDefaults.buttonColors(
                    containerColor = if (contacto.isPrincipal) DeepRed else surfaceVariantColor,
                    contentColor   = if (contacto.isPrincipal) Color.White else primaryColor
                ),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (contacto.isPrincipal) contacto.numero
                        else stringResource(R.string.llamar, contacto.numero),
                        fontWeight = FontWeight.Bold,
                        fontSize   = 16.sp
                    )
                }
            }
        }
    }
}