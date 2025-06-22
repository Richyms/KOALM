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
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.ui.unit.sp
import com.example.koalm.data.HabitosRepository
import com.example.koalm.model.MetricasHabito
import com.example.koalm.model.ProgresoDiario
import com.example.koalm.repository.HabitoRepository
import com.example.koalm.services.notifications.HydrationNotificationService
import com.example.koalm.ui.components.ExitoDialogoGuardadoAnimado
import com.example.koalm.ui.components.FalloDialogoGuardadoAnimado
import com.example.koalm.ui.components.ValidacionesDialogoAnimado
import com.example.koalm.ui.screens.habitos.personalizados.TooltipDialogAyuda
import java.time.LocalDate
import kotlin.math.roundToInt

private const val TAG = "PantallaConfigHidratacion" // Unique tag for this file

//@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaConfiguracionHabitoHidratacion (
    navController: NavHostController,
    habitoId: String? = null
) {
    val context = LocalContext.current
    val userEmail = FirebaseAuth.getInstance().currentUser?.email

    //------------------------------ Estados --------------------------------
    var descripcion       by remember { mutableStateOf("") }
    var recordatorios  by remember { mutableStateOf(false) }
    val esEdicion = habitoId != null

    /*  Barra  */
    var cantLitros by remember { mutableStateOf(2) } // 2 litros por defecto, como entero
    val rangoLitros = 0.5f..10f // medio litro a 10 litros

    /* Horas */
    val horarios = remember { mutableStateListOf<LocalTime>()}
    var horaIni         by remember { mutableStateOf(LocalTime.of(8,0)) }
    var mostrarTimePickerIni    by remember { mutableStateOf(false) }
    var horaFin         by remember { mutableStateOf(LocalTime.of(22,0)) }
    var mostrarTimePickerFin    by remember { mutableStateOf(false) }
    var mostrarTimePicker by remember { mutableStateOf(false) }
    var horaAEditarIndex by remember { mutableStateOf<Int?>(null) }

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
    var isLoading by remember { mutableStateOf(false) }
    val habitosRepository = remember { HabitoRepository() }


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

    val habitoEditando = remember { mutableStateOf<Habito?>(null) }
    var habitoExistente by remember { mutableStateOf<Habito?>(null) }
    val currentUser = auth.currentUser

    LaunchedEffect(habitoId) {
        if (habitoId != null) {
            val resultado = habitosRepository.obtenerHabito(habitoId)
            resultado.fold(
                onSuccess = { habito ->
                    habitoEditando.value = habito
                    cantLitros = habito.objetivoPaginas
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
                        titulo = "Hidratación",
                        descripcion = descripcion.ifEmpty { context.getString(R.string.hidratacionn_notification_default_text) },
                        diasSeleccionados = List(7) { true },
                        clase = ClaseHabito.FISICO,
                        tipo = TipoHabito.HIDRATACION,
                        horarios =  horarios.map {
                            it.format(
                                DateTimeFormatter.ofPattern(
                                    "HH:mm"
                                )
                            )
                        },
                        objetivoPaginas = cantLitros,
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

                                val notificationService = HydrationNotificationService()

                                notificationService.cancelNotifications(context)

                                notificationService.scheduleNotification(
                                    context = context,
                                    horarios = horarios.map { it.format(DateTimeFormatter.ofPattern("HH:mm")) },
                                    descripcion = descripcion,
                                    durationMinutes = 0,
                                    additionalData = mapOf(
                                        "habito_id" to habitoId,
                                        "is_hydration" to true
                                    )
                                )


                                // Referencias para guardar progreso
                                val db = FirebaseFirestore.getInstance()
                                val userHabitsRef = userEmail?.let {
                                    db.collection("habitos").document(it)
                                        .collection("predeterminados")
                                }

                                val progreso = ProgresoDiario(
                                    realizados = 0,
                                    completado = false,
                                    totalObjetivoDiario = cantLitros
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

                                val notificationService = HydrationNotificationService()

                                notificationService.cancelNotifications(context)

                                notificationService.scheduleNotification(
                                    context = context,
                                    horarios = horarios.map { it.format(DateTimeFormatter.ofPattern("HH:mm")) },
                                    descripcion = descripcion,
                                    durationMinutes = 0,
                                    additionalData = mapOf(
                                        "habito_id" to nuevoHabitoId,
                                        "is_hydration" to true
                                    )
                                )


                                // Referencias para guardar progreso
                                val db = FirebaseFirestore.getInstance()
                                val userHabitsRef = userEmail?.let {
                                    db.collection("habitos").document(it)
                                        .collection("predeterminados")
                                }

                                val progreso = ProgresoDiario(
                                    realizados = 0,
                                    completado = false,
                                    totalObjetivoDiario = cantLitros
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

    */


    val permissionLauncherHydratation = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            //guardarHabitoHidratacion()
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
                            text = formatearLitros(cantLitros.toDouble()),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        DurationSliderHidra(
                            value        = cantLitros,
                            onValueChange = { cantLitros = it },
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ){
                        Text(
                            text = "Horario de recordatorios: *",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                        TooltipDialogAyuda(
                            titulo = "Horario de recordatorios",
                            mensaje = "Configura los horarios en los que deseas recibir recordatorios para tomar agua. Recibirás una notificación en cada hora que elijas para ayudarte a mantener una buena hidratación."
                        )
                    }


                    // 🟢 Lista de horarios
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

                    /*
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
                    */
                }
            }

            Spacer(Modifier.weight(1f))

            /*-------------------------Guardar------------------------------------*/
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
    value: Int,
    onValueChange: (Int) -> Unit,
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
                value = value.toFloat(),
                onValueChange = {
                    val rounded = it.roundToInt()
                    onValueChange(rounded)
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                },
                valueRange = valueRange,
                steps = (valueRange.endInclusive - valueRange.start).toInt() - 1, // solo enteros
                colors = SliderDefaults.colors(
                    activeTrackColor = Color.Transparent,
                    inactiveTrackColor = Color.Transparent,
                    activeTickColor = Color.Transparent,
                    inactiveTickColor = Color.Transparent,
                    thumbColor = Color.Transparent
                ),
                modifier = Modifier.fillMaxSize()
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
