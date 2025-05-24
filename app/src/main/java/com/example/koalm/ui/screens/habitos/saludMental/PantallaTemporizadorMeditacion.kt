package com.example.koalm.ui.screens.habitos.saludMental

import android.content.Context
import android.media.MediaPlayer
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.koalm.R
import kotlinx.coroutines.delay
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaTemporizadorMeditacion(
    navController: NavHostController,
    duracionMinutos: Int
) {
    var isTimerRunning by remember { mutableStateOf(false) }
    // Convertir minutos a segundos
    var timeLeft by remember { mutableStateOf(duracionMinutos * 60) } // duración en segundos
    val totalTime = (duracionMinutos * 60).toFloat()
    var isMusicPlaying by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val mediaPlayer = remember { MediaPlayer.create(context, R.raw.meditation_music) }
    
    // Obtener el color primario fuera del Canvas
    val primaryColor = MaterialTheme.colorScheme.primary

    // Cleanup MediaPlayer when the composable is disposed
    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer.release()
        }
    }

    LaunchedEffect(isTimerRunning) {
        while (isTimerRunning && timeLeft > 0) {
            delay(1000)
            timeLeft--
        }
        if (timeLeft == 0) {
            isTimerRunning = false
            mediaPlayer.pause()
            isMusicPlaying = false
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
                        .size(300.dp),
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
                            color = primaryColor,
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
                    text = if (isTimerRunning) "Meditando..." else "Controla tu sesión",
                    style = MaterialTheme.typography.bodyLarge
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Botones de control
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(16.dp)
                ) {
                    // Botón de Play/Pause Timer
                    FilledTonalButton(
                        onClick = {
                            isTimerRunning = !isTimerRunning
                        }
                    ) {
                        Icon(
                            if (isTimerRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isTimerRunning) "Pausar" else "Iniciar"
                        )
                    }

                    // Botón de Play/Pause Música
                    FilledTonalButton(
                        onClick = {
                            if (isMusicPlaying) {
                                mediaPlayer.pause()
                            } else {
                                mediaPlayer.start()
                            }
                            isMusicPlaying = !isMusicPlaying
                        }
                    ) {
                        Icon(
                            if (isMusicPlaying) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = if (isMusicPlaying) "Pausar música" else "Reproducir música"
                        )
                    }

                    // Botón de Reiniciar
                    FilledTonalButton(
                        onClick = {
                            timeLeft = duracionMinutos * 60 // Convertir minutos a segundos
                            isTimerRunning = false
                            mediaPlayer.seekTo(0)
                            if (isMusicPlaying) {
                                mediaPlayer.pause()
                                isMusicPlaying = false
                            }
                        }
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Reiniciar"
                        )
                    }
                }
            }
        }
    }
} 