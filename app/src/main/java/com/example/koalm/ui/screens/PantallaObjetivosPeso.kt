package com.example.koalm.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.koalm.ui.components.BarraNavegacionInferior
import com.example.koalm.ui.theme.VerdePrincipal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaObjetivosPeso(
    navController: NavHostController,
    pesoInicial: Float,
    fechaInicial: String,
    pesoActual: Float,
    pesoObjetivo: Float
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Objetivos") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
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

            // Peso inicial
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Peso inicial", fontWeight = FontWeight.SemiBold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$pesoInicial kg el $fechaInicial",
                        color = VerdePrincipal,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Default.CalendarToday, contentDescription = null, tint = VerdePrincipal)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Peso actual
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Peso actual", fontWeight = FontWeight.SemiBold)
                Text("$pesoActual kg", color = VerdePrincipal)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Peso objetivo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Peso objetivo", fontWeight = FontWeight.SemiBold)
                Text("$pesoObjetivo kg", color = VerdePrincipal)
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun VistaPreviaPantallaObjetivos() {
    val navController = rememberNavController()
    PantallaObjetivosPeso(
        navController = navController,
        pesoInicial = 74.5f,
        fechaInicial = "1 de febrero del 2025",
        pesoActual = 72f,
        pesoObjetivo = 69f
    )
}
