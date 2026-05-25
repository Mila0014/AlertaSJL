package com.example.sjl_alert_v4.actividades

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sjl_alert_v4.modelos.*
import com.example.sjl_alert_v4.sharedPrefs.PreferenceManager
import com.example.sjl_alert_v4.ui.theme.*
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

    // ── Estado ────────────────────────────────────────────────────────────
    val incidencias by db.incidenciaDao()
        .obtenerPorUsuario(usuarioId)
        .collectAsState(initial = emptyList())

    var cargando         by remember { mutableStateOf(false) }
    var mensajeSnackbar  by remember { mutableStateOf("") }
    var mostrarSnackbar  by remember { mutableStateOf(false) }

    // Diálogos
    var incidenciaAEditar   by remember { mutableStateOf<IncidenciaEntity?>(null) }
    var incidenciaAEliminar by remember { mutableStateOf<IncidenciaEntity?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }

    // ── Sincronizar con Azure al abrir ────────────────────────────────────
    LaunchedEffect(Unit) {
        cargando = true
        val resultado = repo.sincronizarDesdeAzure(usuarioId)
        cargando = false
        if (resultado is ResultadoApi.Error) {
            snackbarHostState.showSnackbar(resultado.mensaje)
        }
    }

    fun mostrarMensaje(msg: String) {
        scope.launch { snackbarHostState.showSnackbar(msg) }
    }

    // ── UI ─────────────────────────────────────────────────────────────────
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Mis Incidencias", fontWeight = FontWeight.Bold, color = primaryColor)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver",
                            tint = primaryColor)
                    }
                },
                actions = {
                    // Botón sincronizar manualmente
                    IconButton(onClick = {
                        scope.launch {
                            cargando = true
                            val r = repo.sincronizarDesdeAzure(usuarioId)
                            cargando = false
                            mostrarMensaje(
                                if (r is ResultadoApi.Exito) "✅ Sincronizado con Azure"
                                else (r as ResultadoApi.Error).mensaje
                            )
                        }
                    }) {
                        Icon(Icons.Default.Sync, contentDescription = "Sincronizar",
                            tint = primaryColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->

        Box(modifier = Modifier.fillMaxSize().padding(padding)) {

            if (cargando) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (incidencias.isEmpty()) {
                // Estado vacío
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Assignment, contentDescription = null,
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
                            onEditar   = { incidenciaAEditar = incidencia },
                            onEliminar = { incidenciaAEliminar = incidencia }
                        )
                    }
                }
            }
        }
    }

    // ── Diálogo: Editar ───────────────────────────────────────────────────
    incidenciaAEditar?.let { inc ->
        DialogoEditar(
            incidencia = inc,
            onDismiss  = { incidenciaAEditar = null },
            onGuardar  = { incidenciaEditada ->
                scope.launch {
                    cargando = true
                    val r = repo.actualizar(incidenciaEditada)
                    cargando = false
                    incidenciaAEditar = null
                    mostrarMensaje(
                        if (r is ResultadoApi.Exito) "✅ Actualizado correctamente"
                        else (r as ResultadoApi.Error).mensaje
                    )
                }
            }
        )
    }

    // ── Diálogo: Confirmar eliminación ────────────────────────────────────
    incidenciaAEliminar?.let { inc ->
        AlertDialog(
            onDismissRequest = { incidenciaAEliminar = null },
            icon  = { Icon(Icons.Default.DeleteForever, tint = DeepRed, contentDescription = null) },
            title = { Text("¿Eliminar incidencia?", fontWeight = FontWeight.Bold) },
            text  = { Text("Se eliminará '${inc.tipo}' tanto del dispositivo como de Azure SQL.") },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            cargando = true
                            val r = repo.eliminar(inc)
                            cargando = false
                            incidenciaAEliminar = null
                            mostrarMensaje(
                                if (r is ResultadoApi.Exito) "✅ Eliminado correctamente"
                                else (r as ResultadoApi.Error).mensaje
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DeepRed)
                ) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { incidenciaAEliminar = null }) { Text("Cancelar") }
            }
        )
    }
}

// ── Tarjeta individual de incidencia ──────────────────────────────────────────
@Composable
private fun TarjetaIncidenciaCrud(
    incidencia: IncidenciaEntity,
    onEditar: () -> Unit,
    onEliminar: () -> Unit
) {
    val fechaFormateada = remember(incidencia.fecha) {
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            .format(Date(incidencia.fecha))
    }

    val colorEstado = when (incidencia.estado) {
        "PENDIENTE"   -> Color(0xFFFFA000)
        "EN_PROCESO"  -> Color(0xFF1976D2)
        "RESUELTO"    -> Color(0xFF2E7D32)
        "RECHAZADO"   -> DeepRed
        else          -> Color(0xFF757575)
    }

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Fila superior: tipo + badge estado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(incidencia.tipo, fontWeight = FontWeight.Bold, fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = colorEstado.copy(alpha = 0.15f)
                ) {
                    Text(incidencia.estado, fontSize = 11.sp, fontWeight = FontWeight.Bold,
                        color = colorEstado, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Ubicación
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.width(4.dp))
                Text(incidencia.ubicacion.ifBlank { "Sin ubicación" },
                    fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1)
            }

            // Descripción
            if (incidencia.descripcion.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(incidencia.descripcion, fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Fila inferior: fecha + botones
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(fechaFormateada, fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Botón Editar
                    OutlinedButton(
                        onClick = onEditar,
                        modifier = Modifier.height(34.dp),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null,
                            modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Editar", fontSize = 12.sp)
                    }

                    // Botón Eliminar
                    Button(
                        onClick = onEliminar,
                        modifier = Modifier.height(34.dp),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DeepRed)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null,
                            modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Eliminar", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

// ── Diálogo de edición ────────────────────────────────────────────────────────
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

    val estados = listOf("PENDIENTE", "EN_PROCESO", "RESUELTO", "RECHAZADO")
    var expandidoEstado by remember { mutableStateOf(false) }

    val primaryColor = MaterialTheme.colorScheme.primary

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Incidencia", fontWeight = FontWeight.Bold) },
        text  = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                // Tipo
                OutlinedTextField(
                    value         = tipo,
                    onValueChange = { tipo = it },
                    label         = { Text("Tipo") },
                    modifier      = Modifier.fillMaxWidth(),
                    shape         = RoundedCornerShape(10.dp),
                    singleLine    = true
                )

                // Descripción
                OutlinedTextField(
                    value         = descripcion,
                    onValueChange = { descripcion = it },
                    label         = { Text("Descripción") },
                    modifier      = Modifier.fillMaxWidth().height(90.dp),
                    shape         = RoundedCornerShape(10.dp),
                    maxLines      = 3
                )

                // Ubicación
                OutlinedTextField(
                    value         = ubicacion,
                    onValueChange = { ubicacion = it },
                    label         = { Text("Ubicación") },
                    modifier      = Modifier.fillMaxWidth(),
                    shape         = RoundedCornerShape(10.dp),
                    singleLine    = true,
                    leadingIcon   = {
                        Icon(Icons.Default.LocationOn, contentDescription = null,
                            tint = primaryColor)
                    }
                )

                // Estado (dropdown)
                ExposedDropdownMenuBox(
                    expanded = expandidoEstado,
                    onExpandedChange = { expandidoEstado = it }
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
                        expanded = expandidoEstado,
                        onDismissRequest = { expandidoEstado = false }
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
            Button(
                onClick = {
                    onGuardar(incidencia.copy(
                        tipo        = tipo.trim(),
                        descripcion = descripcion.trim(),
                        ubicacion   = ubicacion.trim(),
                        estado      = estado
                    ))
                },
                enabled = tipo.isNotBlank(),
                colors  = ButtonDefaults.buttonColors(containerColor = primaryColor)
            ) { Text("Guardar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}