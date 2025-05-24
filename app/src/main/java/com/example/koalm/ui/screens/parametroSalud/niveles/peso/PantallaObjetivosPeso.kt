package com.example.koalm.ui.screens.parametroSalud.niveles.peso

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.koalm.ui.components.BarraNavegacionInferior
import com.example.koalm.ui.theme.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.MutableState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaObjetivosPeso(
    navController: NavHostController,
    datos: DatosPeso = datosMockPeso
) {
    val pesoInicial = remember { mutableStateOf(datos.pesoInicial) }

    val pesoObjetivo = remember { mutableStateOf(datos.pesoObjetivo) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Objetivos") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
                actions = {
                    IconButton(onClick = { /* guardar cambios */ }) {
                        Icon(Icons.Default.Check, contentDescription = "Guardar")
                    }
                }
            )
        },
        bottomBar = {
            BarraNavegacionInferior(navController, "inicio")
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 10.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Top
        ) {
            ComponenteInputs("Peso inicial", pesoInicial, datos.fechaInicial)
            ComponenteInputs("Peso objetivo", pesoObjetivo)

        }
    }
}

@Composable
fun ComponenteInputs(
    textoLabel: String,
    dato: MutableState<Float>,
    fecha: String? = null
) {
    val texto = remember { mutableStateOf(dato.value.toFloat().toString()) }

    Spacer(modifier = Modifier.height(16.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(textoLabel, fontWeight = FontWeight.SemiBold)

        val cajaTexto = @Composable {
            Box(
                modifier = Modifier
                    .width(60.dp)
                    .border(1.dp, VerdePrincipal, shape = RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                BasicTextField(
                    value = texto.value,
                    onValueChange = {
                        texto.value = it
                        dato.value = it.toFloatOrNull() ?: dato.value
                    },
                    textStyle = LocalTextStyle.current.copy(
                        color = VerdePrincipal,
                        fontSize = 16.sp
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        if (fecha != null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                cajaTexto()
                Spacer(modifier = Modifier.width(6.dp))
                Text("kg el $fecha", color = VerdePrincipal, fontSize = 16.sp)
                Spacer(modifier = Modifier.width(2.dp))
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = VerdePrincipal,
                    modifier = Modifier.size(17.dp)
                )
            }
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                cajaTexto()
                Spacer(modifier = Modifier.width(6.dp))
                Text("kg", color = VerdePrincipal, fontSize = 16.sp)
            }
        }
    }
}


data class DatosPeso(
    val pesoInicial: Float,
    val fechaInicial: String,
    val pesoActual: Float,
    val pesoObjetivo: Float
)

val datosMockPeso = DatosPeso(
    pesoInicial = 74.5f,
    fechaInicial = "01/02/2025",
    pesoActual = 72f,
    pesoObjetivo = 69f
)

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun VistaPreviaPantallaObjetivosPeso() {
    PantallaObjetivosPeso(navController = rememberNavController())
}