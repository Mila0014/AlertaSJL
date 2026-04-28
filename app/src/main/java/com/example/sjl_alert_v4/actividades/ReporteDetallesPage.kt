package com.example.sjl_alert_v4.actividades

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.rememberAsyncImagePainter
import com.example.sjl_alert_v4.modelos.AppDatabase
import com.example.sjl_alert_v4.modelos.IncidenciaEntity
import com.example.sjl_alert_v4.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.Image

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReporteDetallesPage(
    reporteId: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }

    var reporte by remember { mutableStateOf<IncidenciaEntity?>(null) }
    var imagenAmpliada by remember { mutableStateOf<Uri?>(null) }

    // Cargar reporte por ID
    LaunchedEffect(reporteId) {
        db.incidenciaDao().obtenerPorId(reporteId).collect { r ->
            reporte = r
        }
    }

    val backgroundColor = MaterialTheme.colorScheme.background
    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant

    // ── Visor de imagen ampliada ───────────────────────────────────────────────
    imagenAmpliada?.let { uri ->
        Dialog(
            onDismissRequest = { imagenAmpliada = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.95f))
                    .clickable { imagenAmpliada = null },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = rememberAsyncImagePainter(uri),
                    contentDescription = "Evidencia ampliada",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentScale = ContentScale.Fit
                )
                IconButton(
                    onClick = { imagenAmpliada = null },
                    modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar",
                        tint = Color.White, modifier = Modifier.size(28.dp))
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle del Reporte", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                },
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = backgroundColor,
                    titleContentColor = primaryColor,
                    navigationIconContentColor = primaryColor
                )
            )
        },
        containerColor = backgroundColor
    ) { paddingValues ->

        if (reporte == null) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = primaryColor)
            }
            return@Scaffold
        }

        val r = reporte!!
        val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val fechaStr = formatter.format(Date(r.fecha))

        val evidenciaUris = if (r.evidencias.isBlank()) emptyList()
        else r.evidencias.split(",").map { Uri.parse(it.trim()) }

        val (estadoColor, estadoLabel) = when (r.estado) {
            "PENDIENTE"  -> Pair(DeepYellow, "Pendiente")
            "EN_PROCESO" -> Pair(primaryColor, "En proceso")
            "RESUELTO"   -> Pair(Color(0xFF2E7D32), "Resuelto")
            "RECHAZADO"  -> Pair(DeepRed, "Rechazado")
            else         -> Pair(onSurfaceVariantColor, r.estado)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // ── Tipo + Estado ──────────────────────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Row(modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Report, contentDescription = null,
                            tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Tipo de Incidencia", fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f))
                            Text(r.tipo, fontWeight = FontWeight.Bold, fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.onErrorContainer)
                        }
                    }
                    Surface(shape = RoundedCornerShape(8.dp),
                        color = estadoColor.copy(alpha = 0.2f)) {
                        Text(estadoLabel,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            fontSize = 11.sp, fontWeight = FontWeight.Bold, color = estadoColor)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Fecha ──────────────────────────────────────────────────────────
            DetalleItem(
                icon = Icons.Default.CalendarToday,
                label = "Fecha y hora",
                valor = fechaStr,
                primaryColor = primaryColor,
                onSurfaceVariantColor = onSurfaceVariantColor
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ── Ubicación ──────────────────────────────────────────────────────
            if (r.ubicacion.isNotBlank()) {
                DetalleItem(
                    icon = Icons.Default.LocationOn,
                    label = "Ubicación",
                    valor = r.ubicacion,
                    primaryColor = primaryColor,
                    onSurfaceVariantColor = onSurfaceVariantColor
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // ── Descripción ────────────────────────────────────────────────────
            if (r.descripcion.isNotBlank()) {
                Text("Descripción", fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                    color = onSurfaceVariantColor)
                Spacer(modifier = Modifier.height(6.dp))
                Card(modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Text(r.descripcion, modifier = Modifier.padding(14.dp),
                        fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 20.sp)
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // ── Evidencias ─────────────────────────────────────────────────────
            if (evidenciaUris.isNotEmpty()) {
                Text("Evidencias (${evidenciaUris.size})",
                    fontSize = 16.sp, fontWeight = FontWeight.Bold, color = primaryColor)
                Text("Toca una imagen para verla en grande",
                    fontSize = 12.sp, color = onSurfaceVariantColor)
                Spacer(modifier = Modifier.height(10.dp))

                // Grid de evidencias
                val chunked = evidenciaUris.chunked(2)
                chunked.forEach { fila ->
                    Row(modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        fila.forEach { uri ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(160.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { imagenAmpliada = uri }
                            ) {
                                Image(
                                    painter = rememberAsyncImagePainter(uri),
                                    contentDescription = "Evidencia",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                                // Ícono de zoom
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(6.dp)
                                        .size(28.dp)
                                        .background(Color.Black.copy(alpha = 0.5f),
                                            RoundedCornerShape(6.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.ZoomIn, contentDescription = null,
                                        tint = Color.White, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                        // Si la fila tiene solo 1 item, rellenar el espacio
                        if (fila.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            } else {
                // Sin evidencias
                Card(modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Row(modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ImageNotSupported, contentDescription = null,
                            tint = onSurfaceVariantColor, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Sin evidencias adjuntas", fontSize = 14.sp,
                            color = onSurfaceVariantColor)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun DetalleItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    valor: String,
    primaryColor: androidx.compose.ui.graphics.Color,
    onSurfaceVariantColor: androidx.compose.ui.graphics.Color
) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(icon, contentDescription = null,
            tint = primaryColor, modifier = Modifier.size(20.dp).padding(top = 2.dp))
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(label, fontSize = 12.sp, color = onSurfaceVariantColor,
                fontWeight = FontWeight.SemiBold)
            Text(valor, fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface)
        }
    }
}