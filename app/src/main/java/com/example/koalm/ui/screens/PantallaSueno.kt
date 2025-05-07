package com.example.koalm.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.koalm.ui.components.BarraNavegacionInferior
import com.example.koalm.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import com.example.koalm.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaSueno(
    navController: NavHostController,
    datos: DatosSueno = datosMockSueño
) {
    val fecha = LocalDate.parse(datos.fecha)
    val fechaFormateada = fecha.format(DateTimeFormatter.ofPattern("dd/MM/yy"))

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sueño de anoche") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        },
        bottomBar = {
            BarraNavegacionInferior(navController, "sueño")
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${datos.puntos}",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.alignByBaseline()
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "pts",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.alignByBaseline()
                )
            }

            Text(
                text = "Este dato es de la última información registrada, $fechaFormateada",
                fontSize = 11.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- Gráfica original restaurada ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = GrisCard)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${datos.horas} h ${datos.minutos} m",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Medium
                    )

                    val dias = listOf("L", "M", "X", "J", "V", "S", "D")

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        dias.zip(datos.historialSemanal).forEach { (dia, sueno) ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Bottom
                            ) {
                                BarraSueno(
                                    suenoLigero = sueno.ligero,
                                    suenoProfundo = sueno.profundo,
                                    despierto = sueno.despierto
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(dia, fontSize = 12.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(22.dp))

                    Column(
                        horizontalAlignment = Alignment.Start,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 34.dp, vertical = 5.dp)
                    ) {
                        LeyendaColor("${datos.sueñoLigero} h", "Sueño ligero", GrisMedio)
                        LeyendaColor("${datos.sueñoProfundo} h", "Sueño profundo", VerdePrincipal)
                        LeyendaColor("${datos.tiempoDespierto} h", "Tiempo despierto", MarronKoala)
                    }
                }
            }
        }
    }
}

