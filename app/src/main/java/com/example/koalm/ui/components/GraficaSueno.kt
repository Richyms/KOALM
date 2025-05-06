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
    val sueñoLigero: Float,
    val sueñoProfundo: Float,
    val tiempoDespierto: Float,
    val historialSemanal: List<DiaSueno>
)

data class DiaSueno(
    val ligero: Float,
    val profundo: Float,
    val despierto: Float
)

@Composable
fun BarraSueno(
    suenoLigero: Float,
    suenoProfundo: Float,
    despierto: Float
) {
    val total = suenoLigero + suenoProfundo + despierto
    val ligeroRatio = suenoLigero / total
    val profundoRatio = suenoProfundo / total
    val despiertoRatio = despierto / total

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
                .size(14.dp)
                .background(color = color, shape = CircleShape)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text("$duracion  -  $etiqueta", fontSize = 16.sp)
    }
}


