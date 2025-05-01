package com.example.koalmv1.ui.screens

import android.app.TimePickerDialog
import android.content.Context
import android.widget.TimePicker
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaConfigurarDesconexionDigital(navController: NavController? = null) {
    val context = LocalContext.current
    var selectedTime by remember { mutableStateOf("10:00 pm") }
    var descripcion by remember { mutableStateOf("Desconectar para reconectar... contigo mismo.") }
    var selectedDays by remember { mutableStateOf(setOf<String>()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Icono de regreso y título
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { navController?.navigateUp() }) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Atrás")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Configurar hábito de desconexión digital",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Contenedor principal
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFEAF4E6), RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            // Campo descripción
            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                label = { Text("Añadir descripción") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("Frecuencia:", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                val dias = listOf("D", "L", "M", "M", "J", "V", "S")
                dias.forEach { dia ->
                    val isSelected = selectedDays.contains(dia)
                    DiaSeleccionable(
                        label = dia,
                        selected = isSelected,
                        onClick = {
                            selectedDays = if (isSelected) {
                                selectedDays - dia
                            } else {
                                selectedDays + dia
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Hora del recordatorio:", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .wrapContentWidth()
                        .clickable {
                            showTimePicker(context) { selectedTime = it }
                        }
                        .background(Color.White, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Filled.Edit, contentDescription = "Editar")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(selectedTime)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Filled.AccessTime, contentDescription = "Reloj")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Cuando te recordemos tu desconexión digital, seleccionarás el tiempo de desconexión.",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { /* Guardar acción */ },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5D9B63))
        ) {
            Text("Guardar", color = Color.White)
        }
    }
}

@Composable
fun DiaSeleccionable(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(if (selected) Color(0xFF5D9B63) else Color.Transparent)
            .border(
                width = 1.dp,
                color = if (selected) Color(0xFF5D9B63) else Color.LightGray,
                shape = CircleShape
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (selected) Color.White else Color.Black,
            fontWeight = FontWeight.Bold
        )
    }
}

// Función para mostrar el TimePicker
fun showTimePicker(context: Context, onTimeSelected: (String) -> Unit) {
    val calendar = Calendar.getInstance()
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val minute = calendar.get(Calendar.MINUTE)

    val timePickerDialog = TimePickerDialog(
        context,
        { _: TimePicker, selectedHour: Int, selectedMinute: Int ->
            val isPM = selectedHour >= 12
            val hourFormatted = if (selectedHour % 12 == 0) 12 else selectedHour % 12
            val minuteFormatted = String.format("%02d", selectedMinute)
            val period = if (isPM) "pm" else "am"
            onTimeSelected("$hourFormatted:$minuteFormatted $period")
        },
        hour,
        minute,
        false
    )
    timePickerDialog.show()
}

@Preview(showBackground = true)
@Composable
fun PreviewPantallaConfigurarDesconexionDigital() {
    PantallaConfigurarDesconexionDigital()
}
