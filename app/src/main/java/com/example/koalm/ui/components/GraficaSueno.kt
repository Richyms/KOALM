package com.example.koalm.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.koalm.ui.theme.*

data class DatosSueno(
    val puntos: Int,
    val fecha: String,
    val horas: Int,
    val minutos: Int,
    val duracionHoras: Float,
    val historialSemanal: List<DiaSueno>
)

data class DiaSueno(
    val duracionHoras: Float
)

@Composable
fun BarraSueno(duracionHoras: Float) {
    val color = when {
        duracionHoras >= 8f -> VerdePrincipal // Buen sueño (8-12 horas)
        duracionHoras >= 7f -> Color(0xFFFFC107) // Regular (7-8 horas)
        else -> Color(0xFFE57373) // Mal sueño (1-6 horas)
    }

    Box(
        modifier = Modifier
            .height(150.dp)
            .width(14.dp)
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
                    .fillMaxHeight(duracionHoras / 12f) // Normalizar a 12 horas máximo
                    .background(color)
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
                .size(14.dp)
                .background(color = color, shape = CircleShape)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text("$duracion  -  $etiqueta", fontSize = 16.sp)
    }
}


