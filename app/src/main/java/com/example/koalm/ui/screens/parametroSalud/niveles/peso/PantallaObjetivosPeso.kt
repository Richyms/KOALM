@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.koalm.ui.screens.parametroSalud.niveles.peso

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.koalm.ui.components.BarraNavegacionInferior
import com.example.koalm.viewmodels.ObjetivosPesoViewModel

@Composable
fun PantallaObjetivosPeso(
    navController: NavHostController,
    viewModel: ObjetivosPesoViewModel = viewModel()
) {
    // Estado desde el ViewModel
    val pesoIni  by viewModel.pesoInicial.collectAsState()
    val fechaIni by viewModel.fechaInicial.collectAsState()
    val pesoAct  by viewModel.pesoActual.collectAsState()
    val fechaAct by viewModel.fechaActual.collectAsState()
    val pesoObj  by viewModel.pesoObjetivo.collectAsState()

    val green = Color(0xFF4CAF50)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Objetivos de peso") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        // Guardar pesoObjetivo en Firestore
                        viewModel.guardarObjetivo {
                            navController.navigateUp()
                        }
                    }) {
                        Icon(Icons.Default.Check, contentDescription = "Guardar")
                    }
                }
            )
        },
        bottomBar = {
            BarraNavegacionInferior(navController, "inicio")
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .padding(24.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            // 1) Peso inicial (sólo lectura)
            FieldRow(
                label = "Peso inicial",
                value = pesoIni.toString(),
                fecha = fechaIni,
                editable = false,
                onValueChange = {}
            )

            // 2) Peso actual (sólo lectura)
            FieldRow(
                label = "Peso actual",
                value = pesoAct.toString(),
                fecha = fechaAct,
                editable = false,
                onValueChange = {}
            )

            // 3) Peso objetivo (editable, actualiza ViewModel)
            FieldRow(
                label = "Peso objetivo",
                value = if (pesoObj == 0f) "" else pesoObj.toString(),
                fecha = null,
                editable = true,
                onValueChange = { nuevoTexto ->
                    nuevoTexto.toFloatOrNull()?.let { nuevo ->
                        viewModel.setObjetivo(nuevo)
                    }
                }
            )
        }
    }
}

@Composable
private fun FieldRow(
    label: String,
    value: String,
    fecha: String?,
    editable: Boolean,
    onValueChange: (String) -> Unit
) {
    val green = Color(0xFF4CAF50)
    val shape = RoundedCornerShape(4.dp)

    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 16.sp, color = Color.Black)

        if (editable) {
            Box(
                Modifier
                    .width(80.dp)
                    .height(30.dp)
                    .border(BorderStroke(1.dp, green), shape)
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    textStyle = TextStyle(fontSize = 16.sp, color = Color.Black),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        } else {
            Text(value, color = Color.Black, fontSize = 16.sp)
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("kg", color = green, fontSize = 14.sp)
            fecha?.let {
                Spacer(Modifier.width(6.dp))
                Text("el $it", color = green, fontSize = 14.sp)
                Spacer(Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = green,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
