/*  PantallaModificarHabitoAlimentacin.kt  */
package com.example.koalm.ui.screens.habitos.saludFisica

import android.widget.Toast
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
import androidx.navigation.NavHostController
import com.example.koalm.R
import com.example.koalm.ui.components.BarraNavegacionInferior
import com.example.koalm.ui.theme.VerdeBorde
import com.example.koalm.ui.theme.VerdeContenedor
import java.time.LocalTime
import java.time.format.DateTimeFormatter


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaModificarHabitoAlimentacion(navController: NavHostController) {
    val context = LocalContext.current
    val horarios = remember { mutableStateListOf("09:00 AM", "03:00 PM", "08:00 PM") }
    var descripcion by remember { mutableStateOf("") }
    var mostrarTimePicker by remember { mutableStateOf(false) }
    var horaRecordatorio by remember { mutableStateOf(LocalTime.of(22, 0)) }
    var selectedIndex by remember { mutableIntStateOf(-1) } // Para saber qué horario editar

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Modificar hábito de alimentación") },
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
                shape   = RoundedCornerShape(16.dp),
                border  = BorderStroke(1.dp, VerdeBorde),
                colors  = CardDefaults.cardColors(containerColor = VerdeContenedor)
            ){
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
                                            selectedIndex = index // Establece el índice para saber qué editar
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
                        Toast.makeText(
                            context,
                            "Configuración de alimentación guardada",
                            Toast.LENGTH_SHORT
                        ).show()
                        navController.navigateUp()
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
        TimePickerDialogAlimentacion(
            initialTime = horaRecordatorio,
            onTimePicked = { selectedTime ->
                horaRecordatorio = selectedTime // Actualizamos la hora seleccionada
                if (selectedIndex >= 0) {
                    // Si estamos editando un horario, lo actualizamos
                    horarios[selectedIndex] = selectedTime.format(DateTimeFormatter.ofPattern("hh:mm a"))
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
