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
import com.example.koalm.model.ProgresoDiario
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
import com.example.koalm.model.MetricasHabito
import com.example.koalm.repository.HabitoRepository
import com.example.koalm.ui.components.ExitoDialogoGuardadoAnimado
import com.example.koalm.ui.components.ValidacionesDialogoAnimado
import com.example.koalm.ui.screens.habitos.personalizados.DiasSeleccionadosResumen
import com.example.koalm.ui.screens.habitos.personalizados.TooltipDialogAyuda
import com.example.koalm.ui.theme.GrisMedio
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.time.LocalDate
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.example.koalm.services.notifications.suenoNotificationService

private const val TAG = "PantallaConfiguracionHabitoSueno"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaConfiguracionHabitoSueno(
    navController: NavHostController,
    habitoId: String? = null
) {
    val context = LocalContext.current
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
                navController.navigate("salud_fisica") {
                    popUpTo("salud_fisica") { inclusive = true }
                    launchSingleTop = true
                }
            }
        )
    }

    var descripcion by remember { mutableStateOf("") }
    val diasSemana = listOf("L", "M", "M", "J", "V", "S", "D")
    var diasSeleccionados by remember { mutableStateOf(List(7) { false }) }

    var horaDormir by remember { mutableStateOf(LocalTime.of(22, 0)) }
    var mostrarTimePickerInicio by remember { mutableStateOf(false) }

    var duracionHoras by remember { mutableStateOf(8) }
    val rangoHoras = 1..12

   // Calcular hora de despertar teniendo en cuenta el cambio de día
    val horaDespertarCalculada = remember(horaDormir, duracionHoras) {
        val duracionMinutos = duracionHoras * 60
        var horaFinal = horaDormir
        var minutosRestantes = duracionMinutos

        while (minutosRestantes > 0) {
            if (minutosRestantes >= 60) {
                horaFinal = horaFinal.plusHours(1)
                minutosRestantes -= 60
            } else {
                horaFinal = horaFinal.plusMinutes(minutosRestantes.toLong())
                minutosRestantes = 0
            }
        }

        horaFinal
    }


    val scope = rememberCoroutineScope()
    val habitosRepository = remember { HabitoRepository() }
    //var duracionMin by remember { mutableStateOf(15f) }    // 1‑180 min


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

    val habitoEditando = remember { mutableStateOf<Habito?>(null) }

    LaunchedEffect(habitoId) {
        if (habitoId != null) {
            val resultado = habitosRepository.obtenerHabito(habitoId)
            resultado.fold(
                onSuccess = { habito ->
                    habitoEditando.value = habito
                    descripcion = habito.descripcion
                    diasSeleccionados = habito.diasSeleccionados
                    horaDormir = try {
                        LocalTime.parse(habito.hora)
                    } catch (e: Exception) {
                        LocalTime.now().plusMinutes(1).withSecond(0).withNano(0)
                    }
                    duracionHoras= habito.objetivoHorasSueno
                },
                onFailure = {
                    Log.e(TAG, "No se pudo cargar el hábito con ID: $habitoId")
                }
            )
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
                        titulo = "Sueño",
                        descripcion = descripcion.ifEmpty { context.getString(R.string.titulo_config_sueno) },
                        clase = ClaseHabito.FISICO,
                        tipo = TipoHabito.SUEÑO,
                        diasSeleccionados = diasSeleccionados,
                        hora = horaDormir.format(DateTimeFormatter.ofPattern("HH:mm")),
                        duracionMinutos = duracionHoras * 60,
                        userId = currentUser.uid,
                        objetivoHorasSueno = duracionHoras,
                        metricasEspecificas = MetricasHabito()
                    )

                    if (habitoId != null) {
                        // EDICIÓN
                        habitosRepository.actualizarHabitoO(habitoId, habito).fold(
                            onSuccess = {
                                Log.d(TAG, "Hábito actualizado exitosamente con ID: $habitoId")
                                Log.d(TAG, "Tipo de hábito: ${habito.tipo}")

                                val notificationService = suenoNotificationService()
                                val notificationTime = LocalDateTime.of(LocalDateTime.now().toLocalDate(), horaDormir)

                                notificationService.cancelNotifications(context)
                                notificationService.scheduleNotification(
                                    context = context,
                                    diasSeleccionados = diasSeleccionados,
                                    hora = notificationTime,
                                    descripcion = descripcion.ifEmpty { context.getString(R.string.sleeping_notification_default_text) },
                                    durationMinutes = 0,
                                    additionalData = mapOf(
                                        "habito_id" to habitoId,
                                        "is_alimentation" to false,
                                        "is_sleeping" to true,
                                        "is_hidratation" to false
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
                                    totalObjetivoDiario = duracionHoras.toInt()
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
                                    navController.navigate("salud_mental") {
                                        popUpTo("salud_mental") { inclusive = true }
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

                                val notificationService = suenoNotificationService()
                                val notificationTime = LocalDateTime.of(LocalDateTime.now().toLocalDate(), horaDormir)

                                Log.d(TAG, "Iniciando servicio de notificaciones")
                                context.startService(Intent(context, suenoNotificationService::class.java))

                                notificationService.scheduleNotification(
                                    context = context,
                                    diasSeleccionados = diasSeleccionados,
                                    hora = notificationTime,
                                    descripcion = descripcion.ifEmpty { context.getString(R.string.sleeping_notification_default_text) },
                                    durationMinutes = 0,
                                    additionalData = mapOf(
                                        "habito_id" to nuevoHabitoId,
                                        "is_alimentation" to false,
                                        "is_sleeping" to true,
                                        "is_hidratation" to false
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
                                    totalObjetivoDiario = duracionHoras.toInt()
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

    // Función para validar si hay conflicto con otros hábitos de sueño
    suspend fun validarConflictoHorario(): Boolean {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return false
        val db = FirebaseFirestore.getInstance()

        try {
            val habitosExistentes = db.collection("habitos")
                .whereEqualTo("userId", userId)
                .whereEqualTo("tipo", "SUEÑO")
                .get()
                .await()

            val minutosInicioNuevo = horaDormir.hour * 60 + horaDormir.minute
            var minutosFinNuevo = minutosInicioNuevo + duracionHoras * 60
            if (minutosFinNuevo >= 1440) minutosFinNuevo -= 1440 // Ajuste si cruza medianoche

            for (documento in habitosExistentes.documents) {
                val horaExistente = documento.getString("hora")?.let {
                    LocalTime.parse(it, DateTimeFormatter.ofPattern("HH:mm"))
                } ?: continue

                val duracionExistente = (documento.getLong("duracionMinutos") ?: 0).toInt()
                val diasHabitoExistente = (documento.get("diasSeleccionados") as? List<*>)?.map {
                    it as? Boolean ?: false
                } ?: List(7) { false }

                // Verificar días solapados
                val diasSolapados = diasSeleccionados.zip(diasHabitoExistente)
                    .mapIndexedNotNull { index, (nuevo, existente) ->
                        if (nuevo && existente) index else null
                    }

                if (diasSolapados.isEmpty()) continue

                val minutosInicioExistente = horaExistente.hour * 60 + horaExistente.minute
                var minutosFinExistente = minutosInicioExistente + duracionExistente
                if (minutosFinExistente >= 1440) minutosFinExistente -= 1440

                // Verificar conflicto de tiempo
                val hayConflicto = when {
                    // Nuevo hábito cruza medianoche
                    minutosFinNuevo < minutosInicioNuevo -> {
                        !(minutosFinNuevo < minutosInicioExistente && minutosInicioNuevo > minutosFinExistente)
                    }
                    // Existente cruza medianoche
                    minutosFinExistente < minutosInicioExistente -> {
                        !(minutosFinExistente < minutosInicioNuevo && minutosInicioExistente > minutosFinNuevo)
                    }
                    // Ninguno cruza medianoche
                    else -> {
                        !(minutosFinNuevo <= minutosInicioExistente || minutosInicioNuevo >= minutosFinExistente)
                    }
                }

                if (hayConflicto) {
                    val diasConflicto = diasSolapados.map {
                        listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo")[it]
                    }

                    Toast.makeText(
                        context,
                        "Ya existe un hábito de sueño programado para los días: ${diasConflicto.joinToString(", ")}",
                        Toast.LENGTH_LONG
                    ).show()

                    return true
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al validar conflicto horario: ${e.message}")
        }

        return false
    }




    /*--------------------------------------------UI---------------------------------------------*/
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
                        label = {Text("Escribe tu motivación") },
                        placeholder = { Text("Dormir bien transforma tu energía, tu salud y tu bienestar mental.") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ){
                        Text(
                            text = stringResource(R.string.label_frecuencia),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                        TooltipDialogAyuda(
                            titulo = "Frecuencia",
                            mensaje = "Selecciona los días de la semana en los que deseas mantener activo tu hábito."
                        )
                    }

                    Column {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            diasSemana.forEachIndexed { i, d ->
                                com.example.koalm.ui.screens.habitos.saludMental.DiaCircle(
                                    label = d,
                                    selected = diasSeleccionados[i],
                                    onClick = {
                                        diasSeleccionados = diasSeleccionados.toMutableList()
                                            .also { it[i] = !it[i] }
                                    }
                                )
                            }
                        }
                        DiasSeleccionadosResumen(diasSeleccionados)
                    }

                    // Horas de dormir y despertar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ){
                        Text(
                            text = "Elige a qué hora planeas dormir: *",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                        TooltipDialogAyuda(
                            titulo = "Hora de inicio",
                            mensaje = "Establece la hora a la que normalmente planeas ir a dormir. El sistema te proporcionará la hora de finalización del sueño adecuada según la calidad de sueño que elijas en el siguiente paso."
                        )
                    }

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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ){
                        Text(
                            text = "Elige la calidad de sueño que deseas: *",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                        TooltipDialogAyuda(
                            titulo = "Calidad de sueño",
                            mensaje = "Según la calidad de sueño que elijas, el sistema calculará automáticamente la hora estimada para despertar."
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        val (activeColor, inactiveColor) = when {
                            duracionHoras >= 8f -> Color(0xFF376A3E) to Color(0xFF376A3E).copy(alpha = 0.3f)
                            duracionHoras >= 6f -> Color(0xFF795A0C) to Color(0xFFF2DDB8)
                            else -> Color(0xFF914B43) to Color(0xFFFFD3CD)
                        }

                        val mensajeSueño = when {
                            duracionHoras >= 8f -> "Sueño nocturno completo"
                            duracionHoras >= 6f -> "Descanso prolongado"
                            duracionHoras >= 2f -> "Descanso parcial"
                            duracionHoras > 0f -> "Siesta o pausa breve"
                            else -> "Sin registro de descanso"
                        }

                        val haptics = LocalHapticFeedback.current

                        Slider(
                            value = duracionHoras.toFloat(),
                            onValueChange = {
                                duracionHoras = it.roundToInt()
                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            },
                            valueRange = rangoHoras.first.toFloat()..rangoHoras.last.toFloat(),
                            steps = rangoHoras.last - rangoHoras.first - 1, // Si rango es 1..12 → steps = 10
                            modifier = Modifier.fillMaxWidth(),
                            colors = SliderDefaults.colors(
                                thumbColor = activeColor,
                                activeTrackColor = activeColor,
                                inactiveTrackColor = inactiveColor
                            )
                        )
                        Column(
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = mensajeSueño,
                                style = MaterialTheme.typography.bodySmall,
                                color = activeColor
                            )
                            Text(
                                text = "${duracionHoras.toInt()} horas",
                                style = MaterialTheme.typography.bodySmall,
                                color = activeColor
                            )
                            val horaDespertarFormateada = if (horaDespertarCalculada.hour >= 24) {
                                horaDespertarCalculada.minusHours(24)
                            } else {
                                horaDespertarCalculada
                            }
                            Text(
                                text = "${horaDormir.format(DateTimeFormatter.ofPattern("HH:mm"))} - ${horaDespertarFormateada.format(DateTimeFormatter.ofPattern("HH:mm"))}",
                                style = MaterialTheme.typography.bodySmall,
                                color = activeColor
                            )
                        }

                    }
  /*
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
                    */
                }
            }

            Spacer(Modifier.height(24.dp))
/*-----------------------Guardar-------------------------------*/
            val scope = rememberCoroutineScope()

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
                            if (!diasSeleccionados.any { it }) {
                                mensajeValidacion = "Por favor, selecciona al menos un día de la semana."
                                return@Button
                            }

                            scope.launch {
                                val conflicto = validarConflictoHorario()
                                if (conflicto) {
                                    // Ya se mostró el mensaje dentro de la función
                                    return@launch
                                }

                                // Si no hay conflicto, continúa con la lógica de guardado
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
