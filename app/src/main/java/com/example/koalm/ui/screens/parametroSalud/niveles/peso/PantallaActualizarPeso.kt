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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.koalm.ui.components.BarraNavegacionInferior
import com.example.koalm.ui.theme.VerdePrincipal

// ✅ Renombrada la clase para evitar conflictos
data class DatosActualizarPeso(
    val pesoActual: Float,
    val fecha: String
)

val datosMockActualizarPeso = DatosActualizarPeso(
    pesoActual = 72f,
    fecha = "6 abril, 2025"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaActualizarPeso(
    navController: NavHostController,
    datos: DatosActualizarPeso = datosMockActualizarPeso,
    onGuardar: () -> Unit = {}
) {
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
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Peso (kg)", fontWeight = FontWeight.SemiBold)
                Text(datos.pesoActual.toString(), color = VerdePrincipal)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Fecha
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Fecha", fontWeight = FontWeight.SemiBold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = datos.fecha,
                        color = VerdePrincipal,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Default.CalendarToday, contentDescription = null, tint = VerdePrincipal)
                }
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
