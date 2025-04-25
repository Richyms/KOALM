package com.example.koalmV1.ui.screens

import android.app.TimePickerDialog
import android.content.Context
import android.widget.TimePicker
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
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
import androidx.navigation.NavController
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigurarHabitoAlimentacionScreen(navController: NavController) {
    val context = LocalContext.current
    val horarios = remember { mutableStateListOf("09:00 AM", "03:00 PM", "08:00 PM") }

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
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = { /* Guardar lógica aquí */ },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                ) {
                    Text("Guardar", color = Color.White)
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 24.dp)
                .fillMaxSize()
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F9ED)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Añadir descripción",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Kool estará observando... así que come bien, o podrías descubrir de lo que es capaz.",
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(text = "Horario de comidas:", fontWeight = FontWeight.Bold)

                    Spacer(modifier = Modifier.height(8.dp))

                    horarios.forEachIndexed { index, hora ->
                        HorarioComidaItem(
                            hora = hora,
                            onEditar = {
                                mostrarTimePicker(context) {
                                    horarios[index] = it
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            mostrarTimePicker(context) {
                                horarios.add(it)
                            }
                        }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Agregar")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Agregar.", fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun HorarioComidaItem(hora: String, onEditar: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(8.dp))
            .padding(8.dp)
            .border(1.dp, Color(0xFF4CAF50), RoundedCornerShape(8.dp))
    ) {
        IconButton(onClick = onEditar) {
            Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Color(0xFF4CAF50))
        }
        Text(text = hora, fontSize = 16.sp)
        Icon(
            imageVector = Icons.Default.AccessTime,
            contentDescription = "Hora",
            tint = Color(0xFF4CAF50)
        )
    }
}

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
