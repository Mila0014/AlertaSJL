package com.example.sjl_alert_v4.actividades

import android.Manifest
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.rememberAsyncImagePainter
import com.example.sjl_alert_v4.modelos.AppDatabase
import com.example.sjl_alert_v4.modelos.IncidenciaEntity
import com.example.sjl_alert_v4.sharedPrefs.PreferenceManager
import com.example.sjl_alert_v4.ui.theme.*
import com.example.sjl_alert_v4.utilidades.LocationHelper
import com.example.sjl_alert_v4.utilidades.MediaHelper
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.util.UUID

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ReportDetailPagePreview() {
    SJL_Alert_v4Theme {
        ReportDetailPage(
            tipoIncidencia = "Alerta de robo",
            onBack = {},
            onReportSent = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportDetailPage(
    tipoIncidencia: String,
    onBack: () -> Unit,
    onReportSent: () -> Unit  // ✅ Navega a lista de incidencias al enviar
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = remember { AppDatabase.getInstance(context) }
    val prefManager = remember { PreferenceManager(context) }
    val locationHelper = remember { LocationHelper(context) }
    val mediaHelper = remember { MediaHelper(context) }

    var descripcion by remember { mutableStateOf("") }
    var ubicacionReferencia by remember { mutableStateOf("Obteniendo ubicación...") }
    var latitud by remember { mutableStateOf<Double?>(null) }
    var longitud by remember { mutableStateOf<Double?>(null) }
    var isEnviando by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }

    val evidencias = remember { mutableStateListOf<Uri>() }
    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }

    val backgroundColor = MaterialTheme.colorScheme.background
    val primaryColor = MaterialTheme.colorScheme.primary
    val onPrimaryColor = MaterialTheme.colorScheme.onPrimary
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant

    // ── Launchers ─────────────────────────────────────────────────────────────
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success -> if (success) tempPhotoUri?.let { evidencias.add(it) } }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris -> evidencias.addAll(uris) }

    val videoLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris -> evidencias.addAll(uris) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        ) {
            locationHelper.getCurrentLocation { location ->
                location?.let {
                    latitud = it.latitude
                    longitud = it.longitude
                    ubicacionReferencia = "Lat: ${"%.5f".format(it.latitude)}, Lon: ${"%.5f".format(it.longitude)}"
                } ?: run { ubicacionReferencia = "No se pudo obtener la ubicación" }
            }
        } else {
            ubicacionReferencia = "Permiso de ubicación denegado"
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val file = mediaHelper.createImageFile()
            val uri = mediaHelper.getUriForFile(file)
            tempPhotoUri = uri
            cameraLauncher.launch(uri)
        } else {
            Toast.makeText(context, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
        }
    }

    // Obtener ubicación al abrir
    LaunchedEffect(Unit) {
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    // ── Diálogo de confirmación de envío ──────────────────────────────────────
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            icon = { Icon(Icons.Default.CheckCircle, contentDescription = null, tint = primaryColor, modifier = Modifier.size(48.dp)) },
            title = { Text("¡Reporte Enviado!", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Tu reporte de \"$tipoIncidencia\" fue registrado exitosamente. " +
                            "Las autoridades han sido notificadas.",
                    color = onSurfaceVariantColor
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmDialog = false
                        onReportSent()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                ) {
                    Text("Ver mis reportes")
                }
            },
            containerColor = backgroundColor
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nuevo Reporte", fontWeight = FontWeight.Bold) },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // ── Tipo de incidencia ─────────────────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Report, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Tipo de Incidencia", fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f))
                        Text(tipoIncidencia, fontWeight = FontWeight.Bold, fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onErrorContainer)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Mapa en tiempo real (OpenStreetMap) ────────────────────────────
            Text("Tu Ubicación en Tiempo Real", fontWeight = FontWeight.Bold,
                color = primaryColor, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                ) {
                    if (latitud != null && longitud != null) {
                        // Mapa real con OSMDroid
                        AndroidView(
                            factory = { ctx ->
                                Configuration.getInstance().userAgentValue = ctx.packageName
                                MapView(ctx).apply {
                                    setTileSource(TileSourceFactory.MAPNIK)
                                    setMultiTouchControls(true)
                                    controller.setZoom(17.0)
                                    val point = GeoPoint(latitud!!, longitud!!)
                                    controller.setCenter(point)
                                    val marker = Marker(this)
                                    marker.position = point
                                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                    marker.title = "Tu ubicación"
                                    overlays.add(marker)
                                }
                            },
                            update = { mapView ->
                                val point = GeoPoint(latitud!!, longitud!!)
                                mapView.controller.setCenter(point)
                                mapView.overlays.clear()
                                val marker = Marker(mapView)
                                marker.position = point
                                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                marker.title = "Tu ubicación"
                                mapView.overlays.add(marker)
                                mapView.invalidate()
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                        // Badge EN TIEMPO REAL
                        Surface(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp),
                            shape = RoundedCornerShape(8.dp),
                            color = primaryColor
                        ) {
                            Text(
                                "● EN VIVO",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    } else {
                        // Estado de carga
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(color = primaryColor)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Obteniendo ubicación...", color = onSurfaceVariantColor, fontSize = 14.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Campo de referencia editable
            OutlinedTextField(
                value = ubicacionReferencia,
                onValueChange = { ubicacionReferencia = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Referencia o dirección") },
                leadingIcon = {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = onSurfaceVariantColor)
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = primaryColor,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ── Evidencia multimedia ───────────────────────────────────────────
            Text("Adjuntar Evidencia", fontWeight = FontWeight.Bold, color = primaryColor, fontSize = 16.sp)
            Text("Puedes subir fotos o videos del incidente",
                fontSize = 13.sp, color = onSurfaceVariantColor)
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Botón Cámara
                OutlinedButton(
                    onClick = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = primaryColor)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Cámara", fontSize = 13.sp)
                }
                // Botón Fotos
                OutlinedButton(
                    onClick = { galleryLauncher.launch("image/*") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = primaryColor)
                ) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Fotos", fontSize = 13.sp)
                }
                // Botón Video
                OutlinedButton(
                    onClick = { videoLauncher.launch("video/*") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = primaryColor)
                ) {
                    Icon(Icons.Default.Videocam, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Video", fontSize = 13.sp)
                }
            }

            // Miniaturas de evidencias
            if (evidencias.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text("${evidencias.size} archivo(s) adjunto(s)",
                    fontSize = 12.sp, color = primaryColor, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(evidencias) { uri ->
                        Box {
                            Image(
                                painter = rememberAsyncImagePainter(uri),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                            // Botón eliminar
                            IconButton(
                                onClick = { evidencias.remove(uri) },
                                modifier = Modifier
                                    .size(22.dp)
                                    .align(Alignment.TopEnd)
                                    .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Eliminar",
                                    tint = Color.White, modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Descripción ───────────────────────────────────────────────────
            Text("Descripción del Incidente", fontWeight = FontWeight.Bold,
                color = primaryColor, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                placeholder = {
                    Text(
                        "Cuéntanos lo que está sucediendo para que podamos ayudarte mejor...",
                        color = onSurfaceVariantColor
                    )
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = primaryColor,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            // ── Botón Enviar ──────────────────────────────────────────────────
            Button(
                onClick = {
                    if (descripcion.isBlank()) {
                        Toast.makeText(context, "Escribe una descripción", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    isEnviando = true
                    scope.launch {
                        try {
                            val usuarioId = prefManager.getSesionUsuarioId()
                            val incidencia = IncidenciaEntity(
                                id = UUID.randomUUID().toString(),
                                tipo = tipoIncidencia,
                                descripcion = descripcion,
                                ubicacion = ubicacionReferencia,
                                latitud = latitud,
                                longitud = longitud,
                                evidencias = evidencias.joinToString(",") { it.toString() },
                                fecha = System.currentTimeMillis(),
                                estado = "PENDIENTE",
                                usuarioId = usuarioId
                            )
                            db.incidenciaDao().insertar(incidencia)
                            isEnviando = false
                            showConfirmDialog = true
                        } catch (e: Exception) {
                            isEnviando = false
                            Toast.makeText(context, "Error al enviar reporte", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryColor,
                    contentColor = onPrimaryColor
                ),
                enabled = !isEnviando
            ) {
                if (isEnviando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = onPrimaryColor,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Default.Send, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Enviar Reporte", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}