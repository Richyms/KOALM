/*  PantallaConfiguracionHabitoHidratacion.kt  */
package com.example.koalm.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
// import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
// import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Schedule
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
import com.example.koalm.services.NotificationService
import com.example.koalm.ui.components.BarraNavegacionInferior
import com.example.koalm.ui.theme.VerdeBorde
import com.example.koalm.ui.theme.VerdeContenedor
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt
import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import android.util.Log
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.ui.text.input.KeyboardType

//@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaConfiguracionHabitoHidratacion (navController: NavHostController) {
    val context = LocalContext.current

    //------------------------------ Estados --------------------------------
    var descripcion       by remember { mutableStateOf("") }
    var recordatorios  by remember { mutableStateOf(false) }

    /*  Barra  */
    var cantlitros by remember { mutableStateOf(2f) } //2 litros por defecto
    val rangoLitros = 0.5f..10f // medio litro a 10 litros

    /* Horas */
    var horaIni         by remember { mutableStateOf(LocalTime.of(8,0)) }
    var mostrarTimePickerIni    by remember { mutableStateOf(false) }
    var horaFin         by remember { mutableStateOf(LocalTime.of(22,0)) }
    var mostrarTimePickerFin    by remember { mutableStateOf(false) }

    /* Recordatorios */
    var numeroRecordatorios by remember { mutableStateOf(10) }

    /*Frecuencia de recodatorios*/
    var minutosFrecuencia by remember { mutableStateOf(30) }
    var frecuenciaActiva by remember { mutableStateOf(false) }

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
                            text = formatearLitros(cantlitros.toDouble()),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        DurationSliderHidra(
                            value        = cantlitros,
                            onValueChange = { cantlitros = it },
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
                                HoraField(
                                    hora = horaIni,
                                    onClick = { mostrarTimePickerIni = true }
                                )
                            }
                            // Hora de fin
                            Box(modifier = Modifier.weight(1f)) {
                                HoraField(
                                    hora = horaFin,
                                    onClick = { mostrarTimePickerFin = true }
                                )
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
                }
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    Toast.makeText(context, "Configuración de hidratación guardada", Toast.LENGTH_SHORT).show()
                    navController.navigateUp()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar")
            }
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

private fun formatearLitros(cantidad: Double): String = when {
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
private fun DurationSliderHidra(
    value: Float,
    onValueChange: (Float) -> Unit,
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
                value = value,
                onValueChange = {
                    onValueChange(it)
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                },
                valueRange = valueRange,
                steps = 0, // ← para que sea continuo
                colors = SliderDefaults.colors(
                    activeTrackColor = Color.Transparent,
                    inactiveTrackColor = Color.Transparent,
                    activeTickColor = Color.Transparent,
                    inactiveTickColor = Color.Transparent,
                    thumbColor = Color.Transparent
                ),
                modifier = Modifier
                    .fillMaxSize()
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
