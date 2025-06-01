package com.example.koalm.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.koalm.ui.theme.*

data class PesoEntrada(val fecha: String, val peso: String)

val datosMockPeso = listOf(
    PesoEntrada("18/05", "67.5 kg"),
    PesoEntrada("20/05", "68.2 kg"),
    PesoEntrada("23/05", "73.9 kg"),
    PesoEntrada("25/05", "71.4 kg"),
    PesoEntrada("28/05", "77.0 kg"),
    PesoEntrada("30/05", "75.5 kg"),
    PesoEntrada("02/06", "79.0 kg")
)

@Composable
fun GraficaPeso(
    modifier: Modifier = Modifier,
    valores: List<Float> = datosMockPeso.map { it.peso.removeSuffix(" kg").toFloat() },
    fechas: List<String> = datosMockPeso.map { it.fecha }
) {
    val maxPeso = (valores.maxOrNull() ?: 1f).coerceAtLeast(1f)
    val minPeso = (valores.minOrNull() ?: 0f)
    val rango = maxPeso - minPeso

    val yLabels = listOf(
        minPeso,
        minPeso + (rango * 0.25f),
        minPeso + (rango * 0.5f),
        minPeso + (rango * 0.75f),
        maxPeso
    ).map { it.toInt() }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
    ) {
        // Eje Y
        Box(
            modifier = Modifier
                .padding(end = 8.dp)
                .fillMaxHeight()
                .width(40.dp)
        ) {
            yLabels.forEachIndexed { i, label ->
                val yRatio = i / (yLabels.size - 1).toFloat()
                Text(
                    text = "$label kg",
                    fontSize = 11.sp,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .offset(y = -(yRatio * 160).dp)
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
                yLabels.forEachIndexed { i, _ ->
                    val yRatio = i / (yLabels.size - 1).toFloat()
                    val yPos = heightPx * (1 - yRatio)
                    drawLine(
                        color = Color.LightGray.copy(alpha = 0.5f),
                        start = Offset(0f, yPos),
                        end = Offset(size.width, yPos),
                        strokeWidth = 1.5f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                valores.forEach { peso ->
                    val ratio = ((peso - minPeso) / (maxPeso - minPeso)).coerceIn(0f, 1f)
                    Box(
                        modifier = Modifier
                            .width(10.dp)
                            .height((ratio * 160).dp.coerceAtLeast(4.dp))
                            .clip(RoundedCornerShape(topStart = 50.dp, topEnd = 50.dp))
                            .background(VerdePrincipal)
                    )
                }
            }
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 48.dp)
            .height(20.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        fechas.forEach {
            Text(
                text = it,
                fontSize = 10.sp,
                color = Color.DarkGray
            )
        }
    }
}
