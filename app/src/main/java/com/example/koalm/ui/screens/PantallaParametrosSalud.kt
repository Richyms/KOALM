package com.example.koalm.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel             // ← ViewModel en Compose
import androidx.navigation.NavController
import com.example.koalm.R
import com.example.koalm.ui.theme.*
import com.example.koalm.viewmodels.StepCounterViewModel        // ← tu ViewModel de pasos

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaParametrosSalud(
    navController: NavController,
    viewModel: StepCounterViewModel = viewModel()              // ← inyectamos el VM
) {
    /* --------- Pasos en tiempo real --------- */
    val pasos by viewModel.steps.observeAsState(0)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Parámetros de salud") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Inicio") },
                    label = { Text("Inicio") },
                    selected = false,
                    onClick = { /* Navegar a Inicio */ }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Star, contentDescription = "Hábitos") },
                    label = { Text("Hábitos") },
                    selected = false,
                    onClick = { /* Navegar a Hábitos */ }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = "Perfil") },
                    label = { Text("Perfil") },
                    selected = true,
                    onClick = { /* Ya estás aquí */ }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(vertical = 5.dp, horizontal = 26.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            /* --- Cabecera/imagen --- */
            Image(
                painter = painterResource(id = R.drawable.training),
                contentDescription = "Koala salud",
                modifier = Modifier
                    .size(150.dp)
                    .offset(y = (-10).dp)
            )

            /* --- Tarjetas mini --- */
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp, 0.dp)
            ) {
                InfoMiniCard(
                    "Pasos",
                    "$pasos/10000",                                  // ← valor dinámico
                    Icons.AutoMirrored.Filled.DirectionsWalk
                )
                InfoMiniCard("Tiempo Activo", "73/100 min", Icons.Default.AccessTime)
                InfoMiniCard("Calorías", "320/500 kcal", Icons.Default.LocalFireDepartment)
            }

         

            /* --- Cartas grandes --- */
            InfoCard(
                titulo = "Sueño",
                dato = "7 h 7 min",
                icono = Icons.Default.Bedtime,
                progreso = 0.88f,
                onClick = { navController.navigate("sueño-de-anoche") }
            )

            InfoCard("Ritmo Cardíaco", "88 PPM", Icons.Default.Favorite)
            InfoCard("Ansiedad", "Moderado", Icons.Default.PsychologyAlt, progreso = 0.6f)
            InfoCard("Peso", "-2.5 kg perdidos", Icons.Default.MonitorWeight, progreso = 0.5f)
            InfoCard("Actividad diaria", "", Icons.AutoMirrored.Filled.DirectionsRun)
        }
    }
}

/* ------------------------------------------------------------------ */
/*  COMPONENTES REUTILIZABLES                                         */
/* ------------------------------------------------------------------ */

@Composable
fun InfoMiniCard(titulo: String, dato: String, icono: ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icono, contentDescription = titulo, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(2.dp))
            Text(titulo, style = MaterialTheme.typography.labelSmall)
        }
        Text(dato, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun InfoCard(
    titulo: String,
    dato: String,
    icono: ImageVector,
    progreso: Float? = null,
    onClick: (() -> Unit)? = null
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .let { if (onClick != null) it.clickable { onClick() } else it },
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 2.dp,
        color = VerdeContenedor,
        border = BorderStroke(1.dp, Color.LightGray)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(icono, contentDescription = titulo, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(titulo, fontWeight = FontWeight.Bold)
                if (dato.isNotBlank()) Text(dato)
            }
            if (progreso != null) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(40.dp)
                ) {
                    CircularProgressIndicator(
                        progress = { progreso },
                        modifier = Modifier.fillMaxSize(),
                        strokeWidth = 4.dp,
                        color = VerdePrincipal
                    )

                    when (titulo) {
                        "Ansiedad" -> Icon(
                            imageVector = Icons.Default.SentimentNeutral,
                            contentDescription = "Nivel de ansiedad",
                            tint = Negro,
                            modifier = Modifier.size(20.dp)
                        )
                        "Sueño" -> Text(
                            text = "/8 h",
                            fontSize = 10.sp,
                            color = Negro,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}
