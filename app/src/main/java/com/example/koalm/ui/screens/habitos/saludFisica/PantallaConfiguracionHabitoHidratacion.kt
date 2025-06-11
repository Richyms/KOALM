/*  PantallaConfiguracionHabitoHidratacion.kt  */
package com.example.koalm.ui.screens.habitos.saludFisica

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
// import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
// import androidx.compose.material.icons.filled.ArrowBack
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
import com.example.koalm.ui.components.BarraNavegacionInferior
import com.example.koalm.ui.theme.VerdeBorde
import com.example.koalm.ui.theme.VerdeContenedor
import java.time.LocalTime
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.ui.text.input.KeyboardType
import com.example.koalm.ui.screens.habitos.saludMental.HoraField
import com.example.koalm.ui.screens.habitos.saludMental.TimePickerDialog

import androidx.compose.runtime.rememberCoroutineScope
import androidx.core.content.ContextCompat


import com.example.koalm.model.ClaseHabito
import com.example.koalm.model.Habito
import com.example.koalm.model.TipoHabito
import com.example.koalm.services.timers.NotificationService
import com.google.firebase.auth.FirebaseAuth

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.tasks.await
import java.time.LocalDateTime
import android.content.Intent
import android.Manifest
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.koalm.data.HabitosRepository
import com.example.koalm.model.MetricasHabito
import com.example.koalm.repository.HabitoRepository
import java.time.LocalDate





//@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaConfiguracionHabitoHidratacion (navController: NavHostController) {
    val context = LocalContext.current

    //------------------------------ Estados --------------------------------
    var descripcion       by remember { mutableStateOf("") }
    var recordatorios  by remember { mutableStateOf(false) }

    /*  Barra  */
    var cantlitros by remember { mutableStateOf(2f) } //2 litros por defecto
    val rangoLitros = 0.5f..10f // medio litro a 10 litros

    /* Horas */
    var horaIni         by remember { mutableStateOf(LocalTime.of(8,0)) }
    var mostrarTimePickerIni    by remember { mutableStateOf(false) }
    var horaFin         by remember { mutableStateOf(LocalTime.of(22,0)) }
    var mostrarTimePickerFin    by remember { mutableStateOf(false) }

    /* Recordatorios */
    var numeroRecordatorios by remember { mutableStateOf(10) }

    /*Frecuencia de recodatorios*/
    var minutosFrecuencia by remember { mutableStateOf(30) }
    var frecuenciaActiva by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid ?: "anonimo"  // Solo como fallback
    val diasSeleccionados = List(7) { true }  // Todos los días activados por default
    val formatter = DateTimeFormatter.ofPattern("HH:mm")
    var horarios = remember { mutableStateListOf<String>() }
    var isLoading by remember { mutableStateOf(false) }
    val habitosRepository = remember { HabitoRepository() }


    fun guardarHabitoHidratacion() {
        Log.d("GuardarHabito", "Función iniciar guardado")
        scope.launch {
            try {
                val userId = auth.currentUser?.uid
                    ?: throw Exception("Usuario no autenticado")

                isLoading = true

                // Generar lista de horarios según inicio, fin y frecuencia
                horarios.clear()
                val start = horaIni.toSecondOfDay() / 60
                val end = horaFin.toSecondOfDay() / 60
                val intervalo = if (frecuenciaActiva) minutosFrecuencia else 60
                var minuto = start
                while (minuto <= end) {
                    val h = minuto / 60
                    val m = minuto % 60
                    horarios.add(String.format("%02d:%02d", h, m))
                    minuto += intervalo
                }

                // Crear objeto Habito con tipo HIDRATACION
                val nuevoHabito = Habito(
                    titulo = "Hidratación",
                    descripcion = descripcion.ifEmpty { "Recordatorios para beber agua" },
                    clase = ClaseHabito.FISICO,
                    tipo = TipoHabito.HIDRATACION,
                    diasSeleccionados = List(7) { true }, // Todos los días
                    hora = horarios.firstOrNull() ?: "",
                    horarios = horarios.toList(),
                    duracionMinutos = 5,
                    userId = userId,
                    fechaCreacion = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME),
                    fechaModificacion = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME),
                    metricasEspecificas = MetricasHabito(mililitrosBebidos = (cantlitros * 1000).toInt()) // Opcional: info litros
                )

                // Guardar en Firebase
                habitosRepository.crearHabito(nuevoHabito).onSuccess { habitoId ->
                    // Programar notificaciones para cada horario
                    val notificationService = NotificationService()
                    context.startService(Intent(context, NotificationService::class.java))

                    horarios.forEachIndexed { index, horaStr ->
                        val hora = LocalTime.parse(horaStr, DateTimeFormatter.ofPattern("HH:mm"))
                        val notificationTime = LocalDateTime.of(LocalDate.now(), hora)

                        notificationService.scheduleNotification(
                            context = context,
                            habitoId = "$habitoId-$index", // ID único por notificación
                            diasSeleccionados = nuevoHabito.diasSeleccionados,
                            hora = notificationTime,
                            descripcion = "Hora de hidratarse 💧",
                            durationMinutes = 0,
                            isAlimentation = false,
                            isSleeping = false,
                            isHidratation = true
                        )
                    }

                    Toast.makeText(context, "Hábito de hidratación guardado con éxito", Toast.LENGTH_LONG).show()
                    navController.navigateUp()

                }.onFailure { e ->
                    Toast.makeText(context, "Error al guardar hábito: ${e.message}", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                isLoading = false
            }
        }
    }


    val permissionLauncherHydratation = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            guardarHabitoHidratacion()
        } else {
            Toast.makeText(
                context,
                "Permiso de notificaciones denegado",
                Toast.LENGTH_SHORT
            ).show()
        }
    }






    //--------------------------- UI --------------------------------------
    Scaffold(
        topBar = {
            TopAppBar(
            title = { Text(stringResource(R.string.titulo_config_hidratacion)) },
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            //---------- Tarjeta Principal -----------
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape   = RoundedCornerShape(16.dp),
                border  = BorderStroke(1.dp, VerdeBorde),
                colors  = CardDefaults.cardColors(containerColor = VerdeContenedor)
            ){
                Column(
                Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    /*  Descripción  */
                    OutlinedTextField(
                        value = descripcion,
                        onValueChange = { descripcion = it },
                        label = { Text(stringResource(R.string.label_descripcion)) },
                        placeholder = { Text(stringResource(R.string.placeholder_descripcion_hidratacion)) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    /*  Duración (Slider personalizado)  */
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = stringResource(R.string.label_cantidad_litros),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = formatearLitros(cantlitros.toDouble()),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        DurationSliderHidra(
                            value        = cantlitros,
                            onValueChange = { cantlitros = it },
                            valueRange    = rangoLitros,
                            tickEvery     = 1,           // marca cada litro
                            modifier      = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = "Selecciona la cantidad de litros diarios a tomar",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    //------- Horas de notificaciones ------------------
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = stringResource(R.string.label_hora_hidratacion),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Hora de inicio
                            Box(modifier = Modifier.weight(1f)){
                                HoraField(hora = horaIni) { mostrarTimePickerIni = true }
                            }
                            // Hora de fin
                            Box(modifier = Modifier.weight(1f)) {
                                HoraField(hora = horaFin) { mostrarTimePickerFin = true }
                            }
                        }
                    }

                    /* Recordatorio Hidratación */
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = stringResource(R.string.label_recordatorio_hidratación),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = numeroRecordatorios.toString(),
                                onValueChange = {
                                    val valor = it.toIntOrNull()
                                    if (valor != null) numeroRecordatorios = valor
                                },
                                modifier = Modifier.width(70.dp), // ancho reducido
                                singleLine = true,
                                label = { Text("Num.") },
                                shape = RoundedCornerShape(8.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )

                            Text(stringResource(R.string.label_recordatorios_agua))

                            Switch(
                                checked = recordatorios,
                                onCheckedChange = { recordatorios = it }
                            )
                        }
                    }

                    /* Frecuencua Recordatorios */
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = stringResource(R.string.label_recordatorio_hidratación_f),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = stringResource(R.string.label_recordatorio_hidratación_t),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "Cada")

                                Spacer(modifier = Modifier.width(8.dp))

                                OutlinedTextField(
                                    value = minutosFrecuencia.toString(),
                                    onValueChange = {
                                        val valor = it.toIntOrNull()
                                        if (valor != null) minutosFrecuencia = valor
                                    },
                                    modifier = Modifier.width(70.dp),
                                    singleLine = true,
                                    shape = RoundedCornerShape(8.dp),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    label = { Text("Min") }
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Text(text = "minutos.")
                            }

                            Switch(
                                checked = frecuenciaActiva,
                                onCheckedChange = { frecuenciaActiva = it }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = {
                        if (ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {
                            guardarHabitoHidratacion()
                        } else {
                            permissionLauncherHydratation.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    },
                    modifier = Modifier
                        .width(200.dp)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    enabled = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.boton_guardar),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }
    }



    if (mostrarTimePickerIni) {
        TimePickerDialog(
            initialTime = horaIni,
            onTimePicked = { horaIni = it },
            onDismiss = { mostrarTimePickerIni = false }
        )
    }

    if (mostrarTimePickerFin) {
        TimePickerDialog(
            initialTime = horaFin,
            onTimePicked = { horaFin = it },
            onDismiss = { mostrarTimePickerFin = false }
        )
    }
}

fun formatearLitros(cantidad: Double): String = when {
    cantidad < 1.0 -> "${(cantidad * 1000).toInt()} ml"
    cantidad == 1.0 -> "1 litro"
    cantidad % 1.0 == 0.0 -> "${cantidad.toInt()} litros"
    else -> "%.1f litros".format(cantidad)
}

/*Ajustar y eliminar los dos private inferiores en caso de verse necesario y remplazarse por su equivalente
del codigo PantallaConfiguracionHabitoEscritura.kt*/

/*─────────────────────────────  SLIDER “PIXEL STYLE”  ───────────────────────*/
@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun DurationSliderHidra(
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
