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
import com.example.koalm.R
import com.example.koalm.ui.components.BarraNavegacionInferior
import com.example.koalm.ui.theme.VerdeBorde
import com.example.koalm.ui.theme.VerdeContenedor
import java.time.LocalTime
import java.time.format.DateTimeFormatter

import com.google.firebase.auth.FirebaseAuth
import com.example.koalm.model.ClaseHabito
import com.example.koalm.model.Habito
import com.example.koalm.model.TipoHabito
import com.example.koalm.repository.HabitoRepository
import com.example.koalm.services.timers.NotificationService
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
private const val TAG = "PantallaConfigAlimentacion" // Unique tag for this file



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaConfiguracionHabitoAlimentacion(navController: NavHostController) {
    val context = LocalContext.current
    val horarios = remember { mutableStateListOf("09:00 AM", "12:00 PM", "07:00 PM") }
    var descripcion by remember { mutableStateOf("") }
    var mostrarTimePicker by remember { mutableStateOf(false) }
    var horaSeleccionada by remember { mutableStateOf(LocalTime.of(12, 0)) }
    var selectedIndex by remember { mutableIntStateOf(-1) }
    val diasSemana = listOf("L-D")
    var horaRecordatorio by remember {
        mutableStateOf(
            LocalTime.now().plusMinutes(1).withSecond(0).withNano(0)
        )
    }

    // Firebase
    val auth = FirebaseAuth.getInstance()
    val habitosRepository = remember { HabitoRepository() }
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }

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
                    titulo = "Alimentacion",
                    descripcion = descripcion.ifEmpty { context.getString(R.string.titulo_config_sueno) },
                    clase = ClaseHabito.FISICO,
                    tipo = TipoHabito.ALIMENTACION,
                    hora = horaSeleccionada.format(DateTimeFormatter.ofPattern("HH:mm")),
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
                        diasSeleccionados = List(7) { true },
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

                    // 🟢 Caja de descripción editable
                    OutlinedTextField(
                        value = descripcion,
                        onValueChange = { descripcion = it },
                        label = { Text(stringResource(R.string.label_descripcion)) },
                        placeholder = { Text(stringResource(R.string.placeholder_descripcion_alimentacion)) },
                        modifier = Modifier.fillMaxWidth()
                    )



                    Text(
                        text = "Horario de comidas: *",
                        fontWeight = FontWeight.Bold
                    )

                    // 🟢 Lista de horarios
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth() // Aseguramos que ocupe el ancho completo
                    ) {
                        horarios.forEachIndexed { index, hora ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween, // Asegura el espaciado entre los elementos
                                modifier = Modifier.fillMaxWidth() // Asegura que ocupe todo el ancho
                            ) {
                                // Muestra el item con la hora
                                Box(modifier = Modifier.weight(1f)) {
                                    // Aquí usamos HoraField para mostrar y editar el horario
                                    HorarioComidaItem(
                                        hora = hora,
                                        onEditar = {
                                            selectedIndex =
                                                index // Establece el índice para saber qué editar
                                            mostrarTimePicker = true
                                        }
                                    )
                                }

                                // Botón de eliminar
                                if (horarios.size > 1) { // Solo permite eliminar si hay más de un horario
                                    IconButton(
                                        onClick = {
                                            if (horarios.size > 1) { // Verifica que haya más de un elemento antes de eliminar
                                                horarios.removeAt(index) // Elimina el horario
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Delete, // Icono de eliminar
                                            contentDescription = "Eliminar horario",
                                            tint = Color.Red // Puedes cambiar a tu color personalizado, por ejemplo, RojoClaro
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 🟢 Botón + Agregar
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable {
                                mostrarTimePicker = true
                            }
                            .padding(top = 8.dp)
                    ) {
                        Icon(Icons.Default.AddCircle, contentDescription = "Agregar")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Agregar", fontSize = 14.sp)
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            // 🟢 Botón Guardar
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
                            guardarHabitoAlimentacion()
                        } else {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    enabled = !isLoading && horarios.isNotEmpty()
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White
                        )
                    } else {
                        Text("Guardar hábito")
                    }
                }
            }
        }
        if (mostrarTimePicker) {
            TimePickerDialogAlimentacion(
                initialTime = horaRecordatorio,
                onTimePicked = { selectedTime ->
                    horaRecordatorio = selectedTime // Actualizamos la hora seleccionada
                    if (selectedIndex >= 0) {
                        // Si estamos editando un horario, lo actualizamos
                        horarios[selectedIndex] =
                            selectedTime.format(DateTimeFormatter.ofPattern("hh:mm a"))
                    } else {
                        // Si no estamos editando, agregamos un nuevo horario
                        horarios.add(selectedTime.format(DateTimeFormatter.ofPattern("hh:mm a")))
                    }
                    // Restablecer selectedIndex a -1 para futuras inserciones
                    selectedIndex = -1
                    mostrarTimePicker = false // Cerramos el TimePicker
                },
                onDismiss = {
                    mostrarTimePicker = false // Cerrar el TimePicker sin hacer nada
                    selectedIndex = -1 // Restablecer selectedIndex cuando se descarta el TimePicker
                }
            )
        }
    }


}

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


/*───────────────────────────── COMPONENTES ───────────────────────────────*/

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

