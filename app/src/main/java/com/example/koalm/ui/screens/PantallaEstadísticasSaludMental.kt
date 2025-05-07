package com.example.koalm.ui.screens

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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaEstadísticasSaludMental(navController: NavHostController) {
    val habitos = listOf(
        HabitoMental("Lectura",""),
        HabitoMental("Meditación",""),
        HabitoMental("Desconexión digital",""),
        HabitoMental("Escritura","")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Estadísticas de hábitos de salud mental") },
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
                rutaActual = "salud_mental"
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
                StatsHabitoMentalCard(habito, navController)
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
 private fun StatsHabitoMentalCard(habito: HabitoMental, navController: NavHostController) {
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
                imageVector = if (habito.titulo == "Lectura") Icons.Default.Book else if (habito.titulo == "Meditación") Icons.Default.SelfImprovement else if (habito.titulo == "Desconexión digital") Icons.Default.NoCell else Icons.Default.Create,
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