package com.example.koalm.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.koalm.ui.components.BarraNavegacionInferior
import androidx.compose.ui.text.font.FontWeight
import com.example.koalm.ui.theme.GrisCard
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import com.example.koalm.ui.theme.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaSueno(
    navController: NavHostController,
    puntos: Int,
    fechaStr: String,
    horasDormidas: Int,
    minutosDormidos: Int,
    suenoLigeroStr: String,
    suenoProfundoStr: String,
    tiempoDespiertoStr: String
) {

    val fecha = LocalDate.parse(fechaStr)
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yy")
    val fechaFormateada = fecha.format(formatter)


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

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$puntos",
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
                        text = "$horasDormidas h $minutosDormidos m",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Medium
                    )

                    val dias = listOf("L", "M", "X", "J", "V", "S", "D")
                    val suenoData = listOf(
                        Triple(3.2f, 4.0f, 0.5f),
                        Triple(3.0f, 4.2f, 0.3f),
                        Triple(3.1f, 4.1f, 0.4f),
                        Triple(3.4f, 4.0f, 0.4f),
                        Triple(3.2f, 4.0f, 0.3f),
                        Triple(2.5f, 3.5f, 0.4f),
                        Triple(2.0f, 1.5f, 1.2f)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        dias.zip(suenoData).forEach { (dia, datos) ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Bottom
                            ) {
                                BarraSueno(
                                    suenoLigero = datos.first,
                                    suenoProfundo = datos.second,
                                    despierto = datos.third,
                                    width = 9.dp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(dia, fontSize = 12.sp)
                            }
                        }
                    }


                    Spacer(modifier = Modifier.height(16.dp))

                    // Leyenda
                    Column(
                        horizontalAlignment = Alignment.Start,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 34.dp, vertical = 5.dp)
                    ) {
                        LeyendaColor(duracion = "$suenoLigeroStr h", etiqueta = "Sueño ligero", color = GrisMedio)
                        LeyendaColor(duracion = "$suenoProfundoStr h", etiqueta = "Sueño profundo", color = VerdePrincipal)
                        LeyendaColor(duracion = "$tiempoDespiertoStr h", etiqueta = "Tiempo despierto", color = MarronKoala)

                    }
                }
            }
        }
    }
}

@Composable
fun BarraSueno(
    suenoLigero: Float,
    suenoProfundo: Float,
    despierto: Float,
    height: Dp = 150.dp,
    width: Dp = 6.dp
) {
    val total = suenoLigero + suenoProfundo + despierto
    val ligeroRatio = suenoLigero / total
    val profundoRatio = suenoProfundo / total
    val despiertoRatio = despierto / total

    Box(
        modifier = Modifier
            .height(height)
            .width(width)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(50.dp)),
            verticalArrangement = Arrangement.Bottom
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(despiertoRatio)
                    .background(MarronKoala)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(profundoRatio)
                    .background(VerdePrincipal)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(ligeroRatio)
                    .background(GrisMedio)
            )
        }
    }
}




@Composable
fun LeyendaColor(duracion: String, etiqueta: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color = color, shape = CircleShape)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text("$duracion  -  $etiqueta", fontSize = 18.sp)
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun VistaPreviaSueno() {
    val navController = rememberNavController()
    PantallaSueno(
        navController = navController,
        puntos = 10,
        fechaStr = "2024-04-21",
        horasDormidas = 4,
        minutosDormidos = 27,
        suenoLigeroStr = "3 h 22 m",
        suenoProfundoStr = "4 h 44 m",
        tiempoDespiertoStr = "0 h 21 m"
    )
}


