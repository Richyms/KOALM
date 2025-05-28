package com.example.koalm.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import com.example.koalm.ui.theme.GrisMedio
import com.example.koalm.ui.theme.MarronKoala
import com.example.koalm.ui.theme.VerdePrincipal

@Composable
fun GraficaEstres(
    valores: List<Float>,
    colores: List<Color>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
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
                        start = Offset(0f, yPos),
                        end = Offset(widthPx, yPos),
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
                valores.forEachIndexed { index, valor ->
                    Box(
                        modifier = Modifier
                            .width(6.dp)
                            .height((valor * 160).dp)
                            .clip(RoundedCornerShape(topStart = 50.dp, topEnd = 50.dp))
                            .background(colores.getOrNull(index) ?: Color.Gray)
                    )
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
