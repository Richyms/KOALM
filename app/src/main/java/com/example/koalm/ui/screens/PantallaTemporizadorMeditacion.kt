package com.example.koalm.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import kotlinx.coroutines.delay
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaTemporizadorMeditacion(navController: NavHostController) {
    var isTimerRunning by remember { mutableStateOf(false) }
    var timeLeft by remember { mutableStateOf(60) } // 1 minuto en segundos
    val totalTime = 60f
    
    // Obtener el color primario fuera del Canvas
    val primaryColor = MaterialTheme.colorScheme.primary

    LaunchedEffect(isTimerRunning) {
        while (isTimerRunning && timeLeft > 0) {
            delay(1000)
            timeLeft--
        }
        if (timeLeft == 0) {
            isTimerRunning = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Meditación") },
                navigationIcon = {
                    IconButton(onClick = navController::navigateUp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(300.dp)
                        .clickable(enabled = !isTimerRunning) {
                            if (!isTimerRunning) {
                                isTimerRunning = true
                                timeLeft = 60
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val center = Offset(size.width / 2, size.height / 2)
                        val radius = min(size.width, size.height) / 2 - 10f
                        
                        // Dibuja el círculo de fondo
                        drawCircle(
                            color = Color.LightGray.copy(alpha = 0.3f),
                            radius = radius,
                            center = center
                        )
                        
                        // Dibuja el progreso
                        val progress = timeLeft / totalTime
                        drawArc(
                            color = primaryColor, // Usar el color obtenido fuera del Canvas
                            startAngle = -90f,
                            sweepAngle = 360f * progress,
                            useCenter = false,
                            style = Stroke(width = 20f),
                            size = Size(radius * 2, radius * 2),
                            topLeft = Offset(center.x - radius, center.y - radius)
                        )
                    }
                    
                    Text(
                        text = "${timeLeft / 60}:${String.format("%02d", timeLeft % 60)}",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = if (isTimerRunning) "Meditando..." else "Toca para comenzar",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
} 