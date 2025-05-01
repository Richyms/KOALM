/*  PantallaNotas.kt  */
package com.example.koalm.ui.screens

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.CheckBoxOutlineBlank
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.runtime.*
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.koalm.R
import com.example.koalm.model.Nota
import com.example.koalm.services.WritingTimerService
import com.example.koalm.ui.components.BarraNavegacionInferior
import com.example.koalm.ui.theme.GrisClaro
import com.example.koalm.ui.theme.VerdeBorde
import com.example.koalm.ui.theme.VerdeContenedor
import com.example.koalm.ui.theme.VerdePrincipal
import com.example.koalm.ui.viewmodels.TimerViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaNotas(navController: NavHostController) {
    val context = LocalContext.current
    var notas by remember { mutableStateOf(listOf<Nota>()) }
    var mostrarDialogoNuevaNota by remember { mutableStateOf(false) }
    var tiempoRestante by remember { mutableStateOf<Long?>(null) }
    var timerActivo by remember { mutableStateOf(false) }
    
    Log.d("PantallaNotas", "Iniciando composición de PantallaNotas")

    // Iniciar temporizador si se recibe la acción
    LaunchedEffect(Unit) {
        val intent = (context as? ComponentActivity)?.intent
        if (intent?.action == "com.example.koalm.START_TIMER") {
            val duracionMinutos = intent.getLongExtra("duration_minutes", 15)
            Log.d("PantallaNotas", "Iniciando temporizador con duración: $duracionMinutos minutos")
            
            // Verificar si el temporizador ya está activo
            val checkTimerIntent = Intent(context, WritingTimerService::class.java).apply {
                action = WritingTimerService.CHECK_TIMER_ACTION
            }
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(checkTimerIntent)
            } else {
                context.startService(checkTimerIntent)
            }
            
            // Iniciar servicio de temporizador
            val timerIntent = Intent(context, WritingTimerService::class.java).apply {
                action = WritingTimerService.START_TIMER_ACTION
                putExtra("duration_minutes", duracionMinutos)
                addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
            }
            
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(timerIntent)
                } else {
                    context.startService(timerIntent)
                }
                Log.d("PantallaNotas", "Servicio de temporizador iniciado correctamente")
            } catch (e: Exception) {
                Log.e("PantallaNotas", "Error al iniciar el servicio: ${e.message}")
            }
        }
    }

    // Escuchar actualizaciones del temporizador
    DisposableEffect(Unit) {
        Log.d("PantallaNotas", "Configurando receptor de actualizaciones del temporizador")
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == WritingTimerService.TIMER_UPDATE_ACTION) {
                    val isActive = intent.getBooleanExtra(WritingTimerService.EXTRA_IS_ACTIVE, false)
                    val remaining = intent.getLongExtra(WritingTimerService.EXTRA_REMAINING_TIME, 0)
                    Log.d("PantallaNotas", "Actualización de temporizador recibida: isActive=$isActive, remaining=$remaining ms")
                    
                    timerActivo = isActive
                    tiempoRestante = if (isActive) remaining else null
                }
            }
        }

        val filter = IntentFilter(WritingTimerService.TIMER_UPDATE_ACTION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            context.registerReceiver(receiver, filter)
        }
        Log.d("PantallaNotas", "Receptor registrado para acción: ${WritingTimerService.TIMER_UPDATE_ACTION}")

        onDispose {
            try {
                context.unregisterReceiver(receiver)
                Log.d("PantallaNotas", "Receptor de temporizador desregistrado")
            } catch (e: Exception) {
                Log.e("PantallaNotas", "Error al desregistrar receptor: ${e.message}")
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.titulo_notas)) },
                navigationIcon = {
                    IconButton(onClick = navController::navigateUp) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.volver)
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { mostrarDialogoNuevaNota = true },
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text(stringResource(R.string.nueva_nota)) },
                containerColor = VerdePrincipal,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        },
        bottomBar = {
            BarraNavegacionInferior(navController, rutaActual = "notas")
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { paddingValores ->
        Column(
            modifier = Modifier
                .padding(paddingValores)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Mostrar el temporizador si está activo
            if (timerActivo && tiempoRestante != null) {
                Log.d("PantallaNotas", "Mostrando temporizador con tiempo restante: ${tiempoRestante}ms")
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, VerdeBorde),
                    colors = CardDefaults.cardColors(containerColor = VerdeContenedor)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Tiempo restante",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = formatTime(tiempoRestante!!),
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }
                }
            }

            // Lista de notas
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(notas) { nota ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, VerdeBorde),
                        colors = CardDefaults.cardColors(containerColor = VerdeContenedor)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = nota.titulo,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = nota.contenido,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Creada: ${nota.fechaCreacion ?: "Fecha no disponible"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

    if (mostrarDialogoNuevaNota) {
        DialogoNuevaNota(
            onDismiss = { mostrarDialogoNuevaNota = false },
            onNotaCreada = { nuevaNota ->
                notas = notas + nuevaNota
                mostrarDialogoNuevaNota = false
            }
        )
    }
}

private fun formatTime(millis: Long): String {
    val minutes = java.util.concurrent.TimeUnit.MILLISECONDS.toMinutes(millis)
    val seconds = java.util.concurrent.TimeUnit.MILLISECONDS.toSeconds(millis) % 60
    return String.format("%02d:%02d", minutes, seconds)
}

@Composable
private fun TarjetaNota(nota: Nota) {
    // Colores y borde suaves para asemejar el mock-up
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, GrisClaro),
        colors = CardDefaults.cardColors(
            containerColor = GrisClaro.copy(alpha = 0.25f) // leve tono verde/gris
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Outlined.CheckBoxOutlineBlank,
                contentDescription = null,
                tint = VerdePrincipal
            )
            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = nota.titulo,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = nota.contenido,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 3,                          // trunca el texto
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Creada: ${nota.fechaCreacion ?: "Fecha no disponible"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DialogoNuevaNota(
    onDismiss: () -> Unit,
    onNotaCreada: (Nota) -> Unit
) {
    var titulo by remember { mutableStateOf("") }
    var contenido by remember { mutableStateOf("") }
    val fechaActual = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.nueva_nota)) },
        text = {
            Column {
                OutlinedTextField(
                    value = titulo,
                    onValueChange = { titulo = it },
                    label = { Text(stringResource(R.string.titulo_nota)) },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = contenido,
                    onValueChange = { contenido = it },
                    label = { Text(stringResource(R.string.contenido_nota)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (titulo.isNotBlank() && contenido.isNotBlank()) {
                        onNotaCreada(Nota(
                            titulo = titulo,
                            contenido = contenido,
                            fechaCreacion = fechaActual,
                            fechaModificacion = fechaActual
                        ))
                    }
                }
            ) { Text(stringResource(android.R.string.ok)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(android.R.string.cancel))
            }
        }
    )
}
