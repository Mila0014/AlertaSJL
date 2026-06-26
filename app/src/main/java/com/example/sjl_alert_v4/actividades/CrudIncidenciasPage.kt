package com.example.sjl_alert_v4.actividades

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sjl_alert_v4.modelos.*
import com.example.sjl_alert_v4.sharedPrefs.PreferenceManager
import com.example.sjl_alert_v4.ui.theme.*
import com.example.sjl_alert_v4.utilidades.SuccessToast
import com.example.sjl_alert_v4.utilidades.ToastData
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrudIncidenciasPage(onBack: () -> Unit) {
    val context     = LocalContext.current
    val scope       = rememberCoroutineScope()
    val prefManager = remember { PreferenceManager(context) }
    val db          = remember { AppDatabase.getInstance(context) }
    val repo        = remember { IncidenciaRepository(db.incidenciaDao()) }
    val usuarioId   = prefManager.getSesionUsuarioId()

    val primaryColor = MaterialTheme.colorScheme.primary

    val incidencias by db.incidenciaDao()
        .obtenerPorUsuario(usuarioId)
        .collectAsState(initial = emptyList())

    var cargando            by remember { mutableStateOf(false) }
    var toastCrud           by remember { mutableStateOf<ToastData?>(null) }
    var incidenciaAEditar   by remember { mutableStateOf<IncidenciaEntity?>(null) }
    var incidenciaAEliminar by remember { mutableStateOf<IncidenciaEntity?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        cargando = true
        val resultado = repo.sincronizarConAzure(usuarioId)
        cargando = false
        if (resultado is ResultadoApi.Error) {
            snackbarHostState.showSnackbar(resultado.mensaje)
        }
    }

    fun mostrarMensaje(msg: String) {
        scope.launch { snackbarHostState.showSnackbar(msg) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Mis Incidencias", fontWeight = FontWeight.Bold, color = primaryColor)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = primaryColor)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        scope.launch {
                            cargando = true
                            val r = repo.sincronizarConAzure(usuarioId)
                            cargando = false
                            mostrarMensaje(
                                if (r is ResultadoApi.Exito) "✅ Sincronizado con Supabase"
                                else (r as ResultadoApi.Error).mensaje
                            )
                        }
                    }) {
                        Icon(Icons.Default.Sync, "Sincronizar", tint = primaryColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background)
            )
        },
        snackbarHost    = { SnackbarHost(snackbarHostState) },
        containerColor  = MaterialTheme.colorScheme.background
    ) { padding ->

        Box(modifier = Modifier.fillMaxSize().padding(padding)) {

            if (cargando) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (incidencias.isEmpty()) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.AutoMirrored.Filled.Assignment, null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("No tienes incidencias aún", fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(incidencias, key = { it.id }) { incidencia ->
                        TarjetaIncidenciaCrud(
                            incidencia = incidencia,
                            onEditar   = { incidenciaAEditar = incidencia }
                        ) { incidenciaAEliminar = incidencia }
                    }
                }
            }

            // Toast anclado arriba
            Box(modifier = Modifier.align(Alignment.TopCenter)) {
                SuccessToast(
                    toastData = toastCrud,
                    onDismiss = { toastCrud = null }
                )
            }
        }
    }

    // Diálogo: Editar
    incidenciaAEditar?.let { inc ->
        DialogoEditar(
            incidencia = inc,
            onDismiss  = { incidenciaAEditar = null }
        ) { incidenciaEditada ->
            scope.launch {
                cargando = true
                val r = repo.actualizar(incidenciaEditada)
                cargando = false
                incidenciaAEditar = null
                if (r is ResultadoApi.Exito) {
                    toastCrud = ToastData(
                        icon     = Icons.Default.EditNote,
                        iconBg   = Color(0xFFE3F2FD),
                        iconTint = Color(0xFF1565C0),
                        title    = "Cambios guardados",
                        message  = "La incidencia fue actualizada correctamente."
                    )
                } else {
                    mostrarMensaje((r as ResultadoApi.Error).mensaje)
                }
            }
        }
    }

    // Diálogo: Confirmar eliminación
    incidenciaAEliminar?.let { inc ->
        AlertDialog(
            onDismissRequest = { incidenciaAEliminar = null },
            icon  = { Icon(Icons.Default.DeleteForever, tint = DeepRed, contentDescription = null) },
            title = { Text("¿Eliminar incidencia?", fontWeight = FontWeight.Bold) },
            text  = { Text("Se eliminará '${inc.tipo}' tanto del dispositivo como de Azure SQL.") },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { incidenciaAEliminar = null },
                        modifier = Modifier.weight(1f).fillMaxHeight().defaultMinSize(minHeight = 48.dp),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "Cancelar",
                            color = primaryColor,
                            textAlign = TextAlign.Center
                        )
                    }
                    Button(
                        onClick = {
                            scope.launch {
                                cargando = true
                                val r = repo.eliminar(inc)
                                cargando = false
                                incidenciaAEliminar = null
                                if (r is ResultadoApi.Exito) {
                                    toastCrud = ToastData(
                                        icon     = Icons.Default.CheckCircle,
                                        iconBg   = Color(0xFFE8F5E9),
                                        iconTint = Color(0xFF2E7D32),
                                        title    = "Eliminado",
                                        message  = "La incidencia fue borrada correctamente."
                                    )
                                } else {
                                    mostrarMensaje((r as ResultadoApi.Error).mensaje)
                                }
                            }
                        },
                        modifier = Modifier.weight(1f).fillMaxHeight().defaultMinSize(minHeight = 48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DeepRed, contentColor = Color.White),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "Eliminar",
                            textAlign = TextAlign.Center
                        )
                    }
                }
            },
            dismissButton = null
        )
    }
}

@Composable
private fun TarjetaIncidenciaCrud(
    incidencia: IncidenciaEntity,
    onEditar: () -> Unit,
    onEliminar: () -> Unit
) {
    val fechaFormateada = remember(incidencia.fecha) {
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(incidencia.fecha))
    }
    val colorEstado = when (incidencia.estado) {
        "PENDIENTE"  -> Color(0xFFFFA000)
        "EN_PROCESO" -> Color(0xFF1976D2)
        "RESUELTO"   -> Color(0xFF2E7D32)
        "RECHAZADO"  -> DeepRed
        else         -> Color(0xFF757575)
    }

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(incidencia.tipo, fontWeight = FontWeight.Bold, fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
                Surface(shape = RoundedCornerShape(20.dp), color = colorEstado.copy(alpha = 0.15f)) {
                    Text(incidencia.estado, fontSize = 11.sp, fontWeight = FontWeight.Bold,
                        color = colorEstado,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, null,
                    modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.width(4.dp))
                Text(incidencia.ubicacion.ifBlank { "Sin ubicación" },
                    fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            }
            if (incidencia.descripcion.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(incidencia.descripcion, fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(fechaFormateada, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick          = onEditar,
                        modifier         = Modifier.height(34.dp),
                        shape            = RoundedCornerShape(8.dp),
                        contentPadding   = PaddingValues(horizontal = 12.dp),
                        colors           = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Edit, null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Editar", fontSize = 12.sp)
                    }
                    Button(
                        onClick        = onEliminar,
                        modifier       = Modifier.height(34.dp),
                        shape          = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp),
                        colors         = ButtonDefaults.buttonColors(containerColor = DeepRed, contentColor = Color.White)
                    ) {
                        Icon(Icons.Default.Delete, null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Eliminar", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DialogoEditar(
    incidencia: IncidenciaEntity,
    onDismiss: () -> Unit,
    onGuardar: (IncidenciaEntity) -> Unit
) {
    var tipo        by remember { mutableStateOf(incidencia.tipo) }
    var descripcion by remember { mutableStateOf(incidencia.descripcion) }
    var ubicacion   by remember { mutableStateOf(incidencia.ubicacion) }
    var estado      by remember { mutableStateOf(incidencia.estado) }

    val estados          = listOf("PENDIENTE", "EN_PROCESO", "RESUELTO", "RECHAZADO")
    var expandidoEstado  by remember { mutableStateOf(false) }
    val primaryColor     = MaterialTheme.colorScheme.primary

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Incidencia", fontWeight = FontWeight.Bold) },
        text  = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value         = tipo,
                    onValueChange = { tipo = it },
                    label         = { Text("Tipo") },
                    modifier      = Modifier.fillMaxWidth(),
                    shape         = RoundedCornerShape(10.dp),
                    singleLine    = true
                )
                OutlinedTextField(
                    value         = descripcion,
                    onValueChange = { descripcion = it },
                    label         = { Text("Descripción") },
                    modifier      = Modifier.fillMaxWidth().height(90.dp),
                    shape         = RoundedCornerShape(10.dp),
                    maxLines      = 3
                )
                OutlinedTextField(
                    value         = ubicacion,
                    onValueChange = { ubicacion = it },
                    label         = { Text("Ubicación") },
                    modifier      = Modifier.fillMaxWidth(),
                    shape         = RoundedCornerShape(10.dp),
                    singleLine    = true,
                    leadingIcon   = {
                        Icon(Icons.Default.LocationOn, null, tint = primaryColor)
                    }
                )
                ExposedDropdownMenuBox(
                    expanded          = expandidoEstado,
                    onExpandedChange  = { expandidoEstado = it }
                ) {
                    OutlinedTextField(
                        value         = estado,
                        onValueChange = {},
                        readOnly      = true,
                        label         = { Text("Estado") },
                        modifier      = Modifier.fillMaxWidth().menuAnchor(),
                        shape         = RoundedCornerShape(10.dp),
                        trailingIcon  = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandidoEstado)
                        }
                    )
                    ExposedDropdownMenu(
                        expanded          = expandidoEstado,
                        onDismissRequest  = { expandidoEstado = false }
                    ) {
                        estados.forEach { opcion ->
                            DropdownMenuItem(
                                text    = { Text(opcion) },
                                onClick = { estado = opcion; expandidoEstado = false }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick  = onDismiss,
                    modifier = Modifier.weight(1f).fillMaxHeight().defaultMinSize(minHeight = 48.dp),
                    shape    = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Cancelar",
                        color = primaryColor,
                        textAlign = TextAlign.Center
                    )
                }
                Button(
                    onClick  = {
                        onGuardar(incidencia.copy(
                            tipo        = tipo.trim(),
                            descripcion = descripcion.trim(),
                            ubicacion   = ubicacion.trim(),
                            estado      = estado
                        ))
                    },
                    modifier = Modifier.weight(1f).fillMaxHeight().defaultMinSize(minHeight = 48.dp),
                    enabled  = tipo.isNotBlank(),
                    shape    = RoundedCornerShape(12.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = primaryColor),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Guardar",
                        textAlign = TextAlign.Center
                    )
                }
            }
        },
        dismissButton = null
    )
}