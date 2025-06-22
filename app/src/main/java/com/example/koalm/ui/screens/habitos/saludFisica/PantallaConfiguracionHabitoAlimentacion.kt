package com.example.koalm.ui.screens.habitos.saludFisica

import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import android.Manifest
import androidx.compose.material.icons.filled.Schedule
import com.example.koalm.R
import com.example.koalm.ui.components.BarraNavegacionInferior
import com.example.koalm.ui.theme.VerdeBorde
import com.example.koalm.ui.theme.VerdeContenedor
import java.time.LocalTime
import java.time.format.DateTimeFormatter

import com.google.firebase.auth.FirebaseAuth
import com.example.koalm.model.ClaseHabito
import com.example.koalm.model.Habito
import com.example.koalm.model.MetricasHabito
import com.example.koalm.model.ProgresoDiario
import com.example.koalm.model.TipoHabito
import com.example.koalm.repository.HabitoRepository
import com.example.koalm.services.notifications.AlimentationNotificationService
import com.example.koalm.services.timers.NotificationService
import com.example.koalm.ui.components.ExitoDialogoGuardadoAnimado
import com.example.koalm.ui.components.FalloDialogoGuardadoAnimado
import com.example.koalm.ui.components.ValidacionesDialogoAnimado
import com.example.koalm.ui.screens.habitos.personalizados.TooltipDialogAyuda
import com.example.koalm.ui.screens.habitos.saludMental.TimePickerDialog
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.LocalDateTime
private const val TAG = "PantallaConfigAlimentacion" // Unique tag for this file



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaConfiguracionHabitoAlimentacion(
    navController: NavHostController,
    habitoId: String? = null
) {
    val context = LocalContext.current
    val userEmail = FirebaseAuth.getInstance().currentUser?.email
    val esEdicion = habitoId != null
    val horarios = remember { mutableStateListOf<LocalTime>()}
    var descripcion by remember { mutableStateOf("") }
    var mostrarTimePicker by remember { mutableStateOf(false) }
    var horaSeleccionada by remember { mutableStateOf(LocalTime.of(12, 0)) }
    var selectedIndex by remember { mutableIntStateOf(-1) }
    var horaAEditarIndex by remember { mutableStateOf<Int?>(null) }
    val diasSemana = listOf("L-D")
    var horaRecordatorio by remember {
        mutableStateOf(
            LocalTime.now().plusMinutes(1).withSecond(0).withNano(0)
        )
    }
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
                navController.navigate("salud_fisica") {
                    popUpTo("salud_fisica") { inclusive = true }
                    launchSingleTop = true
                }
            }
        )
    }

    var mostrarDialogoFalloHora by remember{ mutableStateOf(false) }
    if (mostrarDialogoFalloHora) {
        FalloDialogoGuardadoAnimado(
            mensaje = "Ya agregaste esta hora.",
            onDismiss = {
                mostrarDialogoFalloHora = false
            }
        )
    }


    // Firebase
    val auth = FirebaseAuth.getInstance()
    val habitosRepository = remember { HabitoRepository() }
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }

    val habitoEditando = remember { mutableStateOf<Habito?>(null) }
    var habitoExistente by remember { mutableStateOf<Habito?>(null) }
    val currentUser = auth.currentUser

    LaunchedEffect(habitoId) {
        if (habitoId != null) {
            val resultado = habitosRepository.obtenerHabito(habitoId)
            resultado.fold(
                onSuccess = { habito ->
                    habitoEditando.value = habito
                    descripcion = habito.descripcion
                    horarios.clear()
                    try {
                        horarios.addAll(habito.horarios.map { LocalTime.parse(it) })
                    } catch (e: Exception) {
                        horarios.add(LocalTime.now().plusMinutes(1).withSecond(0).withNano(0))
                    }

                },
                onFailure = {
                    Log.e(TAG, "No se pudo cargar el hábito con ID: $habitoId")
                }
            )
        }
    }

    LaunchedEffect(esEdicion, habitoId) {
        if (esEdicion && habitoId != null && currentUser != null) {
            val snapshot = FirebaseFirestore.getInstance()
                .collection("habitos")
                .document(currentUser.uid)
                .collection("predeterminados")
                .document(habitoId)
                .get()
                .await()

            habitoExistente = snapshot.toObject(Habito::class.java)
        }
    }

    /* --------------------  Permission launcher (POST_NOTIFICATIONS)  -------------------- */
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                mensajeValidacion = "Debes iniciar sesión para crear un hábito"
                return@rememberLauncherForActivityResult
            }

            scope.launch {
                try {
                    // Crear el hábito en Firebase
                    val habito = Habito(
                        id = habitoId ?: "",  // Para edición o creación nueva
                        titulo = "Alimentación",
                        descripcion = descripcion.ifEmpty { context.getString(R.string.alimentationn_notification_default_text) },
                        diasSeleccionados = List(7) { true },
                        clase = ClaseHabito.FISICO,
                        tipo = TipoHabito.ALIMENTACION,
                        horarios =  horarios.map {
                            it.format(
                                DateTimeFormatter.ofPattern(
                                    "HH:mm"
                                )
                            )
                        },
                        objetivoPaginas = horarios.size,
                        userId = currentUser.uid,
                        fechaCreacion = if (esEdicion) habitoExistente?.fechaCreacion else LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                        metricasEspecificas = MetricasHabito(),
                        rachaActual = if (!esEdicion) {
                            0
                        } else {
                            habitoExistente?.rachaActual ?: 0
                        },
                        rachaMaxima = if (!esEdicion) {
                            0
                        } else {
                            habitoExistente?.rachaMaxima ?: 0
                        },
                        ultimoDiaCompletado = if (esEdicion) habitoExistente?.ultimoDiaCompletado else null
                    )

                    if (habitoId != null) {
                        // EDICIÓN
                        habitosRepository.actualizarHabitoO(habitoId, habito).fold(
                            onSuccess = {
                                Log.d(TAG, "Hábito actualizado exitosamente con ID: $habitoId")
                                Log.d(TAG, "Tipo de hábito: ${habito.tipo}")

                                val notificationService = AlimentationNotificationService()

                                notificationService.cancelNotifications(context)

                                notificationService.scheduleNotification(
                                    context = context,
                                    horarios = horarios.map { it.format(DateTimeFormatter.ofPattern("HH:mm")) },
                                    descripcion = descripcion,
                                    durationMinutes = 0,
                                    additionalData = mapOf(
                                        "habito_id" to habitoId,
                                        "is_alimentation" to true
                                    )
                                )


                                // Referencias para guardar progreso
                                val db = FirebaseFirestore.getInstance()
                                val userHabitsRef = userEmail?.let {
                                    db.collection("habitos").document(it)
                                        .collection("predeterminados")
                                }

                                val cantidadHorarios = horarios.size
                                val progreso = ProgresoDiario(
                                    realizados = 0,
                                    completado = false,
                                    totalObjetivoDiario = cantidadHorarios
                                )

                                val progresoRef = userHabitsRef?.document(habitoId)
                                    ?.collection("progreso")
                                    ?.document(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))

                                progresoRef?.set(progreso.toMap())?.addOnSuccessListener {
                                    Log.d(TAG, "Guardando progreso para hábito ID: $habitoId, fecha: ${LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))}")
                                }?.addOnFailureListener { e ->
                                    Toast.makeText(
                                        context,
                                        "Error al guardar el progreso: ${e.message}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    navController.navigate("salud_fisica") {
                                        popUpTo("salud_fisica") { inclusive = true }
                                        launchSingleTop = true
                                    }
                                }

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

                                val notificationService = AlimentationNotificationService()

                                notificationService.cancelNotifications(context)

                                notificationService.scheduleNotification(
                                    context = context,
                                    horarios = horarios.map { it.format(DateTimeFormatter.ofPattern("HH:mm")) },
                                    descripcion = descripcion,
                                    durationMinutes = 0,
                                    additionalData = mapOf(
                                        "habito_id" to nuevoHabitoId,
                                        "is_alimentation" to true
                                    )
                                )


                                // Referencias para guardar progreso
                                val db = FirebaseFirestore.getInstance()
                                val userHabitsRef = userEmail?.let {
                                    db.collection("habitos").document(it)
                                        .collection("predeterminados")
                                }

                                val cantidadHorarios = horarios.size

                                val progreso = ProgresoDiario(
                                    realizados = 0,
                                    completado = false,
                                    totalObjetivoDiario = cantidadHorarios
                                )

                                val progresoRef = userHabitsRef?.document(nuevoHabitoId)
                                    ?.collection("progreso")
                                    ?.document(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))

                                progresoRef?.set(progreso.toMap())?.addOnSuccessListener {
                                    Log.d(TAG, "Guardando progreso para hábito ID: $nuevoHabitoId, fecha: ${LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))}")
                                }?.addOnFailureListener { e ->
                                    Toast.makeText(
                                        context,
                                        "Error al guardar el progreso: ${e.message}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    navController.navigate("salud_mental") {
                                        popUpTo("salud_mental") { inclusive = true }
                                        launchSingleTop = true
                                    }
                                }

                                mostrarDialogoExito = true
                            },
                            onFailure = { error ->
                                Log.e(TAG, "Error al crear el hábito: ${error.message}", error)
                                Toast.makeText(
                                    context,
                                    "Error al crear el hábito: ${error.message}",
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
            mensajeValidacion = "Se requieren permisos de notificaciones"
        }
    }


    /*
    fun guardarHabitoAlimentacion() {
        if (horarios.isEmpty()) {
            Toast.makeText(context, "Debes agregar al menos un horario", Toast.LENGTH_SHORT).show()
            return
        }

        isLoading = true
        scope.launch {
            try {
                val userId = auth.currentUser?.uid
                    ?: throw Exception("Usuario no autenticado")

                val nuevoHabito = Habito(
                    titulo = "Alimentación",
                    descripcion = descripcion.ifEmpty { "Recordatorios de comidas" },
                    clase = ClaseHabito.FISICO,
                    tipo = TipoHabito.ALIMENTACION,
                    diasSeleccionados = List(7) { true }, // Todos los días por defecto
                    hora = horarios.first(), // Hora principal
                    horarios = horarios.toList(), // Lista completa de horarios
                    duracionMinutos = 30, // Duración estimada por comida
                    userId = userId
                )

                // Guardar en Firebase
                habitosRepository.crearHabito(nuevoHabito).onSuccess { habitoId ->
                    // Programar notificaciones para cada horario
                    val notificationService = NotificationService()
                    context.startService(Intent(context, NotificationService::class.java))

                    horarios.forEachIndexed { index, horaStr ->
                        val hora = LocalTime.parse(horaStr, DateTimeFormatter.ofPattern("hh:mm a"))
                        val notificationTime = LocalDateTime.of(LocalDate.now(), hora)

                        notificationService.scheduleNotification(
                            context = context,
                            habitoId = "$habitoId-$index", // ID único para cada notificación
                            diasSeleccionados = nuevoHabito.diasSeleccionados,
                            hora = notificationTime,
                            descripcion = "Es hora de tu comida",
                            durationMinutes = 0,
                            isAlimentation = true,
                            isSleeping = false,
                            isHidratation = false
                        )
                    }

                    Toast.makeText(
                        context,
                        "Hábito de alimentación guardado con ${horarios.size} recordatorios",
                        Toast.LENGTH_SHORT
                    ).show()
                    navController.popBackStack()
                }.onFailure { e ->
                    Toast.makeText(
                        context,
                        "Error al guardar: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    context,
                    "Error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                isLoading = false
            }
        }
    }

    */

    /*----------------------------------UI-----------------------------------------------*/
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configurar hábito de alimentación") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        },
        bottomBar = { BarraNavegacionInferior(navController, "configurar_habito") }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 24.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, VerdeBorde),
                colors = CardDefaults.cardColors(containerColor = VerdeContenedor)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    // Caja de descripción editable
                    OutlinedTextField(
                        value = descripcion,
                        onValueChange = { descripcion = it },
                        label = { Text(stringResource(R.string.label_descripcion)) },
                        placeholder = { Text(stringResource(R.string.placeholder_descripcion_alimentacion)) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ){
                        Text(
                            text = "Horario de comidas: *",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                        TooltipDialogAyuda(
                            titulo = "Horario de comidas",
                            mensaje = "Configura los horarios en los que sueles realizar tus comidas principales. Recibirás una notificación en cada hora que elijas para ayudarte a cumplir con tu hábito."
                        )
                    }


                    // Lista de horarios
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        horarios.forEachIndexed { index, hora ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(modifier = Modifier.weight(1f)) {
                                    HoraField(hora = hora) {
                                        mostrarTimePicker = true
                                        horaAEditarIndex = index
                                    }
                                }

                                IconButton(
                                    onClick = {
                                        horarios.removeAt(index)
                                    },
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Eliminar horario",
                                        tint = Color.Red
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable {
                                horaAEditarIndex = null // <- Indica que es una nueva hora
                                mostrarTimePicker = true
                            }
                            .padding(top = 8.dp)
                    ) {
                        Icon(
                            Icons.Default.AddCircle,
                            contentDescription = "Agregar",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Agregar hora.",
                            fontSize = 14.sp,
                            color = Color.Black
                        )
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            // Botón Guardar
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
                            if (horarios.isNotEmpty()) {
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                mensajeValidacion = "Por favor, selecciona al menos un recordatorio."
                                return@Button
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

        if (mostrarTimePicker) {
            TimePickerDialog(
                initialTime = LocalTime.now(),
                onTimePicked = { nuevaHora ->
                    val horaSinSegundos = nuevaHora.withSecond(0).withNano(0)

                    if (horaAEditarIndex != null) {
                        // Editando
                        horarios[horaAEditarIndex!!] = horaSinSegundos
                    } else {
                        // Agregando
                        if (!horarios.contains(horaSinSegundos)) {
                            horarios.add(horaSinSegundos)
                        } else {
                            mostrarDialogoFalloHora = true
                        }
                    }

                    mostrarTimePicker = false
                },
                onDismiss = { mostrarTimePicker = false }
            )
        }

    }


}


/*───────────────────────────── COMPONENTES ───────────────────────────────*/
// 🟢 Item de horario individual (alineado y tamaño igual a sueño)
@Composable
fun HorarioComidaItem(hora: String, onEditar: () -> Unit) {
    Surface(
        tonalElevation = 0.dp,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFF9E9E9E)),

        color = Color.White,
        modifier = Modifier
            .widthIn(max = 180.dp)
            .height(48.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Editar",
                tint = Color(0xFF478D4F),
                modifier = Modifier
                    .size(20.dp)
                    .clickable(onClick = onEditar)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = hora,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )

            Icon(
                imageVector = Icons.Default.AccessTime,
                contentDescription = "Hora",
                tint = Color(0xFF000000),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialogAlimentacion(
    initialTime: LocalTime,
    onTimePicked: (LocalTime) -> Unit,
    onDismiss: () -> Unit
) {
    val state = rememberTimePickerState(
        initialHour = initialTime.hour,
        initialMinute = initialTime.minute,
        is24Hour = false
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                onTimePicked(LocalTime.of(state.hour, state.minute))
                onDismiss()
            }) { Text("Aceptar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        },
        text = {
            TimePicker(state = state, modifier = Modifier.fillMaxWidth())
        }
    )
}


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
