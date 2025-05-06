package com.example.koalm.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.koalm.ui.components.BarraNavegacionInferior
import com.example.koalm.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import com.example.koalm.ui.components.*

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaSueno(
    navController: NavHostController,
    datos: DatosSueno = datosMockSueño
) {
    val fecha = LocalDate.parse(datos.fecha)
    val fechaFormateada = fecha.format(DateTimeFormatter.ofPattern("dd/MM/yy"))

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sueño de anoche") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        },
        bottomBar = {
            BarraNavegacionInferior(navController, "sueño")
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${datos.puntos}",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.alignByBaseline()
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "pts",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.alignByBaseline()
                )
            }

            Text(
                text = "Este dato es de la última información registrada, $fechaFormateada",
                fontSize = 11.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(16.dp))
            EstadisticasCard(datos = datos)
        }
    }
}

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


val datosMockSueño = DatosSueno( // Estos datos van a ser recuperados del back
    puntos = 87,
    fecha = "2024-05-02",
    horas = 7,
    minutos = 15,
    sueñoLigero = 3.2f,
    sueñoProfundo = 3.8f,
    tiempoDespierto = 0.5f,
    historialSemanal = listOf(
        DiaSueno(3.0f, 3.5f, 0.5f), // Lunes
        DiaSueno(3.2f, 3.6f, 0.3f), // Martes
        DiaSueno(3.1f, 3.4f, 0.5f), // Miércoles
        DiaSueno(3.4f, 3.2f, 0.4f), // Jueves
        DiaSueno(3.0f, 4.0f, 0.2f), // Viernes
        DiaSueno(2.5f, 3.0f, 0.7f), // Sábado
        DiaSueno(2.0f, 2.5f, 1.0f)  // Domingo
    )
)

