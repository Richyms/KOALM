package com.example.koalm.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.koalm.ui.theme.GrisCard
import com.example.koalm.ui.theme.VerdePrincipal

/* ---------- DATA CLASS ---------- */
data class PesoEntrada(val fecha: String, val peso: String)

/* ---------- MOCK DE DATOS ---------- */
val datosMockPeso = listOf(
    PesoEntrada("27/05", "73.2 kg"),
    PesoEntrada("28/05", "72.5 kg"),
    PesoEntrada("29/05", "71.8 kg"),
    PesoEntrada("30/05", "70.6 kg"),
    PesoEntrada("31/05", "69.9 kg"),
    PesoEntrada("01/06", "69.2 kg"),
    PesoEntrada("02/06", "68.7 kg")
)

/* ---------- COMPOSABLE ---------- */
@Composable
fun GraficaPeso(valores: List<Float> = datosMockPeso.map { it.peso.removeSuffix(" kg").toFloat() }) {
    val fechas = datosMockPeso.map { it.fecha }

    val maxPeso = (valores.maxOrNull() ?: 80f).coerceAtLeast(1f)
    val minPeso = (valores.minOrNull() ?: 60f).coerceAtMost(100f)

    // Etiquetas dinámicas
    val pasos = 4
    val rango = maxPeso - minPeso
    val yLabels = (0..pasos).map { i ->
        (minPeso + (rango / pasos) * i).toInt().toFloat()
    }.reversed()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = GrisCard)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 4.dp, vertical = 16.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth().weight(1f)) {
                // Eje Y
                Column(
                    modifier = Modifier
                        .width(45.dp)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.End
                ) {
                    yLabels.forEach { label ->
                        Text(
                            text = "${label.toInt()} kg",
                            fontSize = 10.sp,
                            color = Color.DarkGray
                        )
                    }
                }

                // Área de gráfica
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    // Líneas horizontales
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val heightPx = size.height
                        val widthPx = size.width
                        yLabels.forEach { label ->
                            val yRatio = (label - minPeso) / (maxPeso - minPeso)
                            val yPos = heightPx * (1 - yRatio)
                            drawLine(
                                color = Color.Gray.copy(alpha = 0.4f),
                                start = Offset(0f, yPos),
                                end = Offset(widthPx, yPos),
                                strokeWidth = 1.5f,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 6f))
                            )
                        }
                    }

                    // Barras
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        valores.forEach { valor ->
                            val altura = ((valor - minPeso) / (maxPeso - minPeso)).coerceIn(0f, 1f) * 160
                            Box(
                                modifier = Modifier
                                    .width(10.dp)
                                    .height(altura.dp)
                                    .clip(RoundedCornerShape(topStart = 50.dp, topEnd = 50.dp))
                                    .background(VerdePrincipal)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Eje X (fechas alineadas)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 45.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Top
                ) {
                    fechas.forEach { fecha ->
                        Text(
                            text = fecha,
                            fontSize = 10.sp,
                            color = Color.DarkGray
                        )
                    }
                }
            }
        }
    }
}

/* ---------- PREVIEW ---------- */
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewGraficaPeso() {
    GraficaPeso()
}
