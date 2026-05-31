package com.example.sjl_alert_v4.actividades
import com.example.sjl_alert_v4.sharedPrefs.PreferenceManager

import androidx.compose.foundation.Image
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sjl_alert_v4.R
import com.example.sjl_alert_v4.red.LoginRequest
import com.example.sjl_alert_v4.red.RetrofitClient
import com.example.sjl_alert_v4.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LoginPreview() {
    SJL_Alert_v4Theme {
        LoginPage(onLoginSuccess = {})
    }
}

@Composable
fun LoginPage(
    onLoginSuccess: () -> Unit = {},
    onForgotPasswordClick: () -> Unit = {},
    onRegisterClick: () -> Unit = {},
    onEmergencyCallClick: () -> Unit = {},
    onMapClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var dniOrEmail by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val prefManager = remember { PreferenceManager(context) }
    var estaBloqueado by remember { mutableStateOf(prefManager.estaBloqueado()) }
    var tiempoRestante by remember { mutableStateOf(prefManager.getTiempoBloqueoRestante()) }

    // Cuenta regresiva del bloqueo
    LaunchedEffect(estaBloqueado) {
        while (estaBloqueado) {
            kotlinx.coroutines.delay(1000)
            tiempoRestante = prefManager.getTiempoBloqueoRestante()
            if (tiempoRestante <= 0L) {
                estaBloqueado = false
                errorMessage = null
            }
        }
    }

    // ── Colores dinámicos del tema activo ──────────────────────────────────────
    val backgroundColor = MaterialTheme.colorScheme.background
    val primaryColor = MaterialTheme.colorScheme.primary
    val onPrimaryColor = MaterialTheme.colorScheme.onPrimary
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant
    val surfaceContainerLowColor = MaterialTheme.colorScheme.surfaceContainer
    val surfaceContainerHighColor = MaterialTheme.colorScheme.surfaceContainerHigh
    val surfaceContainerHighestColor = MaterialTheme.colorScheme.surfaceContainerHighest
    val primaryContainerColor = MaterialTheme.colorScheme.primaryContainer
    val outlineColor = MaterialTheme.colorScheme.outline

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        // Decorative blobs
        Box(
            modifier = Modifier
                .size(300.dp)
                .offset(x = 150.dp, y = (-100).dp)
                .background(color = primaryContainerColor.copy(alpha = 0.2f), shape = CircleShape)
                .align(Alignment.TopEnd)
        )
        Box(
            modifier = Modifier
                .size(400.dp)
                .offset(x = (-120).dp, y = 100.dp)
                .background(color = primaryColor.copy(alpha = 0.08f), shape = CircleShape)
                .align(Alignment.BottomStart)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // LOGO
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo_sjl),
                    contentDescription = "Logo SJL Alerta",
                    modifier = Modifier.size(120.dp),
                    contentScale = ContentScale.Fit
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "SJL Alerta",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            color = primaryColor,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = (-0.5).sp
                        )
                    )
                    Text(
                        text = "Portal del Vecino",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = onSurfaceVariantColor,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // LOGIN CARD
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = surfaceColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = "Bienvenido",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            color = primaryColor,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Ingresa tus datos para continuar",
                        style = MaterialTheme.typography.bodyMedium.copy(color = onSurfaceVariantColor)
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    // DNI O CORREO
                    Text(
                        text = "DNI O CORREO ELECTRÓNICO",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = onSurfaceVariantColor,
                            letterSpacing = 0.8.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = dniOrEmail,
                        onValueChange = {
                            dniOrEmail = it
                            errorMessage = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Ej: 70654321", color = outlineColor) },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null, tint = onSurfaceVariantColor)
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        shape = RoundedCornerShape(12.dp),
                        isError = errorMessage != null,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryColor,
                            unfocusedBorderColor = surfaceContainerHighColor,
                            focusedContainerColor = surfaceContainerHighestColor,
                            unfocusedContainerColor = surfaceContainerLowColor,
                            focusedTextColor = onSurfaceColor,
                            unfocusedTextColor = onSurfaceColor,
                        )
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // CONTRASEÑA
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "CONTRASEÑA",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = onSurfaceVariantColor,
                                letterSpacing = 0.8.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        TextButton(
                            onClick = onForgotPasswordClick,
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                text = "¿Olvidaste tu contraseña?",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = primaryColor
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            errorMessage = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("••••••••", color = outlineColor) },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = onSurfaceVariantColor)
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible)
                                        Icons.Default.VisibilityOff
                                    else
                                        Icons.Default.Visibility,
                                    contentDescription = null,
                                    tint = outlineColor
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible)
                            VisualTransformation.None
                        else
                            PasswordVisualTransformation(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        shape = RoundedCornerShape(12.dp),
                        isError = errorMessage != null,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryColor,
                            unfocusedBorderColor = surfaceContainerHighColor,
                            focusedContainerColor = surfaceContainerHighestColor,
                            unfocusedContainerColor = surfaceContainerLowColor,
                            focusedTextColor = onSurfaceColor,
                            unfocusedTextColor = onSurfaceColor,
                        )
                    )

                    // MENSAJE DE ERROR
                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Error,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = errorMessage!!,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // REMEMBER ME
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = rememberMe,
                            onCheckedChange = { rememberMe = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = primaryColor,
                                uncheckedColor = outlineColor
                            )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Mantenerse conectado",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = onSurfaceVariantColor,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // BOTÓN LOGIN CON VALIDACIÓN
                    Button(
                        onClick = {
                            when {
                                dniOrEmail.isBlank() && password.isBlank() -> {
                                    errorMessage = "Todos los campos son obligatorios"
                                }
                                dniOrEmail.isBlank() -> {
                                    errorMessage = "Ingresa tu DNI o correo"
                                }
                                password.isBlank() -> {
                                    errorMessage = "Ingresa tu contraseña"
                                }
                                else -> {
                                    isLoading = true
                                    errorMessage = null
                                    scope.launch(Dispatchers.IO) {
                                        try {
                                            // Verifica si está bloqueado
                                            if (prefManager.estaBloqueado()) {
                                                val mins = prefManager.getTiempoBloqueoRestante() / 60000
                                                val segs = (prefManager.getTiempoBloqueoRestante() % 60000) / 1000
                                                withContext(Dispatchers.Main) {
                                                    isLoading = false
                                                    estaBloqueado = true
                                                    errorMessage = "Cuenta bloqueada. Intenta en ${mins}m ${segs}s"
                                                }
                                                return@launch
                                            }
                                            // Llamada a la API de Azure — login
                                            val response = RetrofitClient.usuarioApi.login(
                                                LoginRequest(
                                                    dniOCorreo = dniOrEmail.trim(),
                                                    contrasena = hashSHA256(password)
                                                )
                                            )
                                            withContext(Dispatchers.Main) {
                                                isLoading = false
                                                when (response.code()) {
                                                    200 -> {
                                                        // Login exitoso
                                                        val usuario = response.body()?.usuario
                                                        if (usuario != null) {
                                                            prefManager.resetearIntentosFallidos()
                                                            prefManager.guardarSesion(
                                                                usuarioId      = usuario.id,
                                                                nombre         = usuario.nombre,
                                                                correo         = usuario.correo,
                                                                mantenerSesion = rememberMe
                                                            )
                                                            onLoginSuccess()
                                                        }
                                                    }
                                                    404 -> {
                                                        // Usuario no encontrado
                                                        errorMessage = "Usuario no encontrado"
                                                    }
                                                    401 -> {
                                                        // Contraseña incorrecta
                                                        prefManager.registrarIntentoFallido()
                                                        val intentos = prefManager.getIntentosFallidos()
                                                        errorMessage = if (prefManager.estaBloqueado()) {
                                                            estaBloqueado = true
                                                            tiempoRestante = prefManager.getTiempoBloqueoRestante()
                                                            "Cuenta bloqueada por 5 minutos"
                                                        } else {
                                                            "Contraseña incorrecta. Intentos restantes: ${3 - intentos}"
                                                        }
                                                    }
                                                    else -> {
                                                        errorMessage = "Error al iniciar sesión (código: ${response.code()})"
                                                    }
                                                }
                                            }
                                        } catch (e: Exception) {
                                            withContext(Dispatchers.Main) {
                                                isLoading = false
                                                errorMessage = "Error: ${e.message ?: "Sin conexión. Verifica tu internet"}"
                                            }
                                        }
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryColor,
                            contentColor = onPrimaryColor
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                        enabled = !isLoading && !estaBloqueado
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = onPrimaryColor,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Iniciar Sesión",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "→", fontSize = 16.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    HorizontalDivider(color = surfaceContainerHighColor)
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "¿Aún no tienes cuenta?",
                            style = MaterialTheme.typography.bodyMedium.copy(color = onSurfaceVariantColor)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        TextButton(
                            onClick = onRegisterClick,
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                text = "Crear Cuenta",
                                fontWeight = FontWeight.Bold,
                                color = primaryColor
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // QUICK ACTIONS
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionCard(
                    modifier = Modifier.weight(1f),
                    iconContainerColor = primaryColor.copy(alpha = 0.15f),
                    icon = { Icon(Icons.Default.Call, contentDescription = null, tint = primaryColor) },
                    label = "Emergencia",
                    value = "105 Central",
                    valueColor = primaryColor,
                    onClick = onEmergencyCallClick
                )
                QuickActionCard(
                    modifier = Modifier.weight(1f),
                    iconContainerColor = primaryColor.copy(alpha = 0.1f),
                    icon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = primaryColor) },
                    label = "SJL Segura",
                    value = "Mapa Real",
                    valueColor = primaryColor,
                    onClick = onMapClick
                )
            }
        }
    }
}

@Composable
private fun QuickActionCard(
    modifier: Modifier = Modifier,
    iconContainerColor: Color,
    icon: @Composable () -> Unit,
    label: String,
    value: String,
    valueColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier,
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconContainerColor),
                contentAlignment = Alignment.Center
            ) {
                icon()
            }
            Column {
                Text(
                    text = label.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 9.sp
                    )
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = valueColor,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}