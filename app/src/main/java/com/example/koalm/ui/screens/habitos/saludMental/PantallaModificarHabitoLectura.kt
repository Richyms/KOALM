/*  PantallaModificarHabitoLectura.kt  */
package com.example.koalm.ui.screens.habitos.saludMental

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Book
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import com.example.koalm.R
import com.example.koalm.model.ClaseHabito
import com.example.koalm.model.Habito
import com.example.koalm.model.TipoHabito
import com.example.koalm.repository.HabitoRepository
import com.example.koalm.services.timers.NotificationService
import com.example.koalm.ui.components.BarraNavegacionInferior
import com.example.koalm.ui.theme.VerdeBorde
import com.example.koalm.ui.theme.VerdeContenedor
import com.google.firebase.auth.FirebaseAuth
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

private const val TAG = "PantallaConfiguracionHabitoLectura"

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PantallaModificarHabitoLectura(navController: NavHostController) {
    val context = LocalContext.current
    val habitosRepository = remember { HabitoRepository() }
    val scope = rememberCoroutineScope()
    val auth = FirebaseAuth.getInstance()

    /*  Duración  */
    var duracionMin by remember { mutableStateOf(15f) }    // 1‑180 min
    val rangoDuracion = 1f..180f

    var descripcion by remember { mutableStateOf("") }
    val diasSemana = listOf("L", "M", "M", "J", "V", "S", "D")
    var diasSeleccionados by remember { mutableStateOf(List(7) { false }) }

    var horaRecordatorio by remember {
        mutableStateOf(
            LocalTime.now().plusMinutes(1).withSecond(0).withNano(0)
        )
    }
    var mostrarTimePicker by remember { mutableStateOf(false) }

    fun scheduleNotification(habito: Habito) {
        Log.d(TAG, "Programando notificación para hábito de lectura")
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
            durationMinutes = habito.duracionMinutos.toLong(),
            isReading = true
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
                    titulo = "Lectura",
                    descripcion = descripcion.ifEmpty { context.getString(R.string.reading_notification_default_text) },
                    clase = ClaseHabito.MENTAL,
                    tipo = TipoHabito.LECTURA,
                    diasSeleccionados = diasSeleccionados,
                    hora = horaRecordatorio.format(DateTimeFormatter.ofPattern("HH:mm")),
                    duracionMinutos = duracionMin.toInt(),
                    notasHabilitadas = false,
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
                            context.getString(R.string.reading_notification_default_text)
                        },
                        durationMinutes = duracionMin.toLong(),
                        notasHabilitadas = false,
                        isMeditation = false,
                        isReading = true,
                        isDigitalDisconnect = false
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
                title = { Text("Modificar hábito de lectura") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
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
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, VerdeBorde),
                colors = CardDefaults.cardColors(containerColor = VerdeContenedor)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {

                    // 🟢 Caja de descripción editable
                    OutlinedTextField(
                        value = descripcion,
                        onValueChange = { descripcion = it },
                        label = { Text(stringResource(R.string.label_descripcion)) },
                        placeholder = { Text(stringResource(R.string.placeholder_descripcion_lectura)) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // 🟢 Frecuencia
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

                    // 🟢 Hora de recordatorio
                    Text(
                        text = stringResource(R.string.label_hora),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    HoraField(horaRecordatorio) { mostrarTimePicker = true }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = stringResource(R.string.label_duracion_lectura),
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
                            text = "Selecciona el tiempo que quieres que dure tu lectura",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            // 🟢 Botón guardar
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
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
                                    titulo = "Lectura",
                                    descripcion = descripcion.ifEmpty { context.getString(R.string.reading_notification_default_text) },
                                    clase = ClaseHabito.MENTAL,
                                    tipo = TipoHabito.LECTURA,
                                    diasSeleccionados = diasSeleccionados,
                                    hora = horaRecordatorio.format(DateTimeFormatter.ofPattern("HH:mm")),
                                    duracionMinutos = duracionMin.toInt(),
                                    notasHabilitadas = false,
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
                                            context.getString(R.string.reading_notification_default_text)
                                        },
                                        durationMinutes = duracionMin.toLong(),
                                        notasHabilitadas = false,
                                        isMeditation = false,
                                        isReading = true,
                                        isDigitalDisconnect = false
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
                        .width(200.dp)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(stringResource(R.string.boton_guardar), color = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }
    }

    if (mostrarTimePicker) {
        TimePickerDialogLectura(
            initialTime = horaRecordatorio,
            onTimePicked = { horaRecordatorio = it },
            onDismiss = { mostrarTimePicker = false }
        )
    }
}

/*───────────────────────────── COMPONENTES ───────────────────────────────*/

private fun formatearDuracion(min: Int): String = when {
    min < 60           -> "$min minutos"
    min == 60          -> "1 hora"
    min % 60 == 0      -> "${min/60} horas"
    else               -> "${min/60} horas ${min%60} min"
}