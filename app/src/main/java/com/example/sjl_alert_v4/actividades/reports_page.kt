package com.example.sjl_alert_v4.actividades

import android.content.pm.PackageManager
import android.location.Geocoder
import android.net.Uri
import android.os.Build
import android.os.Looper
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import coil.compose.rememberAsyncImagePainter
import com.example.sjl_alert_v4.R
import com.example.sjl_alert_v4.modelos.AppDatabase
import com.example.sjl_alert_v4.modelos.IncidenciaEntity
import com.example.sjl_alert_v4.sharedPrefs.PreferenceManager
import com.example.sjl_alert_v4.ui.theme.*
import com.example.sjl_alert_v4.utilidades.MediaHelper
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import java.util.Locale
import java.util.UUID

// ─────────────────────────────────────────────────────────────────────────────
private data class TipoIncidencia(
    val id: String,
    val titulo: String,
    val icono: ImageVector,
    val iconoBg: Color,
    val iconoColor: Color
)

// ─────────────────────────────────────────────────────────────────────────────
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ReportsPreview() {
    SJL_Alert_v4Theme {
        ReportsPage(
            onLogout = {},
            onNavigateToDirectory = {},
            onNavigateToSettings = {},
            onNavigateToMisReportes = {}
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun ReportsPage(
    onLogout: () -> Unit,
    onNavigateToDirectory: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToMisReportes: () -> Unit = {}
) {
    val context     = LocalContext.current
    val scope       = rememberCoroutineScope()
    val db          = remember { AppDatabase.getInstance(context) }
    val prefManager = remember { PreferenceManager(context) }
    val mediaHelper = remember { MediaHelper(context) }

    val primaryColor          = MaterialTheme.colorScheme.primary
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant

    // ── Strings para usar en lambdas/coroutines ────────────────────────────
    val strAlertaEnviada   = stringResource(R.string.alerta_enviada)
    val strErrorGuardar    = stringResource(R.string.error_guardar)
    val strObteniendoUbic  = stringResource(R.string.obteniendo_ubicacion)
    val strUbicacionActual = "Ubicación actual"

    // ── Tipos de incidencia ────────────────────────────────────────────────
    val tipos = listOf(
        TipoIncidencia("ruidos",    stringResource(R.string.ruidos_molestos), Icons.Default.VolumeUp,  SoftPurple, DeepPurple),
        TipoIncidencia("libadores", stringResource(R.string.libadores),       Icons.Default.LocalBar,  SoftYellow, DeepYellow),
        TipoIncidencia("robo",      stringResource(R.string.alerta_robo),     Icons.Default.Warning,   SoftRed,    DeepRed),
        TipoIncidencia("siniestro", stringResource(R.string.siniestro),       Icons.Default.Whatshot,  SoftRed,    DeepRed),
        TipoIncidencia("otro",      stringResource(R.string.otro),            Icons.Default.Edit,
            Color(0xFFE8F5E9), Color(0xFF2E7D32))
    )

    // ── Estado formulario ──────────────────────────────────────────────────
    var tipoSeleccionadoId  by remember { mutableStateOf<String?>(null) }
    var tipoPersonalizado   by remember { mutableStateOf("") }

    var ubicacion           by remember { mutableStateOf(strObteniendoUbic) }
    var ubicacionGps        by remember { mutableStateOf("") }
    var ubicacionEditada    by remember { mutableStateOf(false) }
    var editandoUbicacion   by remember { mutableStateOf(false) }
    var ubicacionTemporal   by remember { mutableStateOf("") }
    var buscandoDireccion   by remember { mutableStateOf(false) }   // ← NUEVO

    var urisSeleccionadas   by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var comentario          by remember { mutableStateOf("") }
    var cargando            by remember { mutableStateOf(false) }
    var mostrarConfirmacion by remember { mutableStateOf(false) }

    // ── Mapa OSMDroid ──────────────────────────────────────────────────────
    var geoPoint by remember { mutableStateOf<GeoPoint?>(null) }
    var mapView  by remember { mutableStateOf<MapView?>(null) }

    // ── Estado GPS/permisos ────────────────────────────────────────────────
    var gpsDesactivado      by remember { mutableStateOf(false) }
    var permisoDenegadoPerm by remember { mutableStateOf(false) }

    // ── URI temporal para foto de cámara ───────────────────────────────────
    var fotoUri by remember { mutableStateOf<Uri?>(null) }

    // ─────────────────────────────────────────────────────────────────────
    // Función auxiliar — coordenadas → dirección legible
    // ─────────────────────────────────────────────────────────────────────
    fun resolverDireccion(lat: Double, lng: Double) {
        try {
            val geocoder = Geocoder(context, Locale("es", "PE"))
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                geocoder.getFromLocation(lat, lng, 1) { dirs ->
                    val dir = if (dirs.isNotEmpty()) dirs[0].getAddressLine(0) ?: strUbicacionActual
                    else "Lat: %.5f, Lng: %.5f".format(lat, lng)
                    ubicacion    = dir
                    ubicacionGps = dir
                    ubicacionEditada = false
                }
            } else {
                @Suppress("DEPRECATION")
                val dirs = geocoder.getFromLocation(lat, lng, 1)
                val dir  = if (!dirs.isNullOrEmpty()) dirs[0].getAddressLine(0) ?: strUbicacionActual
                else "Lat: %.5f, Lng: %.5f".format(lat, lng)
                ubicacion    = dir
                ubicacionGps = dir
                ubicacionEditada = false
            }
        } catch (e: Exception) {
            val dir = "Lat: %.5f, Lng: %.5f".format(lat, lng)
            ubicacion    = dir
            ubicacionGps = dir
            ubicacionEditada = false
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // NUEVO: Función — texto de dirección → coordenadas → mover mapa
    // ─────────────────────────────────────────────────────────────────────
    fun buscarDireccionEnMapa(direccion: String) {
        buscandoDireccion = true
        try {
            val geocoder = Geocoder(context, Locale("es", "PE"))
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                geocoder.getFromLocationName(direccion, 1) { resultados ->
                    buscandoDireccion = false
                    if (resultados.isNotEmpty()) {
                        val r = resultados[0]
                        geoPoint = GeoPoint(r.latitude, r.longitude)
                    } else {
                        Toast.makeText(context, "No se encontró la dirección en el mapa", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                @Suppress("DEPRECATION")
                val resultados = geocoder.getFromLocationName(direccion, 1)
                buscandoDireccion = false
                if (!resultados.isNullOrEmpty()) {
                    val r = resultados[0]
                    geoPoint = GeoPoint(r.latitude, r.longitude)
                } else {
                    Toast.makeText(context, "No se encontró la dirección en el mapa", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            buscandoDireccion = false
            Toast.makeText(context, "Error al buscar dirección: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // ── Launchers galería / cámara / permisos ──────────────────────────────
    val galeriaLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris -> urisSeleccionadas = urisSeleccionadas + uris }

    val camaraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { exito -> if (exito) fotoUri?.let { uri -> urisSeleccionadas = urisSeleccionadas + uri } }

    val permisoCamaraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) {
            val archivo = mediaHelper.createImageFile()
            val uri     = mediaHelper.getUriForFile(archivo)
            fotoUri = uri
            camaraLauncher.launch(uri)
        } else {
            Toast.makeText(context, "Permiso de cámara denegado. Actívalo en Ajustes.", Toast.LENGTH_LONG).show()
        }
    }

    val permisoAlmacenamientoLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) galeriaLauncher.launch("image/*")
        else Toast.makeText(context, "Permiso de almacenamiento denegado. Actívalo en Ajustes.", Toast.LENGTH_LONG).show()
    }

    val permisoUbicacionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permisos ->
        val concedido = permisos[android.Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permisos[android.Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (concedido) {
            permisoDenegadoPerm = false
            gpsDesactivado      = false
            try {
                val fusedClient = LocationServices.getFusedLocationProviderClient(context)
                val cts = com.google.android.gms.tasks.CancellationTokenSource()
                fusedClient.getCurrentLocation(
                    com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY, cts.token
                ).addOnSuccessListener { loc ->
                    if (loc != null) {
                        geoPoint = GeoPoint(loc.latitude, loc.longitude)
                        resolverDireccion(loc.latitude, loc.longitude)
                    } else {
                        gpsDesactivado = true
                        ubicacion = "Activa el GPS para detectar tu ubicación"
                    }
                }.addOnFailureListener {
                    gpsDesactivado = true
                    ubicacion = "Activa el GPS para detectar tu ubicación"
                }
            } catch (e: SecurityException) { /* no debería pasar */ }
        } else {
            permisoDenegadoPerm = true
            ubicacion = "Permiso de ubicación denegado"
        }
    }

    // ── Obtener ubicación al abrir la pantalla ─────────────────────────────
    LaunchedEffect(Unit) {
        val tienePermiso = ContextCompat.checkSelfPermission(
            context, android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!tienePermiso) {
            permisoUbicacionLauncher.launch(arrayOf(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ))
            return@LaunchedEffect
        }

        try {
            val fusedClient = LocationServices.getFusedLocationProviderClient(context)

            val locationManager = context.getSystemService(android.content.Context.LOCATION_SERVICE)
                    as android.location.LocationManager
            val gpsHabilitado = locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)
                    || locationManager.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER)

            if (!gpsHabilitado) {
                gpsDesactivado = true
                ubicacion = "Activa el GPS para detectar tu ubicación"
                return@LaunchedEffect
            }

            val cts = com.google.android.gms.tasks.CancellationTokenSource()
            fusedClient.getCurrentLocation(
                com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY, cts.token
            ).addOnSuccessListener { loc ->
                if (loc != null) {
                    gpsDesactivado = false
                    geoPoint = GeoPoint(loc.latitude, loc.longitude)
                    resolverDireccion(loc.latitude, loc.longitude)
                } else {
                    fusedClient.lastLocation.addOnSuccessListener { last ->
                        if (last != null) {
                            gpsDesactivado = false
                            geoPoint = GeoPoint(last.latitude, last.longitude)
                            resolverDireccion(last.latitude, last.longitude)
                        } else {
                            val request = com.google.android.gms.location.LocationRequest.Builder(
                                com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY, 5000L
                            ).setMaxUpdates(1).build()
                            val callback = object : com.google.android.gms.location.LocationCallback() {
                                override fun onLocationResult(result: com.google.android.gms.location.LocationResult) {
                                    result.lastLocation?.let { l ->
                                        gpsDesactivado = false
                                        geoPoint = GeoPoint(l.latitude, l.longitude)
                                        resolverDireccion(l.latitude, l.longitude)
                                    }
                                    fusedClient.removeLocationUpdates(this)
                                }
                            }
                            fusedClient.requestLocationUpdates(request, callback, Looper.getMainLooper())
                        }
                    }
                }
            }.addOnFailureListener {
                gpsDesactivado = true
                ubicacion = "Activa el GPS para detectar tu ubicación"
            }
        } catch (e: SecurityException) {
            permisoDenegadoPerm = true
            ubicacion = "Permiso de ubicación denegado"
        }
    }

    // ── etiquetaTipo derivada ──────────────────────────────────────────────
    val etiquetaTipo by remember(tipoSeleccionadoId, tipoPersonalizado) {
        derivedStateOf {
            if (tipoSeleccionadoId == "otro") tipoPersonalizado.trim()
            else tipos.find { it.id == tipoSeleccionadoId }?.titulo ?: ""
        }
    }

    // ── Abrir galería/cámara con permiso ──────────────────────────────────
    fun abrirGaleriaConPermiso() {
        val permiso = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            android.Manifest.permission.READ_MEDIA_IMAGES
        else android.Manifest.permission.READ_EXTERNAL_STORAGE
        if (ContextCompat.checkSelfPermission(context, permiso) == PackageManager.PERMISSION_GRANTED)
            galeriaLauncher.launch("image/*")
        else permisoAlmacenamientoLauncher.launch(permiso)
    }

    fun abrirCamaraConPermiso() {
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED) {
            val archivo = mediaHelper.createImageFile()
            val uri     = mediaHelper.getUriForFile(archivo)
            fotoUri = uri
            camaraLauncher.launch(uri)
        } else {
            permisoCamaraLauncher.launch(android.Manifest.permission.CAMERA)
        }
    }

    // ── Guardar en Room ────────────────────────────────────────────────────
    fun guardarReporte() {
        scope.launch {
            cargando = true
            try {
                val usuarioId = prefManager.getSesionUsuarioId()

                val ubicacionFinal = if (ubicacionEditada) ubicacion else ubicacionGps.ifBlank { ubicacion }

                val incidencia = IncidenciaEntity(
                    id          = UUID.randomUUID().toString(),
                    tipo        = etiquetaTipo,
                    descripcion = comentario.trim(),
                    ubicacion   = ubicacionFinal,
                    latitud     = geoPoint?.latitude,
                    longitud    = geoPoint?.longitude,
                    evidencias  = urisSeleccionadas.joinToString(",") { it.toString() },
                    fecha       = System.currentTimeMillis(),
                    estado      = "PENDIENTE",
                    usuarioId   = usuarioId
                )
                db.incidenciaDao().insertar(incidencia)
                Toast.makeText(context, strAlertaEnviada, Toast.LENGTH_SHORT).show()
                cargando = false
                onNavigateToMisReportes()
            } catch (e: Exception) {
                cargando = false
                Toast.makeText(context, strErrorGuardar.format(e.message), Toast.LENGTH_LONG).show()
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // UI
    // ─────────────────────────────────────────────────────────────────────
    Scaffold(
        topBar = { TopHeader(onLogout = onLogout, onNavigateToSettings = onNavigateToSettings) },
        bottomBar = {
            BottomNavigationBar(
                currentScreen    = "reports",
                onHomeClick      = onNavigateToMisReportes,
                onReportsClick   = {},
                onDirectoryClick = onNavigateToDirectory
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            Text(stringResource(R.string.reportar_incidencia),
                fontSize = 28.sp, fontWeight = FontWeight.Bold, color = primaryColor)
            Text(stringResource(R.string.reportar_subtitulo),
                fontSize = 16.sp, color = onSurfaceVariantColor, lineHeight = 22.sp,
                modifier = Modifier.padding(vertical = 8.dp))

            Spacer(modifier = Modifier.height(24.dp))

            // ── 1. Tipo de Incidencia ──────────────────────────────────────
            SeccionTitulo(stringResource(R.string.tipo_incidencia))
            Spacer(modifier = Modifier.height(12.dp))

            tipos.chunked(2).forEach { fila ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    fila.forEachIndexed { idx, tipo ->
                        ReportCard(
                            title      = tipo.titulo,
                            icon       = tipo.icono,
                            iconBg     = tipo.iconoBg,
                            iconColor  = tipo.iconoColor,
                            modifier   = Modifier.weight(1f),
                            isSelected = tipoSeleccionadoId == tipo.id,
                            onClick    = { tipoSeleccionadoId = tipo.id }
                        )
                        if (idx < fila.size - 1) Spacer(modifier = Modifier.width(16.dp))
                    }
                    if (fila.size == 1) Spacer(modifier = Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            AnimatedVisibility(visible = tipoSeleccionadoId == "otro") {
                OutlinedTextField(
                    value         = tipoPersonalizado,
                    onValueChange = { tipoPersonalizado = it },
                    label         = { Text(stringResource(R.string.describe_tipo)) },
                    placeholder   = { Text(stringResource(R.string.describe_tipo_placeholder)) },
                    modifier      = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    shape         = RoundedCornerShape(12.dp),
                    singleLine    = true,
                    leadingIcon   = { Icon(Icons.Default.Edit, contentDescription = null, tint = primaryColor) }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ── 2. Ubicación ───────────────────────────────────────────────
            SeccionTitulo(stringResource(R.string.ubicacion_actual))
            Spacer(modifier = Modifier.height(12.dp))

            // Banner GPS desactivado
            AnimatedVisibility(visible = gpsDesactivado && !permisoDenegadoPerm) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    shape    = RoundedCornerShape(12.dp),
                    colors   = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.GpsOff, contentDescription = null,
                                tint = Color(0xFFE65100), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("GPS desactivado", fontWeight = FontWeight.Bold,
                                fontSize = 14.sp, color = Color(0xFFE65100))
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Activa el GPS en tu dispositivo para detectar tu ubicación automáticamente, o ingresa la dirección manualmente.",
                            fontSize = 13.sp, color = Color(0xFF5D4037), lineHeight = 18.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = {
                                    context.startActivity(android.content.Intent(
                                        android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                                },
                                modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE65100))
                            ) {
                                Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Activar GPS", fontSize = 13.sp)
                            }
                            Button(
                                onClick = { ubicacionTemporal = ""; editandoUbicacion = true },
                                modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100))
                            ) {
                                Icon(Icons.Default.EditLocation, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Ingresar", fontSize = 13.sp)
                            }
                        }
                    }
                }
            }

            // Banner permiso denegado
            AnimatedVisibility(visible = permisoDenegadoPerm) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    shape    = RoundedCornerShape(12.dp),
                    colors   = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOff, contentDescription = null,
                                tint = DeepRed, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Permiso de ubicación denegado", fontWeight = FontWeight.Bold,
                                fontSize = 14.sp, color = DeepRed)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Para detectar tu ubicación automáticamente, concede el permiso en Ajustes. También puedes ingresar la dirección manualmente.",
                            fontSize = 13.sp, color = Color(0xFF5D4037), lineHeight = 18.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = {
                                    context.startActivity(android.content.Intent(
                                        android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                        android.net.Uri.fromParts("package", context.packageName, null)))
                                },
                                modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = DeepRed)
                            ) {
                                Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Ajustes", fontSize = 13.sp)
                            }
                            Button(
                                onClick = { ubicacionTemporal = ""; editandoUbicacion = true },
                                modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = DeepRed)
                            ) {
                                Icon(Icons.Default.EditLocation, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Ingresar", fontSize = 13.sp)
                            }
                        }
                    }
                }
            }

            // ── Mapa OSMDroid ──────────────────────────────────────────────
            Box(
                modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(16.dp))
            ) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory  = { ctx ->
                        Configuration.getInstance().load(
                            ctx, android.preference.PreferenceManager.getDefaultSharedPreferences(ctx))
                        MapView(ctx).apply {
                            setTileSource(TileSourceFactory.MAPNIK)
                            setMultiTouchControls(false)
                            isClickable = false
                            controller.setZoom(17.5)
                            mapView = this
                        }
                    },
                    update = { map ->
                        mapView = map
                        geoPoint?.let { punto ->
                            map.overlays.clear()
                            val marcador = org.osmdroid.views.overlay.Marker(map).apply {
                                position = punto
                                setAnchor(org.osmdroid.views.overlay.Marker.ANCHOR_CENTER,
                                    org.osmdroid.views.overlay.Marker.ANCHOR_BOTTOM)
                                title = "Tu ubicación"
                            }
                            map.overlays.add(marcador)
                            map.controller.setZoom(17.5)
                            map.controller.animateTo(punto)
                            map.invalidate()
                        }
                    }
                )

                // Punto rojo pulsante
                val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                val escala by infiniteTransition.animateFloat(
                    initialValue = 1f, targetValue = 1.8f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(900, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse), label = "escala")
                val alpha by infiniteTransition.animateFloat(
                    initialValue = 0.5f, targetValue = 0f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(900), repeatMode = RepeatMode.Reverse), label = "alpha")

                Box(modifier = Modifier.align(Alignment.Center), contentAlignment = Alignment.Center) {
                    Box(modifier = Modifier.size((28 * escala).dp).clip(CircleShape).background(DeepRed.copy(alpha = alpha)))
                    Box(modifier = Modifier.size(14.dp).clip(CircleShape).background(DeepRed).border(2.dp, Color.White, CircleShape))
                }

                Text(stringResource(R.string.en_tiempo_real),
                    modifier = Modifier.align(Alignment.TopEnd).padding(12.dp),
                    fontSize = 10.sp, fontWeight = FontWeight.Bold, color = primaryColor)

                Surface(
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 10.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                    shadowElevation = 4.dp
                ) {
                    Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.MyLocation, contentDescription = null,
                            modifier = Modifier.size(14.dp), tint = onSurfaceVariantColor)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = ubicacion, fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface, maxLines = 1)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── Campo de ubicación editable ────────────────────────────────
            OutlinedTextField(
                value         = ubicacion,
                onValueChange = { nuevaUbicacion ->
                    ubicacion        = nuevaUbicacion
                    ubicacionEditada = nuevaUbicacion != ubicacionGps
                },
                label       = { Text("Ubicación") },
                modifier    = Modifier.fillMaxWidth(),
                shape       = RoundedCornerShape(12.dp),
                singleLine  = true,
                leadingIcon = {
                    Icon(Icons.Default.EditLocation, contentDescription = null, tint = primaryColor)
                },
                trailingIcon = {
                    if (ubicacionEditada) {
                        Icon(Icons.Default.Edit, contentDescription = "Editada manualmente",
                            tint = primaryColor)
                    } else {
                        Icon(Icons.Default.GpsFixed, contentDescription = "Ubicación GPS",
                            tint = Color(0xFF2E7D32))
                    }
                },
                supportingText = {
                    Text(
                        if (ubicacionEditada) "Ubicación corregida manualmente"
                        else "Ubicación detectada por GPS",
                        fontSize = 11.sp,
                        color = if (ubicacionEditada) primaryColor else Color(0xFF2E7D32)
                    )
                }
            )

            // ── BOTÓN ACTUALIZAR MAPA — solo aparece si el usuario editó ──
            AnimatedVisibility(visible = ubicacionEditada) {
                Button(
                    onClick  = { buscarDireccionEnMapa(ubicacion) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    shape  = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                ) {
                    if (buscandoDireccion) {
                        CircularProgressIndicator(
                            modifier    = Modifier.size(18.dp),
                            color       = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Buscando...", fontWeight = FontWeight.Bold)
                    } else {
                        Icon(Icons.Default.Map, contentDescription = null,
                            modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Actualizar mapa", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ── 3. Adjuntar Evidencia ──────────────────────────────────────
            SeccionTitulo(stringResource(R.string.adjuntar_evidencia))
            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                BotonEvidencia(
                    icono    = Icons.Default.Upload,
                    etiqueta = stringResource(R.string.subir_foto_video),
                    modifier = Modifier.weight(1f),
                    onClick  = { abrirGaleriaConPermiso() }
                )
                Spacer(modifier = Modifier.width(16.dp))
                BotonEvidencia(
                    icono    = Icons.Default.CameraAlt,
                    etiqueta = stringResource(R.string.tomar_foto),
                    modifier = Modifier.weight(1f),
                    onClick  = { abrirCamaraConPermiso() }
                )
            }

            AnimatedVisibility(visible = urisSeleccionadas.isNotEmpty()) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null,
                            tint = Color(0xFF2E7D32), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(stringResource(R.string.archivos_adjuntos, urisSeleccionadas.size),
                            fontSize = 14.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.weight(1f))
                        TextButton(onClick = { urisSeleccionadas = emptyList() }) {
                            Text(stringResource(R.string.quitar_todo), color = DeepRed, fontSize = 13.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    urisSeleccionadas.chunked(2).forEach { fila ->
                        Row(modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            fila.forEach { uri ->
                                Box(modifier = Modifier.weight(1f).height(120.dp).clip(RoundedCornerShape(10.dp))) {
                                    Image(painter = rememberAsyncImagePainter(uri),
                                        contentDescription = "Vista previa",
                                        modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                                    IconButton(
                                        onClick  = { urisSeleccionadas = urisSeleccionadas - uri },
                                        modifier = Modifier.align(Alignment.TopEnd).size(28.dp)
                                            .background(Color.Black.copy(alpha = 0.55f), RoundedCornerShape(50))
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = "Quitar imagen",
                                            tint = Color.White, modifier = Modifier.size(14.dp))
                                    }
                                }
                            }
                            if (fila.size == 1) Spacer(modifier = Modifier.weight(1f))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ── 4. Comentario ──────────────────────────────────────────────
            SeccionTitulo(stringResource(R.string.comentario_adicional))
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value         = comentario,
                onValueChange = { comentario = it },
                placeholder   = { Text(stringResource(R.string.comentario_placeholder)) },
                modifier      = Modifier.fillMaxWidth().height(120.dp),
                shape         = RoundedCornerShape(12.dp),
                maxLines      = 5
            )

            Spacer(modifier = Modifier.height(32.dp))

            // ── 5. Botón Enviar ────────────────────────────────────────────
            val puedeEnviar = tipoSeleccionadoId != null &&
                    (tipoSeleccionadoId != "otro" || tipoPersonalizado.isNotBlank()) &&
                    ubicacion.isNotBlank() &&
                    !cargando

            Button(
                onClick  = { mostrarConfirmacion = true },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape    = RoundedCornerShape(16.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = DeepRed),
                enabled  = puedeEnviar
            ) {
                if (cargando) {
                    CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.Campaign, contentDescription = null)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(stringResource(R.string.enviar_alerta), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }

            Text(stringResource(R.string.enviar_alerta_aviso),
                fontSize = 12.sp, color = onSurfaceVariantColor, lineHeight = 18.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp, bottom = 24.dp))
        }
    }

    // ── Diálogo: Confirmar envío ───────────────────────────────────────────
    if (mostrarConfirmacion) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmacion = false },
            icon  = { Icon(Icons.Default.Campaign, tint = DeepRed, contentDescription = null) },
            title = { Text(stringResource(R.string.enviar_alerta_pregunta), fontWeight = FontWeight.Bold) },
            text  = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilaConfirmacion(stringResource(R.string.tipo_label), etiquetaTipo)
                    FilaConfirmacion(stringResource(R.string.ubicacion_label), ubicacion)
                    Text(
                        if (ubicacionEditada) "  ✎ Ubicación corregida manualmente"
                        else "  ⊕ Ubicación detectada por GPS",
                        fontSize = 11.sp,
                        color = if (ubicacionEditada) primaryColor else Color(0xFF2E7D32)
                    )
                    FilaConfirmacion(stringResource(R.string.archivos_label),
                        stringResource(R.string.archivos_adjuntos, urisSeleccionadas.size))
                    if (comentario.isNotBlank())
                        FilaConfirmacion(stringResource(R.string.comentario_label), comentario)
                }
            },
            confirmButton = {
                Button(
                    onClick = { mostrarConfirmacion = false; guardarReporte() },
                    colors  = ButtonDefaults.buttonColors(containerColor = DeepRed)
                ) { Text(stringResource(R.string.confirmar_enviar)) }
            },
            dismissButton = {
                TextButton(onClick = { mostrarConfirmacion = false }) {
                    Text(stringResource(R.string.cancelar))
                }
            }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Composables privados de apoyo
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SeccionTitulo(texto: String) {
    Text(texto, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
}

@Composable
private fun FilaConfirmacion(etiqueta: String, valor: String) {
    Row {
        Text("$etiqueta: ", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        Text(valor, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun BotonEvidencia(icono: ImageVector, etiqueta: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    OutlinedCard(onClick = onClick, modifier = modifier.height(100.dp), shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.fillMaxSize().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center) {
            Icon(icono, contentDescription = null, modifier = Modifier.size(32.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(6.dp))
            Text(etiqueta, fontSize = 13.sp, fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface, textAlign = TextAlign.Center, lineHeight = 16.sp)
        }
    }
}

// ── TopHeader ─────────────────────────────────────────────────────────────────
@Composable
fun TopHeader(onLogout: () -> Unit, onNavigateToSettings: () -> Unit) {
    val primaryColor          = MaterialTheme.colorScheme.primary
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant
    val surfaceColor          = MaterialTheme.colorScheme.surface
    val onSurfaceColor        = MaterialTheme.colorScheme.onSurface
    var mostrarDialogo by remember { mutableStateOf(false) }

    if (mostrarDialogo) {
        AlertDialog(
            onDismissRequest = { mostrarDialogo = false },
            title = { Text("Cerrar sesión", fontWeight = FontWeight.Bold, color = primaryColor) },
            text  = { Text("¿Deseas salir de tu cuenta?") },
            confirmButton = {
                Button(onClick = { mostrarDialogo = false; onLogout() },
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor)) { Text("Salir") }
            },
            dismissButton = {
                OutlinedButton(onClick = { mostrarDialogo = false }) { Text("Cancelar") }
            }
        )
    }

    Row(
        modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(painter = painterResource(id = R.drawable.logo_sjl),
                contentDescription = stringResource(R.string.sjl_alerta_header),
                modifier = Modifier.size(32.dp).clip(CircleShape), contentScale = ContentScale.Fit)
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.sjl_alerta_header), fontWeight = FontWeight.Bold,
                fontSize = 20.sp, color = primaryColor)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.NotificationsNone, contentDescription = null, tint = onSurfaceVariantColor)
            Spacer(modifier = Modifier.width(16.dp))
            IconButton(onClick = { mostrarDialogo = true }) {
                Icon(Icons.Default.Logout, contentDescription = stringResource(R.string.logout),
                    tint = onSurfaceVariantColor)
            }
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = onNavigateToSettings) {
                Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(onSurfaceColor),
                    contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.configuracion_btn),
                        tint = surfaceColor, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

// ── ReportCard ────────────────────────────────────────────────────────────────
@Composable
fun ReportCard(
    title: String, icon: ImageVector, iconBg: Color, iconColor: Color,
    modifier: Modifier = Modifier, isSelected: Boolean = false, onClick: () -> Unit = {}
) {
    val surfaceColor   = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val primaryColor   = MaterialTheme.colorScheme.primary

    Card(
        onClick   = onClick,
        modifier  = modifier.height(140.dp).then(
            if (isSelected) Modifier.border(2.dp, primaryColor, RoundedCornerShape(16.dp)) else Modifier),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = if (isSelected) SoftRed else surfaceColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 6.dp else 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.Center) {
            Box(modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(iconBg),
                contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = iconColor)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                color = if (isSelected) DeepRed else onSurfaceColor, lineHeight = 18.sp)
        }
    }
}

// ── BottomNavigationBar ───────────────────────────────────────────────────────
@Composable
fun BottomNavigationBar(
    currentScreen: String,
    onHomeClick: () -> Unit,
    onReportsClick: () -> Unit,
    onDirectoryClick: () -> Unit
) {
    Surface(modifier = Modifier.fillMaxWidth(), shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.surface) {
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically) {
            BottomNavItem(Icons.Default.Home, stringResource(R.string.inicio),
                currentScreen == "home", onHomeClick)
            BottomNavItem(Icons.Default.Assignment, stringResource(R.string.reportes),
                currentScreen == "reports", onReportsClick)
            BottomNavItem(Icons.Default.ContactPhone, stringResource(R.string.directorio),
                currentScreen == "directory", onDirectoryClick)
        }
    }
}

// ── BottomNavItem ─────────────────────────────────────────────────────────────
@Composable
fun BottomNavItem(icon: ImageVector, label: String, isSelected: Boolean, onClick: () -> Unit) {
    val primaryColor          = MaterialTheme.colorScheme.primary
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant
    Column(
        modifier = Modifier.clip(RoundedCornerShape(12.dp)).clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, contentDescription = label,
            tint = if (isSelected) primaryColor else onSurfaceVariantColor)
        Text(label, fontSize = 12.sp,
            color = if (isSelected) primaryColor else onSurfaceVariantColor,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
    }
}