package com.example.koalm.ui.screens.parametroSalud

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.koalm.R
import com.example.koalm.data.StepCounterRepository
import com.example.koalm.ui.components.snapshotsAsState
import com.example.koalm.ui.components.BarraNavegacionInferior
import com.example.koalm.ui.theme.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.time.LocalDate

/* ----------  UI PRINCIPAL  ---------- */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaParametrosSalud(
    navController: NavHostController
) {
    val correo = FirebaseAuth.getInstance().currentUser?.email
    val metas = remember(correo) {
        Firebase.firestore.collection("usuarios")
            .document(correo ?: "")
            .collection("metasSalud")
            .document("valores")
    }

    val metaPasos by metas.snapshotsAsState { it?.getLong("metaPasos")?.toInt() ?: 10000 }
    val metaMinutos by metas.snapshotsAsState { it?.getLong("metaMinutos")?.toInt() ?: 100 }
    val metaCalorias by metas.snapshotsAsState { it?.getLong("metaCalorias")?.toInt() ?: 500 }

    val pasos by StepCounterRepository.steps.collectAsState()
    val segundos by StepCounterRepository.activeSeconds.collectAsState()
    val minutos = segundos / 60

    val today = LocalDate.now().toString()
    val calorias: Int = if (correo != null) {
        val doc = remember(correo, today) {
            Firebase.firestore.collection("usuarios")
                .document(correo)
                .collection("metricasDiarias")
                .document(today)
        }
        val c by doc.snapshotsAsState { it?.getLong("calorias")?.toInt() ?: 0 }
        c
    } else 0

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
            BarraNavegacionInferior(
                navController = navController,
                rutaActual = "estadisticas"
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 26.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(5.dp))

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
                InfoMiniCard("Pasos", "$pasos/$metaPasos", Icons.AutoMirrored.Filled.DirectionsWalk)
                InfoMiniCard("Tiempo Activo", "$minutos/$metaMinutos min", Icons.Default.AccessTime)
                InfoMiniCard("Calorías", "$calorias kcal/$metaCalorias kcal", Icons.Default.LocalFireDepartment)
            }

            /* ---------- Tarjetas grandes (mock) ---------- */
            InfoCard("Sueño", "7 h 7 min", Icons.Default.Bedtime, 0.88f) {
                navController.navigate("sueño-de-anoche")
            }
            InfoCard("Ritmo Cardíaco", "88 PPM", Icons.Default.Favorite) {
                navController.navigate("ritmo-cardiaco")
            }
            InfoCard("Estrés", "Moderado", Icons.Default.PsychologyAlt, 0.6f) {
                navController.navigate("nivel-de-estres")
            }
            InfoCard("Peso", "-2.5 kg", Icons.Default.MonitorWeight, 0.5f) {
                navController.navigate("control-peso")
            }
            InfoCard("Actividad diaria", "Completada", Icons.AutoMirrored.Filled.DirectionsRun) {
                navController.navigate("actividad-diaria")
            }

            Spacer(Modifier.height(70.dp))
        }
    }
}

/* ----------  COMPONENTES AUXILIARES  ---------- */
@Composable
fun InfoMiniCard(titulo: String, dato: String, icono: ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icono, contentDescription = titulo, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(2.dp))
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
            .let { if (onClick != null) it.clickable(onClick = onClick) else it },
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
            Spacer(Modifier.width(16.dp))
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
                        Text("/8 h", fontSize = 10.sp, color = Negro, fontWeight = FontWeight.SemiBold)
                    } else if (titulo == "Estrés") {
                        Icon(
                            Icons.Default.SentimentNeutral,
                            contentDescription = null,
                            tint = Negro,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

/* ----------  PREVIEW  ---------- */
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun VistaPreviaPantallaParametrosSalud() {
    PantallaParametrosSalud(rememberNavController())
}
