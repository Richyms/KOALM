package com.example.koalm.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.koalm.ui.theme.*
import androidx.compose.ui.platform.LocalDensity
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun GraficaSueno(datos: DatosSueno, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Título y subtítulo
        Text(
            text = "Registro semanal de sueño",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = VerdePrincipal,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = "Comparación entre horas dormidas y objetivo configurado",
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Leyenda superior
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            LeyendaItem(
                color = VerdePrincipal.copy(alpha = 0.3f),
                texto = "Horas objetivo",
                forma = "barra"
            )
            LeyendaItem(
                color = VerdePrincipal,
                texto = "≥ 8h Real",
                forma = "barra"
            )
            LeyendaItem(
                color = Color(0xFFFFC107),
                texto = "6-7h Real",
                forma = "barra"
            )
            LeyendaItem(
                color = Color(0xFFE57373),
                texto = "< 6h Real",
                forma = "barra"
            )
        }

        // Contenedor principal de la gráfica
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Etiquetas del eje Y (Horas)
                Column(
                    modifier = Modifier
                        .width(40.dp)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Mostrar horas de 0 a 12 en el eje Y
                    for (hora in 12 downTo 0) {
                        Text(
                            text = "${hora}h",
                            fontSize = 12.sp,
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth(),
                            color = Color.Gray
                        )
                    }
                }

                // Línea vertical separadora
                Spacer(
                    modifier = Modifier
                        .width(1.dp)
                        .fillMaxHeight()
                        .background(Color.LightGray)
                )

                // Gráfica
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom
                ) {
                    val fechaInicial = LocalDate.parse(datos.fecha).minusDays(6)
                    datos.historialSemanal.forEachIndexed { index, diaSueno ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom,
                            modifier = Modifier.weight(1f)
                        ) {
                            val fecha = fechaInicial.plusDays(index.toLong())
                            
                            BarrasDobleSueno(
                                horasDormidas = diaSueno.duracionHoras,
                                horasObjetivo = diaSueno.horasObjetivo,
                                maxHoras = 12f // Máximo de 12 horas en el eje Y
                            )
                            
                            // Día de la semana y fecha
                            Text(
                                text = fecha.format(DateTimeFormatter.ofPattern("EE")),
                                fontSize = 12.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                            Text(
                                text = fecha.format(DateTimeFormatter.ofPattern("dd/MM")),
                                fontSize = 10.sp,
                                color = Color.Gray.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }

        // Información adicional
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            InfoCard(
                titulo = "Promedio real",
                valor = String.format("%.1fh", datos.duracionHoras / 7)
            )
            InfoCard(
                titulo = "Total semanal",
                valor = "${datos.horas}h ${datos.minutos}m"
            )
            InfoCard(
                titulo = "Puntuación",
                valor = "${datos.puntos}/100"
            )
        }
    }
}

@Composable
fun LeyendaItem(color: Color, texto: String, forma: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        if (forma == "barra") {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(16.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(color)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(color = color, shape = CircleShape)
            )
        }
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = texto,
            fontSize = 10.sp,
            color = Color.Gray
        )
    }
}

@Composable
fun InfoCard(titulo: String, valor: String) {
    Card(
        modifier = Modifier
            .padding(4.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = GrisCard)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = titulo,
                fontSize = 12.sp,
                color = Color.Gray
            )
            Text(
                text = valor,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = VerdePrincipal
            )
        }
    }
}

@Composable
fun BarrasDobleSueno(
    horasDormidas: Float,
    horasObjetivo: Float,
    maxHoras: Float
) {
    val colorReal = when {
        horasDormidas >= 8f -> VerdePrincipal
        horasDormidas >= 6f -> Color(0xFFFFC107)
        else -> Color(0xFFE57373)
    }

    Row(
        modifier = Modifier
            .height(220.dp)
            .width(32.dp),// Altura fija para todas las barras
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        // Barra del objetivo (siempre verde semi-transparente)
        Box(
            modifier = Modifier
                .width(12.dp)
                .height((horasObjetivo / maxHoras * 220).dp)
                .clip(RoundedCornerShape(4.dp))
                .background(VerdePrincipal.copy(alpha = 0.3f))
        )

        // Barra de horas reales
        Box(
            modifier = Modifier
                .width(12.dp)
                .height((horasDormidas / maxHoras * 220).dp)
                .clip(RoundedCornerShape(4.dp))
                .background(colorReal)
        )
    }
}


