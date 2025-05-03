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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.outlined.CheckBoxOutlineBlank
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.runtime.*
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
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
import com.example.koalm.ui.theme.VerdePrincipal
import com.example.koalm.ui.viewmodels.TimerViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaNotas(navController: NavHostController) {
    val context = LocalContext.current
    var notas by remember { mutableStateOf(listOf<Nota>()) }
    var mostrarDialogoNuevaNota by remember { mutableStateOf(false) }
    var notaAEditar by remember { mutableStateOf<Nota?>(null) }
    val timerViewModel: TimerViewModel = viewModel()
    
    // Observar el estado del temporizador desde el ViewModel
    val tiempoRestante by timerViewModel.timeLeft.collectAsState()
    val timerActivo by timerViewModel.isRunning.collectAsState()
    
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    
    Log.d("PantallaNotas", "Iniciando composición de PantallaNotas")

    // Cargar notas del usuario actual
    LaunchedEffect(Unit) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            db.collection("notas")
                .whereEqualTo("userId", userId)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        Log.w("PantallaNotas", "Error escuchando cambios", e)
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        val nuevasNotas = mutableListOf<Nota>()
                        for (doc in snapshot.documents) {
                            val nota = Nota(
                                id = doc.id,
                                titulo = doc.getString("titulo") ?: "",
                                contenido = doc.getString("contenido") ?: "",
                                userId = doc.getString("userId"),
                                fechaCreacion = doc.getString("fechaCreacion"),
                                fechaModificacion = doc.getString("fechaModificacion")
                            )
                            nuevasNotas.add(nota)
                        }
                        notas = nuevasNotas
                    }
                }
        }
    }

    // Escuchar actualizaciones del temporizador del servicio
    DisposableEffect(Unit) {
        Log.d("PantallaNotas", "Configurando receptor de actualizaciones del temporizador")
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == WritingTimerService.TIMER_UPDATE_ACTION) {
                    val isActive = intent.getBooleanExtra(WritingTimerService.EXTRA_IS_ACTIVE, false)
                    val remaining = intent.getLongExtra(WritingTimerService.EXTRA_REMAINING_TIME, 0)
                    Log.d("PantallaNotas", "Actualización de temporizador recibida: isActive=$isActive, remaining=$remaining ms")
                    
                    // Actualizar el ViewModel
                    timerViewModel.updateTimeLeft(remaining)
                    timerViewModel.updateIsRunning(isActive)
                }
            }
        }

        val filter = IntentFilter(WritingTimerService.TIMER_UPDATE_ACTION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            ContextCompat.registerReceiver(
                context,
                receiver,
                filter,
                ContextCompat.RECEIVER_NOT_EXPORTED
            )
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

    // Verificar estado del temporizador al iniciar
    LaunchedEffect(Unit) {
        try {
            val checkTimerIntent = Intent(context, WritingTimerService::class.java).apply {
                action = WritingTimerService.CHECK_TIMER_ACTION
            }
            context.startService(checkTimerIntent)
        } catch (e: Exception) {
            Log.e("PantallaNotas", "Error al verificar el temporizador: ${e.message}")
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
            /* ——————— Fila del temporizador ——————— */
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                /* Botón que inicia la cuenta atrás */
                Button(
                    onClick = {
                        val intent = Intent(context, WritingTimerService::class.java).apply {
                            action = WritingTimerService.START_TIMER_ACTION
                            putExtra("duration_minutes", 1L)  // 1 minuto
                        }
                        
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            context.startForegroundService(intent)
                        } else {
                            context.startService(intent)
                        }
                        
                        // Iniciar el temporizador en el ViewModel
                        timerViewModel.start(60_000)  // 1 minuto en milisegundos
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VerdePrincipal,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Iniciar temporizador")
                }

                Text(
                    text = if (timerActivo) formatTime(tiempoRestante) else "00:00",
                    style = MaterialTheme.typography.headlineMedium,
                    color = if (timerActivo) VerdePrincipal
                            else MaterialTheme.colorScheme.onSurfaceVariant
                )
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
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = nota.titulo,
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.weight(1f)
                                )
                                Row {
                                    IconButton(
                                        onClick = { notaAEditar = nota }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Editar",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            nota.id?.let { id ->
                                                db.collection("notas").document(id).delete()
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Eliminar",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
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
                val userId = auth.currentUser?.uid
                if (userId != null) {
                    // Crear un nuevo documento en Firestore
                    val docRef = db.collection("notas").document()
                    val notaConId = nuevaNota.copy(id = docRef.id) // Asignar el ID antes de guardar
                    
                    docRef.set(notaConId.toMap())
                        .addOnSuccessListener {
                            Log.d("PantallaNotas", "Nota creada con ID: ${docRef.id}")
                            notas = notas + notaConId
                        }
                        .addOnFailureListener { e ->
                            Log.w("PantallaNotas", "Error creando nota", e)
                        }
                }
                mostrarDialogoNuevaNota = false
            }
        )
    }

    if (notaAEditar != null) {
        DialogoEditarNota(
            nota = notaAEditar!!,
            onDismiss = { notaAEditar = null },
            onNotaEditada = { notaEditada ->
                notaEditada.id?.let { id ->
                    db.collection("notas").document(id)
                        .update(notaEditada.toMap())
                        .addOnSuccessListener {
                            notas = notas.map { if (it.id == id) notaEditada else it }
                        }
                }
                notaAEditar = null
            }
        )
    }
}

@Composable
private fun DialogoEditarNota(
    nota: Nota,
    onDismiss: () -> Unit,
    onNotaEditada: (Nota) -> Unit
) {
    var titulo by remember { mutableStateOf(nota.titulo) }
    var contenido by remember { mutableStateOf(nota.contenido) }
    val fechaActual = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Nota") },
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
                        onNotaEditada(nota.copy(
                            titulo = titulo,
                            contenido = contenido,
                            fechaModificacion = fechaActual
                        ))
                    }
                }
            ) { Text("Guardar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

private fun formatTime(millis: Long): String {
    val minutes = millis / 60_000
    val seconds = (millis % 60_000) / 1_000
    return String.format("%02d:%02d", minutes, seconds)
}

@Composable
private fun DialogoNuevaNota(
    onDismiss: () -> Unit,
    onNotaCreada: (Nota) -> Unit
) {
    var titulo by remember { mutableStateOf("") }
    var contenido by remember { mutableStateOf("") }
    val fechaActual = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid

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
                    if (titulo.isNotBlank() && contenido.isNotBlank() && userId != null) {
                        onNotaCreada(Nota(
                            titulo = titulo,
                            contenido = contenido,
                            userId = userId,
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
