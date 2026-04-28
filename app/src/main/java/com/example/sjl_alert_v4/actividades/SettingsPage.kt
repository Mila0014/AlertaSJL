package com.example.sjl_alert_v4.actividades

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.sjl_alert_v4.modelos.AppDatabase
import com.example.sjl_alert_v4.sharedPrefs.PreferenceManager
import com.example.sjl_alert_v4.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun AjustesPrefs(
    onBack: () -> Unit,
    onThemeChanged: () -> Unit = {}
) {
    val context = LocalContext.current
    val preferenceManager = remember { PreferenceManager(context) }
    val db = remember { AppDatabase.getInstance(context) }
    val scope = rememberCoroutineScope()

    var darkMode by remember { mutableStateOf(preferenceManager.isDarkMode()) }
    var emailNotifications by remember { mutableStateOf(preferenceManager.isEmailNotificationEnabled()) }
    var communityAlerts by remember { mutableStateOf(preferenceManager.isCommunityAlertsEnabled()) }
    var locationSharing by remember { mutableStateOf(true) }

    val usuarioId = preferenceManager.getSesionUsuarioId()
    val sesionNombre = preferenceManager.getSesionNombre()
    val sesionCorreo = preferenceManager.getSesionCorreo()

    var mostrarDialogoTelefono by remember { mutableStateOf(false) }
    var mostrarDialogoCorreo by remember { mutableStateOf(false) }
    var mostrarDialogoContrasena by remember { mutableStateOf(false) }

    var telefonoActual by remember { mutableStateOf("") }
    var correoActual by remember { mutableStateOf(sesionCorreo) }
    var edadUsuario by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(usuarioId) {
        if (usuarioId != -1) {
            val usuario = db.usuarioDao().buscarPorId(usuarioId)
            usuario?.let {
                telefonoActual = it.telefono
                correoActual = it.correo
                edadUsuario = calcularEdad(it.fechaNacimiento)
            }
        }
    }

    // ── Colores dinámicos del tema activo ──────────────────────────────────────
    val primaryColor = MaterialTheme.colorScheme.primary
    val onPrimaryColor = MaterialTheme.colorScheme.onPrimary
    val backgroundColor = MaterialTheme.colorScheme.background
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant
    val surfaceContainerHighColor = MaterialTheme.colorScheme.surfaceContainerHigh

    // ── DIÁLOGO: Editar Teléfono ──────────────────────────────────────────────
    if (mostrarDialogoTelefono) {
        EditarCampoDialog(
            titulo = "Editar Teléfono",
            etiqueta = "Nuevo teléfono",
            placeholder = "Ej: 987654321",
            valorInicial = telefonoActual,
            keyboardType = KeyboardType.Phone,
            icono = Icons.Default.Phone,
            validar = { valor ->
                when {
                    valor.length < 9 -> "El teléfono debe tener 9 dígitos"
                    !valor.all { it.isDigit() } -> "Solo se permiten números"
                    else -> null
                }
            },
            onConfirmar = { nuevoTelefono ->
                scope.launch {
                    try {
                        db.usuarioDao().actualizarTelefono(usuarioId, nuevoTelefono)
                        telefonoActual = nuevoTelefono
                        Toast.makeText(context, "Teléfono actualizado", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(context, "Error al actualizar", Toast.LENGTH_SHORT).show()
                    }
                }
                mostrarDialogoTelefono = false
            },
            onDismiss = { mostrarDialogoTelefono = false }
        )
    }

    // ── DIÁLOGO: Editar Correo ────────────────────────────────────────────────
    if (mostrarDialogoCorreo) {
        EditarCampoDialog(
            titulo = "Editar Correo",
            etiqueta = "Nuevo correo electrónico",
            placeholder = "correo@ejemplo.com",
            valorInicial = correoActual,
            keyboardType = KeyboardType.Email,
            icono = Icons.Default.Email,
            validar = { valor ->
                when {
                    valor.isBlank() -> "El correo no puede estar vacío"
                    !valor.contains("@") -> "Ingresa un correo válido"
                    else -> null
                }
            },
            onConfirmar = { nuevoCorreo ->
                scope.launch {
                    try {
                        db.usuarioDao().actualizarCorreo(usuarioId, nuevoCorreo.trim().lowercase())
                        correoActual = nuevoCorreo.trim().lowercase()
                        preferenceManager.guardarSesion(usuarioId, sesionNombre, correoActual)
                        Toast.makeText(context, "Correo actualizado", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(context, "Error al actualizar", Toast.LENGTH_SHORT).show()
                    }
                }
                mostrarDialogoCorreo = false
            },
            onDismiss = { mostrarDialogoCorreo = false }
        )
    }

    // ── DIÁLOGO: Cambiar Contraseña ───────────────────────────────────────────
    if (mostrarDialogoContrasena) {
        CambiarContrasenaDialog(
            usuarioId = usuarioId,
            db = db,
            onDismiss = { mostrarDialogoContrasena = false },
            onExito = {
                Toast.makeText(context, "Contraseña actualizada", Toast.LENGTH_SHORT).show()
                mostrarDialogoContrasena = false
            },
            onError = { msg ->
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }
        )
    }

    // ── PANTALLA PRINCIPAL ────────────────────────────────────────────────────
    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(backgroundColor)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Atrás", tint = primaryColor)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Configuración",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = primaryColor
                )
            }
        },
        containerColor = backgroundColor
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // ── TARJETA DE PERFIL ─────────────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = primaryColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .background(color = onPrimaryColor.copy(alpha = 0.2f), shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = sesionNombre.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                            color = onPrimaryColor,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = sesionNombre.ifEmpty { "Usuario" },
                            color = onPrimaryColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = correoActual.ifEmpty { "Sin correo" },
                            color = onPrimaryColor.copy(alpha = 0.8f),
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = onPrimaryColor.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "Vecino activo",
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                                    color = onPrimaryColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            if (edadUsuario != null) {
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = onPrimaryColor.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "$edadUsuario años",
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                                        color = onPrimaryColor,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── SECCIÓN: Mi cuenta ────────────────────────────────────────
            SettingsSection(title = "Mi cuenta") {
                SettingsItem(
                    icon = Icons.Default.Phone,
                    title = "Teléfono",
                    subtitle = telefonoActual.ifEmpty { "No configurado" },
                    onClick = { mostrarDialogoTelefono = true }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = surfaceContainerHighColor)
                SettingsItem(
                    icon = Icons.Default.Email,
                    title = "Correo electrónico",
                    subtitle = correoActual.ifEmpty { "No configurado" },
                    onClick = { mostrarDialogoCorreo = true }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = surfaceContainerHighColor)
                SettingsItem(
                    icon = Icons.Default.Lock,
                    title = "Cambiar contraseña",
                    subtitle = "••••••••",
                    onClick = { mostrarDialogoContrasena = true }
                )
            }

            // ── SECCIÓN: Preferencias ─────────────────────────────────────
            SettingsSection(title = "Preferencias de la App") {
                SettingsSwitchItem(
                    icon = Icons.Default.DarkMode,
                    title = "Modo Oscuro",
                    checked = darkMode,
                    onCheckedChange = {
                        darkMode = it
                        preferenceManager.setDarkMode(it)
                        onThemeChanged()
                    }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = surfaceContainerHighColor)
                SettingsItem(
                    icon = Icons.Default.Language,
                    title = "Idioma",
                    subtitle = "Español",
                    onClick = { }
                )
            }

            // ── SECCIÓN: Notificaciones ───────────────────────────────────
            SettingsSection(title = "Notificaciones") {
                SettingsSwitchItem(
                    icon = Icons.Default.Notifications,
                    title = "Notificaciones por Correo",
                    checked = emailNotifications,
                    onCheckedChange = {
                        emailNotifications = it
                        preferenceManager.setEmailNotificationEnabled(it)
                    }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = surfaceContainerHighColor)
                SettingsSwitchItem(
                    icon = Icons.Default.Security,
                    title = "Alertas Comunitarias",
                    checked = communityAlerts,
                    onCheckedChange = {
                        communityAlerts = it
                        preferenceManager.setCommunityAlertsEnabled(it)
                    }
                )
            }

            // ── SECCIÓN: Seguridad y Privacidad ───────────────────────────
            SettingsSection(title = "Seguridad y Privacidad") {
                SettingsSwitchItem(
                    icon = Icons.Default.LocationOn,
                    title = "Compartir Ubicación en Emergencias",
                    checked = locationSharing,
                    onCheckedChange = { locationSharing = it }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = surfaceContainerHighColor)
                SettingsItem(
                    icon = Icons.Default.History,
                    title = "Historial de Reportes",
                    subtitle = "Ver mis incidencias pasadas",
                    onClick = { }
                )
            }

            // ── SECCIÓN: Ayuda ────────────────────────────────────────────
            SettingsSection(title = "Ayuda y Soporte") {
                SettingsItem(
                    icon = Icons.Default.Info,
                    title = "Términos y Condiciones",
                    onClick = { }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = surfaceContainerHighColor)
                SettingsItem(
                    icon = Icons.Default.PrivacyTip,
                    title = "Política de Privacidad",
                    onClick = { }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Versión 1.0.0",
                modifier = Modifier.align(Alignment.CenterHorizontally),
                color = onSurfaceVariantColor,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// ── DIÁLOGO GENÉRICO: Editar un campo de texto ────────────────────────────────
@Composable
private fun EditarCampoDialog(
    titulo: String,
    etiqueta: String,
    placeholder: String,
    valorInicial: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    icono: ImageVector,
    validar: (String) -> String?,
    onConfirmar: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var valor by remember { mutableStateOf(valorInicial) }
    var error by remember { mutableStateOf<String?>(null) }

    val primaryColor = MaterialTheme.colorScheme.primary
    val onPrimaryColor = MaterialTheme.colorScheme.onPrimary
    val surfaceColor = MaterialTheme.colorScheme.surface
    val surfaceContainerHighColor = MaterialTheme.colorScheme.surfaceContainerHigh
    val outlineColor = MaterialTheme.colorScheme.outline

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = surfaceColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(icono, contentDescription = null, tint = primaryColor, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = titulo,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = primaryColor
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = valor,
                    onValueChange = {
                        valor = it
                        error = null
                    },
                    label = { Text(etiqueta) },
                    placeholder = { Text(placeholder, color = outlineColor) },
                    isError = error != null,
                    supportingText = {
                        if (error != null) {
                            Text(text = error!!, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = primaryColor,
                        unfocusedBorderColor = surfaceContainerHighColor,
                        focusedLabelColor = primaryColor
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancelar", color = primaryColor)
                    }
                    Button(
                        onClick = {
                            val mensajeError = validar(valor)
                            if (mensajeError != null) {
                                error = mensajeError
                            } else {
                                onConfirmar(valor)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                    ) {
                        Text("Guardar", color = onPrimaryColor)
                    }
                }
            }
        }
    }
}

// ── DIÁLOGO: Cambiar contraseña ───────────────────────────────────────────────
@Composable
private fun CambiarContrasenaDialog(
    usuarioId: Int,
    db: com.example.sjl_alert_v4.modelos.AppDatabase,
    onDismiss: () -> Unit,
    onExito: () -> Unit,
    onError: (String) -> Unit
) {
    val scope = rememberCoroutineScope()
    var contrasenaActual by remember { mutableStateOf("") }
    var nuevaContrasena by remember { mutableStateOf("") }
    var confirmarContrasena by remember { mutableStateOf("") }
    var verActual by remember { mutableStateOf(false) }
    var verNueva by remember { mutableStateOf(false) }
    var verConfirmar by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val primaryColor = MaterialTheme.colorScheme.primary
    val onPrimaryColor = MaterialTheme.colorScheme.onPrimary
    val surfaceColor = MaterialTheme.colorScheme.surface
    val surfaceContainerHighColor = MaterialTheme.colorScheme.surfaceContainerHigh
    val outlineColor = MaterialTheme.colorScheme.outline

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = surfaceColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = primaryColor, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Cambiar Contraseña",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = primaryColor
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = contrasenaActual,
                    onValueChange = { contrasenaActual = it; error = null },
                    label = { Text("Contraseña actual") },
                    singleLine = true,
                    visualTransformation = if (verActual) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { verActual = !verActual }) {
                            Icon(
                                imageVector = if (verActual) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = null,
                                tint = outlineColor
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = primaryColor,
                        unfocusedBorderColor = surfaceContainerHighColor,
                        focusedLabelColor = primaryColor
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = nuevaContrasena,
                    onValueChange = { nuevaContrasena = it; error = null },
                    label = { Text("Nueva contraseña") },
                    placeholder = { Text("Mínimo 6 caracteres", color = outlineColor) },
                    singleLine = true,
                    visualTransformation = if (verNueva) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { verNueva = !verNueva }) {
                            Icon(
                                imageVector = if (verNueva) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = null,
                                tint = outlineColor
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = primaryColor,
                        unfocusedBorderColor = surfaceContainerHighColor,
                        focusedLabelColor = primaryColor
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = confirmarContrasena,
                    onValueChange = { confirmarContrasena = it; error = null },
                    label = { Text("Confirmar nueva contraseña") },
                    singleLine = true,
                    isError = confirmarContrasena.isNotEmpty() && nuevaContrasena != confirmarContrasena,
                    visualTransformation = if (verConfirmar) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { verConfirmar = !verConfirmar }) {
                            Icon(
                                imageVector = if (verConfirmar) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = null,
                                tint = outlineColor
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = primaryColor,
                        unfocusedBorderColor = surfaceContainerHighColor,
                        focusedLabelColor = primaryColor
                    )
                )

                if (error != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = error!!, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isLoading
                    ) {
                        Text("Cancelar", color = primaryColor)
                    }
                    Button(
                        onClick = {
                            error = when {
                                contrasenaActual.isBlank() -> "Ingresa tu contraseña actual"
                                nuevaContrasena.length < 6 -> "La nueva contraseña debe tener al menos 6 caracteres"
                                nuevaContrasena != confirmarContrasena -> "Las contraseñas no coinciden"
                                nuevaContrasena == contrasenaActual -> "La nueva contraseña debe ser diferente"
                                else -> null
                            }
                            if (error == null) {
                                isLoading = true
                                scope.launch {
                                    try {
                                        val usuario = db.usuarioDao().buscarPorId(usuarioId)
                                        val hashActual = hashSHA256(contrasenaActual)
                                        if (usuario == null || usuario.contrasena != hashActual) {
                                            error = "La contraseña actual es incorrecta"
                                            isLoading = false
                                            return@launch
                                        }
                                        db.usuarioDao().actualizarContrasena(usuarioId, hashSHA256(nuevaContrasena))
                                        isLoading = false
                                        onExito()
                                    } catch (e: Exception) {
                                        isLoading = false
                                        onError("Error al actualizar: ${e.message}")
                                    }
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = onPrimaryColor, strokeWidth = 2.dp)
                        } else {
                            Text("Guardar", color = onPrimaryColor)
                        }
                    }
                }
            }
        }
    }
}

// ── Composables reutilizables ─────────────────────────────────────────────────

@Composable
fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surface

    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = primaryColor,
            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = surfaceColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column { content() }
        }
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant

    Surface(onClick = onClick, color = Color.Transparent) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = primaryColor, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = onSurfaceColor)
                if (subtitle != null) {
                    Text(text = subtitle, fontSize = 14.sp, color = onSurfaceVariantColor)
                }
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = onSurfaceVariantColor, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
fun SettingsSwitchItem(
    icon: ImageVector,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val onPrimaryColor = MaterialTheme.colorScheme.onPrimary
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val surfaceContainerHighColor = MaterialTheme.colorScheme.surfaceContainerHigh

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = primaryColor, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = onSurfaceColor, modifier = Modifier.weight(1f))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = onPrimaryColor,
                checkedTrackColor = primaryColor,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = surfaceContainerHighColor
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AjustesPrefsPreview() {
    SJL_Alert_v4Theme {
        AjustesPrefs(onBack = {})
    }
}