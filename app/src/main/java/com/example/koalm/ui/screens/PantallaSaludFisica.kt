package com.example.koalm.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.koalm.ui.theme.*
import com.example.koalm.ui.components.BarraNavegacionInferior

data class HabitoFisico(
    val titulo: String,
    val descripcion: String,
    val completado: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaSaludFisica(navController: NavHostController) {
    val habitos = listOf(
        HabitoFisico("Sueño", "Registra tus horas de sueño y mejora tu descanso."),
        HabitoFisico("Alimentación", "Comer a tus horas, nutre más que los alimentos."),
        HabitoFisico("Hidratación", "Recuerda hidratarte cada día"),
        HabitoFisico("Por ajustar", "Se agregara cuando se tenga")
    )
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de hábitos de salud fisica") },
                navigationIcon = {
                    IconButton(onClick = navController::navigateUp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
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
            Text(
                text = "Plantilla de hábitos",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            habitos.forEach { habito ->
                HabitoFisicoCard(habito, navController)
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitoFisicoCard(habito: HabitoFisico, navController: NavHostController) {
    Card(
        onClick = {
            if (habito.titulo == "Sueño") {
                navController.navigate("configurar_habito_sueno")
            }else{
                if(habito.titulo == "Hidratación"){
                    navController.navigate("configurar_habito_hidratacion")
                }
                if(habito.titulo == "Alimentación"){
                    navController.navigate("configurar_habito_alimentacion")
                }
            }
        },
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
                imageVector = if (habito.completado) Icons.Default.CheckCircle else Icons.Default.AddCircle,
                contentDescription = if (habito.completado) "Hábito completado" else "Agregar hábito",
                tint = VerdePrincipal,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = habito.titulo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = habito.descripcion,
                    style = MaterialTheme.typography.bodyMedium,
                    color = GrisMedio
                )
            }
        }
    }
}