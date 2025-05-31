/*  PantallaConfiguracionHabitoMeditación.kt  */
package com.example.koalm.ui.screens.habitos.saludMental

import androidx.compose.foundation.BorderStroke
// import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
// import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
// import androidx.compose.ui.geometry.CornerRadius
// import androidx.compose.ui.geometry.Offset
// import androidx.compose.ui.hapticfeedback.HapticFeedback
// import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
// import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.koalm.R
import com.example.koalm.ui.components.BarraNavegacionInferior
import com.example.koalm.ui.theme.VerdeBorde
import com.example.koalm.ui.theme.VerdeContenedor
import com.example.koalm.utils.TimeUtils
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt
import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.util.Log
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import com.example.koalm.model.ClaseHabito
import com.example.koalm.model.Habito
import com.example.koalm.model.TipoHabito
import com.example.koalm.repository.HabitoRepository
import com.example.koalm.services.notifications.MeditationNotificationService
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

/* foundation */
// import androidx.compose.foundation.Canvas          // ←  dibujar el track
import androidx.compose.foundation.clickable       // ←  .clickable() que reporta error
import com.example.koalm.model.ProgresoDiario
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate

//@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PantallaConfiguracionHabitoMeditacion(navController: NavHostController) {
    val context = LocalContext.current
    val TAG = "PantallaConfiguracionHabitoMeditacion"
    val habitosRepository = remember { HabitoRepository() }
    val scope = rememberCoroutineScope()
    val auth = FirebaseAuth.getInstance()
    val userEmail = FirebaseAuth.getInstance().currentUser?.email
    
    //------------------------------ Estados -------------------------------------
    var descripcion         by remember { mutableStateOf("") }
    val diasSemana          = listOf("L","M","M","J","V","S","D")
    var diasSeleccionados   by remember { mutableStateOf(List(7){false})}

    /* Hora */
    var horaRecordatorio by remember { 
        mutableStateOf(
            LocalTime.now().plusMinutes(1).withSecond(0).withNano(0)
        ) 
    }
    var mostrarTimePicker    by remember { mutableStateOf(false) }

    /* Duración */
    var duracionMin by remember { mutableStateOf(15f) }    // 1‑180 min
    val rangoDuracion = 1f..180f

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
                    titulo = "Meditación",
                    descripcion = descripcion.ifEmpty { context.getString(R.string.meditation_notification_default_text) },
                    clase = ClaseHabito.MENTAL,
                    tipo = TipoHabito.MEDITACION,
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
                    val notificationService = MeditationNotificationService()
                    val notificationTime = LocalDateTime.of(LocalDateTime.now().toLocalDate(), horaRecordatorio)

                    Log.d(TAG, "Iniciando servicio de notificaciones")
                    context.startService(Intent(context, MeditationNotificationService::class.java))

                    notificationService.scheduleNotification(
                        context = context,
                        diasSeleccionados = diasSeleccionados,
                        hora = notificationTime,
                        descripcion = descripcion.ifEmpty {
                            context.getString(R.string.meditation_notification_default_text)
                        },
                        durationMinutes = duracionMin.toLong(),
                        additionalData = mapOf(
                            "habito_id" to habitoId,
                            "is_meditation" to true,
                            "is_reading" to false,
                            "is_digital_disconnect" to false,
                            "notas_habilitadas" to false,
                            "sonidos_habilitados" to false,
                            "ejercicio_respiracion" to false
                        )
                    )
                    // Obtener la referencia al usuario actual en Firebase Authentication
                    val db = FirebaseFirestore.getInstance()
                    val userHabitsRef = userEmail?.let {
                        db.collection("habitos").document(it)
                            .collection("predeterminados")
                    }

                    // Crear el objeto de progreso
                    val progreso = ProgresoDiario(
                        realizados = 0,
                        completado = false,
                        totalObjetivoDiario = 1
                    )

                    // Referenciar al documento de progreso usando la fecha actual como ID
                    val progresoRef = userHabitsRef?.document(habitoId)
                        ?.collection("progreso")
                        ?.document(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))

                    // Guardar en Firestore usando el .toMap()
                    progresoRef?.set(progreso.toMap())?.addOnSuccessListener {
                        Log.d(TAG, "Guardando progreso para hábito ID: $habitoId, fecha: ${LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))}")
                        Toast.makeText(
                            context,
                            "Progreso diario guardado",
                            Toast.LENGTH_LONG
                        ).show()
                        navController.navigateUp()
                    }?.addOnFailureListener { e ->
                        Toast.makeText(
                            context,
                            "Error al guardar el progreso: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                        navController.navigateUp()
                    }

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

    //------------------------------ UI ------------------------------------------
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.titulo_config_meditacion)) },
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
        ) {//--------------------- Tarjeta principal ----------------------
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

                    /*  Descripción  */
                    OutlinedTextField(
                        value = descripcion,
                        onValueChange = { descripcion = it },
                        label = { Text(stringResource(R.string.label_descripcion)) },
                        placeholder = { Text(stringResource(R.string.placeholder_descripcion_meditacion)) },
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
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        diasSemana.forEachIndexed { i, d ->
                            DiaCircle(
                                label = d,
                                selected = diasSeleccionados[i],
                                onClick = {
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
                    HoraField(horaRecordatorio) { mostrarTimePicker = true }


                    /*  Duración (Slider personalizado)  */
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = stringResource(R.string.label_duracion_meditacion),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = TimeUtils.formatearDuracion(duracionMin.roundToInt()),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        DurationSlider(
                            value = duracionMin,
                            onValueChange = { duracionMin = it },
                            valueRange = rangoDuracion,
                            tickEvery = 15,           // marca cada 15 min
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = "Selecciona el tiempo que quieres que dure tu hábito de meditación",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            /* ----------------------------  Card de Meditación  --------------------------- */
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { 
                        try {
                            navController.navigate("temporizador_meditacion/${duracionMin.roundToInt()}") {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error al navegar al temporizador: ${e.message}")
                            Toast.makeText(
                                context,
                                "Error al abrir el temporizador",
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
                        text = "Iniciar Meditación",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            /* ----------------------------  Botón Guardar  --------------------------- */
            Button(
                onClick = {
                    if (diasSeleccionados.any { it }) {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        Toast.makeText(
                            context,
                            "Selecciona al menos un día",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar")
            }
        }
    }

    //------------------------ Time  Picker ------------------------------
    if (mostrarTimePicker) {
        TimePickerDialog(
            initialTime = horaRecordatorio,
            onTimePicked = { horaRecordatorio = it },
            onDismiss = { mostrarTimePicker = false }
        )
    }
}

/*──────────────────────────  HELPERS  ─────────────────────────────────────*/
