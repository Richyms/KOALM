package com.example.koalm.ui.screens.parametroSalud.niveles.ritmoCardiaco

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.*
import com.example.koalm.ui.components.BarraNavegacionInferior
import com.example.koalm.ui.theme.*
import androidx.compose.ui.draw.clip
import com.example.koalm.ui.components.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaRitmoCardiaco(
    navController: NavHostController,
    datos: DatosRitmoCardiaco = datosMockRitmo
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ritmo cardíaco") },
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
                    imageVector = Icons.Default.FavoriteBorder,
                    contentDescription = null,
                    tint = Color.Red,
                    modifier = Modifier.size(50.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("${datos.ritmo} LPM", fontSize = 36.sp, fontWeight = FontWeight.Bold)
            }

            Text(
                "Este dato es de la última información registrada, ${datos.fechaUltimaInfo}",
                fontSize = 11.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = GrisCard)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    GraficaRitmoCardiaco(datos = datos.datos)
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            val zonasFormateadas = listOf(
                Triple("Ligero", "00:02:11", AzulLigero),
                Triple("Intensivo", "00:03:52", AmarilloIntensivo),
                Triple("VO Máx", "00:01:23", RojoVOMAx),
                Triple("Aeróbico", "00:17:01", VerdeAerobico),
                Triple("Anaeróbico", "00:08:59", NaranjaAnaerobico)
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(32.dp)) {
                    zonasFormateadas.take(3).forEach { (zona, tiempo, color) ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(tiempo, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(zona, fontSize = 12.sp, color = GrisMedio)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(64.dp)) {
                    zonasFormateadas.drop(3).forEach { (zona, tiempo, color) ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(tiempo, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(zona, fontSize = 12.sp, color = GrisMedio)
                        }
                    }
                }
            }
        }
    }
}

val datosMockRitmo = DatosRitmoCardiaco(
    ritmo = 135,
    fechaUltimaInfo = "23/04/25",
    datos = listOf(180f, 60f, 140f, 90f, 88f, 112f, 50f, 145f, 160f, 190f)
)

