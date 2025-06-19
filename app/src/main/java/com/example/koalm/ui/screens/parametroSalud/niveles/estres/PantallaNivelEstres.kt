package com.example.koalm.ui.screens.parametroSalud.niveles.estres

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.SentimentNeutral
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.koalm.ui.components.*
import com.example.koalm.ui.theme.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaEstres(
    navController: NavHostController
) {
    val context = LocalContext.current
    val correo = FirebaseAuth.getInstance().currentUser?.email
    val ansiedad = remember(correo) {
        Firebase.firestore.collection("resultadosAnsiedad")
            .document(correo ?: "")
            .collection("historial")
            .document("Resultado 1")
    }
    val ResAnsiedad by ansiedad.snapshotsAsState { it?.getString("nivel")?.toString()}
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nivel de estrés") },
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.SentimentNeutral,
                    contentDescription = null,
                    tint = VerdePrincipal,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${ResAnsiedad}",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = GrisCard)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    ) {
                        GraficaEstres(
                            valores = listOf(0.6f, 0.7f, 1f, 0.8f, 0.4f, 0.6f, 0.5f, 0.3f, 0.2f, 0.4f, 0.3f, 0.5f, 0.6f, 0.4f, 0.8f, 0.7f, 0.6f, 0.5f, 0.7f, 0.9f, 1f, 0.9f, 0.6f, 0.3f),
                            colores = listOf(0.6f, 0.7f, 1f, 0.8f, 0.4f, 0.6f, 0.5f, 0.3f, 0.2f, 0.4f, 0.3f, 0.5f, 0.6f, 0.4f, 0.8f, 0.7f, 0.6f, 0.5f, 0.7f, 0.9f, 1f, 0.9f, 0.6f, 0.3f).map {
                                when {
                                    it > 0.8f -> MarronKoala
                                    it > 0.5f -> GrisMedio
                                    else -> VerdePrincipal
                                }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 52.dp, end = 3.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("0", fontSize = 10.sp)
                        Text("12", fontSize = 10.sp)
                        Text("24", fontSize = 10.sp)
                    }

                    Spacer(modifier = Modifier.height(22.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column {
                            Text("Promedio de estrés", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(VerdePrincipal, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Medio")
                            }
                        }

                        Column {
                            Text("Mayor estrés", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("${ResAnsiedad}")
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = { navController.navigate("test_de_ansiedad") },
                colors = ButtonDefaults.buttonColors(containerColor = VerdePrincipal),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .height(40.dp)
                    .width(150.dp)
                    .align(Alignment.CenterHorizontally)
            ) {
                Text("Realizar test", fontSize = 16.sp)
            }
        }
    }
}



