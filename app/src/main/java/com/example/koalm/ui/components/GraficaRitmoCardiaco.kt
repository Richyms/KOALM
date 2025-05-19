package com.example.koalm.ui.components
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.koalm.ui.theme.*

@Composable
fun GraficaRitmoCardiaco(
    datos: List<Float>,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
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

            datos.forEachIndexed { index, valor ->
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

