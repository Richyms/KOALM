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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.*
import com.example.koalm.ui.components.BarraNavegacionInferior
import com.example.koalm.ui.theme.*
import androidx.compose.ui.draw.clip

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
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .padding(start = 40.dp)

                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val stepX = size.width / (datos.datos.size - 1)
                            val stepY = size.height / 200f

                            val puntos = datos.datos.mapIndexed { i, v ->
                                Offset(x = i * stepX, y = size.height - v * stepY)
                            }

                            val path = Path().apply {
                                moveTo(puntos.first().x, size.height)
                                puntos.forEach { lineTo(it.x, it.y) }
                                lineTo(puntos.last().x, size.height)
                                close()
                            }

                            drawPath(
                                path = path,
                                brush = Brush.verticalGradient(colors = listOf(Color(0xFFB7EACB), Color.Transparent))
                            )

                            for (i in 0 until puntos.size - 1) {
                                drawLine(
                                    color = Color(0xFFF8844F),
                                    start = puntos[i],
                                    end = puntos[i + 1],
                                    strokeWidth = 3f
                                )
                            }

                            datos.datos.forEachIndexed { index, valor ->
                                drawCircle(
                                    color = colorZona(valor),
                                    radius = 4.dp.toPx(),
                                    center = puntos[index]
                                )
                            }

                            val etiquetasTextoY = listOf(56, 84, 112, 140, 196)
                            etiquetasTextoY.forEach { valor ->
                                val posY = size.height - (valor * stepY)
                                drawContext.canvas.nativeCanvas.drawText(
                                    valor.toString(),
                                    -82f,
                                    posY + 4.dp.toPx(),
                                    android.graphics.Paint().apply {
                                        color = GrisMedio.toArgb()
                                        textSize = 28f
                                    }
                                )
                            }
                        }


                    }

                    Spacer(modifier = Modifier.height(8.dp))

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
                // Primera fila
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

fun colorZona(valor: Float): Color {
    return when {
        valor <= 60f -> AzulLigero // Ligero
        valor <= 100f -> VerdeAerobico // Aeróbico
        valor <= 140f -> AmarilloIntensivo // Intensivo
        valor <= 170f -> NaranjaAnaerobico // Anaeróbico
        else -> RojoVOMAx // VO Máx
    }
}

data class DatosRitmoCardiaco(
    val ritmo: Int,
    val fechaUltimaInfo: String,
    val datos: List<Float>
)

val datosMockRitmo = DatosRitmoCardiaco(
    ritmo = 135,
    fechaUltimaInfo = "23/04/25",
    datos = listOf(180f, 60f, 140f, 90f, 88f, 112f, 50f, 145f, 160f, 190f)
)