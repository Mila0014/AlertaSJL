package com.example.sjl_alert_v4.actividades

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sjl_alert_v4.modelos.AppDatabase
import com.example.sjl_alert_v4.modelos.IncidenciaEntity
import com.example.sjl_alert_v4.sharedPrefs.PreferenceManager
import com.example.sjl_alert_v4.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MisReportesPage(
    onBack: () -> Unit,
    onLogout: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNuevoReporte: () -> Unit,
    onNavigateToDirectory: () -> Unit = {},
    onVerDetalles: (String) -> Unit = {}   // ✅ navega a detalles con el ID
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }
    val prefManager = remember { PreferenceManager(context) }
    val usuarioId = remember { prefManager.getSesionUsuarioId() }
    val nombre = remember { prefManager.getSesionNombre() }

    val reportes by db.incidenciaDao()
        .obtenerPorUsuario(usuarioId)
        .collectAsState(initial = emptyList())

    val backgroundColor = MaterialTheme.colorScheme.background
    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant

    Scaffold(
        topBar = {
            TopHeader(onLogout = onLogout, onNavigateToSettings = onNavigateToSettings)
        },
        bottomBar = {
            BottomNavigationBar(
                currentScreen = "home",
                onHomeClick = { },
                onReportsClick = onNuevoReporte,
                onDirectoryClick = onNavigateToDirectory
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNuevoReporte,
                containerColor = primaryColor,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nuevo reporte")
            }
        },
        containerColor = backgroundColor
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            Text("Hola, ${nombre.ifBlank { "Vecino" }} 👋",
                fontSize = 22.sp, fontWeight = FontWeight.Bold, color = primaryColor)
            Text("Aquí están tus reportes enviados.",
                fontSize = 14.sp, color = onSurfaceVariantColor,
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp))

            if (reportes.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Assignment, contentDescription = null,
                            modifier = Modifier.size(72.dp),
                            tint = onSurfaceVariantColor.copy(alpha = 0.4f))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("No tienes reportes aún", fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold, color = onSurfaceVariantColor)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Ve a Reportes para crear uno",
                            fontSize = 14.sp, color = onSurfaceVariantColor.copy(alpha = 0.7f))
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(onClick = onNuevoReporte,
                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                            shape = RoundedCornerShape(12.dp)) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Crear reporte", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(reportes) { reporte ->
                        // ✅ Al tocar la card va a detalles
                        ReporteCard(
                            reporte = reporte,
                            onClick = { onVerDetalles(reporte.id) }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }
}

@Composable
fun ReporteCard(
    reporte: IncidenciaEntity,
    onClick: () -> Unit = {}
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant

    val formatter = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }
    val fechaStr = formatter.format(Date(reporte.fecha))

    val (estadoColor, estadoLabel) = when (reporte.estado) {
        "PENDIENTE"  -> Pair(DeepYellow, "Pendiente")
        "EN_PROCESO" -> Pair(primaryColor, "En proceso")
        "RESUELTO"   -> Pair(Color(0xFF2E7D32), "Resuelto")
        "RECHAZADO"  -> Pair(DeepRed, "Rechazado")
        else         -> Pair(onSurfaceVariantColor, reporte.estado)
    }

    val cantidadEvidencias = if (reporte.evidencias.isBlank()) 0
    else reporte.evidencias.split(",").size

    Card(
        onClick = onClick,   // ✅ clickeable
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp))
                        .background(SoftRed.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Report, contentDescription = null,
                            tint = DeepRed, modifier = Modifier.size(22.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(reporte.tipo, fontWeight = FontWeight.Bold,
                            fontSize = 15.sp, color = primaryColor)
                        Text(fechaStr, fontSize = 11.sp, color = onSurfaceVariantColor)
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = RoundedCornerShape(8.dp), color = estadoColor.copy(alpha = 0.15f)) {
                        Text(estadoLabel,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            fontSize = 11.sp, fontWeight = FontWeight.Bold, color = estadoColor)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.ChevronRight, contentDescription = "Ver detalles",
                        tint = onSurfaceVariantColor, modifier = Modifier.size(20.dp))
                }
            }

            if (reporte.descripcion.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(reporte.descripcion, fontSize = 13.sp,
                    color = onSurfaceVariantColor, maxLines = 2)
            }
            if (reporte.ubicacion.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null,
                        tint = onSurfaceVariantColor, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(reporte.ubicacion, fontSize = 12.sp,
                        color = onSurfaceVariantColor, maxLines = 1)
                }
            }
            if (cantidadEvidencias > 0) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AttachFile, contentDescription = null,
                        tint = primaryColor, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("$cantidadEvidencias evidencia(s) adjunta(s)",
                        fontSize = 12.sp, color = primaryColor)
                }
            }
        }
    }
}