package com.example.koalm.ui.screens.parametroSalud

/* ----------  IMPORTS  ---------- */
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.koalm.ui.theme.*
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.koalm.ui.components.BarraNavegacionInferior

/* ----------  DATA CLASS ---------- */
data class PesoEntrada(val fecha: String, val peso: String)

/* ----------  PANTALLA PROGRESO ---------- */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaProgresoPeso(
    navController: NavHostController
) {
    // Datos simulados (más adelante conecta con backend)
    val entradas = listOf(
        PesoEntrada("Martes, 27 Mayo, 2025", "67.2 kg"),
        PesoEntrada("Miércoles, 7 Mayo, 2025", "70 kg"),
        PesoEntrada("Viernes, 20 Abril, 2025", "73 kg")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Progreso") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        },
        bottomBar = {
            // Puedes usar tu BarraNavegacionInferior si quieres
            BarraNavegacionInferior(navController = navController, rutaActual = "progreso-peso")
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                Text("Aquí va la gráfica", color = Color.DarkGray)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("Entradas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, fontSize = 16.sp)

            Divider()

            LazyColumn {
                items(entradas) { entrada ->
                    EntradaPesoItem(entrada)
                    Divider()
                }
            }
        }
    }
}

/* ----------  ITEM DE ENTRADA ---------- */
@Composable
fun EntradaPesoItem(entrada: PesoEntrada) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(entrada.fecha, fontSize = 16.sp, color = VerdePrincipal)
            Text(entrada.peso, fontSize = 15.sp)
        }
        Icon(
            imageVector = Icons.Default.Photo,
            contentDescription = "Foto de avance",
            modifier = Modifier.size(36.dp)
        )
    }
}

/* ----------  PREVIEW ---------- */
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewPantallaProgreso() {
    PantallaProgresoPeso(navController = rememberNavController())
}
