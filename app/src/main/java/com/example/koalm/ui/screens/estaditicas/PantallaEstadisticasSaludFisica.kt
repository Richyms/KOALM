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
import com.example.koalm.ui.screens.habitos.saludFisica.HabitoFisico


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaEstadísticasSaludFisica(navController: NavHostController) {
    val habitos = listOf(
        HabitoFisico("Sueño", ""),
        HabitoFisico("Alimentación", ""),
        HabitoFisico("Hidratación", ""),
        HabitoFisico("Por ajustar", "")
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
        ) {

            habitos.forEach { habito ->
                StatsHabitoFisicoCard(habito, navController)
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StatsHabitoFisicoCard(habito: HabitoFisico, navController: NavHostController) {
    Card(
        onClick = {},
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
                imageVector = if (habito.titulo == "Sueño") Icons.Default.Bed else if (habito.titulo == "Alimentación") Icons.Default.LocalDining else if (habito.titulo == "Hidratación") Icons.Default.WaterDrop else Icons.Default.DoNotDisturb,
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
            }
        }
    }
}