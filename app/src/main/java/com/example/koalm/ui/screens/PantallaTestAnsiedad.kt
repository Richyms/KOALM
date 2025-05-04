package com.example.koalm.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaTestAnsiedad(navController: NavController? = null) {
    val preguntas = listOf(
        "Sentirse nervios@, intranquil@ o con los nervios de punta",
        "No poder dejar de preocuparse o no poder controlar la preocupación",
        "Preocuparse demasiado por diferentes cosas",
        "Dificultad para relajarse",
        "Estar tan inquiet@ que es difícil permanecer sentad@ tranquilamente",
        "Molestarse o ponerse irritable fácilmente",
        "Sentir miedo como si algo terrible pudiera pasar"
    )

    val opciones = listOf("Nunca", "Varios días", "Más de la mitad del tiempo", "Casi todos los días")
    val respuestas = remember { mutableStateListOf(*Array(preguntas.size) { -1 }) }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController = navController)
        },
        topBar = {
            TopAppBar(
                title = {
                    Text("Test de ansiedad", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                },
                navigationIcon = {
                    IconButton(onClick = { navController?.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }

    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxSize()
        ) {
            preguntas.forEachIndexed { index, pregunta ->
                PreguntaCard(
                    pregunta = pregunta,
                    opciones = opciones,
                    seleccionada = respuestas[index],
                    onSeleccionar = { respuestas[index] = it }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    // Aquí puedes manejar el cálculo de resultados, por ejemplo:
                    val resultado = calcularResultado(respuestas)
                    // O navegar a otra pantalla con el resultado:
                    navController?.navigate("resultado/$resultado")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5D9B63))
            ) {
                Text("Resultados", color = Color.White)
            }
        }
    }
}

@Composable
fun PreguntaCard(
    pregunta: String,
    opciones: List<String>,
    seleccionada: Int,
    onSeleccionar: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = Color(0xFFEAF4E6), shape = RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Text(text = pregunta, fontSize = 14.sp, color = Color.Black)
        Spacer(modifier = Modifier.height(8.dp))
        opciones.forEachIndexed { index, opcion ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                RadioButton(
                    selected = seleccionada == index,
                    onClick = { onSeleccionar(index) },
                    colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF5D9B63))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(opcion)
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavController?) {
    NavigationBar(
        containerColor = Color.White
    ) {
        NavigationBarItem(
            selected = true,
            onClick = { navController?.navigate("inicio") },
            icon = { Icon(Icons.Default.Home, contentDescription = "Inicio") },
            label = { Text("Inicio") }
        )
        NavigationBarItem(
            selected = false,
            onClick = { navController?.navigate("habitos") },
            icon = { Icon(Icons.Default.FavoriteBorder, contentDescription = "Hábitos") },
            label = { Text("Hábitos") }
        )
        NavigationBarItem(
            selected = false,
            onClick = { navController?.navigate("perfil") },
            icon = { Icon(Icons.Default.Person, contentDescription = "Perfil") },
            label = { Text("Perfil") }
        )
    }
}

fun calcularResultado(respuestas: List<Int>): Int {
    // Ejemplo de cómo podrías calcular un resultado basado en las respuestas
    return respuestas.count { it != -1 } // Contar cuántas preguntas fueron respondidas
}

@Preview(showBackground = true)
@Composable
fun PreviewPantallaTestAnsiedad() {
    PantallaTestAnsiedad()
}