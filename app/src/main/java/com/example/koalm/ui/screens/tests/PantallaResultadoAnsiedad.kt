package com.example.koalm.ui.screens.tests

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.koalm.ui.components.BarraNavegacionInferior
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import android.util.Log
import com.example.koalm.ui.theme.*
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

data class ResultadoAnsiedad(
    val nivel: String,
    val descripcion: String,
    val recomendaciones: String,
    val color: Color
)

fun obtenerResultadoAnsiedad(puntaje: Int): ResultadoAnsiedad {
    return when (puntaje) {
        in 0..4 -> ResultadoAnsiedad(
            nivel = "Ansiedad Mínima",
            descripcion = "Los síntomas de ansiedad son mínimos o ausentes. Este nivel es considerado normal y no requiere intervención específica.",
            recomendaciones = "• Mantén tus hábitos saludables\n• Continúa con tu rutina de ejercicio\n• Practica técnicas de relajación preventivas",
            color = Color(0xFF4CAF50)
        )
        in 5..9 -> ResultadoAnsiedad(
            nivel = "Ansiedad Leve",
            descripcion = "Presentas síntomas leves de ansiedad. Aunque no son severos, podrían beneficiarse de seguimiento y monitoreo.",
            recomendaciones = "• Incorpora ejercicios de respiración diarios\n• Mantén un diario de emociones\n• Considera reducir la cafeína\n• Establece una rutina de sueño regular",
            color = Color(0xFF8BC34A)
        )
        in 10..14 -> ResultadoAnsiedad(
            nivel = "Ansiedad Moderada",
            descripcion = "Los síntomas indican un nivel moderado de ansiedad que podría estar afectando tu vida diaria.",
            recomendaciones = "• Considera buscar apoyo profesional\n• Practica meditación o mindfulness\n• Mantén un horario estructurado\n• Identifica y gestiona tus desencadenantes",
            color = Color(0xFFFFC107)
        )
        else -> ResultadoAnsiedad(
            nivel = "Ansiedad Severa",
            descripcion = "Los síntomas indican un nivel severo d e ansiedad que requiere atención profesional.",
            recomendaciones = "• Busca ayuda profesional inmediata\n• Mantén contacto regular con tu red de apoyo\n• Aprende técnicas de manejo de crisis\n• Sigue un plan de tratamiento estructurado",
            color = Color(0xFFF44336)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaResultadoAnsiedad(
    navController: NavHostController,
    puntaje: Int
) {
    val resultado = obtenerResultadoAnsiedad(puntaje)
    val firestore = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val userEmail = auth.currentUser?.email

    LaunchedEffect(Unit) {
        if (userEmail != null) {
            try {
                val datos = mapOf(
                    "nivel" to resultado.nivel,
                    "puntaje" to puntaje,
                    "timestamp" to com.google.firebase.Timestamp.now()
                )
                val Guardado = firestore.collection("resultadosAnsiedad")
                    .document(userEmail)
                    .collection("historial")

                try {
                    val Anteriores = Guardado.get().await()

                    val conteo = Anteriores.size() + 1
                    val IdPerso = "Resultado $conteo"

                    Guardado.document(IdPerso) // Genera ID automático
                        .set(datos)
                    Log.d("Firebase", "Resultado guardado con éxito con Id:$IdPerso")
                } catch (e: Exception) {
                    Log.e("Firebase", "Error al guardar resultado: ${e.message}")
                }
            } catch (e: Exception) {
                Log.e("Firebase", "Usuario no autenticado")
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Resultado del Test", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        },
        bottomBar = {
            BarraNavegacionInferior(navController, "estadisticas")
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = resultado.color.copy(alpha = 0.1f))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Tu puntaje: $puntaje",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = resultado.color
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = resultado.nivel,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = resultado.color
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = "¿Qué significa?",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = resultado.descripcion,
                        fontSize = 16.sp,
                        color = Color.Black.copy(alpha = 0.8f),
                        lineHeight = 24.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = "Recomendaciones",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = resultado.recomendaciones,
                        fontSize = 16.sp,
                        color = Color.Black.copy(alpha = 0.8f),
                        lineHeight = 24.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { navController.navigate("menu") },
                modifier = Modifier
                    .width(200.dp)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = VerdePrincipal),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Volver al inicio", fontSize = 16.sp)
            }
        }
    }
}