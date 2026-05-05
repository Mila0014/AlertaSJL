package com.example.sjl_alert_v4.actividades

import android.location.Geocoder
import android.net.Uri
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
import androidx.core.content.FileProvider
import com.example.sjl_alert_v4.R
import com.example.sjl_alert_v4.modelos.AppDatabase
import com.example.sjl_alert_v4.modelos.IncidenciaEntity
import com.example.sjl_alert_v4.sharedPrefs.PreferenceManager
import com.example.sjl_alert_v4.ui.theme.*
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import java.io.File
import java.util.Locale
import java.util.UUID

private data class TipoIncidencia(
    val id: String,
    val titulo: String,
    val icono: ImageVector,
    val iconoBg: Color,
    val iconoColor: Color
)

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

    val primaryColor          = MaterialTheme.colorScheme.primary
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant

    // ── Strings capturados para usar en lambdas/coroutines ────────────────
    val strAlertaEnviada     = stringResource(R.string.alerta_enviada)
    val strErrorGuardar      = stringResource(R.string.error_guardar)
    val strObteniendoUbic    = stringResource(R.string.obteniendo_ubicacion)
    val strUbicacionActual   = "Ubicación actual"
    val strPermisoDenegado   = "Permiso de ubicación no concedido"

    // ── Tipos de incidencia (localizados) ─────────────────────────────────
    val tipos = listOf(
        TipoIncidencia("ruidos",    stringResource(R.string.ruidos_molestos), Icons.Default.VolumeUp,  SoftPurple, DeepPurple),
        TipoIncidencia("libadores", stringResource(R.string.libadores),       Icons.Default.LocalBar,  SoftYellow, DeepYellow),
        TipoIncidencia("robo",      stringResource(R.string.alerta_robo),     Icons.Default.Warning,   SoftRed,    DeepRed),
        TipoIncidencia("siniestro", stringResource(R.string.siniestro),       Icons.Default.Whatshot,  SoftRed,    DeepRed),
        TipoIncidencia("otro",      stringResource(R.string.otro),            Icons.Default.Edit,
            Color(0xFFE8F5E9), Color(0xFF2E7D32))
    )

    // ── Estado del formulario ──────────────────────────────────────────────
    var tipoSeleccionadoId  by remember { mutableStateOf<String?>(null) }
    var tipoPersonalizado   by remember { mutableStateOf("") }
    var ubicacion           by remember { mutableStateOf(strObteniendoUbic) }
    var editandoUbicacion   by remember { mutableStateOf(false) }
    var ubicacionTemporal   by remember { mutableStateOf("") }
    var urisSeleccionadas   by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var comentario          by remember { mutableStateOf("") }
    var cargando            by remember { mutableStateOf(false) }
    var mostrarConfirmacion by remember { mutableStateOf(false) }

    // ── Mapa OSMDroid ──────────────────────────────────────────────────────
    var geoPoint by remember { mutableStateOf<GeoPoint?>(null) }
    var mapView  by remember { mutableStateOf<MapView?>(null) }

    // ── Launchers ──────────────────────────────────────────────────────────
    val galeriaLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris -> urisSeleccionadas = urisSeleccionadas + uris }

    var fotoUri by remember { mutableStateOf<Uri?>(null) }
    val camaraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { exito -> if (exito) fotoUri?.let { uri -> urisSeleccionadas = urisSeleccionadas + uri } }

    val permisoCamaraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) {
            val archivo = File(context.cacheDir, "foto_${System.currentTimeMillis()}.jpg")
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", archivo)
            fotoUri = uri
            camaraLauncher.launch(uri)
        }
    }

    // ── Obtener ubicación GPS ──────────────────────────────────────────────
    LaunchedEffect(Unit) {
        try {
            val fusedClient = LocationServices.getFusedLocationProviderClient(context)
            fusedClient.lastLocation.addOnSuccessListener { loc ->
                if (loc != null) {
                    val punto = GeoPoint(loc.latitude, loc.longitude)
                    geoPoint = punto
                    mapView?.controller?.animateTo(punto)
                    try {
                        val geocoder = Geocoder(context, Locale("es", "PE"))
                        val dirs = geocoder.getFromLocation(loc.latitude, loc.longitude, 1)
                        if (!dirs.isNullOrEmpty()) {
                            ubicacion = dirs[0].getAddressLine(0) ?: strUbicacionActual
                        }
                    } catch (e: Exception) {
                        ubicacion = "Lat: %.5f, Lng: %.5f".format(loc.latitude, loc.longitude)
                    }
                }
            }
        } catch (e: SecurityException) {
            ubicacion = strPermisoDenegado
        }
    }

    val etiquetaTipo by remember(tipoSeleccionadoId, tipoPersonalizado) {
        derivedStateOf {
            if (tipoSeleccionadoId == "otro") tipoPersonalizado.trim()
            else tipos.find { it.id == tipoSeleccionadoId }?.titulo ?: ""
        }
    }

    // ── Guardar en Room ────────────────────────────────────────────────────
    fun guardarReporte() {
        scope.launch {
            cargando = true
            try {
                val usuarioId = prefManager.getSesionUsuarioId()
                val incidencia = IncidenciaEntity(
                    id          = UUID.randomUUID().toString(),
                    tipo        = etiquetaTipo,
                    descripcion = comentario.trim(),
                    ubicacion   = ubicacion,
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

    // ── UI ─────────────────────────────────────────────────────────────────
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

            // ── Título ─────────────────────────────────────────────────────
            Text(
                text = stringResource(R.string.reportar_incidencia),
                fontSize = 28.sp, fontWeight = FontWeight.Bold, color = primaryColor
            )
            Text(
                text = stringResource(R.string.reportar_subtitulo),
                fontSize = 16.sp, color = onSurfaceVariantColor, lineHeight = 22.sp,
                modifier = Modifier.padding(vertical = 8.dp)
            )

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
                    leadingIcon   = {
                        Icon(Icons.Default.Edit, contentDescription = null, tint = primaryColor)
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ── 2. Ubicación ───────────────────────────────────────────────
            SeccionTitulo(stringResource(R.string.ubicacion_actual))
            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp))
            ) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory  = { ctx ->
                        Configuration.getInstance().load(
                            ctx,
                            android.preference.PreferenceManager.getDefaultSharedPreferences(ctx)
                        )
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
                            map.controller.setZoom(17.5)
                            map.controller.setCenter(punto)
                            map.invalidate()
                        }
                    }
                )

                // Punto rojo pulsante animado
                val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                val escala by infiniteTransition.animateFloat(
                    initialValue  = 1f, targetValue = 1.8f,
                    animationSpec = infiniteRepeatable(
                        animation  = tween(900, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ), label = "escala"
                )
                val alpha by infiniteTransition.animateFloat(
                    initialValue  = 0.5f, targetValue = 0f,
                    animationSpec = infiniteRepeatable(
                        animation  = tween(900),
                        repeatMode = RepeatMode.Reverse
                    ), label = "alpha"
                )

                Box(modifier = Modifier.align(Alignment.Center), contentAlignment = Alignment.Center) {
                    Box(modifier = Modifier.size((28 * escala).dp).clip(CircleShape).background(DeepRed.copy(alpha = alpha)))
                    Box(modifier = Modifier.size(14.dp).clip(CircleShape).background(DeepRed).border(2.dp, Color.White, CircleShape))
                }

                Text(
                    text = stringResource(R.string.en_tiempo_real),
                    modifier   = Modifier.align(Alignment.TopEnd).padding(12.dp),
                    fontSize   = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color      = primaryColor
                )

                Surface(
                    modifier        = Modifier.align(Alignment.BottomCenter).padding(bottom = 10.dp),
                    shape           = RoundedCornerShape(20.dp),
                    color           = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                    shadowElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.MyLocation, contentDescription = null,
                            modifier = Modifier.size(14.dp), tint = onSurfaceVariantColor)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = ubicacion, fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface, maxLines = 1)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick  = { ubicacionTemporal = ubicacion; editandoUbicacion = true },
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(12.dp),
                colors   = ButtonDefaults.outlinedButtonColors(contentColor = primaryColor)
            ) {
                Icon(Icons.Default.EditLocation, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.modificar_ubicacion))
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
                    onClick  = { galeriaLauncher.launch("image/*") }
                )
                Spacer(modifier = Modifier.width(16.dp))
                BotonEvidencia(
                    icono    = Icons.Default.CameraAlt,
                    etiqueta = stringResource(R.string.tomar_foto),
                    modifier = Modifier.weight(1f),
                    onClick  = {
                        permisoCamaraLauncher.launch(android.Manifest.permission.CAMERA)
                    }
                )
            }

            AnimatedVisibility(visible = urisSeleccionadas.isNotEmpty()) {
                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null,
                        tint = Color(0xFF2E7D32), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.archivos_adjuntos, urisSeleccionadas.size),
                        fontSize = 14.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    TextButton(onClick = { urisSeleccionadas = emptyList() }) {
                        Text(stringResource(R.string.quitar_todo), color = DeepRed, fontSize = 13.sp)
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

            // ── 5. Botón Enviar Alerta ─────────────────────────────────────
            val puedeEnviar = tipoSeleccionadoId != null &&
                    (tipoSeleccionadoId != "otro" || tipoPersonalizado.isNotBlank()) &&
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

            Text(
                text      = stringResource(R.string.enviar_alerta_aviso),
                fontSize  = 12.sp,
                color     = onSurfaceVariantColor,
                lineHeight = 18.sp,
                textAlign = TextAlign.Center,
                modifier  = Modifier.fillMaxWidth().padding(top = 10.dp, bottom = 24.dp)
            )
        }
    }

    // ── Diálogo: Modificar ubicación ───────────────────────────────────────
    if (editandoUbicacion) {
        AlertDialog(
            onDismissRequest = { editandoUbicacion = false },
            title = { Text(stringResource(R.string.modificar_ubicacion_titulo), fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value         = ubicacionTemporal,
                    onValueChange = { ubicacionTemporal = it },
                    label         = { Text(stringResource(R.string.direccion)) },
                    modifier      = Modifier.fillMaxWidth(),
                    shape         = RoundedCornerShape(12.dp),
                    leadingIcon   = {
                        Icon(Icons.Default.EditLocation, contentDescription = null, tint = primaryColor)
                    }
                )
            },
            confirmButton = {
                Button(
                    onClick = { ubicacion = ubicacionTemporal; editandoUbicacion = false },
                    colors  = ButtonDefaults.buttonColors(containerColor = primaryColor)
                ) { Text(stringResource(R.string.confirmar)) }
            },
            dismissButton = {
                TextButton(onClick = { editandoUbicacion = false }) {
                    Text(stringResource(R.string.cancelar))
                }
            }
        )
    }

    // ── Diálogo: Confirmar envío ───────────────────────────────────────────
    if (mostrarConfirmacion) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmacion = false },
            icon  = { Icon(Icons.Default.Campaign, tint = DeepRed, contentDescription = null) },
            title = { Text(stringResource(R.string.enviar_alerta_pregunta), fontWeight = FontWeight.Bold) },
            text  = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilaConfirmacion(stringResource(R.string.tipo_label),      etiquetaTipo)
                    FilaConfirmacion(stringResource(R.string.ubicacion_label), ubicacion)
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

// ── Composables privados ──────────────────────────────────────────────────────

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
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
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
            title = {
                Text(text = "Cerrar sesión", fontWeight = FontWeight.Bold, color = primaryColor)
            },
            text = {
                Text("¿Deseas salir de tu cuenta?")
            },
            confirmButton = {
                Button(
                    onClick = { mostrarDialogo = false; onLogout() },
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                ) { Text("Salir") }
            },
            dismissButton = {
                OutlinedButton(onClick = { mostrarDialogo = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter            = painterResource(id = R.drawable.logo_sjl),
                contentDescription = stringResource(R.string.sjl_alerta_header),
                modifier           = Modifier.size(32.dp).clip(CircleShape),
                contentScale       = ContentScale.Fit
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.sjl_alerta_header), fontWeight = FontWeight.Bold, fontSize = 20.sp, color = primaryColor)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.NotificationsNone, contentDescription = null, tint = onSurfaceVariantColor)
            Spacer(modifier = Modifier.width(16.dp))
            IconButton(onClick = { mostrarDialogo = true }) {
                Icon(Icons.Default.Logout, contentDescription = stringResource(R.string.logout), tint = onSurfaceVariantColor)
            }
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = onNavigateToSettings) {
                Box(
                    modifier         = Modifier.size(36.dp).clip(CircleShape).background(onSurfaceColor),
                    contentAlignment = Alignment.Center
                ) {
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
            if (isSelected) Modifier.border(2.dp, primaryColor, RoundedCornerShape(16.dp)) else Modifier
        ),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = if (isSelected) SoftRed else surfaceColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 6.dp else 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.Center) {
            Box(
                modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconColor)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
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
    Surface(modifier = Modifier.fillMaxWidth(), shadowElevation = 8.dp, color = MaterialTheme.colorScheme.surface) {
        Row(
            modifier              = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            BottomNavItem(Icons.Default.Home,         stringResource(R.string.inicio),     currentScreen == "home",      onHomeClick)
            BottomNavItem(Icons.Default.Assignment,   stringResource(R.string.reportes),   currentScreen == "reports",   onReportsClick)
            BottomNavItem(Icons.Default.ContactPhone, stringResource(R.string.directorio), currentScreen == "directory", onDirectoryClick)
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
        Icon(icon, contentDescription = label, tint = if (isSelected) primaryColor else onSurfaceVariantColor)
        Text(text = label, fontSize = 12.sp,
            color = if (isSelected) primaryColor else onSurfaceVariantColor,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
    }
}