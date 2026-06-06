package com.example.sjl_alert_v4.actividades

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

// ─────────────────────────────────────────────────────────────────────────────
// Data class que expone los estados de aceptación al padre
// ─────────────────────────────────────────────────────────────────────────────
data class TerminosAceptacion(
    val aceptaTerminos: Boolean = false,
    val aceptaAlmacenamientoDatos: Boolean = false
) {
    /** El botón de Registrar solo debe habilitarse cuando ambos son true */
    val puedeRegistrarse: Boolean get() = aceptaTerminos && aceptaAlmacenamientoDatos
}

// ─────────────────────────────────────────────────────────────────────────────
// Diálogo principal de Términos y Condiciones
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun TerminosCondicionesDialog(
    onDismiss: () -> Unit,
    onAceptar: (TerminosAceptacion) -> Unit
) {
    val primaryColor      = MaterialTheme.colorScheme.primary
    val surfaceColor      = MaterialTheme.colorScheme.surface
    val onSurfaceColor    = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant  = MaterialTheme.colorScheme.onSurfaceVariant
    val primaryContainer  = MaterialTheme.colorScheme.primaryContainer
    val outlineColor      = MaterialTheme.colorScheme.outline
    val errorColor        = MaterialTheme.colorScheme.error

    var aceptaTerminos           by remember { mutableStateOf(false) }
    var aceptaAlmacenamiento     by remember { mutableStateOf(false) }
    var mostrarErrorCheckbox     by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false   // forzar lectura antes de salir
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.90f)
                .clip(RoundedCornerShape(20.dp))
                .background(surfaceColor)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {

                // ── Cabecera con gradiente ────────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    primaryColor,
                                    primaryColor.copy(alpha = 0.8f)
                                )
                            )
                        )
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Gavel,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                        Column {
                            Text(
                                text = "Términos y Condiciones",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = "SJL Alerta — Seguridad Ciudadana",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            )
                        }
                    }

                    // Botón de cerrar
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = Color.White
                        )
                    }
                }

                // ── Cuerpo scrolleable ────────────────────────────────────
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    // Fecha de vigencia
                    Text(
                        text = "Última actualización: Junio 2026",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    )

                    // ── Sección 1: Sobre la Aplicación ───────────────────
                    TerminosSeccion(
                        numero = "1",
                        titulo = "Sobre la Aplicación",
                        icono = Icons.Default.Info,
                        primaryColor = primaryColor,
                        primaryContainer = primaryContainer,
                        onSurfaceColor = onSurfaceColor
                    ) {
                        TerminosParrafo(
                            text = "SJL Alerta es una aplicación de seguridad ciudadana desarrollada para los vecinos del " +
                                    "distrito de San Juan de Lurigancho. Su propósito es facilitar el reporte de incidencias " +
                                    "(robos, incendios, personas en estado de ebriedad, entre otros), la comunicación con las " +
                                    "autoridades locales y la mejora de la convivencia en el vecindario.",
                            color = onSurfaceVariant
                        )
                    }

                    // ── Sección 2: Datos Recopilados ──────────────────────
                    TerminosSeccion(
                        numero = "2",
                        titulo = "Datos Recopilados",
                        icono = Icons.Default.DataUsage,
                        primaryColor = primaryColor,
                        primaryContainer = primaryContainer,
                        onSurfaceColor = onSurfaceColor
                    ) {
                        TerminosParrafo(
                            text = "Únicamente recopilamos los datos estrictamente necesarios para el funcionamiento de la aplicación:",
                            color = onSurfaceVariant
                        )
                        TerminosItemLista(
                            items = listOf(
                                "Datos de registro: nombre, apellido, DNI, fecha de nacimiento, correo electrónico, teléfono y dirección.",
                                "Datos de ubicación: GPS para geolocalizar los incidentes reportados.",
                                "Imágenes: fotos adjuntas como evidencia visual del incidente."
                            ),
                            color = onSurfaceVariant,
                            primaryColor = primaryColor
                        )
                        TerminosParrafo(
                            text = "Todos los datos se almacenan de forma segura en Microsoft Azure SQL Database, con cifrado " +
                                    "en reposo y protección contra accesos no autorizados.",
                            color = onSurfaceVariant
                        )
                    }

                    // ── Sección 3: Uso adecuado ───────────────────────────
                    TerminosSeccion(
                        numero = "3",
                        titulo = "Uso Adecuado de la Aplicación",
                        icono = Icons.Default.Shield,
                        primaryColor = primaryColor,
                        primaryContainer = primaryContainer,
                        onSurfaceColor = onSurfaceColor
                    ) {
                        TerminosParrafo(
                            text = "El usuario se compromete a utilizar SJL Alerta únicamente para reportar incidentes reales " +
                                    "(robos, incendios, personas en estado de ebriedad en la vía pública, etc.). No se permite:",
                            color = onSurfaceVariant
                        )
                        TerminosItemLista(
                            items = listOf(
                                "Enviar reportes falsos, engañosos o malintencionados.",
                                "Utilizar la aplicación para acosar, difamar o dañar a otros usuarios.",
                                "Intentar acceder a cuentas de otros vecinos.",
                                "Utilizar la aplicación para actividades ilegales."
                            ),
                            color = onSurfaceVariant,
                            primaryColor = primaryColor
                        )
                        TerminosParrafo(
                            text = "El uso inadecuado de la aplicación puede generar la suspensión temporal o definitiva de la cuenta del usuario.",
                            color = onSurfaceVariant
                        )
                    }

                    // ── Sección 4: Responsabilidad del Usuario ────────────
                    TerminosSeccion(
                        numero = "4",
                        titulo = "Responsabilidad del Usuario",
                        icono = Icons.Default.PersonOutline,
                        primaryColor = primaryColor,
                        primaryContainer = primaryContainer,
                        onSurfaceColor = onSurfaceColor
                    ) {
                        TerminosItemLista(
                            items = listOf(
                                "Veracidad de los reportes: el usuario es el único responsable de la veracidad de los reportes enviados.",
                                "Confidencialidad de la cuenta: el usuario es responsable de mantener segura su contraseña y no compartirla con terceros.",
                                "Notificación: el usuario debe notificar inmediatamente sobre cualquier uso no autorizado de su cuenta."
                            ),
                            color = onSurfaceVariant,
                            primaryColor = primaryColor
                        )
                        TerminosParrafo(
                            text = "SJL Alerta no se hace responsable por el mal uso que se dé a la información reportada ni " +
                                    "por las consecuencias de reportes falsos.",
                            color = onSurfaceVariant
                        )
                    }

                    // ── Sección 5: Privacidad y Protección de Datos ───────
                    TerminosSeccion(
                        numero = "5",
                        titulo = "Privacidad y Protección de Datos",
                        icono = Icons.Default.Lock,
                        primaryColor = primaryColor,
                        primaryContainer = primaryContainer,
                        onSurfaceColor = onSurfaceColor
                    ) {
                        TerminosParrafo(
                            text = "Los datos personales recopilados serán utilizados exclusivamente para:",
                            color = onSurfaceVariant
                        )
                        TerminosItemLista(
                            items = listOf(
                                "Identificar al vecino que realiza el reporte.",
                                "Autenticar el inicio de sesión.",
                                "Geolocalizar los incidentes.",
                                "Contactar al usuario si es necesario."
                            ),
                            color = onSurfaceVariant,
                            primaryColor = primaryColor
                        )
                        TerminosParrafo(
                            text = "Los datos personales no serán compartidos con terceros sin el consentimiento del usuario, " +
                                    "excepto por requerimiento legal de las autoridades competentes.",
                            color = onSurfaceVariant
                        )
                    }

                    // ── Sección 6: Cancelación de la Cuenta ──────────────
                    TerminosSeccion(
                        numero = "6",
                        titulo = "Cancelación de la Cuenta",
                        icono = Icons.Default.PersonOff,
                        primaryColor = primaryColor,
                        primaryContainer = primaryContainer,
                        onSurfaceColor = onSurfaceColor
                    ) {
                        TerminosParrafo(
                            text = "El usuario puede solicitar la cancelación de su cuenta en cualquier momento escribiendo al " +
                                    "correo de soporte. Una vez cancelada la cuenta:",
                            color = onSurfaceVariant
                        )
                        TerminosItemLista(
                            items = listOf(
                                "Se eliminarán sus datos personales de la base de datos.",
                                "Se eliminarán sus reportes asociados.",
                                "El proceso de eliminación se completará dentro de los 30 días posteriores a la solicitud."
                            ),
                            color = onSurfaceVariant,
                            primaryColor = primaryColor
                        )
                    }

                    // ── Sección 7: Modificaciones de los Términos ─────────
                    TerminosSeccion(
                        numero = "7",
                        titulo = "Modificaciones de los Términos",
                        icono = Icons.Default.Edit,
                        primaryColor = primaryColor,
                        primaryContainer = primaryContainer,
                        onSurfaceColor = onSurfaceColor
                    ) {
                        TerminosParrafo(
                            text = "SJL Alerta se reserva el derecho de modificar estos términos y condiciones en cualquier " +
                                    "momento. Los cambios serán notificados a través de la aplicación o por correo electrónico. " +
                                    "El uso continuado de la aplicación después de dichos cambios constituye la aceptación de los nuevos términos.",
                            color = onSurfaceVariant
                        )
                    }

                    // ── Sección 8: Legislación Aplicable ──────────────────
                    TerminosSeccion(
                        numero = "8",
                        titulo = "Legislación Aplicable",
                        icono = Icons.Default.AccountBalance,
                        primaryColor = primaryColor,
                        primaryContainer = primaryContainer,
                        onSurfaceColor = onSurfaceColor
                    ) {
                        TerminosParrafo(
                            text = "Estos términos y condiciones se rigen por las leyes de la República del Perú. Cualquier " +
                                    "disputa relacionada con estos términos será sometida a la jurisdicción de los tribunales " +
                                    "de Lima, Perú.",
                            color = onSurfaceVariant
                        )
                    }

                    // ── Sección 9: Contacto ────────────────────────────────
                    TerminosSeccion(
                        numero = "9",
                        titulo = "Contacto",
                        icono = Icons.Default.ContactMail,
                        primaryColor = primaryColor,
                        primaryContainer = primaryContainer,
                        onSurfaceColor = onSurfaceColor
                    ) {
                        TerminosParrafo(
                            text = "Si el usuario tiene preguntas sobre estos términos y condiciones, puede contactar a los " +
                                    "administradores de SJL Alerta a través del correo electrónico:",
                            color = onSurfaceVariant
                        )
                        Text(
                            text = "📧 soporte@alertasjl.com",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = primaryColor,
                                fontWeight = FontWeight.SemiBold,
                                lineHeight = 20.sp
                            ),
                            modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                        )
                    }

                    // ── Sección 10: Aceptación de los Términos ────────────
                    TerminosSeccion(
                        numero = "10",
                        titulo = "Aceptación de los Términos",
                        icono = Icons.Default.CheckCircle,
                        primaryColor = primaryColor,
                        primaryContainer = primaryContainer,
                        onSurfaceColor = onSurfaceColor
                    ) {
                        TerminosParrafo(
                            text = "Al registrarse en SJL Alerta, el usuario declara haber leído, comprendido y aceptado la " +
                                    "totalidad de los presentes términos y condiciones.",
                            color = onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                }

                // ── Sección de Checkboxes y Botón ─────────────────────────
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(surfaceColor)
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HorizontalDivider(color = outlineColor.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(4.dp))

                    // Error si intenta aceptar sin marcar
                    if (mostrarErrorCheckbox) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.errorContainer)
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = errorColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Debes aceptar ambas casillas para continuar con el registro.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            )
                        }
                    }

                    // ── Checkbox 1: Términos y Condiciones ────────────────
                    TerminosCheckboxRow(
                        checked = aceptaTerminos,
                        onCheckedChange = {
                            aceptaTerminos = it
                            if (it) mostrarErrorCheckbox = false
                        },
                        primaryColor = primaryColor,
                        outlineColor = outlineColor,
                        onSurfaceColor = onSurfaceColor
                    ) {
                        Text(
                            buildAnnotatedString {
                                append("He leído y acepto los ")
                                withStyle(SpanStyle(color = primaryColor, fontWeight = FontWeight.Bold)) {
                                    append("Términos y Condiciones")
                                }
                                append(" de uso de SJL Alerta.")
                            },
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = onSurfaceColor,
                                lineHeight = 18.sp
                            )
                        )
                    }

                    // ── Checkbox 2: Almacenamiento de datos en Azure ───────
                    TerminosCheckboxRow(
                        checked = aceptaAlmacenamiento,
                        onCheckedChange = {
                            aceptaAlmacenamiento = it
                            if (it) mostrarErrorCheckbox = false
                        },
                        primaryColor = primaryColor,
                        outlineColor = outlineColor,
                        onSurfaceColor = onSurfaceColor
                    ) {
                        Text(
                            buildAnnotatedString {
                                append("Autorizo el ")
                                withStyle(SpanStyle(color = primaryColor, fontWeight = FontWeight.Bold)) {
                                    append("registro y almacenamiento")
                                }
                                append(" de mis datos personales en la base de datos de SJL Alerta (Microsoft Azure).")
                            },
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = onSurfaceColor,
                                lineHeight = 18.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // ── Botones de acción ─────────────────────────────────
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = onSurfaceVariant
                            )
                        ) {
                            Text(
                                text = "Cancelar",
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Button(
                            onClick = {
                                if (!aceptaTerminos || !aceptaAlmacenamiento) {
                                    mostrarErrorCheckbox = true
                                } else {
                                    onAceptar(
                                        TerminosAceptacion(
                                            aceptaTerminos = true,
                                            aceptaAlmacenamientoDatos = true
                                        )
                                    )
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = primaryColor,
                                contentColor = Color.White
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Aceptar",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Composable: fila de checkbox con contenido flexible
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun TerminosCheckboxRow(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    primaryColor: Color,
    outlineColor: Color,
    onSurfaceColor: Color,
    content: @Composable () -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = if (checked) primaryColor else outlineColor.copy(alpha = 0.5f),
        animationSpec = tween(200),
        label = "borderColor"
    )
    val bgColor by animateColorAsState(
        targetValue = if (checked) primaryColor.copy(alpha = 0.06f) else Color.Transparent,
        animationSpec = tween(200),
        label = "bgColor"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = 1.5.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .background(bgColor)
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = primaryColor,
                uncheckedColor = outlineColor,
                checkmarkColor = Color.White
            )
        )
        Box(modifier = Modifier.weight(1f)) {
            content()
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Composable: sección con número, ícono y título
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun TerminosSeccion(
    numero: String,
    titulo: String,
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    primaryColor: Color,
    primaryContainer: Color,
    onSurfaceColor: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(primaryContainer.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icono,
                    contentDescription = null,
                    tint = primaryColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                text = "$numero. $titulo",
                style = MaterialTheme.typography.titleSmall.copy(
                    color = onSurfaceColor,
                    fontWeight = FontWeight.Bold
                )
            )
        }
        Column(
            modifier = Modifier.padding(start = 4.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            content = content
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Composable: párrafo de texto normal
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun TerminosParrafo(text: String, color: Color) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall.copy(
            color = color,
            lineHeight = 20.sp,
            textAlign = TextAlign.Justify
        )
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Composable: lista de ítems con viñeta circular
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun TerminosItemLista(
    items: List<String>,
    color: Color,
    primaryColor: Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        items.forEach { item ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "•",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = primaryColor,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.padding(top = 1.dp)
                )
                Text(
                    text = item,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = color,
                        lineHeight = 19.sp
                    ),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}