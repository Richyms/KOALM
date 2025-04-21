/*  PantallaConfiguracionHabitoEscritura.kt  */
package com.example.koalm.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
// import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
// import androidx.compose.ui.geometry.CornerRadius
// import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
// import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
// import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
// import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.koalm.R
import com.example.koalm.services.NotificationService
import com.example.koalm.ui.components.BarraNavegacionInferior
import com.example.koalm.ui.theme.VerdeBorde
import com.example.koalm.ui.theme.VerdeContenedor
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt
import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import android.util.Log
import android.content.Intent

/* foundation */
// import androidx.compose.foundation.Canvas          // ←  dibujar el track
import androidx.compose.foundation.clickable       // ←  .clickable() que reporta error
import androidx.compose.material.icons.automirrored.filled.ArrowBack

/* ui */
// import androidx.compose.ui.geometry.Size
// import androidx.compose.ui.unit.IntOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaConfiguracionHabitoEscritura(navController: NavHostController) {
    val context = LocalContext.current
    val TAG = "PantallaConfiguracion"

    //------------------------------ Estados --------------------------------
    var descripcion       by remember { mutableStateOf("") }
    var notasHabilitadas  by remember { mutableStateOf(false) }
    val diasSemana        = listOf("L","M","M","J","V","S","D")
    var diasSeleccionados by remember { mutableStateOf(List(7){false}) }

    /*  Duración  */
    var duracionMin by remember { mutableStateOf(15f) }    // 1‑180 min
    val rangoDuracion = 1f..180f

    /*  Hora  */
    var hora                 by remember { mutableStateOf(LocalTime.of(22,0)) }
    var mostrarTimePicker    by remember { mutableStateOf(false) }

    // Launcher para solicitar permisos
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        Log.d(TAG, "Permiso de notificación: $isGranted")
        if (isGranted) {
            // Programar notificación
            val notificationService = NotificationService()
            val now = LocalDateTime.now()
            val notificationTime = LocalDateTime.of(
                now.toLocalDate(),
                hora
            )
            
            Log.d(TAG, "Iniciando servicio de notificaciones")
            context.startService(Intent(context, NotificationService::class.java))
            
            notificationService.scheduleNotification(
                context = context,
                diasSeleccionados = diasSeleccionados,
                hora = notificationTime,
                descripcion = descripcion.ifEmpty { context.getString(R.string.notification_default_text) },
                durationMinutes = duracionMin.toLong()
            )
            
            Toast.makeText(context, context.getString(R.string.success_notifications_scheduled), Toast.LENGTH_SHORT).show()
            navController.navigateUp()
        } else {
            Toast.makeText(context, context.getString(R.string.error_notification_permission), Toast.LENGTH_LONG).show()
        }
    }

    //------------------------------ UI -------------------------------------
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.titulo_config_escritura)) },
                navigationIcon = {
                    IconButton(onClick = navController::navigateUp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        },
        bottomBar = {
            BarraNavegacionInferior(navController,"configurar_habito")
        }
    ) { innerPadding ->

        Column(
            Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            //--------------------- Tarjeta principal ----------------------
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape   = RoundedCornerShape(16.dp),
                border  = BorderStroke(1.dp, VerdeBorde),
                colors  = CardDefaults.cardColors(containerColor = VerdeContenedor)
            ) {
                Column(
                    Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {

                    /*  Descripción  */
                    OutlinedTextField(
                        value = descripcion,
                        onValueChange = { descripcion = it },
                        label = { Text(stringResource(R.string.label_descripcion)) },
                        placeholder = { Text(stringResource(R.string.placeholder_descripcion)) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    /*  Días  */
                    Text(
                        text = stringResource(R.string.label_frecuencia),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        diasSemana.forEachIndexed { i, d ->
                            DiaCircle(
                                label = d,
                                selected = diasSeleccionados[i],
                                onClick  = {
                                    diasSeleccionados = diasSeleccionados.toMutableList()
                                        .also { it[i] = !it[i] }
                                }
                            )
                        }
                    }

                    /*  Hora  */
                    Text(
                        text = stringResource(R.string.label_hora),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    HoraField(hora) { mostrarTimePicker = true }

                    /*  Duración (Slider personalizado)  */
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = stringResource(R.string.label_duracion_escritura),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = formatearDuracion(duracionMin.roundToInt()),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        DurationSlider(
                            value        = duracionMin,
                            onValueChange = { duracionMin = it },
                            valueRange    = rangoDuracion,
                            tickEvery     = 15,           // marca cada 15 min
                            modifier      = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = "Selecciona el tiempo que quieres que dure tu hábito de escritura",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    /*  Switch  */
                    Row(
                        Modifier.fillMaxWidth(),
                        Arrangement.SpaceBetween,
                        Alignment.CenterVertically
                    ) {
                        Text(stringResource(R.string.label_notas))
                        Switch(
                            checked = notasHabilitadas,
                            onCheckedChange = { notasHabilitadas = it }
                        )
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            /*  Guardar  */
            Button(
                onClick = { 
                    // Verificar si hay días seleccionados
                    if (!diasSeleccionados.any { it }) {
                        Toast.makeText(context, context.getString(R.string.error_no_days_selected), Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    Log.d(TAG, "Guardando configuración de notificaciones")
                    Log.d(TAG, "Días seleccionados: $diasSeleccionados")
                    Log.d(TAG, "Hora: $hora")
                    Log.d(TAG, "Descripción: $descripcion")

                    // Verificar permisos de notificación
                    when {
                        ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) == PackageManager.PERMISSION_GRANTED -> {
                            Log.d(TAG, "Permiso de notificación ya concedido")
                            // Ya tenemos permiso, programar notificación
                            val notificationService = NotificationService()
                            val now = LocalDateTime.now()
                            val notificationTime = LocalDateTime.of(
                                now.toLocalDate(),
                                hora
                            )
                            
                            Log.d(TAG, "Iniciando servicio de notificaciones")
                            context.startService(Intent(context, NotificationService::class.java))
                            
                            notificationService.scheduleNotification(
                                context = context,
                                diasSeleccionados = diasSeleccionados,
                                hora = notificationTime,
                                descripcion = descripcion.ifEmpty { context.getString(R.string.notification_default_text) },
                                durationMinutes = duracionMin.toLong()
                            )
                            
                            Toast.makeText(context, context.getString(R.string.success_notifications_scheduled), Toast.LENGTH_SHORT).show()
                            navController.navigateUp()
                        }
                        else -> {
                            Log.d(TAG, "Solicitando permiso de notificación")
                            // Solicitar permiso
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.boton_guardar))
            }
        }
    }

    //------------------------ Time  Picker ------------------------------
    if (mostrarTimePicker) {
        TimePickerDialog(
            initialTime  = hora,
            onTimePicked = { hora = it },
            onDismiss    = { mostrarTimePicker = false }
        )
    }
}

/*────────────────────────────────  COMPONENTES  ─────────────────────────────*/
@Composable
private fun DiaCircle(label: String, selected: Boolean, onClick: () -> Unit) {
    val bg          = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent
    val borderColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
    val textColor   = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant

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

@Composable
private fun HoraField(hora: LocalTime, onClick: () -> Unit) {
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
            Icon(Icons.Default.Edit, null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(8.dp))
            Text(
                text = hora.format(DateTimeFormatter.ofPattern("hh:mm a")),
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(Modifier.weight(1f))
            Icon(Icons.Default.Schedule, null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/*─────────────────────────────  SLIDER “PIXEL STYLE”  ───────────────────────*/
@Composable
fun DurationSlider(
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
                modifier = Modifier
                    .fillMaxWidth()
                    .height(trackHeight)
                    .align(Alignment.Center),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val tickCount = ((valueRange.endInclusive - valueRange.start) / tickEvery).toInt() + 1
                repeat(tickCount) {
                    Box(
                        modifier = Modifier
                            .size(trackHeight * 0.3f)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f))
                    )
                }
            }

            // Slider real — libre y con haptics
            Slider(
                value = value,
                onValueChange = {
                    onValueChange(it)
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                },
                valueRange = valueRange,
                steps = 0, // ← para que sea continuo
                colors = SliderDefaults.colors(
                    activeTrackColor = Color.Transparent,
                    inactiveTrackColor = Color.Transparent,
                    activeTickColor = Color.Transparent,
                    inactiveTickColor = Color.Transparent,
                    thumbColor = Color.Transparent
                ),
                modifier = Modifier
                    .fillMaxSize()
            )

            // Thumb visual, atado a la posición actual
            val progress = (value - valueRange.start) / (valueRange.endInclusive - valueRange.start)
            val thumbOffset = with(density) { (progress * maxWidthPx).toDp() }
            
            Box(
                modifier = Modifier
                    .width(thumbWidth)
                    .height(trackHeight + 24.dp) // Aumentado a 24.dp para hacer el thumb más largo
                    .align(Alignment.CenterStart)
                    .offset(x = thumbOffset)
                    .clip(RoundedCornerShape(thumbWidth))
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
    }
}


/*─────────────────────────────  TIME PICKER  ───────────────────────────────*/
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    initialTime : LocalTime,
    onTimePicked: (LocalTime) -> Unit,
    onDismiss   : () -> Unit,
) {
    val state = rememberTimePickerState(initialTime.hour, initialTime.minute, false)

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                onTimePicked(LocalTime.of(state.hour, state.minute)); onDismiss()
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

/*──────────────────────────  HELPERS  ─────────────────────────────────────*/
private fun formatearDuracion(min: Int): String = when {
    min < 60           -> "$min minutos"
    min == 60          -> "1 hora"
    min % 60 == 0      -> "${min/60} horas"
    else               -> "${min/60} horas ${min%60} min"
}
