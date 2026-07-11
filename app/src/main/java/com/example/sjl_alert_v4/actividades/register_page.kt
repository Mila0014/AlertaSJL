package com.example.sjl_alert_v4.actividades

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sjl_alert_v4.R
import com.example.sjl_alert_v4.red.RegistroRequest
import com.example.sjl_alert_v4.red.RetrofitClient
import com.example.sjl_alert_v4.ui.theme.*
import com.example.sjl_alert_v4.utilidades.SuccessToast
import com.example.sjl_alert_v4.utilidades.ToastData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.security.MessageDigest

fun hashSHA256(input: String): String {
    val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
    return bytes.joinToString("") { "%02x".format(it) }
}

fun calcularEdad(fechaTexto: String): Int? {
    if (fechaTexto.length < 10) return null
    return try {
        val partes = fechaTexto.split("/")
        if (partes.size != 3) return null
        val dia  = partes[0].toInt()
        val mes  = partes[1].toInt()
        val anio = partes[2].toInt()
        if (mes < 1 || mes > 12) return null
        if (dia < 1 || dia > 31) return null
        if (anio < 1900) return null
        val nacimiento = java.util.Calendar.getInstance().apply { set(anio, mes - 1, dia) }
        val hoy = java.util.Calendar.getInstance()
        if (nacimiento.after(hoy)) return null
        var edad = hoy.get(java.util.Calendar.YEAR) - nacimiento.get(java.util.Calendar.YEAR)
        if (
            hoy.get(java.util.Calendar.MONTH) < nacimiento.get(java.util.Calendar.MONTH) ||
            (hoy.get(java.util.Calendar.MONTH) == nacimiento.get(java.util.Calendar.MONTH) &&
                    hoy.get(java.util.Calendar.DAY_OF_MONTH) < nacimiento.get(java.util.Calendar.DAY_OF_MONTH))
        ) edad--
        if (edad < 0) null else edad
    } catch (e: Exception) { null }
}

class DateVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val trimmed = if (text.text.length >= 8) text.text.substring(0..7) else text.text
        var out = ""
        for (i in trimmed.indices) {
            out += trimmed[i]
            if (i == 1 || i == 3) out += "/"
        }
        val offsetTranslator = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 1) return offset
                if (offset <= 3) return offset + 1
                if (offset <= 8) return offset + 2
                return 10
            }
            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= 2) return offset
                if (offset <= 5) return offset - 1
                if (offset <= 10) return offset - 2
                return 8
            }
        }
        return TransformedText(AnnotatedString(out), offsetTranslator)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun RegisterPreview() {
    SJL_Alert_v4Theme { RegisterPage() }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterPage(
    onRegisterSuccess: () -> Unit = {},
    onBackToLogin: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope   = rememberCoroutineScope()

    var nombre              by remember { mutableStateOf("") }
    var apellido            by remember { mutableStateOf("") }
    var dni                 by remember { mutableStateOf("") }
    var fechaNacimiento     by remember { mutableStateOf("") }
    
    val datePickerState = rememberDatePickerState()
    var showDatePicker  by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    LaunchedEffect(isPressed) {
        if (isPressed) {
            showDatePicker = true
        }
    }
    var correo              by remember { mutableStateOf("") }
    var telefono            by remember { mutableStateOf("") }
    var direccion           by remember { mutableStateOf("") }
    var contrasena          by remember { mutableStateOf("") }
    var confirmarContrasena by remember { mutableStateOf("") }
    var passwordVisible        by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    val fechaNacimientoFormateada = when {
        fechaNacimiento.length <= 2 -> fechaNacimiento
        fechaNacimiento.length <= 4 -> "${fechaNacimiento.take(2)}/${fechaNacimiento.drop(2)}"
        else -> "${fechaNacimiento.take(2)}/${fechaNacimiento.drop(2).take(2)}/${fechaNacimiento.drop(4)}"
    }
    val edad = calcularEdad(fechaNacimientoFormateada)

    var isLoading    by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var toastRegistro by remember { mutableStateOf<ToastData?>(null) }

    var mostrarDialogoTerminos by remember { mutableStateOf(false) }
    var terminosAceptados      by remember { mutableStateOf(TerminosAceptacion()) }

    val backgroundColor              = MaterialTheme.colorScheme.background
    val primaryColor                 = MaterialTheme.colorScheme.primary
    val onPrimaryColor               = MaterialTheme.colorScheme.onPrimary
    val surfaceColor                 = MaterialTheme.colorScheme.surface
    val onSurfaceColor               = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariantColor        = MaterialTheme.colorScheme.onSurfaceVariant
    val surfaceContainerLowColor     = MaterialTheme.colorScheme.surfaceContainer
    val surfaceContainerHighColor    = MaterialTheme.colorScheme.surfaceContainerHigh
    val surfaceContainerHighestColor = MaterialTheme.colorScheme.surfaceContainerHighest
    val primaryContainerColor        = MaterialTheme.colorScheme.primaryContainer
    val outlineColor                 = MaterialTheme.colorScheme.outline

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
                .statusBarsPadding()
                .navigationBarsPadding()
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
                    painter           = painterResource(id = R.drawable.logo_sjl),
                    contentDescription = "Logo SJL Alerta",
                    modifier          = Modifier.size(100.dp),
                    contentScale      = ContentScale.Fit
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text  = "SJL Alerta",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            color         = primaryColor,
                            fontWeight    = FontWeight.ExtraBold,
                            letterSpacing = (-0.5).sp
                        )
                    )
                    Text(
                        text  = "Crear cuenta de vecino",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color      = onSurfaceVariantColor,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // CARD DEL FORMULARIO
            Card(
                modifier  = Modifier.fillMaxWidth(),
                shape     = RoundedCornerShape(16.dp),
                colors    = CardDefaults.cardColors(containerColor = surfaceColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {

                    Text(
                        text  = "Registro",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            color      = primaryColor,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text  = "Completa tus datos personales",
                        style = MaterialTheme.typography.bodyMedium.copy(color = onSurfaceVariantColor)
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    // Nombre y Apellido
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            RegisterFieldLabel("Nombre", onSurfaceVariantColor)
                            Spacer(modifier = Modifier.height(8.dp))
                            RegisterTextField(
                                value                        = nombre,
                                onValueChange                = { nombre = it },
                                placeholder                  = "Juan",
                                primaryColor                 = primaryColor,
                                onSurfaceColor               = onSurfaceColor,
                                onSurfaceVariantColor        = onSurfaceVariantColor,
                                surfaceContainerLowColor     = surfaceContainerLowColor,
                                surfaceContainerHighColor    = surfaceContainerHighColor,
                                surfaceContainerHighestColor = surfaceContainerHighestColor,
                                outlineColor                 = outlineColor,
                                leadingIcon = {
                                    Icon(Icons.Default.Person, null, tint = onSurfaceVariantColor)
                                }
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            RegisterFieldLabel("Apellido", onSurfaceVariantColor)
                            Spacer(modifier = Modifier.height(8.dp))
                            RegisterTextField(
                                value                        = apellido,
                                onValueChange                = { apellido = it },
                                placeholder                  = "Pérez",
                                primaryColor                 = primaryColor,
                                onSurfaceColor               = onSurfaceColor,
                                onSurfaceVariantColor        = onSurfaceVariantColor,
                                surfaceContainerLowColor     = surfaceContainerLowColor,
                                surfaceContainerHighColor    = surfaceContainerHighColor,
                                surfaceContainerHighestColor = surfaceContainerHighestColor,
                                outlineColor                 = outlineColor
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // DNI
                    RegisterFieldLabel("DNI", onSurfaceVariantColor)
                    Spacer(modifier = Modifier.height(8.dp))
                    RegisterTextField(
                        value                        = dni,
                        onValueChange                = { if (it.length <= 8 && it.all { c -> c.isDigit() }) dni = it },
                        placeholder                  = "Ej: 70654321",
                        keyboardType                 = KeyboardType.Number,
                        primaryColor                 = primaryColor,
                        onSurfaceColor               = onSurfaceColor,
                        onSurfaceVariantColor        = onSurfaceVariantColor,
                        surfaceContainerLowColor     = surfaceContainerLowColor,
                        surfaceContainerHighColor    = surfaceContainerHighColor,
                        surfaceContainerHighestColor = surfaceContainerHighestColor,
                        outlineColor                 = outlineColor,
                        leadingIcon = {
                            Icon(Icons.Default.Badge, null, tint = onSurfaceVariantColor)
                        }
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Fecha de nacimiento
                    RegisterFieldLabel("Fecha de nacimiento", onSurfaceVariantColor)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value         = fechaNacimiento,
                            onValueChange = { },
                            readOnly      = true,
                            interactionSource = interactionSource,
                            visualTransformation = DateVisualTransformation(),
                            modifier      = Modifier.weight(1f),
                            placeholder   = { Text("DD/MM/AAAA", color = outlineColor) },
                            leadingIcon   = {
                                Icon(Icons.Default.CalendarToday, null, tint = onSurfaceVariantColor)
                            },
                            singleLine      = true,
                            shape           = RoundedCornerShape(12.dp),
                            colors          = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor      = primaryColor,
                                unfocusedBorderColor    = surfaceContainerHighColor,
                                focusedContainerColor   = surfaceContainerHighestColor,
                                unfocusedContainerColor = surfaceContainerLowColor,
                                focusedTextColor        = onSurfaceColor,
                                unfocusedTextColor      = onSurfaceColor,
                            ),
                            isError = fechaNacimiento.length == 8 && (edad == null || edad < 18)
                        )
                        if (edad != null) {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = if (edad >= 18) primaryColor.copy(alpha = 0.15f)
                                else MaterialTheme.colorScheme.errorContainer
                            ) {
                                Text(
                                    text     = "$edad años",
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    color    = if (edad >= 18) primaryColor
                                    else MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.Bold,
                                    fontSize   = 14.sp
                                )
                            }
                        }
                    }
                    if (fechaNacimiento.length == 8 && edad != null && edad < 18) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text  = "Debes ser mayor de 18 años para registrarte",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp
                        )
                    }
                    if (fechaNacimiento.length == 8 && edad == null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text  = "Ingresa una fecha válida",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Correo
                    RegisterFieldLabel("Correo electrónico", onSurfaceVariantColor)
                    Spacer(modifier = Modifier.height(8.dp))
                    RegisterTextField(
                        value                        = correo,
                        onValueChange                = { correo = it },
                        placeholder                  = "correo@ejemplo.com",
                        keyboardType                 = KeyboardType.Email,
                        primaryColor                 = primaryColor,
                        onSurfaceColor               = onSurfaceColor,
                        onSurfaceVariantColor        = onSurfaceVariantColor,
                        surfaceContainerLowColor     = surfaceContainerLowColor,
                        surfaceContainerHighColor    = surfaceContainerHighColor,
                        surfaceContainerHighestColor = surfaceContainerHighestColor,
                        outlineColor                 = outlineColor,
                        leadingIcon = {
                            Icon(Icons.Default.Email, null, tint = onSurfaceVariantColor)
                        }
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Teléfono
                    RegisterFieldLabel("Teléfono", onSurfaceVariantColor)
                    Spacer(modifier = Modifier.height(8.dp))
                    RegisterTextField(
                        value                        = telefono,
                        onValueChange                = { if (it.length <= 9 && it.all { c -> c.isDigit() }) telefono = it },
                        placeholder                  = "Ej: 987654321",
                        keyboardType                 = KeyboardType.Phone,
                        primaryColor                 = primaryColor,
                        onSurfaceColor               = onSurfaceColor,
                        onSurfaceVariantColor        = onSurfaceVariantColor,
                        surfaceContainerLowColor     = surfaceContainerLowColor,
                        surfaceContainerHighColor    = surfaceContainerHighColor,
                        surfaceContainerHighestColor = surfaceContainerHighestColor,
                        outlineColor                 = outlineColor,
                        leadingIcon = {
                            Icon(Icons.Default.Phone, null, tint = onSurfaceVariantColor)
                        }
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Dirección
                    RegisterFieldLabel("Dirección (opcional)", onSurfaceVariantColor)
                    Spacer(modifier = Modifier.height(8.dp))
                    RegisterTextField(
                        value                        = direccion,
                        onValueChange                = { direccion = it },
                        placeholder                  = "Av. Los Próceres 123",
                        primaryColor                 = primaryColor,
                        onSurfaceColor               = onSurfaceColor,
                        onSurfaceVariantColor        = onSurfaceVariantColor,
                        surfaceContainerLowColor     = surfaceContainerLowColor,
                        surfaceContainerHighColor    = surfaceContainerHighColor,
                        surfaceContainerHighestColor = surfaceContainerHighestColor,
                        outlineColor                 = outlineColor,
                        leadingIcon = {
                            Icon(Icons.Default.Home, null, tint = onSurfaceVariantColor)
                        }
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Contraseña
                    RegisterFieldLabel("Contraseña", onSurfaceVariantColor)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value         = contrasena,
                        onValueChange = { contrasena = it },
                        modifier      = Modifier.fillMaxWidth(),
                        placeholder   = { Text("Mínimo 6 caracteres", color = outlineColor) },
                        leadingIcon   = {
                            Icon(Icons.Default.Lock, null, tint = onSurfaceVariantColor)
                        },
                        trailingIcon  = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    if (passwordVisible) Icons.Default.VisibilityOff
                                    else Icons.Default.Visibility,
                                    null, tint = outlineColor
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None
                        else PasswordVisualTransformation(),
                        singleLine      = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        shape           = RoundedCornerShape(12.dp),
                        colors          = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor      = primaryColor,
                            unfocusedBorderColor    = surfaceContainerHighColor,
                            focusedContainerColor   = surfaceContainerHighestColor,
                            unfocusedContainerColor = surfaceContainerLowColor,
                            focusedTextColor        = onSurfaceColor,
                            unfocusedTextColor      = onSurfaceColor,
                        )
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Confirmar Contraseña
                    RegisterFieldLabel("Confirmar contraseña", onSurfaceVariantColor)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value         = confirmarContrasena,
                        onValueChange = { confirmarContrasena = it },
                        modifier      = Modifier.fillMaxWidth(),
                        placeholder   = { Text("Repite tu contraseña", color = outlineColor) },
                        leadingIcon   = {
                            Icon(Icons.Default.LockOpen, null, tint = onSurfaceVariantColor)
                        },
                        trailingIcon  = {
                            IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                Icon(
                                    if (confirmPasswordVisible) Icons.Default.VisibilityOff
                                    else Icons.Default.Visibility,
                                    null, tint = outlineColor
                                )
                            }
                        },
                        visualTransformation = if (confirmPasswordVisible) VisualTransformation.None
                        else PasswordVisualTransformation(),
                        singleLine      = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        shape           = RoundedCornerShape(12.dp),
                        colors          = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor      = primaryColor,
                            unfocusedBorderColor    = surfaceContainerHighColor,
                            focusedContainerColor   = surfaceContainerHighestColor,
                            unfocusedContainerColor = surfaceContainerLowColor,
                            focusedTextColor        = onSurfaceColor,
                            unfocusedTextColor      = onSurfaceColor,
                        ),
                        isError = confirmarContrasena.isNotEmpty() && contrasena != confirmarContrasena
                    )
                    if (confirmarContrasena.isNotEmpty() && contrasena != confirmarContrasena) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text  = "Las contraseñas no coinciden",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp
                        )
                    }

                    // Términos y Condiciones
                    Spacer(modifier = Modifier.height(20.dp))
                    HorizontalDivider(color = surfaceContainerHighColor)
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .border(
                                width = 1.dp,
                                color = if (terminosAceptados.puedeRegistrarse)
                                    primaryColor.copy(alpha = 0.5f)
                                else surfaceContainerHighColor,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .background(
                                if (terminosAceptados.puedeRegistrarse)
                                    primaryColor.copy(alpha = 0.07f)
                                else surfaceContainerLowColor
                            )
                            .clickable { mostrarDialogoTerminos = true }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Gavel,
                            contentDescription = null,
                            tint     = if (terminosAceptados.puedeRegistrarse) primaryColor else onSurfaceVariantColor,
                            modifier = Modifier.size(22.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text  = "Términos y Condiciones",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color      = if (terminosAceptados.puedeRegistrarse) primaryColor else onSurfaceColor
                                )
                            )
                            Text(
                                text  = if (terminosAceptados.puedeRegistrarse) "Aceptados ✓" else "Toca para leer y aceptar",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = if (terminosAceptados.puedeRegistrarse) primaryColor else onSurfaceVariantColor
                                )
                            )
                        }
                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = if (terminosAceptados.aceptaTerminos)
                                    primaryColor.copy(alpha = 0.15f)
                                else surfaceContainerHighColor
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = if (terminosAceptados.aceptaTerminos)
                                            Icons.Default.CheckCircle
                                        else Icons.Default.RadioButtonUnchecked,
                                        contentDescription = null,
                                        tint     = if (terminosAceptados.aceptaTerminos) primaryColor else onSurfaceVariantColor,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text  = "T&C",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color      = if (terminosAceptados.aceptaTerminos) primaryColor else onSurfaceVariantColor,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = if (terminosAceptados.aceptaAlmacenamientoDatos)
                                    primaryColor.copy(alpha = 0.15f)
                                else surfaceContainerHighColor
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = if (terminosAceptados.aceptaAlmacenamientoDatos)
                                            Icons.Default.CheckCircle
                                        else Icons.Default.RadioButtonUnchecked,
                                        contentDescription = null,
                                        tint     = if (terminosAceptados.aceptaAlmacenamientoDatos) primaryColor else onSurfaceVariantColor,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text  = "Datos",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color      = if (terminosAceptados.aceptaAlmacenamientoDatos) primaryColor else onSurfaceVariantColor,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                        }
                        Icon(
                            imageVector        = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint     = onSurfaceVariantColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    if (!terminosAceptados.puedeRegistrarse && errorMessage == "terminos") {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text     = "Debes leer y aceptar los Términos y Condiciones para continuar.",
                            color    = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp
                        )
                    }

                    if (errorMessage != null && errorMessage != "terminos") {
                        Spacer(modifier = Modifier.height(12.dp))
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Error,
                                    contentDescription = null,
                                    tint     = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text     = errorMessage!!,
                                    color    = MaterialTheme.colorScheme.onErrorContainer,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    // BOTÓN REGISTRAR
                    Button(
                        onClick = {
                            errorMessage = when {
                                nombre.isBlank() || apellido.isBlank() || dni.isBlank() ||
                                        fechaNacimiento.isBlank() || correo.isBlank() || telefono.isBlank() ||
                                        contrasena.isBlank() || confirmarContrasena.isBlank() ->
                                    "Todos los campos son obligatorios"
                                dni.length != 8 -> "El DNI debe tener 8 dígitos"
                                fechaNacimiento.length < 8 || edad == null ->
                                    "Ingresa tu fecha de nacimiento válida"
                                edad < 18 -> "Debes ser mayor de 18 años para registrarte"
                                !correo.contains("@") -> "Ingresa un correo válido"
                                telefono.length < 9 -> "Ingresa un teléfono válido"
                                contrasena.length < 6 ->
                                    "La contraseña debe tener mínimo 6 caracteres"
                                contrasena != confirmarContrasena ->
                                    "Las contraseñas no coinciden"
                                !terminosAceptados.puedeRegistrarse -> "terminos"
                                else -> null
                            }
                            if (errorMessage == null) {
                                isLoading = true
                                scope.launch(Dispatchers.IO) {
                                    try {
                                        val response = RetrofitClient.usuarioApi.registro(
                                            RegistroRequest(
                                                nombre          = nombre.trim(),
                                                apellido        = apellido.trim(),
                                                dni             = dni.trim(),
                                                correo          = correo.trim().lowercase(),
                                                telefono        = telefono.trim(),
                                                contrasena      = hashSHA256(contrasena),
                                                direccion       = direccion.trim(),
                                                fechaRegistro   = System.currentTimeMillis(),
                                                fechaNacimiento = fechaNacimientoFormateada
                                            )
                                        )
                                        withContext(Dispatchers.Main) {
                                            isLoading = false
                                            when (response.code()) {
                                                201 -> {
                                                    toastRegistro = ToastData(
                                                        icon     = Icons.Default.CheckCircle,
                                                        iconBg   = Color(0xFFE8F5E9),
                                                        iconTint = Color(0xFF2E7D32),
                                                        title    = "¡Cuenta creada!",
                                                        message  = "Registro exitoso. Ya puedes iniciar sesión."
                                                    )
                                                }
                                                409 -> {
                                                    errorMessage = response.body()?.error
                                                        ?: "El usuario ya está registrado"
                                                }
                                                else -> {
                                                    errorMessage = "Error al registrar (código: ${response.code()})"
                                                }
                                            }
                                        }
                                    } catch (e: Exception) {
                                        withContext(Dispatchers.Main) {
                                            isLoading = false
                                            errorMessage = "Error: ${e.message ?: "Sin conexión"}"
                                        }
                                    }
                                }
                            }
                        },
                        modifier  = Modifier.fillMaxWidth().height(54.dp),
                        shape     = RoundedCornerShape(12.dp),
                        colors    = ButtonDefaults.buttonColors(
                            containerColor = primaryColor,
                            contentColor   = onPrimaryColor
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                        enabled   = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier    = Modifier.size(22.dp),
                                color       = onPrimaryColor,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Crear Cuenta", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("→", fontSize = 16.sp)
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
                            text  = "¿Ya tienes cuenta?",
                            style = MaterialTheme.typography.bodyMedium.copy(color = onSurfaceVariantColor)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        TextButton(onClick = onBackToLogin, contentPadding = PaddingValues(0.dp)) {
                            Text("Iniciar Sesión", fontWeight = FontWeight.Bold, color = primaryColor)
                        }
                    }
                }
            }
        }

        // Toast anclado arriba (dentro del Box raíz)
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
        ) {
            SuccessToast(
                toastData = toastRegistro,
                onDismiss = {
                    toastRegistro = null
                    onRegisterSuccess()
                }
            )
        }

        // Diálogo de Términos y Condiciones
        if (mostrarDialogoTerminos) {
            TerminosCondicionesDialog(
                onDismiss = { mostrarDialogoTerminos = false },
                onAceptar = { aceptacion ->
                    terminosAceptados = aceptacion
                    mostrarDialogoTerminos = false
                    if (errorMessage == "terminos") errorMessage = null
                }
            )
        }

        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val cal = java.util.Calendar.getInstance(
                                java.util.TimeZone.getTimeZone("UTC")
                            ).apply { timeInMillis = millis }
                            val dia = cal.get(java.util.Calendar.DAY_OF_MONTH).toString().padStart(2, '0')
                            val mes = (cal.get(java.util.Calendar.MONTH) + 1).toString().padStart(2, '0')
                            val anio = cal.get(java.util.Calendar.YEAR).toString()
                            fechaNacimiento = "$dia$mes$anio"
                        }
                        showDatePicker = false
                    }) {
                        Text("Aceptar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("Cancelar")
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }
    }
}

@Composable
private fun RegisterFieldLabel(text: String, color: Color) {
    Text(
        text  = text,
        style = MaterialTheme.typography.labelSmall.copy(
            color         = color,
            letterSpacing = 0.8.sp,
            fontWeight    = FontWeight.SemiBold
        )
    )
}

@Composable
private fun RegisterTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    primaryColor: Color,
    onSurfaceColor: Color,
    onSurfaceVariantColor: Color,
    surfaceContainerLowColor: Color,
    surfaceContainerHighColor: Color,
    surfaceContainerHighestColor: Color,
    outlineColor: Color,
    leadingIcon: @Composable (() -> Unit)? = null
) {
    OutlinedTextField(
        value         = value,
        onValueChange = onValueChange,
        modifier      = Modifier.fillMaxWidth(),
        placeholder   = { Text(placeholder, color = outlineColor) },
        leadingIcon   = leadingIcon,
        singleLine    = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape  = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor      = primaryColor,
            unfocusedBorderColor    = surfaceContainerHighColor,
            focusedContainerColor   = surfaceContainerHighestColor,
            unfocusedContainerColor = surfaceContainerLowColor,
            focusedTextColor        = onSurfaceColor,
            unfocusedTextColor      = onSurfaceColor,
        )
    )
}