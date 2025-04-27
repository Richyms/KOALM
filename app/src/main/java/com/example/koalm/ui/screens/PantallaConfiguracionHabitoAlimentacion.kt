package com.example.koalm.ui.screens

import android.app.TimePickerDialog
import android.content.Context
import android.widget.TimePicker
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.koalm.ui.components.BarraNavegacionInferior
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaConfiguracionHabitoAlimentacion(navController: NavHostController) {
    val context = LocalContext.current
    val horarios = remember { mutableStateListOf("09:00 AM", "03:00 PM", "08:00 PM") }
    var descripcion by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configurar hábito de alimentación") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
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
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F9ED)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    // 🟢 Caja de descripción editable
                    OutlinedTextField(
                        value = descripcion,
                        onValueChange = { descripcion = it },
                        placeholder = { Text("Kool estará observando... así que come bien, o podrías descubrir de lo que es capaz.") },
                        modifier = Modifier.fillMaxWidth()
                    )



                    Text(
                        text = "Horario de comidas:",
                        fontWeight = FontWeight.Bold
                    )

                    // 🟢 Lista de horarios
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        horarios.forEachIndexed { index, hora ->
                            HorarioComidaItem(
                                hora = hora,
                                onEditar = {
                                    mostrarTimePicker(context) {
                                        horarios[index] = it
                                    }
                                }
                            )
                        }
                    }

                    // 🟢 Botón + Agregar
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable {
                                mostrarTimePicker(context) {
                                    horarios.add(it)
                                }
                            }
                            .padding(top = 8.dp)
                    ) {
                        Icon(Icons.Default.AddCircle, contentDescription = "Agregar")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Agregar.", fontSize = 14.sp)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // 🟢 Botón Guardar
            Button(
                onClick = {
                    Toast.makeText(context, "Configuración de alimentación guardada", Toast.LENGTH_SHORT).show()
                    navController.navigateUp()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar")
            }
        }
    }
}

// 🟢 Item de horario individual (alineado y tamaño igual a sueño)
@Composable
fun HorarioComidaItem(hora: String, onEditar: () -> Unit) {
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

// 🟢 Picker para seleccionar nueva hora
fun mostrarTimePicker(context: Context, onTimeSelected: (String) -> Unit) {
    val calendar = Calendar.getInstance()
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val minute = calendar.get(Calendar.MINUTE)

    TimePickerDialog(
        context,
        { _: TimePicker, selectedHour: Int, selectedMinute: Int ->
            val amPm = if (selectedHour >= 12) "PM" else "AM"
            val hourFormatted = if (selectedHour % 12 == 0) 12 else selectedHour % 12
            val minuteFormatted = String.format("%02d", selectedMinute)
            onTimeSelected("$hourFormatted:$minuteFormatted $amPm")
        },
        hour,
        minute,
        false
    ).show()
}
