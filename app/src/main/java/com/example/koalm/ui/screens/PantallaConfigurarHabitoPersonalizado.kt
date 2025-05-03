package com.example.koalm.ui.screens

import android.app.DatePickerDialog
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Water
import androidx.compose.material.icons.filled.Book
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Task
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.ui.unit.sp
import com.example.koalm.ui.theme.VerdePrincipal


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
    var iconoSeleccionado by remember { mutableStateOf(Icons.Default.Favorite) }
    val iconos = listOf(Icons.Default.Favorite, Icons.Default.Book, Icons.Default.FitnessCenter, Icons.Default.Water)

    var mostrarSelectorColor by remember { mutableStateOf(false) }
    var colorSeleccionado by remember { mutableStateOf(Color(0xFF388E3C)) }
    var recordatorioActivo by remember { mutableStateOf(false) }
    var frecuenciaActivo by remember { mutableStateOf(false) }
    var finalizarActivo by remember { mutableStateOf(false) }
    val horarios = remember { mutableStateListOf(LocalTime.of(7, 0)) }
    var horaAEditarIndex by remember { mutableStateOf<Int?>(null) }




    var modoAutomatico by remember { mutableStateOf(true) }
    var modoPersonalizado by remember { mutableStateOf(true) }
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
                    Divider(
                        color = Color.Gray,
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    Text(
                        text = stringResource(R.string.label_vista_previa),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Divider(
                        color = Color.Gray,
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    OutlinedTextField(
                        value = nombreHabito,
                        onValueChange = { nombreHabito = it },
                        label = { Text(stringResource(R.string.label_nombre_habito)) },
                        modifier = Modifier.fillMaxWidth()
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
                                imageVector = iconoSeleccionado,
                                contentDescription = null,
                                tint = colorSeleccionado
                            )
                        }

                    }

                    OutlinedTextField(
                        value = descripcion,
                        onValueChange = { descripcion = it },
                        label = { Text(stringResource(R.string.label_descripcion)) },
                        modifier = Modifier.fillMaxWidth()
                    )


                    Divider(
                        color = Color.Gray,
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    Text(
                    text = stringResource(R.string.label_configuracion_adicional),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                    )

                    Divider(
                        color = Color.Gray,
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )




                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        //horizontalArrangement = Arrangement.Start
                    ) {
                        Text(
                            text = stringResource(R.string.label_frecuencia_P),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Switch(
                            checked = frecuenciaActivo,
                            onCheckedChange = { frecuenciaActivo = it },
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
                    /*Text(
                        text = stringResource(R.string.label_frecuencia_P),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )*/

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        listOf("S", "L", "M", "M", "J", "V", "S").forEachIndexed { index, dia ->
                            DiaCircle(label = dia, selected = diasSeleccionados[index]) {
                                diasSeleccionados = diasSeleccionados.toMutableList()
                                    .also { it[index] = !it[index] }
                            }
                        }
                    }
                }
                    Divider(
                        color = Color.Gray,
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    /*Text(
                        text = stringResource(R.string.label_hora_recordatorio),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )

                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        HoraField(hora = horaRecordatorio) { mostrarTimePicker = true }
                    }*/


                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        //horizontalArrangement = Arrangement.Start
                    ) {
                        Text(
                            text = stringResource(R.string.label_switch_activar_recordatorio),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Switch(
                            checked = recordatorioActivo,
                            onCheckedChange = { recordatorioActivo = it },
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
                            /* Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                HoraField(hora = horaRecordatorio) { mostrarTimePicker = true }*/

                            // 🟢 Lista de horarios
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                horarios.forEachIndexed { index, hora ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        // Campo de hora
                                        Box(modifier = Modifier.width(200.dp)) {
                                            HoraField(hora = hora) {
                                                mostrarTimePicker = true
                                                horaAEditarIndex = index
                                            }
                                        }

                                        // Botón eliminar alineado a la derecha
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



                                // 🟢 Botón + Agregar
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
                                    Text(text = "Agregar.", fontSize = 14.sp)
                                }

                                }

                        }











                    Divider(
                        color = Color.Gray,
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        //horizontalArrangement = Arrangement.Start
                    ) {
                        Text(
                            text = stringResource(R.string.label_finaliza_el),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Switch(
                            checked = finalizarActivo,
                            onCheckedChange = { finalizarActivo = it },
                            modifier = Modifier.padding(start = 10.dp),
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = VerdePrincipal,
                                uncheckedThumbColor = Color.LightGray,
                                uncheckedTrackColor = Color.Gray
                            )
                        )
                    }

                    /*Text(
                        text = stringResource(R.string.label_finaliza_el),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )*/

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

                            Spacer(modifier = Modifier.width(8.dp)) // pequeño espacio

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

                            Spacer(modifier = Modifier.width(8.dp)) // pequeño espacio

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

            Button(
                onClick = {
                    if (nombreHabito.isBlank()) {
                        Toast.makeText(context, errorNombre, Toast.LENGTH_SHORT).show()
                    } else if (horaRecordatorio == null) {
                        Toast.makeText(context, errorHora, Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, mensajeExito, Toast.LENGTH_SHORT).show()
                        navController.navigateUp()
                    }
                },
                modifier = Modifier
                    .width(150.dp)
                    .align(Alignment.CenterHorizontally)
            ) {
                Text(text = stringResource(R.string.boton_guardar))
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
                                cal.get(Calendar.DAY_OF_MONTH)
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
                    selectedDayContainerColor = VerdeContenedor, // fondo del día seleccionado
                    selectedDayContentColor = Color.Black,        // texto del día seleccionado
                    todayDateBorderColor = VerdeBorde             // borde para hoy
                )
            )
        }
    }




    if (mostrarSelectorIconos) {
        val iconos = listOf(
            // Salud y Bienestar
            Icons.Default.Favorite,
            Icons.Default.FitnessCenter,
            Icons.Default.SelfImprovement,
            Icons.Default.Spa,
            Icons.Default.AccessibilityNew,
            Icons.Default.MonitorHeart,

            // Educación y Lectura
            Icons.Default.Book,
            Icons.Default.MenuBook,
            Icons.Default.School,
            Icons.Default.Lightbulb,

            // Trabajo y Productividad
            Icons.Default.Work,
            Icons.Default.Check,
            Icons.Default.Task,
            Icons.Default.Event,
            Icons.Default.Schedule,
            Icons.Default.List,

            // Tecnología y Comunicación
            Icons.Default.Phone,
            Icons.Default.Email,
            Icons.Default.Notifications,
            Icons.Default.Smartphone,
            Icons.Default.Devices,

            // Hogar y Vida diaria
            Icons.Default.Home,
            Icons.Default.Bedtime,
            Icons.Default.Kitchen,
            Icons.Default.WbSunny,
            Icons.Default.ShoppingCart,

            // Emociones y Sociales
            Icons.Default.Face,
            Icons.Default.Mood,
            Icons.Default.EmojiEmotions,
            Icons.Default.ThumbUp,
            Icons.Default.People,

            // Varios
            Icons.Default.Star,
            Icons.Default.MusicNote,
            Icons.Default.Pets,
            Icons.Default.Explore,
            Icons.Default.TravelExplore,
            Icons.Default.DirectionsRun
        )


        AlertDialog(
            onDismissRequest = { mostrarSelectorIconos = false },
            title = { Text("Selecciona un icono") },
            text = {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4), // 4 columnas
                    modifier = Modifier
                        .height(200.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    userScrollEnabled = true
                ) {
                    items(iconos) { icono ->
                        IconButton(
                            onClick = {
                                iconoSeleccionado = icono
                                mostrarSelectorIconos = false

                            },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(imageVector = icono, contentDescription = null,  tint = if (icono == iconoSeleccionado) colorSeleccionado else Color.Gray)
                        }
                    }
                }
            },
            confirmButton = {}
        )

    }

    if (mostrarSelectorColor) {
        val colores = listOf(
            Color(0xFF388E3C), Color(0xFF1976D2), Color(0xFFF57C00), Color(0xFFD32F2F), Color(0xFF7B1FA2),
            Color(0xFF009688), Color(0xFFFFC107), Color(0xFF9C27B0), Color(0xFF00BCD4), Color(0xFFFF5722),
            Color(0xFF4CAF50), Color(0xFF3F51B5), Color(0xFFCDDC39), Color(0xFF795548), Color(0xFF607D8B),
            Color(0xFFE91E63), Color(0xFF8BC34A), Color(0xFF673AB7), Color(0xFFB71C1C), Color(0xFF1E88E5)

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
