package com.example.sjl_alert_v4.actividades

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.vector.ImageVector
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
    val icon: ImageVector,
    val iconBg: Color = SoftRed,
    val iconTint: Color = DeepRed,
    val isPrincipal: Boolean = false,
    val whatsapp: String? = null
)

@Composable
fun DirectorioPage(
    onBack: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToReports: () -> Unit,
    onLogout: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {}
) {
    // ── Listas de contactos categorizadas ─────────────────────────────────
    
    val contactosPrincipales = listOf(
        Contacto(
            nombre      = stringResource(R.string.contacto_serenazgo_nombre),
            descripcion = stringResource(R.string.contacto_serenazgo_desc),
            numero      = "(01) 510-2090",
            icon        = Icons.Default.Shield,
            iconBg      = SoftRed,
            iconTint    = DeepRed,
            isPrincipal = true,
            whatsapp    = "926369993"
        ),
        Contacto(
            nombre      = stringResource(R.string.contacto_pnp_nombre),
            descripcion = stringResource(R.string.contacto_pnp_desc),
            numero      = "105",
            icon        = Icons.Default.Policy,
            iconBg      = SoftPurple,
            iconTint    = DeepPurple
        ),
        Contacto(
            nombre      = stringResource(R.string.contacto_bomberos_nombre),
            descripcion = stringResource(R.string.contacto_bomberos_desc),
            numero      = "116",
            icon        = Icons.Default.FireTruck,
            iconBg      = SoftRed.copy(alpha = 0.7f),
            iconTint    = DeepRed
        ),
        Contacto(
            nombre      = stringResource(R.string.contacto_samu_nombre),
            descripcion = stringResource(R.string.contacto_samu_desc),
            numero      = "106",
            icon        = Icons.Default.MedicalServices,
            iconBg      = SoftYellow,
            iconTint    = DeepYellow
        ),
        Contacto(
            nombre      = stringResource(R.string.contacto_comisaria_se_nombre),
            descripcion = stringResource(R.string.contacto_comisaria_se_desc),
            numero      = "(01) 387-6001",
            icon        = Icons.Default.Apartment,
            iconBg      = LightBlue,
            iconTint    = SecondaryBlue
        )
    )

    val otrasComisarias = listOf(
        Contacto(
            nombre = stringResource(R.string.contacto_comisaria_zarate_nombre),
            descripcion = "",
            numero = "(01) 458-1212",
            icon = Icons.Default.Policy
        ),
        Contacto(
            nombre = stringResource(R.string.contacto_comisaria_canto_rey_nombre),
            descripcion = "",
            numero = "(01) 388-3450",
            icon = Icons.Default.Policy
        )
    )

    val prensa = listOf(
        Contacto(
            nombre = stringResource(R.string.contacto_radio_exitosa_nombre),
            descripcion = stringResource(R.string.contacto_radio_exitosa_desc),
            numero = "940800800",
            icon = Icons.Default.Radio,
            iconBg = SoftPurple,
            iconTint = DeepPurple
        ),
        Contacto(
            nombre = stringResource(R.string.contacto_rpp_nombre),
            descripcion = stringResource(R.string.contacto_rpp_desc),
            numero = "951431013",
            icon = Icons.Default.Newspaper,
            iconBg = SoftYellow,
            iconTint = DeepYellow
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
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
            }

            items(contactosPrincipales) { contacto ->
                NewContactCard(contacto)
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.otras_comisarias_titulo),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(otrasComisarias) { comisaria ->
                SecondaryContactCard(comisaria)
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.prensa_denuncias_titulo),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(prensa) { contactoPrensa ->
                NewContactCard(contactoPrensa)
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
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
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                        .background(contacto.iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        contacto.icon,
                        contentDescription = null,
                        tint               = contacto.iconTint,
                        modifier           = Modifier.size(28.dp)
                    )
                }
                if (contacto.isPrincipal) {
                    Surface(
                        color = SoftRed.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text     = stringResource(R.string.principal),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color    = DeepRed
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                contacto.nombre,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = primaryColor
            )
            Text(
                contacto.descripcion,
                fontSize = 14.sp,
                color = onSurfaceVariantColor
            )
            
            if (contacto.whatsapp != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Sms,
                        contentDescription = null,
                        tint = Color(0xFF25D366),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.whatsapp_label, contacto.whatsapp),
                        fontSize = 13.sp,
                        color = onSurfaceVariantColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
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
                    Icon(
                        Icons.Default.Call,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
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

@Composable
fun SecondaryContactCard(contacto: Contacto) {
    val context = LocalContext.current
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant
    val primaryColor = MaterialTheme.colorScheme.primary

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    contacto.nombre,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor
                )
                Text(
                    contacto.numero,
                    fontSize = 14.sp,
                    color = onSurfaceVariantColor
                )
            }
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                    .clickable { 
                        val numeroLimpio = contacto.numero
                            .replace(" ", "")
                            .replace("(", "")
                            .replace(")", "")
                            .replace("-", "")
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$numeroLimpio"))
                        context.startActivity(intent)
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Call,
                    contentDescription = "Llamar",
                    tint = primaryColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
