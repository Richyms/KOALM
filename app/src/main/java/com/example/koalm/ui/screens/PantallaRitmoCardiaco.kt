package com.example.koalm.ui.screens

import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.koalm.ui.components.BarraNavegacionInferior
import com.example.koalm.ui.theme.*
import androidx.compose.ui.draw.clip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaRitmoCardiaco(
    navController: NavHostController,
    ritmo: Int,
    fechaUltimaInfo: String,
    datos: List<Float>,
    zonas: List<Pair<String, String>>
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
                Text("$ritmo LPM", fontSize = 36.sp, fontWeight = FontWeight.Bold)
            }

            Text(
                "Este dato es de la última información registrada, $fechaUltimaInfo",
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
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .padding(start = 40.dp)

                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val stepX = size.width / (datos.size - 1)
                            val stepY = size.height / 200f

                            val puntos = datos.mapIndexed { i, v ->
                                Offset(x = i * stepX, y = size.height - v * stepY)
                            }

                            val path = Path().apply {
                                moveTo(puntos.first().x, size.height)
                                puntos.forEach { lineTo(it.x, it.y) }
                                lineTo(puntos.last().x, size.height)
                                close()
                            }

                            // Gradiente
                            drawPath(
                                path = path,
                                brush = Brush.verticalGradient(
                                    colors = listOf(Color(0xFFB7EACB), Color.Transparent)
                                )
                            )

                            // Línea
                            for (i in 0 until puntos.size - 1) {
                                drawLine(
                                    color = Color(0xFFF8844F),
                                    start = puntos[i],
                                    end = puntos[i + 1],
                                    strokeWidth = 3f
                                )
                            }

                            // Puntos
                            datos.forEachIndexed { index, valor ->
                                val punto = puntos[index]
                                drawCircle(
                                    color = colorZona(valor),
                                    radius = 4.dp.toPx(),
                                    center = punto
                                )
                            }

                            // Líneas horizontales (Y)
                            val etiquetasY = listOf(56, 84, 112, 140, 196)
                            val stepEtiY = size.height / 200f
                            etiquetasY.forEach { y ->
                                val posY = size.height - (y * stepEtiY)
                                drawLine(
                                    color = Color.LightGray.copy(alpha = 0.5f),
                                    start = Offset(0f, posY),
                                    end = Offset(size.width, posY),
                                    strokeWidth = 1f,
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                                )

                                // Etiqueta Y
                                drawContext.canvas.nativeCanvas.drawText(
                                    y.toString(),
                                    -60f,
                                    posY - 2.dp.toPx(),
                                    android.graphics.Paint().apply {
                                        color = GrisMedio.toArgb()
                                        textSize = 24f
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Etiquetas de tiempo (X)
                    val etiquetasX = listOf("0", "3", "6", "12", "15", "18", "21", "24")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 36.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        etiquetasX.forEach {
                            Text(text = it, fontSize = 10.sp, color = GrisMedio)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Colores
            val colores = listOf(
                AzulLigero,
                VerdeAerobico,
                AmarilloIntensivo,
                NaranjaAnaerobico,
                RojoVOMAx
            )

            Spacer(modifier = Modifier.height(16.dp))

            val zonasFormateadas = listOf(
                Triple("Ligero", "00:02:11", Color(0xFF00CFF9)),
                Triple("Intensivo", "00:03:52", Color(0xFFFFA20D)),
                Triple("VO Máx", "00:01:23", Color(0xFFFF1A1A)),
                Triple("Aeróbico", "00:17:01", Color(0xFF8D9D3D)),
                Triple("Anaeróbico", "00:08:59", Color(0xFFFF5E3A))
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Primera fila
                Row(horizontalArrangement = Arrangement.spacedBy(32.dp)) {
                    zonasFormateadas.take(3).forEach { (zona, tiempo, color) ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(tiempo, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(zona, fontSize = 12.sp, color = GrisMedio)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Segunda fila
                Row(horizontalArrangement = Arrangement.spacedBy(64.dp)) {
                    zonasFormateadas.drop(3).forEach { (zona, tiempo, color) ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(tiempo, fontWeight = FontWeight.Bold, fontSize = 14.sp)
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

fun colorZona(valor: Float): Color {
    return when {
        valor <= 60f -> AzulLigero // Ligero
        valor <= 100f -> VerdeAerobico // Aeróbico
        valor <= 140f -> AmarilloIntensivo // Intensivo
        valor <= 170f -> NaranjaAnaerobico // Anaeróbico
        else -> RojoVOMAx // VO Máx
    }
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun VistaPreviaPantallaRitmoCardiaco() {
    val navController = rememberNavController()
    val datos = listOf(180f, 60f, 140f, 90f, 88f, 112f, 50f, 145f, 160f, 190f)
    val zonas = listOf(
        "Ligero" to "00:02:11",
        "Intensivo" to "00:03:52",
        "VO Máx" to "00:01:23",
        "Aeróbico" to "00:17:01",
        "Anaeróbico" to "00:08:59"
    )
    PantallaRitmoCardiaco(
        navController = navController,
        ritmo = 135,
        fechaUltimaInfo = "23/04/25",
        datos = datos,
        zonas = zonas
    )
}
