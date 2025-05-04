package com.example.koalm.ui.screens

/* ----------  IMPORTS  ---------- */
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.koalm.R
import com.example.koalm.ui.theme.*
import com.example.koalm.viewmodels.StepCounterViewModel

/* ----------  UI PRINCIPAL  ---------- */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaParametrosSalud(
    navController: NavController,
    datos: DatosSalud = datosMockSalud,
    viewModel: StepCounterViewModel = viewModel()          // ← VM de pasos
) {
    /* Pasos en vivo */
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
                NavigationBarItem(icon = { Icon(Icons.Default.Home, contentDescription = "Inicio") },  label = { Text("Inicio") },  selected = false, onClick = {})
                NavigationBarItem(icon = { Icon(Icons.Default.Star, contentDescription = "Hábitos") }, label = { Text("Hábitos") }, selected = false, onClick = {})
                NavigationBarItem(icon = { Icon(Icons.Default.Person, contentDescription = "Perfil") }, label = { Text("Perfil") }, selected = true,  onClick = {})
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 26.dp)
                .verticalScroll(rememberScrollState()),     // scroll vertical
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(5.dp))

            Image(
                painter = painterResource(id = R.drawable.training),
                contentDescription = "Koala salud",
                modifier = Modifier
                    .size(150.dp)
                    .offset(y = (-10).dp)
            )

            /* ---------- Tarjetas mini ---------- */
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                InfoMiniCard("Pasos", "$pasos/10000", Icons.AutoMirrored.Filled.DirectionsWalk)
                InfoMiniCard("Tiempo Activo", datos.tiempoActivo, Icons.Default.AccessTime)
                InfoMiniCard("Calorías", datos.calorias, Icons.Default.LocalFireDepartment)
            }

            Text(
                text = "Este dato es de la última información registrada, ${datos.fechaUltimaActualizacion}",
                fontSize = 11.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 5.dp, bottom = 10.dp)
            )

            /* ---------- Tarjetas grandes ---------- */
            InfoCard("Sueño",            datos.sueño,          Icons.Default.Bedtime,     datos.progresoSueño)     { navController.navigate("sueño-de-anoche") }
            InfoCard("Ritmo Cardíaco",   datos.ritmoCardiaco,  Icons.Default.Favorite)    { navController.navigate("ritmo-cardiaco") }
            InfoCard("Estrés",           datos.ansiedad,       Icons.Default.PsychologyAlt, datos.progresoAnsiedad){ navController.navigate("nivel-de-estres") }
            InfoCard("Peso",             datos.peso,           Icons.Default.MonitorWeight, datos.progresoPeso)     { navController.navigate("objetivos-peso") }
            InfoCard("Actividad diaria", datos.actividadDiaria,Icons.AutoMirrored.Filled.DirectionsRun)            { navController.navigate("actividad-diaria") }

            Spacer(modifier = Modifier.height(70.dp)) // deja espacio para bottomBar
        }
    }
}

/* ----------  COMPONENTES AUXILIARES  ---------- */
@Composable
fun InfoMiniCard(titulo: String, dato: String, icono: ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icono, contentDescription = titulo, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(2.dp))
            Text(titulo, style = MaterialTheme.typography.labelSmall)
        }
        Text(dato, fontWeight = FontWeight.Bold, fontSize = 15.sp)
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
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(40.dp)) {
                    CircularProgressIndicator(
                        progress = { progreso },
                        modifier = Modifier.fillMaxSize(),
                        strokeWidth = 4.dp,
                        color = VerdePrincipal
                    )
                    if (titulo == "Sueño") {
                        Text(
                            text = "/8 h",
                            fontSize = 10.sp,
                            color = Negro,
                            fontWeight = FontWeight.SemiBold
                        )
                    } else if (titulo == "Estrés") {
                        Icon(
                            imageVector = Icons.Default.SentimentNeutral,
                            contentDescription = "Nivel de estrés",
                            tint = Negro,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

/* ----------  MODELO DE DATOS (mockeable)  ---------- */
data class DatosSalud(
    val fechaUltimaActualizacion: String,
    val pasos: String,
    val tiempoActivo: String,
    val calorias: String,
    val sueño: String,
    val progresoSueño: Float,
    val ritmoCardiaco: String,
    val ansiedad: String,
    val progresoAnsiedad: Float,
    val peso: String,
    val progresoPeso: Float,
    val actividadDiaria: String
)

/* ----------  Datos de prueba para Preview  ---------- */
val datosMockSalud = DatosSalud(
    fechaUltimaActualizacion = "02/05/2025",
    pasos = "7400/10000",
    tiempoActivo = "73/100 min",
    calorias = "320/500 kcal",
    sueño = "7 h 7 min",
    progresoSueño = 0.88f,
    ritmoCardiaco = "88 PPM",
    ansiedad = "Moderado",
    progresoAnsiedad = 0.6f,
    peso = "-2.5 kg perdidos",
    progresoPeso = 0.5f,
    actividadDiaria = "Completada"
)

/* ----------  PREVIEW  ---------- */
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun VistaPreviaPantallaParametrosSalud() {
    val navController = rememberNavController()
    PantallaParametrosSalud(
        navController = navController,
        datos = datosMockSalud
    )
}
