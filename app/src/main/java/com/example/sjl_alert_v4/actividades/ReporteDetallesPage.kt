package com.example.sjl_alert_v4.actividades

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.rememberAsyncImagePainter
import com.example.sjl_alert_v4.R
import com.example.sjl_alert_v4.modelos.AppDatabase
import com.example.sjl_alert_v4.modelos.IncidenciaEntity
import com.example.sjl_alert_v4.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

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

    LaunchedEffect(reporteId) {
        db.incidenciaDao().obtenerPorId(reporteId).collect { r -> reporte = r }
    }

    val backgroundColor       = MaterialTheme.colorScheme.background
    val primaryColor          = MaterialTheme.colorScheme.primary
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant

    // ── Strings para estados ──────────────────────────────────────────────
    val strPendiente = stringResource(R.string.pendiente)
    val strEnProceso = stringResource(R.string.en_proceso)
    val strResuelto  = stringResource(R.string.resuelto)
    val strRechazado = stringResource(R.string.rechazado)

    // ── Visor de imagen ampliada ──────────────────────────────────────────
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
                    contentDescription = stringResource(R.string.evidencias),
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    contentScale = ContentScale.Fit
                )
                IconButton(
                    onClick = { imagenAmpliada = null },
                    modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = stringResource(R.string.cerrar),
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.detalle_reporte), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.atras))
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
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = primaryColor)
            }
            return@Scaffold
        }

        val r = reporte!!
        val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val fechaStr = formatter.format(Date(r.fecha))

        // ── Construir lista de URIs de evidencias ──────────────────────────
        // Prioridad: campo evidencias (multi-imagen) → campo imagenUri (imagen única)
        val evidenciaUris: List<Uri> = when {
            r.evidencias.isNotBlank() -> {
                r.evidencias.split(",")
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                    .map { Uri.parse(it) }
            }
            r.imagenUri != null -> listOf(Uri.parse(r.imagenUri))
            else -> emptyList()
        }

        val (estadoColor, estadoLabel) = when (r.estado) {
            "PENDIENTE"  -> Pair(DeepYellow, strPendiente)
            "EN_PROCESO" -> Pair(primaryColor, strEnProceso)
            "RESUELTO"   -> Pair(Color(0xFF2E7D32), strResuelto)
            "RECHAZADO"  -> Pair(DeepRed, strRechazado)
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

            // ── Tipo + Estado ──────────────────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Report, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = stringResource(R.string.tipo_incidencia_label),
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f)
                            )
                            Text(r.tipo, fontWeight = FontWeight.Bold, fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.onErrorContainer)
                        }
                    }
                    Surface(shape = RoundedCornerShape(8.dp), color = estadoColor.copy(alpha = 0.2f)) {
                        Text(
                            estadoLabel,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            fontSize = 11.sp, fontWeight = FontWeight.Bold, color = estadoColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Fecha ──────────────────────────────────────────────────────
            DetalleItem(
                icon = Icons.Default.CalendarToday,
                label = stringResource(R.string.fecha_hora),
                valor = fechaStr,
                primaryColor = primaryColor,
                onSurfaceVariantColor = onSurfaceVariantColor
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ── Ubicación ──────────────────────────────────────────────────
            if (r.ubicacion.isNotBlank()) {
                DetalleItem(
                    icon = Icons.Default.LocationOn,
                    label = stringResource(R.string.ubicacion_label),
                    valor = r.ubicacion,
                    primaryColor = primaryColor,
                    onSurfaceVariantColor = onSurfaceVariantColor
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // ── Descripción ────────────────────────────────────────────────
            if (r.descripcion.isNotBlank()) {
                Text(
                    text = stringResource(R.string.descripcion),
                    fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                    color = onSurfaceVariantColor
                )
                Spacer(modifier = Modifier.height(6.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Text(r.descripcion, modifier = Modifier.padding(14.dp),
                        fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 20.sp)
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // ── Evidencias / Imagen del reporte ────────────────────────────
            // CP-09.1 / CP-09.2: Muestra las imágenes guardadas en evidencias o imagenUri
            // CP-09.3: Muestra mensaje "Sin evidencias" si no hay imágenes
            if (evidenciaUris.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.evidencias, evidenciaUris.size),
                    fontSize = 16.sp, fontWeight = FontWeight.Bold, color = primaryColor
                )
                Text(
                    text = stringResource(R.string.toca_imagen),
                    fontSize = 12.sp, color = onSurfaceVariantColor
                )
                Spacer(modifier = Modifier.height(10.dp))

                val chunked = evidenciaUris.chunked(2)
                chunked.forEach { fila ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
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
                                    contentDescription = stringResource(R.string.evidencias, 1),
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                                // Ícono zoom en esquina inferior derecha
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(6.dp)
                                        .size(28.dp)
                                        .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(6.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.ZoomIn,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                        if (fila.size == 1) Spacer(modifier = Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            } else {
                // CP-09.3: Sin imagen seleccionada → muestra tarjeta informativa
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.ImageNotSupported,
                            contentDescription = null,
                            tint = onSurfaceVariantColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            stringResource(R.string.sin_evidencias),
                            fontSize = 14.sp,
                            color = onSurfaceVariantColor
                        )
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
        Icon(
            icon,
            contentDescription = null,
            tint = primaryColor,
            modifier = Modifier.size(20.dp).padding(top = 2.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(label, fontSize = 12.sp, color = onSurfaceVariantColor, fontWeight = FontWeight.SemiBold)
            Text(valor, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}