package com.example.koalm.ui.screens.habitos.personalizados

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.koalm.R
import com.example.koalm.ui.components.BarraNavegacionInferior
import com.example.koalm.ui.theme.VerdeBorde
import com.example.koalm.ui.theme.VerdeContenedor
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.ui.unit.sp
import com.example.koalm.ui.theme.VerdePrincipal
import com.example.koalm.model.HabitoPersonalizado
import com.example.koalm.model.ProgresoDiario
import com.example.koalm.model.Recordatorios
import com.example.koalm.ui.components.SelectorDeIconoDialog
import com.example.koalm.ui.components.obtenerIconoPorNombre
import com.example.koalm.ui.screens.habitos.saludMental.DiaCircle
import com.example.koalm.ui.screens.habitos.saludMental.HoraField
import com.example.koalm.ui.screens.habitos.saludMental.TimePickerDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaConfigurarHabitoPersonalizado(navController: NavHostController) {
    val context = LocalContext.current

    var nombreHabito by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var horaRecordatorio by remember { mutableStateOf(LocalTime.of(7, 0)) }
    var mostrarTimePicker by remember { mutableStateOf(false) }
    var diasSeleccionados by remember { mutableStateOf(List(7) { false }) }
    var modoFecha by remember { mutableStateOf(true) }
    var fechaSeleccionada by remember { mutableStateOf<LocalDate?>(null) }
    var mostrarDatePicker by remember { mutableStateOf(false) }
    var diasDuracion by remember { mutableStateOf("") }
    val errorNombre = stringResource(R.string.error_nombre)
    val errorHora = stringResource(R.string.error_hora)
    val mensajeExito = stringResource(R.string.mensaje_guardado)

    var mostrarSelectorIconos by remember { mutableStateOf(false) }
    var iconoSeleccionado by remember { mutableStateOf("") }

    var mostrarSelectorColor by remember { mutableStateOf(false) }
    var colorSeleccionado by remember { mutableStateOf(Color(0xFFF6FBF2)) }
    var recordatorioActivo by remember { mutableStateOf(false) }
    var frecuenciaActivo by remember { mutableStateOf(false) }
    var finalizarActivo by remember { mutableStateOf(false) }
    val horarios = remember { mutableStateListOf<LocalTime>()}
    var horaAEditarIndex by remember { mutableStateOf<Int?>(null) }
    var unaVezPorHabito by remember { mutableStateOf("") }


    var modoAutomatico by remember { mutableStateOf(true) }
    var modoPersonalizado by remember { mutableStateOf(true) }

    val colorIcono = parseColorFromFirebase(colorSeleccionado.toString(),darken = true)
    var nombreError by remember { mutableStateOf(false)}

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.titulo_config_habito_personalizado)) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
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
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = VerdeContenedor),
                border = BorderStroke(1.dp, VerdeBorde),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {

                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = stringResource(R.string.label_vista_previa),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = nombreHabito,
                        onValueChange = {
                            val regex = Regex("^[a-zA-Z0-9][a-zA-Z0-9 ]*\$")

                            if (it.length <= 20 && (it.isEmpty() || regex.matches(it))) {
                                nombreHabito = it
                                nombreError = false
                            } else if (it.length > 20) {
                                nombreError = true
                            }
                        },
                        label = { Text(stringResource(R.string.label_nombre_habito)) },
                        isError = nombreError,
                        modifier = Modifier.fillMaxWidth(),
                        supportingText = {
                            if (nombreError) {
                                Text(
                                    text = "Máximo 20 caracteres.",
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedButton(
                            onClick = { mostrarSelectorColor = true },
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(1.dp, VerdeBorde),
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color.Transparent,
                                contentColor = Color.Black
                            )
                        ) {
                            Text("Color")
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                Modifier
                                    .size(20.dp)
                                    .background(colorSeleccionado, CircleShape)
                                    .border(
                                        width = 1.dp,
                                        color = Color.Gray,
                                        shape = CircleShape
                                )
                            )
                        }

                        OutlinedButton(
                            onClick = { mostrarSelectorIconos = true },
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(1.dp, VerdeBorde),
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color.Transparent,
                                contentColor = Color.Black
                            )
                        ) {
                            Text("Icono")
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = obtenerIconoPorNombre(iconoSeleccionado),
                                contentDescription = null,
                                tint = colorIcono
                            )
                        }

                    }

                    OutlinedTextField(
                        value = descripcion,
                        onValueChange = { descripcion = it },
                        label = { Text(stringResource(R.string.label_descripcion)) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        thickness = 1.dp,
                        color = Color.Gray
                    )

                    Text(
                        text = stringResource(R.string.label_configuracion_adicional),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(R.string.label_frecuencia_P),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Switch(
                            checked = frecuenciaActivo,
                            onCheckedChange = {
                                frecuenciaActivo = it
                                if (!it) {
                                    diasSeleccionados = List(7) { false } // Reinicia los días a no seleccionados
                                    }
                            },
                            modifier = Modifier.padding(start = 10.dp),
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = VerdePrincipal,
                                uncheckedThumbColor = Color.LightGray,
                                uncheckedTrackColor = Color.Gray
                            )
                        )
                    }

                    if (frecuenciaActivo) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            listOf("L", "M", "M", "J", "V", "S", "D").forEachIndexed { index, dia ->
                                DiaCircle(label = dia, selected = diasSeleccionados[index]) {
                                    diasSeleccionados = diasSeleccionados.toMutableList()
                                        .also { it[index] = !it[index] }
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(R.string.label_switch_activar_recordatorio),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Switch(
                            checked = recordatorioActivo,
                            onCheckedChange = {
                                recordatorioActivo = it
                                if (!it) {
                                    horarios.clear()
                                    modoPersonalizado = true
                                    }
                            },
                            modifier = Modifier.padding(start = 10.dp),
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = VerdePrincipal,
                                uncheckedThumbColor = Color.LightGray,
                                uncheckedTrackColor = Color.Gray
                            )
                        )
                    }

                    if (recordatorioActivo) {
                        Text(
                            text = stringResource(R.string.label_tipo_notificacion),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            OutlinedButton(
                                onClick = { modoPersonalizado = true },
                                border = BorderStroke(1.dp, VerdeBorde),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (modoPersonalizado) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    contentColor = if (modoPersonalizado) Color.White else Color.Black
                                ),
                                shape = RoundedCornerShape(50),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(text = stringResource(R.string.boton_Personalizado))
                            }

                            OutlinedButton(
                                onClick = { modoPersonalizado = false },
                                border = BorderStroke(1.dp, VerdeBorde),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (!modoPersonalizado) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    contentColor = if (!modoPersonalizado) Color.White else Color.Black
                                ),
                                shape = RoundedCornerShape(50),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(text = stringResource(R.string.boton_Automatico))
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (modoPersonalizado) {
                            Text(
                                text = stringResource(R.string.label_notificacion_personalizada),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium
                            )

                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                horarios.forEachIndexed { index, hora ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Box(modifier = Modifier.width(200.dp)) {
                                            HoraField(hora = hora) {
                                                mostrarTimePicker = true
                                                horaAEditarIndex = index
                                            }
                                        }

                                        IconButton(onClick = {
                                            horarios.removeAt(index)
                                        }) {
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
                                        horarios.add(LocalTime.now())
                                    }
                                    .padding(top = 8.dp)
                            ) {
                                Icon(Icons.Default.AddCircle, contentDescription = "Agregar")
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "Agregar hora.", fontSize = 14.sp)
                            }
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        thickness = 1.dp,
                        color = Color.Gray
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(R.string.label_finaliza_el),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Switch(
                            checked = finalizarActivo,
                            onCheckedChange = {
                                finalizarActivo = it
                                if (!it) {
                                    fechaSeleccionada = null
                                    diasDuracion = ""
                                    modoFecha = true
                                    }
                            },
                            modifier = Modifier.padding(start = 10.dp),
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = VerdePrincipal,
                                uncheckedThumbColor = Color.LightGray,
                                uncheckedTrackColor = Color.Gray
                            )
                        )
                    }

                    if (finalizarActivo) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            OutlinedButton(
                                onClick = { modoFecha = true },
                                border = BorderStroke(1.dp, VerdeBorde),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (modoFecha) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    contentColor = if (modoFecha) Color.White else Color.Black
                                ),
                                shape = RoundedCornerShape(50),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(text = stringResource(R.string.boton_fecha))
                            }

                            OutlinedButton(
                                onClick = { modoFecha = false },
                                border = BorderStroke(1.dp, VerdeBorde),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (!modoFecha) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    contentColor = if (!modoFecha) Color.White else Color.Black
                                ),
                                shape = RoundedCornerShape(50),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(text = stringResource(R.string.boton_dias))
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (modoFecha) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(R.string.label_ultimo_dia),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Medium
                                )

                                OutlinedButton(
                                    onClick = { mostrarDatePicker = true },
                                    border = BorderStroke(1.dp, VerdeBorde),
                                    shape = RoundedCornerShape(50),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = Color.Transparent,
                                        contentColor = MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    Icon(Icons.Default.CalendarToday, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        fechaSeleccionada?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                                            ?: stringResource(R.string.boton_seleccionar_fecha),
                                        color = Color.Black
                                    )
                                }
                            }
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = stringResource(R.string.label_despues_dias),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Medium
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                OutlinedTextField(
                                    value = diasDuracion,
                                    onValueChange = { diasDuracion = it },
                                    modifier = Modifier
                                        .width(70.dp)
                                        .height(48.dp),
                                    singleLine = true,
                                    shape = RoundedCornerShape(50),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center)
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Text(
                                    text = stringResource(R.string.label_dias),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            }
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
                        // Validación del campo de nombre
                        if (nombreHabito.isBlank()) {
                            Toast.makeText(context, "El nombre del hábito es obligatorio", Toast.LENGTH_SHORT).show()
                        } else  if (nombreHabito.length >= 20) {
                            Toast.makeText(context, "El nombre del hábito no debe tener más de 20 caracteres.", Toast.LENGTH_SHORT).show()
                        } else {
                            // Obtener la referencia al usuario actual en Firebase Authentication
                            val userEmail = FirebaseAuth.getInstance().currentUser?.email
                            val db = FirebaseFirestore.getInstance()

                            // Asegurarnos de que el correo del usuario no sea nulo
                            if (userEmail != null) {
                                // Referencia a la colección de hábitos del usuario
                                val userHabitsRef = db.collection("habitos").document(userEmail)
                                    .collection("personalizados")

                                // Crear el objeto HabitoPersonalizado
                                val habitoPersonalizado = HabitoPersonalizado(
                                    nombre = nombreHabito,
                                    colorEtiqueta = colorSeleccionado.toString(),
                                    iconoEtiqueta = iconoSeleccionado.toString(),
                                    descripcion = descripcion,
                                    frecuencia = diasSeleccionados, // Ejemplo: ["lunes", "miércoles"]
                                    recordatorios = Recordatorios(
                                        tipo = if (modoPersonalizado) "personalizado" else "automatico",
                                        horas = if (modoPersonalizado)
                                            horarios.map { it.format(DateTimeFormatter.ofPattern("HH:mm")) }
                                        else
                                            HabitoPersonalizado.generarHorasAutomaticas()
                                    ),
                                    fechaInicio = HabitoPersonalizado.calcularFechaInicio(),
                                    fechaFin = HabitoPersonalizado.calcularFechaFin(
                                        modoFecha,
                                        fechaSeleccionada.toString(), diasDuracion
                                    ),
                                    modoFin = if (modoFecha) "calendario" else "dias",  // Definimos el modo de fin
                                    unaVezPorHabito = if (!recordatorioActivo) 1 else 0,
                                    rachaActual = 0,  // Inicialmente 0, la racha se actualizará después
                                    rachaMaxima = 0,  // Inicialmente 0, la racha máxima se actualizará después
                                    ultimoDiaCompletado = null,  // Inicialmente no hay último día completado
                                )

                                // Convertir el objeto a un mapa
                                val habitoMap = habitoPersonalizado.toMap()

                                // Usar el nombre del hábito como ID, reemplazando espacios por guiones bajos
                                val habitoId = nombreHabito.replace(" ", "_")

                                // Guardar el hábito en Firestore bajo la subcolección "personalizados" del usuario
                                userHabitsRef.document(habitoId)  // Usando el nombre del hábito como ID
                                    .set(habitoMap)
                                    .addOnSuccessListener {
                                        Toast.makeText(
                                            context,
                                            "Hábito guardado con éxito",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        navController.navigateUp()
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(
                                            context,
                                            "Error al guardar el hábito: ${e.message}",
                                            Toast.LENGTH_LONG
                                        ).show()
                                        navController.navigateUp()
                                    }

                                // Crear el objeto de progreso
                                val progreso = ProgresoDiario(
                                    realizados = 0,
                                    completado = false,
                                    totalRecordatoriosPorDia = if (modoPersonalizado) horarios.size else 3
                                )

                                // Referenciar al documento de progreso usando la fecha actual como ID
                                val progresoRef = userHabitsRef.document(habitoId)
                                    .collection("progreso")
                                    .document(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))

                                // Guardar en Firestore usando el .toMap()
                                progresoRef.set(progreso.toMap())
                                    .addOnSuccessListener {
                                        Toast.makeText(
                                            context,
                                            "Progreso diario guardado",
                                            Toast.LENGTH_LONG
                                        ).show()
                                        navController.navigateUp()
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(
                                            context,
                                            "Error al guardar el progreso: ${e.message}",
                                            Toast.LENGTH_LONG
                                        ).show()
                                        navController.navigateUp()
                                    }

                            } else {
                                Toast.makeText(
                                    context,
                                    "Usuario no autenticado",
                                    Toast.LENGTH_LONG
                                ).show()
                                navController.navigateUp()
                            }
                        }
                    },
                    modifier = Modifier
                        .width(200.dp)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(
                        stringResource(R.string.boton_guardar),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }

    if (mostrarTimePicker) {
        TimePickerDialog(
            initialTime = horaAEditarIndex?.let { horarios[it] } ?: LocalTime.now(),
            onTimePicked = { nuevaHora ->
                horaAEditarIndex?.let { index ->
                    horarios[index] = nuevaHora
                }
                mostrarTimePicker = false
            },
            onDismiss = { mostrarTimePicker = false }
        )
    }


    if (mostrarDatePicker) {
        val localeContext = remember { context.withLocale(Locale("es", "MX")) }

        val today = Calendar.getInstance()
        val millisHoy = today.timeInMillis
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = millisHoy
        )

        DatePickerDialog(
            onDismissRequest = { mostrarDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        if (millis >= millisHoy) {
                            val cal = Calendar.getInstance().apply { timeInMillis = millis }
                            fechaSeleccionada = LocalDate.of(
                                cal.get(Calendar.YEAR),
                                cal.get(Calendar.MONTH) + 1,
                                cal.get(Calendar.DAY_OF_MONTH) + 1
                            )
                            mostrarDatePicker = false
                        } else {
                            Toast.makeText(context, "Selecciona una fecha válida", Toast.LENGTH_SHORT).show()
                        }
                    }
                }) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDatePicker = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                showModeToggle = false,
                colors = DatePickerDefaults.colors(
                    selectedDayContainerColor = VerdeContenedor,
                    selectedDayContentColor = Color.Black,
                    todayDateBorderColor = VerdeBorde
                )
            )
        }
    }

    if (mostrarSelectorIconos) {
        SelectorDeIconoDialog(
            iconoSeleccionadoNombre= iconoSeleccionado,
            onSeleccionar = { iconoSeleccionado = it },
            onCerrar = { mostrarSelectorIconos = false }
        )
    }

    if (mostrarSelectorColor) {
        val colores = listOf(
            Color(0xFFA5D6A7), // Verde suave
            Color(0xFF90CAF9), // Azul claro
            Color(0xFFFFCC80), // Naranja pastel
            Color(0xFFEF9A9A), // Rojo rosado
            Color(0xCEB39DDB), // Púrpura pastel
            Color(0xFF80CBC4), // Verde azulado claro
            Color(0xFFFFF59D), // Amarillo claro
            Color(0xFFD1C4E9), // Lavanda suave
            Color(0xFFB2EBF2), // Azul verdoso muy claro
            Color(0xFFFFAB91), // Coral claro
            Color(0xFFC5E1A5), // Verde lima pálido
            Color(0xFF9FA8DA), // Azul lavanda
            Color(0xFFF0F4C3), // Verde amarillento tenue
            Color(0xFFD7CCC8), // Marrón claro grisáceo
            Color(0xFFCFD8DC), // Azul gris claro
            Color(0xFFF8BBD0), // Rosa bebé
            Color(0xFFDCEDC8), // Verde muy suave
            Color(0xFFE1BEE7), // Violeta claro
            Color(0xFFEF5350), // Rojo coral más fuerte
            Color(0xFF64B5F6)  //Azul cielo
        )

        AlertDialog(
            onDismissRequest = { mostrarSelectorColor = false },
            title = { Text("Selecciona un color") },
            text = {
                Column {
                    colores.chunked(5).forEach { fila ->
                        Row(
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            fila.forEach { color ->
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(color, CircleShape)
                                        .clickable {
                                            colorSeleccionado = color
                                            mostrarSelectorColor = false
                                        }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }

}

// Extensión para oscurecer el color
fun Color.darken(factor: Float): Color {
    return Color(
        red = (red * (1 - factor)).coerceIn(0f, 1f),
        green = (green * (1 - factor)).coerceIn(0f, 1f),
        blue = (blue * (1 - factor)).coerceIn(0f, 1f),
        alpha = alpha
    )
}

// Función para hacer el parseo de color desde FB
fun parseColorFromFirebase(colorString: String, darken: Boolean = false, darkenFactor: Float = 0.15f): Color {
    val regex = Regex("""Color\(([\d.]+), ([\d.]+), ([\d.]+), ([\d.]+),.*\)""")
    val match = regex.find(colorString)
    return if (match != null) {
        val (r, g, b, a) = match.destructured
        val baseColor = Color(r.toFloat(), g.toFloat(), b.toFloat(), a.toFloat())
        if (darken) baseColor.darken(darkenFactor) else baseColor
    } else {
        Log.e("ColorParse", "No se pudo parsear el color: $colorString")
        Color.Gray
    }
}

fun Context.withLocale(locale: Locale): Context {
    val config = resources.configuration
    config.setLocale(locale)
    return createConfigurationContext(config)
}

@Composable
fun HorarioItem(hora: String, onEditar: () -> Unit) {
    Surface(
        tonalElevation = 0.dp,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFF4CAF50)),
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
                tint = Color(0xFF4CAF50),
                modifier = Modifier
                    .size(18.dp)
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
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

