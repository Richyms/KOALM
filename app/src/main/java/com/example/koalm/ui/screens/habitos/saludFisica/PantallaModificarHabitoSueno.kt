/*  PantallaModificarHabitoSueno.kt  */
package com.example.koalm.ui.screens.habitos.saludFisica

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

import com.example.koalm.ui.components.BarraNavegacionInferior
import com.example.koalm.ui.theme.VerdeBorde
import com.example.koalm.ui.theme.VerdeContenedor
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.AddCircle
import com.example.koalm.R
import com.example.koalm.ui.screens.habitos.saludMental.DiaCircle
import com.example.koalm.ui.screens.habitos.saludMental.HoraField
import com.example.koalm.ui.screens.habitos.saludMental.TimePickerDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaModificarHabitoSueno(navController: NavHostController) {
    val context = LocalContext.current

    var descripcion by remember { mutableStateOf("") }
    val diasSemana = listOf("L", "M", "M", "J", "V", "S", "D")
    var diasSeleccionados by remember { mutableStateOf(List(7) { false }) }

    var horaDormir by remember { mutableStateOf(LocalTime.of(22, 0)) }
    var mostrarTimePickerInicio by remember { mutableStateOf(false) }

    var duracionHoras by remember { mutableStateOf(8f) }
    val rangoHoras = 1f..12f
    val horaDespertarCalculada = horaDormir.plusHours(duracionHoras.toLong())

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Modificar hábito de sueño") },
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
                        label = { Text(stringResource(R.string.label_descripcion)) },
                        placeholder = { Text(stringResource(R.string.placeholder_descripcion_sueño)) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Frecuencia: *")
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        diasSemana.forEachIndexed { i, d ->
                            DiaCircle(
                                label = d,
                                selected = diasSeleccionados[i],
                                onClick = {
                                    diasSeleccionados = diasSeleccionados.toMutableList().also { it[i] = !it[i] }
                                }
                            )
                        }
                    }
                    Text("Elige a qué hora planeas dormir: *")

                    // Horas de dormir y despertar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                // Campo de hora de dormir
                                HoraField(
                                    hora = horaDormir,
                                    onClick = { mostrarTimePickerInicio = true }
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                // Etiqueta de "Inicio del sueño"
                                Text(
                                    text = "Inicio del sueño",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                // Campo de hora de despertar
                                HoraFieldCentrada(horaDespertarCalculada)
                                Spacer(modifier = Modifier.height(4.dp))
                                // Etiqueta de "Fin del sueño"
                                Text(
                                    text = "Fin del sueño",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }


                    // Slider editable
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        val (activeColor, inactiveColor) = when {
                            duracionHoras >= 8f -> Color(0xFF376A3E) to Color(0xFF376A3E).copy(alpha = 0.3f)
                            duracionHoras >= 6f -> Color(0xFF795A0C) to Color(0xFFF2DDB8)
                            else -> Color(0xFF914B43) to Color(0xFFFFD3CD)
                        }

                        val mensajeSueño = when {
                            duracionHoras >= 8f -> "Sueño excelente"
                            duracionHoras >= 6f -> "Sueño regular"
                            else -> "No te hagas tanto daño"
                        }

                        val haptics = LocalHapticFeedback.current

                        Slider(
                            value = duracionHoras,
                            onValueChange = {
                                duracionHoras = it
                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            },
                            valueRange = rangoHoras,
                            steps = 11,
                            modifier = Modifier.fillMaxWidth(),
                            colors = SliderDefaults.colors(
                                thumbColor = activeColor,
                                activeTrackColor = activeColor,
                                inactiveTrackColor = inactiveColor
                            )
                        )
                        Text(
                            text = "$mensajeSueño\n${duracionHoras.roundToInt()} horas",
                            style = MaterialTheme.typography.bodySmall,
                            color = activeColor,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }

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
                }
            }

            Spacer(Modifier.weight(1f))

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = {
                        Toast.makeText(
                            context,
                            "Configuración de sueño guardada",
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