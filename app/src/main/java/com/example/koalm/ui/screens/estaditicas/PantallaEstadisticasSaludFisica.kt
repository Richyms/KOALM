package com.example.koalm.ui.screens.estaditicas

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.koalm.ui.theme.*
import com.example.koalm.ui.components.BarraNavegacionInferior
import com.example.koalm.model.TipoHabito
import com.example.koalm.model.ClaseHabito
import com.example.koalm.model.Habito

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaEstadisticasSaludFisica(navController: NavHostController) {
    // Filtramos solo los hábitos físicos usando tu enum ClaseHabito
    val habitosFisicos = listOf(
        Habito(titulo = "Sueño", clase = ClaseHabito.FISICO, tipo = TipoHabito.SUEÑO),
        Habito(titulo = "Alimentación", clase = ClaseHabito.FISICO, tipo = TipoHabito.ALIMENTACION),
        Habito(titulo = "Hidratación", clase = ClaseHabito.FISICO, tipo = TipoHabito.HIDRATACION),
        Habito(titulo = "Actividad Física", clase = ClaseHabito.FISICO)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Estadísticas de hábitos de salud física") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Regresar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            BarraNavegacionInferior(
                navController = navController,
                rutaActual = "salud_fisica"
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ){
            habitosFisicos.forEach { habito ->
                StatsHabitoCard(habito, navController)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StatsHabitoCard(habito: Habito, navController: NavHostController) {
    val icono = when(habito.tipo) {
        TipoHabito.SUEÑO -> Icons.Default.Bedtime
        TipoHabito.ALIMENTACION -> Icons.Default.Restaurant
        TipoHabito.HIDRATACION -> Icons.Default.WaterDrop
        else -> Icons.Default.FitnessCenter
    }

    Card(
        onClick = { /* Navegar a detalles del hábito */ },
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, VerdeBorde, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = VerdeContenedor.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icono,
                contentDescription = null,
                tint = Negro,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = habito.titulo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                // Puedes añadir más información aquí como progreso, etc.
            }
        }
    }
}