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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.sjl_alert_v4.R
import com.example.sjl_alert_v4.modelos.AppDatabase
import com.example.sjl_alert_v4.sharedPrefs.PreferenceManager
import com.example.sjl_alert_v4.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun AjustesPrefs(
    onBack: () -> Unit,
    onLogout: () -> Unit = {},
    onThemeChanged: () -> Unit = {}
) {
    val context = LocalContext.current
    val preferenceManager = remember { PreferenceManager(context) }
    val db = remember { AppDatabase.getInstance(context) }
    val scope = rememberCoroutineScope()

    // ── Estado de preferencias ────────────────────────────────────────────────
    var darkMode by remember { mutableStateOf(preferenceManager.isDarkMode()) }
    var emailNotifications by remember { mutableStateOf(preferenceManager.isEmailNotificationEnabled()) }
    var communityAlerts by remember { mutableStateOf(preferenceManager.isCommunityAlertsEnabled()) }
    var locationSharing by remember { mutableStateOf(preferenceManager.isLocationSharingEnabled()) }

    // ── Tamaño de fuente e idioma ─────────────────────────────────────────────
    var fontSizeActual by remember { mutableStateOf(preferenceManager.getFontSize()) }
    var idiomaActual by remember { mutableStateOf(preferenceManager.getLanguage()) }

    // ── Datos del usuario en sesión ───────────────────────────────────────────
    val usuarioId = preferenceManager.getSesionUsuarioId()
    val sesionNombre = preferenceManager.getSesionNombre()
    val sesionCorreo = preferenceManager.getSesionCorreo()
    val sesionTelefono = preferenceManager.getSesionTelefono()

    // ── Control de diálogos ───────────────────────────────────────────────────
    var mostrarDialogoTelefono by remember { mutableStateOf(false) }
    var mostrarDialogoCorreo by remember { mutableStateOf(false) }
    var mostrarDialogoContrasena by remember { mutableStateOf(false) }
    var mostrarDialogoIdioma by remember { mutableStateOf(false) }
    var mostrarDialogoLogout by remember { mutableStateOf(false) }
    var mostrarDialogoTerminos by remember { mutableStateOf(false) }
    var mostrarDialogoPrivacidad by remember { mutableStateOf(false) }

    // ── Datos cargados desde la DB ────────────────────────────────────────────
    // Inicializamos desde SharedPrefs (instantáneo) y luego Room los confirma/actualiza
    var telefonoActual by remember { mutableStateOf(sesionTelefono) }
    var correoActual by remember { mutableStateOf(sesionCorreo) }
    var edadUsuario by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(usuarioId) {
        if (usuarioId != -1) {
            val usuario = db.usuarioDao().buscarPorId(usuarioId)
            usuario?.let {
                if (it.telefono.isNotBlank()) telefonoActual = it.telefono
                correoActual = it.correo
                edadUsuario = calcularEdad(it.fechaNacimiento)
            }
        }
    }

    // ── Colores dinámicos del tema activo ─────────────────────────────────────
    val primaryColor = MaterialTheme.colorScheme.primary
    val onPrimaryColor = MaterialTheme.colorScheme.onPrimary
    val backgroundColor = MaterialTheme.colorScheme.background
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant
    val surfaceContainerHighColor = MaterialTheme.colorScheme.surfaceContainerHigh

    // ── Strings capturados antes de coroutines ────────────────────────────────
    val strTelefonoActualizado = stringResource(R.string.telefono_actualizado)
    val strCorreoActualizado = stringResource(R.string.correo_actualizado)
    val strContrasenaActualizada = stringResource(R.string.contrasena_actualizada)
    val strErrorActualizar = stringResource(R.string.error_actualizar)
    val strErrorTelefonoDigitos = stringResource(R.string.error_telefono_digitos)
    val strErrorSoloNumeros = stringResource(R.string.error_solo_numeros)
    val strErrorCorreoVacio = stringResource(R.string.error_correo_vacio)
    val strErrorCorreoInvalido = stringResource(R.string.error_correo_invalido)

    // ── DIÁLOGO: Editar Teléfono ──────────────────────────────────────────────
    if (mostrarDialogoTelefono) {
        EditarCampoDialog(
            titulo = stringResource(R.string.editar_telefono),
            etiqueta = stringResource(R.string.nuevo_telefono),
            placeholder = "Ej: 987654321",
            valorInicial = telefonoActual,
            keyboardType = KeyboardType.Phone,
            icono = Icons.Default.Phone,
            validar = { valor ->
                when {
                    valor.length < 9 -> strErrorTelefonoDigitos
                    !valor.all { it.isDigit() } -> strErrorSoloNumeros
                    else -> null
                }
            },
            onConfirmar = { nuevoTelefono ->
                scope.launch {
                    try {
                        db.usuarioDao().actualizarTelefono(usuarioId, nuevoTelefono)
                        telefonoActual = nuevoTelefono
                        // También actualizar SharedPrefs para que persista
                        preferenceManager.guardarSesion(
                            usuarioId = usuarioId,
                            nombre = sesionNombre,
                            correo = correoActual,
                            telefono = nuevoTelefono
                        )
                        Toast.makeText(context, strTelefonoActualizado, Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(context, strErrorActualizar, Toast.LENGTH_SHORT).show()
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
            titulo = stringResource(R.string.editar_correo),
            etiqueta = stringResource(R.string.nuevo_correo),
            placeholder = "correo@ejemplo.com",
            valorInicial = correoActual,
            keyboardType = KeyboardType.Email,
            icono = Icons.Default.Email,
            validar = { valor ->
                when {
                    valor.isBlank() -> strErrorCorreoVacio
                    !valor.contains("@") -> strErrorCorreoInvalido
                    else -> null
                }
            },
            onConfirmar = { nuevoCorreo ->
                scope.launch {
                    try {
                        db.usuarioDao().actualizarCorreo(usuarioId, nuevoCorreo.trim().lowercase())
                        correoActual = nuevoCorreo.trim().lowercase()
                        preferenceManager.guardarSesion(
                            usuarioId = usuarioId,
                            nombre = sesionNombre,
                            correo = correoActual,
                            telefono = telefonoActual  // preservar teléfono
                        )
                        Toast.makeText(context, strCorreoActualizado, Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(context, strErrorActualizar, Toast.LENGTH_SHORT).show()
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
                Toast.makeText(context, strContrasenaActualizada, Toast.LENGTH_SHORT).show()
                mostrarDialogoContrasena = false
            },
            onError = { msg ->
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }
        )
    }

    // ── DIÁLOGO: Seleccionar Idioma ───────────────────────────────────────────
    if (mostrarDialogoIdioma) {
        IdiomaDialog(
            idiomaActual = idiomaActual,
            onConfirmar = { nuevoIdioma ->
                idiomaActual = nuevoIdioma
                preferenceManager.setLanguage(nuevoIdioma)
                mostrarDialogoIdioma = false
                
                // Recrear la actividad para aplicar el cambio de idioma inmediatamente
                var curContext = context
                while (curContext is android.content.ContextWrapper) {
                    if (curContext is android.app.Activity) {
                        curContext.recreate()
                        break
                    }
                    curContext = curContext.baseContext
                }
            },
            onDismiss = { mostrarDialogoIdioma = false }
        )
    }

    // ── DIÁLOGO: Confirmar Logout ─────────────────────────────────────────────
    if (mostrarDialogoLogout) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoLogout = false },
            title = {
                Text(text = stringResource(R.string.cerrar_sesion), fontWeight = FontWeight.Bold, color = primaryColor)
            },
            text = {
                Text(stringResource(R.string.salir_pregunta))
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { mostrarDialogoLogout = false },
                        modifier = Modifier.weight(1f).fillMaxHeight().defaultMinSize(minHeight = 48.dp),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.cancelar),
                            color = primaryColor,
                            textAlign = TextAlign.Center
                        )
                    }
                    Button(
                        onClick = {
                            mostrarDialogoLogout = false
                            onLogout()
                        },
                        modifier = Modifier.weight(1f).fillMaxHeight().defaultMinSize(minHeight = 48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.salir_confirmar),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            },
            dismissButton = null
        )
    }

    // ── DIÁLOGO: Términos y Condiciones (Solo Lectura) ─────────────────────────
    if (mostrarDialogoTerminos) {
        TerminosCondicionesDialog(
            soloLectura = true,
            mostrarSoloTerminos = true,
            onDismiss = { mostrarDialogoTerminos = false }
        )
    }

    // ── DIÁLOGO: Política de Privacidad (Solo Lectura) ─────────────────────────
    if (mostrarDialogoPrivacidad) {
        PoliticaPrivacidadDialog(
            onDismiss = { mostrarDialogoPrivacidad = false }
        )
    }

    // ── PANTALLA PRINCIPAL ────────────────────────────────────────────────────
    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(backgroundColor)
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = stringResource(R.string.atras),
                        tint = primaryColor
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.configuracion),
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
                            text = sesionNombre.ifEmpty { stringResource(R.string.usuario) },
                            color = onPrimaryColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = correoActual.ifEmpty { stringResource(R.string.sin_correo) },
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
                                    text = stringResource(R.string.vecino_activo),
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
                                        text = "$edadUsuario ${stringResource(R.string.anios)}",
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
            SettingsSection(title = stringResource(R.string.mi_cuenta)) {
                SettingsItem(
                    icon = Icons.Default.Phone,
                    title = stringResource(R.string.telefono),
                    subtitle = telefonoActual.ifEmpty { stringResource(R.string.no_configurado) },
                    onClick = { mostrarDialogoTelefono = true }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = surfaceContainerHighColor)
                SettingsItem(
                    icon = Icons.Default.Email,
                    title = stringResource(R.string.correo_electronico),
                    subtitle = correoActual.ifEmpty { stringResource(R.string.no_configurado) },
                    onClick = { mostrarDialogoCorreo = true }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = surfaceContainerHighColor)
                SettingsItem(
                    icon = Icons.Default.Lock,
                    title = stringResource(R.string.cambiar_contrasena),
                    subtitle = "••••••••",
                    onClick = { mostrarDialogoContrasena = true }
                )
            }

            // ── SECCIÓN: Preferencias ─────────────────────────────────────
            SettingsSection(title = stringResource(R.string.preferencias_app)) {

                SettingsSwitchItem(
                    icon = Icons.Default.DarkMode,
                    title = stringResource(R.string.modo_oscuro),
                    checked = darkMode,
                    onCheckedChange = {
                        darkMode = it
                        preferenceManager.setDarkMode(it)
                        onThemeChanged()
                    }
                )

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = surfaceContainerHighColor)

                // ── Tamaño de fuente con slider ───────────────────────────
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.TextFields,
                            contentDescription = null,
                            tint = primaryColor,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = stringResource(R.string.tamano_fuente),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = primaryColor.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = when {
                                    fontSizeActual <= 13f -> stringResource(R.string.pequeno)
                                    fontSizeActual <= 16f -> stringResource(R.string.normal)
                                    fontSizeActual <= 19f -> stringResource(R.string.grande)
                                    else -> stringResource(R.string.muy_grande)
                                },
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                                fontSize = 12.sp,
                                color = primaryColor,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Slider(
                        value = fontSizeActual,
                        onValueChange = { fontSizeActual = it },
                        onValueChangeFinished = {
                            preferenceManager.setFontSize(fontSizeActual)
                            onThemeChanged()
                        },
                        valueRange = 12f..22f,
                        steps = 3,
                        colors = SliderDefaults.colors(
                            thumbColor = primaryColor,
                            activeTrackColor = primaryColor,
                            inactiveTrackColor = surfaceContainerHighColor
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("A", fontSize = 11.sp, color = onSurfaceVariantColor)
                        Text("A", fontSize = 18.sp, color = onSurfaceVariantColor, fontWeight = FontWeight.Medium)
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = surfaceContainerHighColor)

                SettingsItem(
                    icon = Icons.Default.Language,
                    title = stringResource(R.string.idioma),
                    subtitle = when (idiomaActual) {
                        "es" -> "🇵🇪  Español"
                        "en" -> "🇺🇸  English"
                        else -> "Español"
                    },
                    onClick = { mostrarDialogoIdioma = true }
                )
            }

            // ── SECCIÓN: Notificaciones ───────────────────────────────────
            SettingsSection(title = stringResource(R.string.notificaciones)) {
                SettingsSwitchItem(
                    icon = Icons.Default.Notifications,
                    title = stringResource(R.string.notif_correo),
                    checked = emailNotifications,
                    onCheckedChange = {
                        emailNotifications = it
                        preferenceManager.setEmailNotificationEnabled(it)
                    }
                )
                /*HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = surfaceContainerHighColor)
                SettingsSwitchItem(
                    icon = Icons.Default.Security,
                    title = stringResource(R.string.alertas_comunitarias),
                    checked = communityAlerts,
                    onCheckedChange = {
                        communityAlerts = it
                        preferenceManager.setCommunityAlertsEnabled(it)
                    }
                )*/
            }

            // ── SECCIÓN: Seguridad y Privacidad ───────────────────────────
            SettingsSection(title = stringResource(R.string.seguridad_privacidad)) {
                SettingsSwitchItem(
                    icon = Icons.Default.LocationOn,
                    title = stringResource(R.string.compartir_ubicacion),
                    checked = locationSharing,
                    onCheckedChange = {
                        locationSharing = it
                        preferenceManager.setLocationSharingEnabled(it)
                    }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = surfaceContainerHighColor)
                /*SettingsItem(
                    icon = Icons.Default.History,
                    title = stringResource(R.string.historial_reportes),
                    subtitle = stringResource(R.string.ver_incidencias),
                    onClick = { }
                )*/
            }

            // ── SECCIÓN: Ayuda ────────────────────────────────────────────
            SettingsSection(title = stringResource(R.string.ayuda_soporte)) {
                SettingsItem(
                    icon = Icons.Default.Info,
                    title = stringResource(R.string.terminos),
                    onClick = { mostrarDialogoTerminos = true }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = surfaceContainerHighColor)
                SettingsItem(
                    icon = Icons.Default.PrivacyTip,
                    title = stringResource(R.string.privacidad),
                    onClick = { mostrarDialogoPrivacidad = true }
                )
            }

            // ── SECCIÓN: Sesión ───────────────────────────────────────────
            SettingsSection(title = stringResource(R.string.cuenta_sesion)) {
                SettingsItem(
                    icon = Icons.Default.Logout,
                    title = stringResource(R.string.cerrar_sesion),
                    subtitle = stringResource(R.string.salir_cuenta_desc),
                    onClick = { mostrarDialogoLogout = true }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = stringResource(R.string.version_app),
                modifier = Modifier.align(Alignment.CenterHorizontally),
                color = onSurfaceVariantColor,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// ── DIÁLOGO: Seleccionar Idioma ───────────────────────────────────────────────
@Composable
private fun IdiomaDialog(
    idiomaActual: String,
    onConfirmar: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val onPrimaryColor = MaterialTheme.colorScheme.onPrimary
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface

    val idiomas = listOf(
        "es" to "🇵🇪  Español",
        "en" to "🇺🇸  English"
    )
    var seleccionado by remember { mutableStateOf(idiomaActual) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = surfaceColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Language, contentDescription = null, tint = primaryColor, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.seleccionar_idioma),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = primaryColor
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                idiomas.forEach { (codigo, etiqueta) ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = seleccionado == codigo,
                            onClick = { seleccionado = codigo },
                            colors = RadioButtonDefaults.colors(selectedColor = primaryColor)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = etiqueta, fontSize = 16.sp, color = onSurfaceColor)
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) {
                        Text(stringResource(R.string.cancelar), color = primaryColor)
                    }
                    Button(
                        onClick = { onConfirmar(seleccionado) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                    ) {
                        Text(stringResource(R.string.aplicar), color = onPrimaryColor)
                    }
                }
            }
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
                    Text(text = titulo, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = primaryColor)
                }
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = valor,
                    onValueChange = { valor = it; error = null },
                    label = { Text(etiqueta) },
                    placeholder = { Text(placeholder, color = outlineColor) },
                    isError = error != null,
                    supportingText = {
                        if (error != null) Text(text = error!!, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
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
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) {
                        Text(stringResource(R.string.cancelar), color = primaryColor)
                    }
                    Button(
                        onClick = {
                            val mensajeError = validar(valor)
                            if (mensajeError != null) error = mensajeError else onConfirmar(valor)
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                    ) {
                        Text(stringResource(R.string.guardar), color = onPrimaryColor)
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

    // Capturar strings antes de coroutines
    val strError1 = stringResource(R.string.error_contrasena_actual)
    val strError2 = stringResource(R.string.error_contrasena_min)
    val strError3 = stringResource(R.string.error_contrasena_no_coinciden)
    val strError4 = stringResource(R.string.error_contrasena_igual)
    val strError5 = stringResource(R.string.error_contrasena_incorrecta)

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
                        text = stringResource(R.string.cambiar_contrasena_titulo),
                        fontWeight = FontWeight.Bold, fontSize = 18.sp, color = primaryColor
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = contrasenaActual,
                    onValueChange = { contrasenaActual = it; error = null },
                    label = { Text(stringResource(R.string.contrasena_actual)) },
                    singleLine = true,
                    visualTransformation = if (verActual) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { verActual = !verActual }) {
                            Icon(if (verActual) Icons.Default.VisibilityOff else Icons.Default.Visibility, null, tint = outlineColor)
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryColor, unfocusedBorderColor = surfaceContainerHighColor, focusedLabelColor = primaryColor)
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = nuevaContrasena,
                    onValueChange = { nuevaContrasena = it; error = null },
                    label = { Text(stringResource(R.string.nueva_contrasena)) },
                    placeholder = { Text(stringResource(R.string.min_6_caracteres), color = outlineColor) },
                    singleLine = true,
                    visualTransformation = if (verNueva) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { verNueva = !verNueva }) {
                            Icon(if (verNueva) Icons.Default.VisibilityOff else Icons.Default.Visibility, null, tint = outlineColor)
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryColor, unfocusedBorderColor = surfaceContainerHighColor, focusedLabelColor = primaryColor)
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = confirmarContrasena,
                    onValueChange = { confirmarContrasena = it; error = null },
                    label = { Text(stringResource(R.string.confirmar_contrasena)) },
                    singleLine = true,
                    isError = confirmarContrasena.isNotEmpty() && nuevaContrasena != confirmarContrasena,
                    visualTransformation = if (verConfirmar) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { verConfirmar = !verConfirmar }) {
                            Icon(if (verConfirmar) Icons.Default.VisibilityOff else Icons.Default.Visibility, null, tint = outlineColor)
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryColor, unfocusedBorderColor = surfaceContainerHighColor, focusedLabelColor = primaryColor)
                )

                if (error != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = error!!, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), enabled = !isLoading) {
                        Text(stringResource(R.string.cancelar), color = primaryColor)
                    }
                    Button(
                        onClick = {
                            error = when {
                                contrasenaActual.isBlank() -> strError1
                                nuevaContrasena.length < 6 -> strError2
                                nuevaContrasena != confirmarContrasena -> strError3
                                nuevaContrasena == contrasenaActual -> strError4
                                else -> null
                            }
                            if (error == null) {
                                isLoading = true
                                scope.launch {
                                    try {
                                        val usuario = db.usuarioDao().buscarPorId(usuarioId)
                                        val hashActual = hashSHA256(contrasenaActual)
                                        if (usuario == null || usuario.contrasena != hashActual) {
                                            error = strError5
                                            isLoading = false
                                            return@launch
                                        }
                                        db.usuarioDao().actualizarContrasena(usuarioId, hashSHA256(nuevaContrasena))
                                        isLoading = false
                                        onExito()
                                    } catch (e: Exception) {
                                        isLoading = false
                                        onError("Error: ${e.message}")
                                    }
                                }
                            }
                        },
                        modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                        enabled = !isLoading
                    ) {
                        if (isLoading) CircularProgressIndicator(modifier = Modifier.size(18.dp), color = onPrimaryColor, strokeWidth = 2.dp)
                        else Text(stringResource(R.string.guardar), color = onPrimaryColor)
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
        Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = primaryColor, modifier = Modifier.padding(start = 8.dp, bottom = 8.dp))
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = surfaceColor), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
            Column { content() }
        }
    }
}

@Composable
fun SettingsItem(icon: ImageVector, title: String, subtitle: String? = null, onClick: () -> Unit) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant
    Surface(onClick = onClick, color = Color.Transparent) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = primaryColor, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = onSurfaceColor)
                if (subtitle != null) Text(text = subtitle, fontSize = 14.sp, color = onSurfaceVariantColor)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = onSurfaceVariantColor, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
fun SettingsSwitchItem(icon: ImageVector, title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val onPrimaryColor = MaterialTheme.colorScheme.onPrimary
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val surfaceContainerHighColor = MaterialTheme.colorScheme.surfaceContainerHigh
    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = primaryColor, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = onSurfaceColor, modifier = Modifier.weight(1f))
        Switch(
            checked = checked, onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = onPrimaryColor, checkedTrackColor = primaryColor, uncheckedThumbColor = Color.White, uncheckedTrackColor = surfaceContainerHighColor)
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