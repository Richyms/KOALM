package com.example.koalm.ui.screens.parametroSalud.niveles.peso

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.koalm.ui.components.BarraNavegacionInferior
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale



// ✅ Renombrada la clase para evitar conflictos
data class DatosActualizarPeso(
    val pesoActual: Float,
)

val datosMockActualizarPeso = DatosActualizarPeso(
    pesoActual = 72f,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaActualizarPeso(
    navController: NavHostController,
    datos: DatosActualizarPeso = datosMockActualizarPeso,
    onGuardar: () -> Unit = {}
) {
    val pesoActual = remember { mutableStateOf(datos.pesoActual) }
    val fechaHoy = LocalDate.now().format(
        DateTimeFormatter.ofPattern("d MMMM, yyyy", Locale("es", "MX"))
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Actualizar peso") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
                actions = {
                    IconButton(onClick = onGuardar) {
                        Icon(Icons.Default.Check, contentDescription = "Guardar", tint = Color.Black)
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
            Spacer(modifier = Modifier.height(16.dp))

            // Peso
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ComponenteInputs("Peso", pesoActual, fechaHoy)
            }
        }
    }
}



@Preview(showBackground = true, showSystemUi = true)
@Composable
fun VistaPreviaPantallaActualizarPeso() {
    PantallaActualizarPeso(
        navController = rememberNavController()
    )
}
