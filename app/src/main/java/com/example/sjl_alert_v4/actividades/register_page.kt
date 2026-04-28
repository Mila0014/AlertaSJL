package com.example.sjl_alert_v4.actividades

import android.widget.Toast
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
import com.example.sjl_alert_v4.modelos.AppDatabase
import com.example.sjl_alert_v4.modelos.Usuario
import com.example.sjl_alert_v4.ui.theme.*
import kotlinx.coroutines.launch
import java.security.MessageDigest

// ----- Función utilitaria: hashea la contraseña con SHA-256 -----
fun hashSHA256(input: String): String {
    val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
    return bytes.joinToString("") { "%02x".format(it) }
}

// ----- Función utilitaria: calcula la edad a partir de DD/MM/AAAA -----
fun calcularEdad(fechaTexto: String): Int? {
    if (fechaTexto.length < 10) return null
    return try {
        val partes = fechaTexto.split("/")
        if (partes.size != 3) return null
        val dia = partes[0].toInt()
        val mes = partes[1].toInt()
        val anio = partes[2].toInt()
        if (mes < 1 || mes > 12) return null
        if (dia < 1 || dia > 31) return null
        if (anio < 1900) return null
        val nacimiento = java.util.Calendar.getInstance().apply {
            set(anio, mes - 1, dia)
        }
        val hoy = java.util.Calendar.getInstance()
        if (nacimiento.after(hoy)) return null
        var edad = hoy.get(java.util.Calendar.YEAR) - nacimiento.get(java.util.Calendar.YEAR)
        if (
            hoy.get(java.util.Calendar.MONTH) < nacimiento.get(java.util.Calendar.MONTH) ||
            (hoy.get(java.util.Calendar.MONTH) == nacimiento.get(java.util.Calendar.MONTH) &&
                    hoy.get(java.util.Calendar.DAY_OF_MONTH) < nacimiento.get(java.util.Calendar.DAY_OF_MONTH))
        ) edad--
        if (edad < 0) null else edad
    } catch (e: Exception) {
        null
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun RegisterPreview() {
    SJL_Alert_v4Theme {
        RegisterPage()
    }
}

@Composable
fun RegisterPage(
    onRegisterSuccess: () -> Unit = {},
    onBackToLogin: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = remember { AppDatabase.getInstance(context) }

    var nombre by remember { mutableStateOf("") }
    var apellido by remember { mutableStateOf("") }
    var dni by remember { mutableStateOf("") }
    var fechaNacimiento by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var direccion by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var confirmarContrasena by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    val edad = calcularEdad(fechaNacimiento)

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

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
        // Blobs decorativos
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
                    modifier = Modifier.size(100.dp),
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
                        text = "Crear cuenta de vecino",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = onSurfaceVariantColor,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // CARD DEL FORMULARIO
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = surfaceColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {

                    Text(
                        text = "Registro",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            color = primaryColor,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Completa tus datos personales",
                        style = MaterialTheme.typography.bodyMedium.copy(color = onSurfaceVariantColor)
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    // ── Nombre y Apellido en fila ──────────────────────────
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            RegisterFieldLabel("NOMBRE", onSurfaceVariantColor)
                            Spacer(modifier = Modifier.height(8.dp))
                            RegisterTextField(
                                value = nombre,
                                onValueChange = { nombre = it },
                                placeholder = "Juan",
                                primaryColor = primaryColor,
                                onSurfaceColor = onSurfaceColor,
                                onSurfaceVariantColor = onSurfaceVariantColor,
                                surfaceContainerLowColor = surfaceContainerLowColor,
                                surfaceContainerHighColor = surfaceContainerHighColor,
                                surfaceContainerHighestColor = surfaceContainerHighestColor,
                                outlineColor = outlineColor,
                                leadingIcon = {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = onSurfaceVariantColor)
                                }
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            RegisterFieldLabel("APELLIDO", onSurfaceVariantColor)
                            Spacer(modifier = Modifier.height(8.dp))
                            RegisterTextField(
                                value = apellido,
                                onValueChange = { apellido = it },
                                placeholder = "Pérez",
                                primaryColor = primaryColor,
                                onSurfaceColor = onSurfaceColor,
                                onSurfaceVariantColor = onSurfaceVariantColor,
                                surfaceContainerLowColor = surfaceContainerLowColor,
                                surfaceContainerHighColor = surfaceContainerHighColor,
                                surfaceContainerHighestColor = surfaceContainerHighestColor,
                                outlineColor = outlineColor
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // ── DNI ────────────────────────────────────────────────
                    RegisterFieldLabel("DNI", onSurfaceVariantColor)
                    Spacer(modifier = Modifier.height(8.dp))
                    RegisterTextField(
                        value = dni,
                        onValueChange = { if (it.length <= 8 && it.all { c -> c.isDigit() }) dni = it },
                        placeholder = "Ej: 70654321",
                        keyboardType = KeyboardType.Number,
                        primaryColor = primaryColor,
                        onSurfaceColor = onSurfaceColor,
                        onSurfaceVariantColor = onSurfaceVariantColor,
                        surfaceContainerLowColor = surfaceContainerLowColor,
                        surfaceContainerHighColor = surfaceContainerHighColor,
                        surfaceContainerHighestColor = surfaceContainerHighestColor,
                        outlineColor = outlineColor,
                        leadingIcon = {
                            Icon(Icons.Default.Badge, contentDescription = null, tint = onSurfaceVariantColor)
                        }
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // ── FECHA DE NACIMIENTO ────────────────────────────────
                    RegisterFieldLabel("FECHA DE NACIMIENTO", onSurfaceVariantColor)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = fechaNacimiento,
                            onValueChange = { input ->
                                val soloDigitos = input.filter { it.isDigit() }.take(8)
                                fechaNacimiento = when {
                                    soloDigitos.length <= 2 -> soloDigitos
                                    soloDigitos.length <= 4 ->
                                        "${soloDigitos.take(2)}/${soloDigitos.drop(2)}"
                                    else ->
                                        "${soloDigitos.take(2)}/${soloDigitos.drop(2).take(2)}/${soloDigitos.drop(4)}"
                                }
                            },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("DD/MM/AAAA", color = outlineColor) },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    tint = onSurfaceVariantColor
                                )
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = primaryColor,
                                unfocusedBorderColor = surfaceContainerHighColor,
                                focusedContainerColor = surfaceContainerHighestColor,
                                unfocusedContainerColor = surfaceContainerLowColor,
                                focusedTextColor = onSurfaceColor,
                                unfocusedTextColor = onSurfaceColor,
                            ),
                            isError = fechaNacimiento.length == 10 && (edad == null || edad < 18)
                        )

                        if (edad != null) {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = if (edad >= 18)
                                    primaryColor.copy(alpha = 0.15f)
                                else
                                    MaterialTheme.colorScheme.errorContainer
                            ) {
                                Text(
                                    text = "$edad años",
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    color = if (edad >= 18) primaryColor else MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }

                    if (fechaNacimiento.length == 10 && edad != null && edad < 18) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Debes ser mayor de 18 años para registrarte",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp
                        )
                    }
                    if (fechaNacimiento.length == 10 && edad == null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Ingresa una fecha válida",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // ── Correo ─────────────────────────────────────────────
                    RegisterFieldLabel("CORREO ELECTRÓNICO", onSurfaceVariantColor)
                    Spacer(modifier = Modifier.height(8.dp))
                    RegisterTextField(
                        value = correo,
                        onValueChange = { correo = it },
                        placeholder = "correo@ejemplo.com",
                        keyboardType = KeyboardType.Email,
                        primaryColor = primaryColor,
                        onSurfaceColor = onSurfaceColor,
                        onSurfaceVariantColor = onSurfaceVariantColor,
                        surfaceContainerLowColor = surfaceContainerLowColor,
                        surfaceContainerHighColor = surfaceContainerHighColor,
                        surfaceContainerHighestColor = surfaceContainerHighestColor,
                        outlineColor = outlineColor,
                        leadingIcon = {
                            Icon(Icons.Default.Email, contentDescription = null, tint = onSurfaceVariantColor)
                        }
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // ── Teléfono ───────────────────────────────────────────
                    RegisterFieldLabel("TELÉFONO", onSurfaceVariantColor)
                    Spacer(modifier = Modifier.height(8.dp))
                    RegisterTextField(
                        value = telefono,
                        onValueChange = { if (it.length <= 9 && it.all { c -> c.isDigit() }) telefono = it },
                        placeholder = "Ej: 987654321",
                        keyboardType = KeyboardType.Phone,
                        primaryColor = primaryColor,
                        onSurfaceColor = onSurfaceColor,
                        onSurfaceVariantColor = onSurfaceVariantColor,
                        surfaceContainerLowColor = surfaceContainerLowColor,
                        surfaceContainerHighColor = surfaceContainerHighColor,
                        surfaceContainerHighestColor = surfaceContainerHighestColor,
                        outlineColor = outlineColor,
                        leadingIcon = {
                            Icon(Icons.Default.Phone, contentDescription = null, tint = onSurfaceVariantColor)
                        }
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // ── Dirección ──────────────────────────────────────────
                    RegisterFieldLabel("DIRECCIÓN (OPCIONAL)", onSurfaceVariantColor)
                    Spacer(modifier = Modifier.height(8.dp))
                    RegisterTextField(
                        value = direccion,
                        onValueChange = { direccion = it },
                        placeholder = "Av. Los Próceres 123",
                        primaryColor = primaryColor,
                        onSurfaceColor = onSurfaceColor,
                        onSurfaceVariantColor = onSurfaceVariantColor,
                        surfaceContainerLowColor = surfaceContainerLowColor,
                        surfaceContainerHighColor = surfaceContainerHighColor,
                        surfaceContainerHighestColor = surfaceContainerHighestColor,
                        outlineColor = outlineColor,
                        leadingIcon = {
                            Icon(Icons.Default.Home, contentDescription = null, tint = onSurfaceVariantColor)
                        }
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // ── Contraseña ─────────────────────────────────────────
                    RegisterFieldLabel("CONTRASEÑA", onSurfaceVariantColor)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = contrasena,
                        onValueChange = { contrasena = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Mínimo 6 caracteres", color = outlineColor) },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = onSurfaceVariantColor)
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null,
                                    tint = outlineColor
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        shape = RoundedCornerShape(12.dp),
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

                    // ── Confirmar Contraseña ───────────────────────────────
                    RegisterFieldLabel("CONFIRMAR CONTRASEÑA", onSurfaceVariantColor)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = confirmarContrasena,
                        onValueChange = { confirmarContrasena = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Repite tu contraseña", color = outlineColor) },
                        leadingIcon = {
                            Icon(Icons.Default.LockOpen, contentDescription = null, tint = onSurfaceVariantColor)
                        },
                        trailingIcon = {
                            IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                Icon(
                                    imageVector = if (confirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null,
                                    tint = outlineColor
                                )
                            }
                        },
                        visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryColor,
                            unfocusedBorderColor = surfaceContainerHighColor,
                            focusedContainerColor = surfaceContainerHighestColor,
                            unfocusedContainerColor = surfaceContainerLowColor,
                            focusedTextColor = onSurfaceColor,
                            unfocusedTextColor = onSurfaceColor,
                        ),
                        isError = confirmarContrasena.isNotEmpty() && contrasena != confirmarContrasena
                    )

                    if (confirmarContrasena.isNotEmpty() && contrasena != confirmarContrasena) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Las contraseñas no coinciden",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp
                        )
                    }

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

                    Spacer(modifier = Modifier.height(28.dp))

                    // ── BOTÓN REGISTRAR ────────────────────────────────────
                    Button(
                        onClick = {
                            errorMessage = when {
                                nombre.isBlank() || apellido.isBlank() ->
                                    "Ingresa tu nombre y apellido"
                                dni.length != 8 ->
                                    "El DNI debe tener 8 dígitos"
                                fechaNacimiento.length < 10 || edad == null ->
                                    "Ingresa tu fecha de nacimiento válida"
                                edad < 18 ->
                                    "Debes ser mayor de 18 años para registrarte"
                                correo.isBlank() || !correo.contains("@") ->
                                    "Ingresa un correo válido"
                                telefono.length < 9 ->
                                    "Ingresa un teléfono válido"
                                contrasena.length < 6 ->
                                    "La contraseña debe tener al menos 6 caracteres"
                                contrasena != confirmarContrasena ->
                                    "Las contraseñas no coinciden"
                                else -> null
                            }

                            if (errorMessage == null) {
                                isLoading = true
                                scope.launch {
                                    try {
                                        val existente = db.usuarioDao().buscarPorDniOCorreo(dni, correo)
                                        if (existente != null) {
                                            errorMessage = "El DNI o correo ya están registrados"
                                            isLoading = false
                                            return@launch
                                        }

                                        val nuevoUsuario = Usuario(
                                            nombre = nombre.trim(),
                                            apellido = apellido.trim(),
                                            dni = dni.trim(),
                                            correo = correo.trim().lowercase(),
                                            telefono = telefono.trim(),
                                            direccion = direccion.trim(),
                                            contrasena = hashSHA256(contrasena),
                                            fechaNacimiento = fechaNacimiento
                                        )

                                        val id = db.usuarioDao().insertarUsuario(nuevoUsuario)
                                        isLoading = false

                                        if (id > 0) {
                                            Toast.makeText(
                                                context,
                                                "¡Cuenta creada exitosamente!",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            onRegisterSuccess()
                                        }
                                    } catch (e: Exception) {
                                        isLoading = false
                                        errorMessage = "Error al guardar: ${e.message}"
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
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = onPrimaryColor,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Crear Cuenta",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "→", fontSize = 16.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    HorizontalDivider(color = surfaceContainerHighColor)
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "¿Ya tienes cuenta?",
                            style = MaterialTheme.typography.bodyMedium.copy(color = onSurfaceVariantColor)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        TextButton(
                            onClick = onBackToLogin,
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                text = "Iniciar Sesión",
                                fontWeight = FontWeight.Bold,
                                color = primaryColor
                            )
                        }
                    }
                }
            }
        }
    }
}

// ----- Composables reutilizables -----

@Composable
private fun RegisterFieldLabel(text: String, color: androidx.compose.ui.graphics.Color) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall.copy(
            color = color,
            letterSpacing = 0.8.sp,
            fontWeight = FontWeight.SemiBold
        )
    )
}

@Composable
private fun RegisterTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    primaryColor: androidx.compose.ui.graphics.Color,
    onSurfaceColor: androidx.compose.ui.graphics.Color,
    onSurfaceVariantColor: androidx.compose.ui.graphics.Color,
    surfaceContainerLowColor: androidx.compose.ui.graphics.Color,
    surfaceContainerHighColor: androidx.compose.ui.graphics.Color,
    surfaceContainerHighestColor: androidx.compose.ui.graphics.Color,
    outlineColor: androidx.compose.ui.graphics.Color,
    leadingIcon: @Composable (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text(placeholder, color = outlineColor) },
        leadingIcon = leadingIcon,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = primaryColor,
            unfocusedBorderColor = surfaceContainerHighColor,
            focusedContainerColor = surfaceContainerHighestColor,
            unfocusedContainerColor = surfaceContainerLowColor,
            focusedTextColor = onSurfaceColor,
            unfocusedTextColor = onSurfaceColor,
        )
    )
}