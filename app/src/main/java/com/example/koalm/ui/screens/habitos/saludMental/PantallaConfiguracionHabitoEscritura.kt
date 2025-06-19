/*  PantallaConfiguracionHabitoEscritura.kt
 *  Pantalla para configurar el hábito de escritura diaria.
 *  Programa notificaciones recurrentes según los días, la hora y la duración especificados.
 */
package com.example.koalm.ui.screens.habitos.saludMental

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import com.example.koalm.R
import com.example.koalm.model.ClaseHabito
import com.example.koalm.model.Habito
import com.example.koalm.model.MetricasHabito
import com.example.koalm.model.ProgresoDiario
import com.example.koalm.model.TipoHabito
import com.example.koalm.repository.HabitoRepository
import com.example.koalm.services.notifications.ReadingNotificationService
import com.example.koalm.services.notifications.WritingNotificationService
import com.example.koalm.services.timers.NotificationService
import com.example.koalm.ui.components.BarraNavegacionInferior
import com.example.koalm.ui.components.ExitoDialogoGuardadoAnimado
import com.example.koalm.ui.components.ValidacionesDialogoAnimado
import com.example.koalm.ui.screens.habitos.personalizados.DiasSeleccionadosResumen
import com.example.koalm.ui.screens.habitos.personalizados.TooltipDialogAyuda
import com.example.koalm.ui.theme.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt
import kotlinx.coroutines.launch
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaConfiguracionHabitoEscritura(
    navController: NavHostController,
    habitoId: String? = null
) {
    val context = LocalContext.current
    val TAG = "PantallaConfiguracionHabito"
    val habitosRepository = remember { HabitoRepository() }
    val scope = rememberCoroutineScope()
    val auth = FirebaseAuth.getInstance()
    val userEmail = FirebaseAuth.getInstance().currentUser?.email
    val esEdicion = habitoId != null

    var mensajeValidacion by remember { mutableStateOf<String?>(null) }

    if (mensajeValidacion != null) {
        ValidacionesDialogoAnimado(
            mensaje = mensajeValidacion!!,
            onDismiss = {
                mensajeValidacion = null
            }
        )
    }

    var mostrarDialogoExito by remember{ mutableStateOf(false) }
    if (mostrarDialogoExito) {
        ExitoDialogoGuardadoAnimado(
            mensaje = "¡Hábito configurado correctamente!",
            onDismiss = {
                mostrarDialogoExito = false
                navController.navigate("salud_mental") {
                    popUpTo("salud_mental") { inclusive = true }
                    launchSingleTop = true
                }
            }
        )
    }

    /* -----------------------------  State  ------------------------------ */
    var descripcion by remember { mutableStateOf("") }
    var notasHabilitadas by remember { mutableStateOf(false) }
    val currentUser = auth.currentUser

    //  Días de la semana (L-Do). Duplicamos "M" para Martes y Miércoles.
    val diasSemana = listOf("L","M","M","J","V","S","D")
    var diasSeleccionados by remember { mutableStateOf(List(7) { false }) }

    //  Duración
    var duracionMin by remember { mutableStateOf(15f) }      // 1-180 min
    val rangoDuracion = 1f..180f

    // Objetivo de páginas
    var objetivoPaginas by remember { mutableStateOf(1) }

    // Cargar la duración guardada del temporizador
    val sharedPreferences = context.getSharedPreferences("AppPreferences", MODE_PRIVATE)
    LaunchedEffect(Unit) {
        val defaultDuration = 15L * 60 * 1000 // 15 minutos en milisegundos
        duracionMin = (sharedPreferences.getLong("writing_timer_duration", defaultDuration) / (60 * 1000)).toFloat()
    }

    //  Hora de notificación
    var hora by remember { 
        mutableStateOf(
            LocalTime.now().plusMinutes(1).withSecond(0).withNano(0)
        ) 
    }
    var mostrarTimePicker by remember { mutableStateOf(false) }

    val habitoEditando = remember { mutableStateOf<Habito?>(null) }

    LaunchedEffect(habitoId) {
        if (habitoId != null) {
            val resultado = habitosRepository.obtenerHabito(habitoId)
            resultado.fold(
                onSuccess = { habito ->
                    habitoEditando.value = habito
                    descripcion = habito.descripcion
                    diasSeleccionados = habito.diasSeleccionados
                    hora = try {
                        LocalTime.parse(habito.hora)
                    } catch (e: Exception) {
                        LocalTime.now().plusMinutes(1).withSecond(0).withNano(0)
                    }
                    duracionMin = habito.duracionMinutos.toFloat()
                    objetivoPaginas = habito.objetivoPaginas ?: 1
                    // Si queremos recuperar más datos de metricasEspecificas, aki
                },
                onFailure = {
                    Log.e("PantallaConfig", "No se pudo cargar el hábito con ID: $habitoId")
                }
            )
        }
    }


    /* --------------------  Permission launcher (POST_NOTIFICATIONS)  -------------------- */
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            scope.launch {
                try {
                    val habito = Habito(
                        id = habitoId ?: "", // Si hay habitoId, es edición
                        titulo = "Escritura",
                        descripcion = descripcion.ifEmpty { context.getString(R.string.notification_default_text) },
                        clase = ClaseHabito.MENTAL,
                        tipo = TipoHabito.ESCRITURA,
                        diasSeleccionados = diasSeleccionados,
                        hora = hora.format(DateTimeFormatter.ofPattern("HH:mm")),
                        duracionMinutos = duracionMin.toInt(),
                        userId = currentUser?.uid,
                        objetivoPaginas = objetivoPaginas,
                        metricasEspecificas = MetricasHabito()
                    )

                    if (habitoId != null) {
                        // EDICIÓN
                        habitosRepository.actualizarHabitoO(habitoId, habito).fold(
                            onSuccess = {
                                Log.d(TAG, "Hábito actualizado exitosamente con ID: $habitoId")
                                Log.d(TAG, "Tipo de hábito: ${habito.tipo}")

                                val notificationService = WritingNotificationService()
                                val notificationTime = LocalDateTime.of(LocalDateTime.now().toLocalDate(), hora)

                                notificationService.cancelNotifications(context)
                                notificationService.scheduleNotification(
                                    context = context,
                                    diasSeleccionados = diasSeleccionados,
                                    hora = notificationTime,
                                    descripcion = descripcion.ifEmpty { context.getString(R.string.reading_notification_default_text) },
                                    durationMinutes = duracionMin.toLong(),
                                    additionalData = mapOf(
                                        "habito_id" to habitoId,
                                        "is_meditation" to false,
                                        "is_reading" to false,
                                        "is_writing" to true,
                                        "is_digital_disconnect" to false
                                    )
                                )
                                mostrarDialogoExito = true
                            },
                            onFailure = { error ->
                                Log.e(TAG, "Error al actualizar hábito: ${error.message}", error)
                                Toast.makeText(
                                    context,
                                    "Error al actualizar hábito: ${error.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        )
                    } else {
                        // CREACIÓN
                        habitosRepository.crearHabito(habito).fold(
                            onSuccess = { nuevoHabitoId ->
                                Log.d(TAG, "Hábito creado exitosamente con ID: $nuevoHabitoId")
                                Log.d(TAG, "Tipo de hábito: ${habito.tipo}")

                                val notificationService = WritingNotificationService()
                                val notificationTime = LocalDateTime.of(LocalDateTime.now().toLocalDate(), hora)

                                Log.d(TAG, "Iniciando servicio de notificaciones")
                                context.startService(Intent(context, NotificationService::class.java))

                                notificationService.scheduleNotification(
                                    context = context,
                                    diasSeleccionados = diasSeleccionados,
                                    hora = notificationTime,
                                    descripcion = descripcion.ifEmpty { context.getString(R.string.reading_notification_default_text) },
                                    durationMinutes = duracionMin.toLong(),
                                    additionalData = mapOf(
                                        "habito_id" to nuevoHabitoId,
                                        "is_meditation" to false,
                                        "is_reading" to false,
                                        "is_writing" to true,
                                        "is_digital_disconnect" to false
                                    )
                                )
                                mostrarDialogoExito = true
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
                    }
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
                title = { Text(stringResource(R.string.titulo_config_escritura)) },
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            /* -----------------------  Tarjeta principal  ----------------------- */
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

                    // Descripción del hábito
                    OutlinedTextField(
                        value = descripcion,
                        onValueChange = { descripcion = it },
                        label = { Text(stringResource(R.string.label_descripcion)) },
                        placeholder = { Text(stringResource(R.string.placeholder_descripcion)) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Objetivo de páginas
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Etiqueta("Objetivo de páginas: *")
                        OutlinedTextField(
                            value = if (objetivoPaginas == 0) "" else objetivoPaginas.toString(),
                            onValueChange = {
                                    nuevoTexto ->
                                nuevoTexto.toIntOrNull()?.let {
                                    if (it > 0) objetivoPaginas = it
                                }
                                if (nuevoTexto.isEmpty()) objetivoPaginas = 0
                            },
                            label = { Text("Páginas por día") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = "Establece cuántas páginas quieres escribir por día.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Selección de días
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ){
                        Etiqueta(stringResource(R.string.label_frecuencia))
                        TooltipDialogAyuda(
                            titulo = "Frecuencia",
                            mensaje = "Selecciona los días de la semana en los que deseas mantener activo tu hábito."
                        )
                    }

                    Column{
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
                                        diasSeleccionados =
                                            diasSeleccionados.toMutableList().also { it[i] = !it[i] }
                                    }
                                )
                            }
                        }
                        DiasSeleccionadosResumen(diasSeleccionados)
                    }

                    // Hora
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ){
                        Etiqueta(stringResource(R.string.label_hora))
                        TooltipDialogAyuda(
                            titulo = "Recordatorio",
                            mensaje = "Establece una notificación personalizada para ayudarte a cumplir tu hábito."
                        )
                    }
                    HoraField(hora) { mostrarTimePicker = true }

                    // Duración
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ){
                            Etiqueta(stringResource(R.string.label_duracion_escritura))
                            TooltipDialogAyuda(
                                titulo = "Duración de la escritura",
                                mensaje = "Configura cuánto tiempo deseas escribir. Al llegar la hora establecida, recibirás una notificación para ayudarte a cumplir con tu hábito."
                            )
                        }
                        Text(
                            text  = formatearDuracion(duracionMin.roundToInt()),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        DurationSlider(
                            value         = duracionMin,
                            onValueChange = { duracionMin = it },
                            valueRange    = rangoDuracion,
                            tickEvery     = 15,          // marca cada 15 min
                            modifier      = Modifier.fillMaxWidth()
                        )
                        Text(
                            text  = stringResource(R.string.hint_duracion),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    /*
                    // Notas
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

                     */
                }
            }

            /* ----------------------------  Card de Notas  --------------------------- */
            /*
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { 
                        try {
                            navController.navigate("notas") {
                                launchSingleTop = true
                                restoreState = true
                                popUpTo("salud_mental") {
                                    saveState = true
                                }
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error al navegar a notas: ${e.message}", e)
                            Toast.makeText(
                                context,
                                "Error al abrir las notas: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, VerdeBorde),
                colors = CardDefaults.cardColors(containerColor = VerdeContenedor)
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.titulo_notas),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            */
            Spacer(Modifier.weight(1f))

            /* ----------------------------  Guardar  --------------------------- */
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            if (!diasSeleccionados.contains(true)) {
                                mensajeValidacion = "Por favor, selecciona al menos un día de la semana."
                                return@Button
                            }

                            if (duracionMin <= 0) {
                                mensajeValidacion = "La duración debe ser mayor a 0 minutos."
                                return@Button
                            }

                            if (objetivoPaginas <= 0 ) {
                                mensajeValidacion = "Por favor, establece cuántas páginas quieres escribir por día."
                                return@Button
                            }


                            val currentUser = auth.currentUser
                            if (currentUser == null) {
                                mensajeValidacion = "Debes iniciar sesión para crear un hábito."
                                return@Button
                            } else {
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }

                        },
                        modifier = Modifier
                            .width(180.dp)
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(stringResource(R.string.boton_guardar), color = MaterialTheme.colorScheme.onPrimary)
                    }

                    if (esEdicion) {
                        // Boton de Cancelar
                        Button(
                            onClick = {
                                navController.navigateUp()
                            },
                            modifier = Modifier
                                .width(180.dp)
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEC615B))
                        ) {
                            Text(
                                stringResource(R.string.boton_cancelar_modificaciones),
                                color = MaterialTheme.colorScheme.onPrimary
                            )

                        }
                    }
                }
            }
        }
    }

    /* ---------------------------  Time Picker  ---------------------------- */
    if (mostrarTimePicker) {
        TimePickerDialog(
            initialTime  = hora,
            onTimePicked = { hora = it },
            onDismiss    = { mostrarTimePicker = false }
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
fun DiaCircle(label: String, selected: Boolean, onClick: () -> Unit) {
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

/**
 * Campo que muestra la hora elegida y abre el `TimePickerDialog`.
 */
@Composable
fun HoraField(hora: LocalTime, onClick: () -> Unit) {
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
                text  = hora.format(DateTimeFormatter.ofPattern("hh:mm a")),
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
                Modifier
                    .fillMaxWidth()
                    .height(trackHeight)
                    .align(Alignment.Center),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment     = Alignment.CenterVertically
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
                    activeTrackColor   = Color.Transparent,
                    inactiveTrackColor = Color.Transparent,
                    activeTickColor    = Color.Transparent,
                    inactiveTickColor  = Color.Transparent,
                    thumbColor         = Color.Transparent
                ),
                modifier = Modifier.fillMaxSize()
            )

            // Thumb visual ligado a la posición actual
            val progress     = (value - valueRange.start) /
                    (valueRange.endInclusive - valueRange.start)
            val thumbOffset  = with(density) { (progress * maxWidthPx).toDp() }

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
fun TimePickerDialog(
    initialTime : LocalTime,
    onTimePicked: (LocalTime) -> Unit,
    onDismiss   : () -> Unit,
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
    min  < 60      -> "$min min"
    min == 60      -> "1 hora"
    min % 60 == 0  -> "${min / 60} h"
    else           -> "${min / 60} h ${min % 60} min"
}
