package com.example.koalm.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.koalm.R
import com.example.koalm.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaParametrosSalud(navController: NavController) {
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
                .padding(26.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.training),
                contentDescription = "Koala salud",
                modifier = Modifier
                    .size(180.dp)
                    .offset(y = (-24).dp)
            )

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-50).dp)
            ) {
                InfoMiniCard("Pasos", "7400/10000", Icons.AutoMirrored.Filled.DirectionsWalk)
                InfoMiniCard("Tiempo Activo", "73/100 min", Icons.Default.AccessTime)
                InfoMiniCard("Calorías", "320/500 kcal", Icons.Default.LocalFireDepartment)
            }

            Text(
                text = "Este dato es de la última información registrada, dd/mm/yy",
                fontSize = 9.sp,
                color = Color.Gray,
                modifier = Modifier.offset(y = (-40).dp)
            )

            InfoCardConProgreso("Sueño", "7 h 7 min", 0.88f, Icons.Default.Bedtime)
            InfoCard("Ritmo Cardíaco", "88 PPM", Icons.Default.Favorite)
            InfoCardConProgreso("Ansiedad", "Moderado", 0.6f, Icons.Default.SentimentNeutral)
            InfoCardConProgreso("Peso", "-2.5 kg perdidos", 0.5f, Icons.Default.MonitorWeight)
            InfoCard("Actividad diaria", "", Icons.AutoMirrored.Filled.DirectionsRun)

        }
    }
}

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
fun InfoCard(titulo: String, dato: String, icono: ImageVector) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = VerdeContenedor)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(icono, contentDescription = titulo, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(titulo, fontWeight = FontWeight.Bold)
                if (dato.isNotBlank()) {
                    Text(dato)
                }
            }
        }
    }
}

@Composable
fun InfoCardConProgreso(titulo: String, dato: String, progreso: Float, icono: ImageVector) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = VerdeContenedor)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(icono, contentDescription = titulo, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(titulo, fontWeight = FontWeight.Bold)
                Text(dato)
            }
            CircularProgressIndicator(
                progress = { progreso },
                modifier = Modifier.size(36.dp),
                strokeWidth = 4.dp,
                color = Color(0xFF5EAF4D)
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun VistaPreviaSalud() {
    val navController = rememberNavController()
    PantallaParametrosSalud(navController)
}
