package com.example.koalm.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import com.example.koalm.R
import com.example.koalm.model.Habito
import com.example.koalm.model.TipoHabito
import com.example.koalm.repository.HabitoRepository
import com.example.koalm.services.notifications.DigitalDisconnectNotificationService
import com.example.koalm.ui.components.BarraNavegacionInferior
import com.example.koalm.ui.theme.VerdeBorde
import com.example.koalm.ui.theme.VerdeContenedor
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

private const val TAG = "PantallaConfiguracionDesconexion"

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaConfigurarDesconexionDigital(navController: NavHostController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val habitosRepository = remember { HabitoRepository() }
    val auth = FirebaseAuth.getInstance()

    /* -----------------------------  State  ------------------------------ */
    var titulo by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }

    //  Días de la semana (L-Do). Duplicamos "M" para Martes y Miércoles.
    val diasSemana = listOf("L", "M", "M", "J", "V", "S", "D")
    var diasSeleccionados by remember { mutableStateOf(List(7) { false }) }

    //  Duración
    var duracionMin by remember { mutableStateOf(15f) }      // 1-180 min
    val rangoDuracion = 1f..180f

    //  Hora de notificación
    var hora by remember {
        mutableStateOf(
            LocalTime.now().plusMinutes(1).withSecond(0).withNano(0)
        )
    }
    var mostrarTimePicker by remember { mutableStateOf(false) }

    /* --------------------  Permission launcher (POST_NOTIFICATIONS)  -------------------- */
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                Toast.makeText(
                    context,
                    "Debes iniciar sesión para crear un hábito",
                    Toast.LENGTH_LONG
                ).show()
                return@rememberLauncherForActivityResult
            }

            scope.launch {
                try {
                    val habito = Habito(
                        titulo = titulo.ifEmpty { "Desconexión Digital" },
                        descripcion = descripcion.ifEmpty { "Tiempo para desconectar y reconectar contigo mismo" },
                        tipo = TipoHabito.DESCONEXION_DIGITAL,
                        hora = hora.format(DateTimeFormatter.ofPattern("HH:mm")),
                        diasSeleccionados = diasSeleccionados,
                        duracionMinutos = duracionMin.roundToInt(),
                        notasHabilitadas = false,
                        userId = currentUser.uid
                    )

                    habitosRepository.crearHabito(habito).fold(
                        onSuccess = { habitoId ->
                            Log.d(TAG, "Hábito creado exitosamente con ID: $habitoId")
                            
                            // Programar notificaciones
                            val notificationService = DigitalDisconnectNotificationService()
                            val notificationTime = LocalDateTime.of(LocalDateTime.now().toLocalDate(), hora)
                            
                            notificationService.scheduleNotification(
                                context = context,
                                diasSeleccionados = diasSeleccionados,
                                hora = notificationTime,
                                descripcion = descripcion.ifEmpty { context.getString(R.string.digital_disconnect_notification_default_text) },
                                durationMinutes = duracionMin.toLong(),
                                additionalData = mapOf(
                                    "habitoId" to habitoId,
                                    "titulo" to habito.titulo
                                )
                            )

                            Toast.makeText(
                                context,
                                "Hábito creado y notificaciones programadas correctamente",
                                Toast.LENGTH_SHORT
                            ).show()
                            navController.navigateUp()
                        },
                        onFailure = { error ->
                            Log.e(TAG, "Error al crear hábito: ${error.message}", error)
                            Toast.makeText(
                                context,
                                "Error al crear hábito: ${error.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Error inesperado: ${e.message}", e)
                    Toast.makeText(
                        context,
                        "Error inesperado: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        } else {
            Toast.makeText(
                context,
                context.getString(R.string.error_notification_permission),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    /* ----------------------------------  UI  ---------------------------------- */
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configurar hábito de desconexión digital") },
                navigationIcon = {
                    IconButton(onClick = navController::navigateUp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        },
        bottomBar = { BarraNavegacionInferior(navController, "configurar_habito") }
    ) { innerPadding ->

        Column(
            Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            /* -----------------------  Tarjeta principal  ----------------------- */
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, VerdeBorde),
                colors = CardDefaults.cardColors(containerColor = VerdeContenedor)
            ) {
                Column(
                    Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {

                    // Título del hábito
                    OutlinedTextField(
                        value = titulo,
                        onValueChange = { titulo = it },
                        label = { Text("Título del hábito") },
                        placeholder = { Text("Desconexión Digital") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Descripción del hábito
                    OutlinedTextField(
                        value = descripcion,
                        onValueChange = { descripcion = it },
                        label = { Text("Añadir descripción") },
                        placeholder = { Text("Desconectar para reconectar... contigo mismo.") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Selección de días
                    Etiqueta("Frecuencia:")
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        diasSemana.forEachIndexed { i, d ->
                            DiaCircleDesconexion(
                                label = d,
                                selected = diasSeleccionados[i],
                                onClick = {
                                    diasSeleccionados =
                                        diasSeleccionados.toMutableList().also { list -> list[i] = !list[i] }
                                }
                            )
                        }
                    }

                    // Hora
                    Etiqueta("Hora del recordatorio:")
                    HoraFieldDesconexion(hora) { mostrarTimePicker = true }

                    // Duración
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Etiqueta("Duración de la desconexión:")
                        Text(
                            text = formatearDuracion(duracionMin.roundToInt()),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        DurationSliderDesconexion(
                            value = duracionMin,
                            onValueChange = { newValue: Float -> duracionMin = newValue },
                            valueRange = rangoDuracion,
                            tickEvery = 15,          // marca cada 15 min
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            /* ----------------------------  Guardar  --------------------------- */
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = {
                        if (!diasSeleccionados.any { it }) {
                            Toast.makeText(
                                context,
                                "Por favor selecciona al menos un día",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@Button
                        }

                        if (ContextCompat.checkSelfPermission(
                                context, Manifest.permission.POST_NOTIFICATIONS
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    },
                    modifier = Modifier
                        .width(200.dp)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Guardar", color = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }
    }

    /* ---------------------------  Time Picker  ---------------------------- */
    if (mostrarTimePicker) {
        TimePickerDialogDesconexion(
            initialTime = hora,
            onTimePicked = { newTime: LocalTime -> hora = newTime },
            onDismiss = { mostrarTimePicker = false }
        )
    }
}

/* ─────────────────────────────────  COMPONENTES  ────────────────────────────── */

@Composable
private fun Etiqueta(texto: String) = Text(
    text = texto,
    style = MaterialTheme.typography.titleMedium,
    fontWeight = FontWeight.Medium
)

/**
 * Muestra un día en forma de círculo seleccionable.
 */
@Composable
private fun DiaCircleDesconexion(label: String, selected: Boolean, onClick: () -> Unit) {
    val bg = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent
    val borderColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
    val textColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        Modifier
            .size(40.dp)
            .clip(CircleShape)
            .border(1.dp, borderColor, CircleShape)
            .background(bg)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = textColor, style = MaterialTheme.typography.labelLarge)
    }
}

/**
 * Campo que muestra la hora elegida y abre el `TimePickerDialog`.
 */
@Composable
private fun HoraFieldDesconexion(hora: LocalTime, onClick: () -> Unit) {
    Surface(
        tonalElevation = 0.dp,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Edit, contentDescription = null,
                tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(8.dp))
            Text(
                text = hora.format(DateTimeFormatter.ofPattern("hh:mm a")),
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(Modifier.weight(1f))
            Icon(Icons.Default.Schedule, contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/* ──────────────────────────  SLIDER «PIXEL STYLE»  ─────────────────────────── */

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
private fun DurationSliderDesconexion(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    tickEvery: Int,
    modifier: Modifier = Modifier,
    trackHeight: Dp = 16.dp,
    thumbWidth: Dp = 4.dp,
) {
    val density = LocalDensity.current
    val haptics = LocalHapticFeedback.current

    BoxWithConstraints(modifier = modifier) {
        val maxWidthPx = with(density) { maxWidth.toPx() }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(trackHeight + 24.dp)
        ) {
            // Track
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(trackHeight)
                    .clip(RoundedCornerShape(trackHeight / 2))
                    .background(MaterialTheme.colorScheme.primary)
                    .align(Alignment.Center)
            )

            // Ticks
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(trackHeight)
                    .align(Alignment.Center),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val tickCount =
                    ((valueRange.endInclusive - valueRange.start) / tickEvery).toInt() + 1
                repeat(tickCount) {
                    Box(
                        Modifier
                            .size(trackHeight * 0.3f)
                            .clip(CircleShape)
                            .background(
                                MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                            )
                    )
                }
            }

            // Slider "real" (transparente, continuo)
            Slider(
                value = value,
                onValueChange = {
                    onValueChange(it)
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                },
                valueRange = valueRange,
                steps = 0, // continuo
                colors = SliderDefaults.colors(
                    activeTrackColor = Color.Transparent,
                    inactiveTrackColor = Color.Transparent,
                    activeTickColor = Color.Transparent,
                    inactiveTickColor = Color.Transparent,
                    thumbColor = Color.Transparent
                ),
                modifier = Modifier.fillMaxSize()
            )

            // Thumb visual ligado a la posición actual
            val progress = (value - valueRange.start) /
                    (valueRange.endInclusive - valueRange.start)
            val thumbOffset = with(density) { (progress * maxWidthPx).toDp() }

            Box(
                Modifier
                    .width(thumbWidth)
                    .height(trackHeight + 24.dp)
                    .align(Alignment.CenterStart)
                    .offset(x = thumbOffset)
                    .clip(RoundedCornerShape(thumbWidth))
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
    }
}

/* ────────────────────────────  TIME PICKER  ──────────────────────────────── */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialogDesconexion(
    initialTime: LocalTime,
    onTimePicked: (LocalTime) -> Unit,
    onDismiss: () -> Unit,
) {
    val state = rememberTimePickerState(
        initialTime.hour, initialTime.minute, is24Hour = false
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                onTimePicked(LocalTime.of(state.hour, state.minute))
                onDismiss()
            }) { Text(stringResource(android.R.string.ok).uppercase()) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(android.R.string.cancel).uppercase())
            }
        },
        text = { TimePicker(state, modifier = Modifier.fillMaxWidth()) }
    )
}

/* ────────────────────────────  HELPERS  ─────────────────────────────────── */

private fun formatearDuracion(min: Int): String = when {
    min < 60 -> "$min min"
    min == 60 -> "1 hora"
    min % 60 == 0 -> "${min / 60} h"
    else -> "${min / 60} h ${min % 60} min"
}
