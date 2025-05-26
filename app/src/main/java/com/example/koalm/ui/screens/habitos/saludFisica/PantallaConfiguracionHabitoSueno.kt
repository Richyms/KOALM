// PantallaConfiguracionHabitoSueno.kt
package com.example.koalm.ui.screens.habitos.saludFisica

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
import androidx.navigation.NavHostController

import com.example.koalm.ui.components.HoraField
import com.example.koalm.ui.components.DiaCircle
import com.example.koalm.services.timers.NotificationService
import com.example.koalm.ui.components.TimePickerDialog
import com.example.koalm.ui.components.BarraNavegacionInferior
import com.example.koalm.ui.theme.VerdeBorde
import com.example.koalm.ui.theme.VerdeContenedor
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.koalm.R
import com.example.koalm.model.ClaseHabito
import com.example.koalm.model.Habito
import com.example.koalm.model.TipoHabito
import com.example.koalm.repository.HabitoRepository
import com.example.koalm.ui.theme.GrisMedio
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.time.LocalDate

private const val TAG = "PantallaConfiguracionHabitoSueno"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaConfiguracionHabitoSueno(navController: NavHostController) {
    val context = LocalContext.current

    var descripcion by remember { mutableStateOf("") }
    val diasSemana = listOf("L", "M", "M", "J", "V", "S", "D")
    var diasSeleccionados by remember { mutableStateOf(List(7) { false }) }

    var horaDormir by remember { mutableStateOf(LocalTime.of(22, 0)) }
    var mostrarTimePickerInicio by remember { mutableStateOf(false) }

    var duracionHoras by remember { mutableStateOf(8f) }
    val rangoHoras = 1f..12f
    val horaDespertarCalculada = horaDormir.plusHours(duracionHoras.toLong())

    val scope = rememberCoroutineScope()
    val habitosRepository = remember { HabitoRepository() }
    var duracionMin by remember { mutableStateOf(15f) }    // 1‑180 min

    val auth = FirebaseAuth.getInstance()

    var horaRecordatorio by remember {
        mutableStateOf(
            LocalTime.now().plusMinutes(1).withSecond(0).withNano(0)
        )
    }

    // Lista dinámica de recordatorios
    val recordatorios = remember {
        mutableStateListOf(
            "Desconectarse de las pantallas",
            "Tomar un té sin cafeína",
            "Escuchar música suave",
            "Evitar comidas pesadas"
        )
    }
    val recordatoriosChecked = remember {
        mutableStateListOf(true, true, true, true)
    }

    // Dialogo de agregar
    var mostrarDialogo by remember { mutableStateOf(false) }
    var nuevoRecordatorio by remember { mutableStateOf("") }

    fun scheduleNotification(habito: Habito) {
        Log.d(TAG, "Programando notificación para hábito de sueno")
        Log.d(TAG, "Tipo de hábito: ${habito.tipo}")

        val horaLocalDateTime = LocalDateTime.parse(
            LocalDate.now().toString() + "T" + habito.hora,
            DateTimeFormatter.ISO_LOCAL_DATE_TIME
        )

        val notificationService = NotificationService()
        notificationService.scheduleNotification(
            context = context,
            habitoId = habito.id,
            diasSeleccionados = habito.diasSeleccionados,
            hora = horaLocalDateTime,
            descripcion = habito.descripcion,
            durationMinutes = 0,
            isSleeping = true
        )
        Log.d(TAG, "Notificación programada exitosamente")
    }

    /* --------------------  Permission launcher (POST_NOTIFICATIONS)  -------------------- */
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            scope.launch {
                val currentUser = auth.currentUser
                if (currentUser == null) {
                    Log.e(TAG, "No hay usuario autenticado")
                    Toast.makeText(
                        context,
                        "Debes iniciar sesión para crear un hábito",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@launch
                }

                // Crear el hábito en Firebase
                val habito = Habito(
                    titulo = "Sueño",
                    descripcion = descripcion.ifEmpty { context.getString(R.string.titulo_config_sueno) },
                    clase = ClaseHabito.FISICO,
                    tipo = TipoHabito.SUEÑO,
                    diasSeleccionados = diasSeleccionados,
                    hora = horaDormir.format(DateTimeFormatter.ofPattern("HH:mm")),
                    duracionMinutos = (duracionHoras * 60).roundToInt(),
                    userId = currentUser.uid
                )

                habitosRepository.crearHabito(habito).onSuccess { habitoId ->
                    Log.d(TAG, "Hábito creado exitosamente con ID: $habitoId")
                    Log.d(TAG, "Tipo de hábito: ${habito.tipo}")

                    // Programar notificación con el ID real del hábito
                    val notificationService = NotificationService()
                    val notificationTime = LocalDateTime.of(LocalDateTime.now().toLocalDate(), horaRecordatorio)

                    Log.d(TAG, "Iniciando servicio de notificaciones")
                    context.startService(Intent(context, NotificationService::class.java))

                    notificationService.scheduleNotification(
                        context = context,
                        habitoId = habitoId,
                        diasSeleccionados = diasSeleccionados,
                        hora = notificationTime,
                        descripcion = descripcion.ifEmpty {
                            context.getString(R.string.sleeping_notification_default_text)
                        },
                        durationMinutes = 0,
                        isAlimentation = false,
                        isSleeping = true,
                        isHidratation = false
                    )

                    Toast.makeText(
                        context,
                        context.getString(R.string.success_notifications_scheduled),
                        Toast.LENGTH_SHORT
                    ).show()
                    navController.navigateUp()
                }.onFailure { error ->
                    Log.e(TAG, "Error al crear el hábito: ${error.message}")
                    Toast.makeText(
                        context,
                        "Error al crear el hábito",
                        Toast.LENGTH_SHORT
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


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configurar hábito de sueño") },
                navigationIcon = {
                    IconButton(onClick = navController::navigateUp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        },
        bottomBar = { BarraNavegacionInferior(navController, "configurar_habito") }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, VerdeBorde),
                colors = CardDefaults.cardColors(containerColor = VerdeContenedor)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {

                    OutlinedTextField(
                        value = descripcion,
                        onValueChange = { descripcion = it },
                        label = {Text("Añadir descripción.") },
                        placeholder = { Text("Ej. Dormir bien transforma tu energía, tu salud y tu bienestar mental.") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Elige a qué hora planeas dormir:")
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        diasSemana.forEachIndexed { i, d ->
                            DiaCircle(
                                label = d,
                                selected = diasSeleccionados[i],
                                onClick = {
                                    diasSeleccionados = diasSeleccionados.toMutableList().also { it[i] = !it[i] }
                                }
                            )
                        }
                    }

                    // Horas de dormir y despertar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            HoraField(
                                hora = horaDormir,
                                onClick = { mostrarTimePickerInicio = true }
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            HoraFieldCentrada(horaDespertarCalculada)
                        }
                    }

                    // Slider editable
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        val (activeColor, inactiveColor) = when {
                            duracionHoras >= 8f -> Color(0xFF376A3E) to Color(0xFF376A3E).copy(alpha = 0.3f)
                            duracionHoras >= 6f -> Color(0xFF795A0C) to Color(0xFFF2DDB8)
                            else -> Color(0xFF914B43) to Color(0xFFFFD3CD)
                        }

                        val mensajeSueño = when {
                            duracionHoras >= 8f -> "Sueño excelente"
                            duracionHoras >= 6f -> "Sueño regular"
                            else -> "No te hagas tanto daño"
                        }

                        val haptics = LocalHapticFeedback.current

                        Slider(
                            value = duracionHoras,
                            onValueChange = {
                                duracionHoras = it
                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            },
                            valueRange = rangoHoras,
                            steps = 11,
                            modifier = Modifier.fillMaxWidth(),
                            colors = SliderDefaults.colors(
                                thumbColor = activeColor,
                                activeTrackColor = activeColor,
                                inactiveTrackColor = inactiveColor
                            )
                        )
                        Text(
                            text = "$mensajeSueño\n${duracionHoras.roundToInt()} horas",
                            style = MaterialTheme.typography.bodySmall,
                            color = activeColor,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }

                    Text("Selecciona los recordatorios que deseas antes de dormir (opcional):")
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        recordatorios.forEachIndexed { index, texto ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = recordatoriosChecked[index],
                                    onCheckedChange = { recordatoriosChecked[index] = it }
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(texto)
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable { mostrarDialogo = true }
                                .padding(top = 8.dp)
                        ) {
                            Icon(Icons.Default.AddCircle, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Agregar.")
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    if (!diasSeleccionados.any { it }) {
                        Toast.makeText(
                            context,
                            context.getString(R.string.error_no_days_selected),
                            Toast.LENGTH_SHORT
                        ).show()
                        return@Button
                    }

                    if (ContextCompat.checkSelfPermission(
                            context, Manifest.permission.POST_NOTIFICATIONS
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        scope.launch {
                            val currentUser = auth.currentUser
                            if (currentUser == null) {
                                Log.e(TAG, "No hay usuario autenticado")
                                Toast.makeText(
                                    context,
                                    "Debes iniciar sesión para crear un hábito",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@launch
                            }

                            // Crear el hábito en Firebase
                            val habito = Habito(
                                titulo = "Sueño",
                                descripcion = descripcion.ifEmpty { context.getString(R.string.titulo_config_sueno) },
                                clase = ClaseHabito.FISICO,
                                tipo = TipoHabito.SUEÑO,
                                diasSeleccionados = diasSeleccionados,
                                hora = horaDormir.format(DateTimeFormatter.ofPattern("HH:mm")),
                                duracionMinutos = (duracionHoras * 60).roundToInt(),
                                userId = currentUser.uid
                            )

                            habitosRepository.crearHabito(habito).onSuccess { habitoId ->
                                Log.d(TAG, "Hábito creado exitosamente con ID: $habitoId")
                                Log.d(TAG, "Tipo de hábito: ${habito.tipo}")

                                // Programar notificación con el ID real del hábito
                                val notificationService = NotificationService()
                                val notificationTime = LocalDateTime.of(LocalDateTime.now().toLocalDate(), horaRecordatorio)

                                Log.d(TAG, "Iniciando servicio de notificaciones")
                                context.startService(Intent(context, NotificationService::class.java))

                                notificationService.scheduleNotification(
                                    context = context,
                                    habitoId = habitoId,
                                    diasSeleccionados = diasSeleccionados,
                                    hora = notificationTime,
                                    descripcion = descripcion.ifEmpty {
                                        context.getString(R.string.sleeping_notification_default_text)
                                    },
                                    durationMinutes = 0,
                                    isAlimentation = false,
                                    isSleeping = true,
                                    isHidratation = false
                                )

                                Toast.makeText(
                                    context,
                                    context.getString(R.string.success_notifications_scheduled),
                                    Toast.LENGTH_SHORT
                                ).show()
                                navController.navigateUp()
                            }.onFailure { error ->
                                Log.e(TAG, "Error al crear el hábito: ${error.message}")
                                Toast.makeText(
                                    context,
                                    "Error al crear el hábito",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    } else {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                },
                modifier = Modifier
                    .width(150.dp) // ancho
                    .align(Alignment.CenterHorizontally) // Centrar
            ) {
                Text("Guardar")
            }
        }
    }

    if (mostrarTimePickerInicio) {
        TimePickerDialog(
            initialTime = horaDormir,
            onTimePicked = { horaDormir = it },
            onDismiss = { mostrarTimePickerInicio = false }
        )
    }

    if (mostrarDialogo) {
        AlertDialog(
            onDismissRequest = { mostrarDialogo = false },
            title = { Text("Nuevo recordatorio") },
            text = {
                OutlinedTextField(
                    value = nuevoRecordatorio,
                    onValueChange = { nuevoRecordatorio = it },
                    placeholder = { Text("Ej. Lavarse los dientes") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (nuevoRecordatorio.isNotBlank()) {
                        recordatorios.add(nuevoRecordatorio)
                        recordatoriosChecked.add(true)
                        nuevoRecordatorio = ""
                        mostrarDialogo = false
                    }
                }) {
                    Text("Agregar")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogo = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
@Composable
fun HoraFieldCentrada(hora: LocalTime) {
    Surface(
        tonalElevation = 0.dp,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = hora.format(DateTimeFormatter.ofPattern("hh:mm a")),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

