package com.example.koalm.ui.screens.parametroSalud.niveles.estres

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.SentimentNeutral
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.koalm.ui.components.BarraNavegacionInferior
    import com.example.koalm.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaEstres(
    navController: NavHostController,
    datos: DatosEstres = datosMockEstres
) {
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
                    text = datos.nivel,
                    fontSize = 36.sp,
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
                        Box(
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .fillMaxHeight()
                                .width(40.dp)
                        ) {
                            val niveles = listOf("Alto" to 0.8f, "Medio" to 0.5f, "Bajo" to 0.2f)
                            niveles.forEach { (texto, y) ->
                                Text(
                                    text = texto,
                                    fontSize = 11.sp,
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .offset(y = -(y * 170).dp)
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val heightPx = size.height
                                val widthPx = size.width
                                val lineY = listOf(0.2f, 0.5f, 0.8f)
                                lineY.forEach { y ->
                                    val yPos = heightPx * (1 - y)
                                    drawLine(
                                        color = Color.LightGray.copy(alpha = 0.5f),
                                        start = androidx.compose.ui.geometry.Offset(0f, yPos),
                                        end = androidx.compose.ui.geometry.Offset(widthPx, yPos),
                                        strokeWidth = 2f,
                                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxSize(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                datos.valores.forEachIndexed { index, valor ->
                                    Box(
                                        modifier = Modifier
                                            .width(6.dp)
                                            .height((valor * 160).dp)
                                            .clip(RoundedCornerShape(topStart = 50.dp, topEnd = 50.dp))
                                            .background(datos.colores.getOrNull(index) ?: GrisCard)
                                    )
                                }
                            }
                        }
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
                                Text(datos.promedio)
                            }
                        }

                        Column {
                            Text("Mayor estrés", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("${datos.mayorInicio} - ${datos.mayorFin}")
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { /* TODO: Agregar acción real */ },
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
    }
}

data class DatosEstres(
    val nivel: String,
    val promedio: String,
    val mayorInicio: String,
    val mayorFin: String,
    val valores: List<Float>,
    val colores: List<Color>
)

val datosMockEstres = DatosEstres( // Estos datos van a ser recuperados del back
    nivel = "Medio",
    promedio = "Bajo-Medio",
    mayorInicio = "00:09:52",
    mayorFin = "00:10:49",
    valores = listOf(0.6f, 0.7f, 1f, 0.8f, 0.4f, 0.6f, 0.5f, 0.3f, 0.2f, 0.4f, 0.3f, 0.5f, 0.6f, 0.4f, 0.8f, 0.7f, 0.6f, 0.5f, 0.7f, 0.9f, 1f, 0.9f, 0.6f, 0.3f),
    colores = listOf(0.6f, 0.7f, 1f, 0.8f, 0.4f, 0.6f, 0.5f, 0.3f, 0.2f, 0.4f, 0.3f, 0.5f, 0.6f, 0.4f, 0.8f, 0.7f, 0.6f, 0.5f, 0.7f, 0.9f, 1f, 0.9f, 0.6f, 0.3f).map {
        when {
            it > 0.8f -> MarronKoala
            it > 0.5f -> GrisMedio
            else -> VerdePrincipal
        }
    }
)