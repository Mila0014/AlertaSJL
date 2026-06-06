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
                        text = "Última actualización: Junio 2025",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    )

                    TerminosSeccion(
                        numero = "1",
                        titulo = "Sobre la Aplicación",
                        icono = Icons.Default.Info,
                        primaryColor = primaryColor,
                        primaryContainer = primaryContainer,
                        onSurfaceColor = onSurfaceColor
                    ) {
                        TerminosParrafo(
                            text = "SJL Alerta es una aplicación de seguridad ciudadana desarrollada para los vecinos del distrito de San Juan de Lurigancho. " +
                                    "Su propósito es facilitar el reporte de incidencias, la comunicación con las autoridades locales y la mejora de la " +
                                    "convivencia en el vecindario.",
                            color = onSurfaceVariant
                        )
                    }

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
                                "Datos de registro: nombre, apellido, DNI, fecha de nacimiento, correo electrónico, teléfono y dirección (opcional).",
                                "Ubicación geográfica: utilizada exclusivamente al momento de crear un reporte de incidencia para asociarla a una zona geográfica.",
                                "Acceso a la cámara y galería: utilizado únicamente para adjuntar fotografías o videos a los reportes ciudadanos.",
                                "Contraseña: almacenada de forma segura mediante cifrado SHA-256; nunca se almacena en texto plano."
                            ),
                            color = onSurfaceVariant,
                            primaryColor = primaryColor
                        )
                        TerminosParrafo(
                            text = "No recopilamos datos adicionales, no rastreamos tu actividad fuera de la aplicación, ni compartimos tu información con terceros con fines comerciales.",
                            color = onSurfaceVariant
                        )
                    }

                    TerminosSeccion(
                        numero = "3",
                        titulo = "Uso de los Datos",
                        icono = Icons.Default.Shield,
                        primaryColor = primaryColor,
                        primaryContainer = primaryContainer,
                        onSurfaceColor = onSurfaceColor
                    ) {
                        TerminosParrafo(
                            text = "Los datos proporcionados serán utilizados exclusivamente para:",
                            color = onSurfaceVariant
                        )
                        TerminosItemLista(
                            items = listOf(
                                "Identificarte como vecino registrado del distrito.",
                                "Gestionar y dar seguimiento a los reportes de incidencias que realices.",
                                "Notificarte sobre el estado de tus reportes.",
                                "Mejorar los servicios de seguridad ciudadana del municipio."
                            ),
                            color = onSurfaceVariant,
                            primaryColor = primaryColor
                        )
                    }

                    TerminosSeccion(
                        numero = "4",
                        titulo = "Almacenamiento en Azure",
                        icono = Icons.Default.Cloud,
                        primaryColor = primaryColor,
                        primaryContainer = primaryContainer,
                        onSurfaceColor = onSurfaceColor
                    ) {
                        TerminosParrafo(
                            text = "Tu información personal y los reportes que realices serán almacenados de forma segura en servidores de Microsoft Azure, " +
                                    "una plataforma en la nube con altos estándares de seguridad y disponibilidad. " +
                                    "El acceso a dicha información está restringido al personal autorizado del sistema SJL Alerta.",
                            color = onSurfaceVariant
                        )
                        TerminosParrafo(
                            text = "Al aceptar el almacenamiento de tus datos, autorizas expresamente el guardado y tratamiento de tu información personal " +
                                    "en la base de datos de la plataforma con el propósito descrito en estos términos.",
                            color = onSurfaceVariant
                        )
                    }

                    TerminosSeccion(
                        numero = "5",
                        titulo = "Permisos del Dispositivo",
                        icono = Icons.Default.PhoneAndroid,
                        primaryColor = primaryColor,
                        primaryContainer = primaryContainer,
                        onSurfaceColor = onSurfaceColor
                    ) {
                        TerminosParrafo(
                            text = "La aplicación solicitará los siguientes permisos del dispositivo:",
                            color = onSurfaceVariant
                        )
                        TerminosItemLista(
                            items = listOf(
                                "Ubicación (solo mientras la app está en uso): para geolocalizar los reportes.",
                                "Cámara: para capturar evidencia fotográfica al hacer un reporte.",
                                "Almacenamiento / Galería: para seleccionar imágenes existentes al adjuntar evidencia.",
                                "Internet: para sincronizar reportes y datos con el servidor."
                            ),
                            color = onSurfaceVariant,
                            primaryColor = primaryColor
                        )
                        TerminosParrafo(
                            text = "Puedes revocar estos permisos en cualquier momento desde la configuración de tu dispositivo. " +
                                    "Ten en cuenta que revocarlos puede limitar el funcionamiento de ciertas características.",
                            color = onSurfaceVariant
                        )
                    }

                    TerminosSeccion(
                        numero = "6",
                        titulo = "Responsabilidad del Usuario",
                        icono = Icons.Default.PersonOutline,
                        primaryColor = primaryColor,
                        primaryContainer = primaryContainer,
                        onSurfaceColor = onSurfaceColor
                    ) {
                        TerminosParrafo(
                            text = "Al usar SJL Alerta, el usuario se compromete a:",
                            color = onSurfaceVariant
                        )
                        TerminosItemLista(
                            items = listOf(
                                "Proporcionar información veraz y actualizada al momento del registro.",
                                "No realizar reportes falsos, malintencionados o con fines de perjudicar a terceros.",
                                "Mantener la confidencialidad de sus credenciales de acceso.",
                                "Utilizar la aplicación exclusivamente con fines de seguridad ciudadana."
                            ),
                            color = onSurfaceVariant,
                            primaryColor = primaryColor
                        )
                    }

                    TerminosSeccion(
                        numero = "7",
                        titulo = "Menores de Edad",
                        icono = Icons.Default.ChildCare,
                        primaryColor = primaryColor,
                        primaryContainer = primaryContainer,
                        onSurfaceColor = onSurfaceColor
                    ) {
                        TerminosParrafo(
                            text = "La aplicación SJL Alerta está dirigida exclusivamente a mayores de 18 años. " +
                                    "No recopilamos intencionalmente datos de menores de edad. " +
                                    "Si eres menor de edad, no debes registrarte ni usar esta aplicación.",
                            color = onSurfaceVariant
                        )
                    }

                    TerminosSeccion(
                        numero = "8",
                        titulo = "Modificaciones",
                        icono = Icons.Default.Edit,
                        primaryColor = primaryColor,
                        primaryContainer = primaryContainer,
                        onSurfaceColor = onSurfaceColor
                    ) {
                        TerminosParrafo(
                            text = "Nos reservamos el derecho de actualizar estos Términos y Condiciones en cualquier momento. " +
                                    "Las modificaciones serán notificadas a través de la propia aplicación. " +
                                    "El uso continuado de SJL Alerta tras la publicación de cambios implica la aceptación de los nuevos términos.",
                            color = onSurfaceVariant
                        )
                    }

                    TerminosSeccion(
                        numero = "9",
                        titulo = "Contacto",
                        icono = Icons.Default.ContactMail,
                        primaryColor = primaryColor,
                        primaryContainer = primaryContainer,
                        onSurfaceColor = onSurfaceColor
                    ) {
                        TerminosParrafo(
                            text = "Para consultas sobre el tratamiento de tus datos o el funcionamiento de la aplicación, " +
                                    "puedes comunicarte con el equipo de SJL Alerta a través de los canales oficiales de la " +
                                    "Municipalidad de San Juan de Lurigancho.",
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
