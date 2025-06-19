/*  PantallaNotas.kt  */
package com.example.koalm.ui.screens.habitos.saludMental

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.util.Log
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
// import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
// import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.koalm.R
import com.example.koalm.model.Nota
// import com.example.koalm.services.timers.WritingTimerService
// import com.example.koalm.services.notifications.NotificationConstants
import com.example.koalm.ui.components.BarraNavegacionInferior
import com.example.koalm.ui.theme.VerdeBorde
import com.example.koalm.ui.theme.VerdeContenedor
import com.example.koalm.ui.theme.VerdePrincipal
// import com.example.koalm.ui.viewmodels.TimerViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaNotas(navController: NavHostController) {
    val context = LocalContext.current
    var notas by remember { mutableStateOf(listOf<Nota>()) }

    /*
    // Temporizador - comentado temporalmente
    val sharedPreferences = remember { context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE) }
    val defaultDuration = 15L * 60 * 1000
    val timerDuration = remember {
        mutableStateOf(sharedPreferences.getLong("writing_timer_duration", defaultDuration))
    }

    val timerViewModel: TimerViewModel = viewModel()
    val tiempoRestante by timerViewModel.timeLeft.collectAsState()
    val timerActivo by timerViewModel.isRunning.collectAsState()
    */

    var mostrarDialogoNuevaNota by remember { mutableStateOf(false) }
    var notaAEditar by remember { mutableStateOf<Nota?>(null) }

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
                        val nuevasNotas = snapshot.documents.mapNotNull { doc ->
                            Nota(
                                id = doc.id,
                                titulo = doc.getString("titulo") ?: "",
                                contenido = doc.getString("contenido") ?: "",
                                userId = doc.getString("userId"),
                                fechaCreacion = doc.getString("fechaCreacion"),
                                fechaModificacion = doc.getString("fechaModificacion")
                            )
                        }
                        notas = nuevasNotas
                    }
                }
        }
    }

    /*
    // Escuchar actualizaciones del temporizador - Comentado
    DisposableEffect(Unit) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == NotificationConstants.TIMER_UPDATE_ACTION) {
                    val isActive = intent.getBooleanExtra(NotificationConstants.EXTRA_IS_ACTIVE, false)
                    val remaining = intent.getLongExtra(NotificationConstants.EXTRA_REMAINING_TIME, 0)
                    timerViewModel.updateTimeLeft(remaining)
                    timerViewModel.updateIsRunning(isActive)
                }
            }
        }

        val filter = IntentFilter(NotificationConstants.TIMER_UPDATE_ACTION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            ContextCompat.registerReceiver(context, receiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
        }

        onDispose {
            try {
                context.unregisterReceiver(receiver)
            } catch (e: Exception) {
                Log.e("PantallaNotas", "Error al desregistrar receptor: ${e.message}")
            }
        }
    }

    // Verificar estado del temporizador al iniciar
    LaunchedEffect(Unit) {
        try {
            val checkTimerIntent = Intent(context, WritingTimerService::class.java).apply {
                action = NotificationConstants.CHECK_TIMER_ACTION
            }
            context.startService(checkTimerIntent)
        } catch (e: Exception) {
            Log.e("PantallaNotas", "Error al verificar el temporizador: ${e.message}")
        }
    }
    */

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
            /*
            // Fila del temporizador - Comentada temporalmente
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        val intent = Intent(context, WritingTimerService::class.java).apply {
                            action = NotificationConstants.START_TIMER_ACTION
                            putExtra(NotificationConstants.EXTRA_DURATION, (timerDuration.value / (60 * 1000)).toLong())
                        }

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            context.startForegroundService(intent)
                        } else {
                            context.startService(intent)
                        }

                        timerViewModel.start(timerDuration.value)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VerdePrincipal,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Iniciar temporizador (${timerDuration.value / (60 * 1000)} min)")
                }

                Text(
                    text = if (timerActivo) formatTime(tiempoRestante) else "00:00",
                    style = MaterialTheme.typography.headlineMedium,
                    color = if (timerActivo) VerdePrincipal else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            */

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
                                    IconButton(onClick = { notaAEditar = nota }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary)
                                    }
                                    IconButton(onClick = {
                                        nota.id?.let { id -> db.collection("notas").document(id).delete() }
                                    }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.Red)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = nota.contenido, style = MaterialTheme.typography.bodyMedium)
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
                    val docRef = db.collection("notas").document()
                    val notaConId = nuevaNota.copy(id = docRef.id)
                    docRef.set(notaConId.toMap())
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
    val fechaActual = SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Nota") },
        text = {
            Column {
                OutlinedTextField(
                    value = titulo,
                    onValueChange = { titulo = it },
                    label = { Text("Título") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = contenido,
                    onValueChange = { contenido = it },
                    label = { Text("Contenido") },
                    modifier = Modifier.fillMaxWidth().height(150.dp)
                )
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    modifier = Modifier.weight(1f),
                    onClick = onDismiss,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEC615B))
                ) {
                    Text(
                        text = "Cancelar",
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
                Button(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        if (titulo.isNotBlank() && contenido.isNotBlank()) {
                            onNotaEditada(
                                nota.copy(
                                    titulo = titulo,
                                    contenido = contenido,
                                    fechaModificacion = fechaActual
                                )
                            )
                        }
                    },
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Guardar")
                }
            }
        }

    )
}

@Composable
private fun DialogoNuevaNota(
    onDismiss: () -> Unit,
    onNotaCreada: (Nota) -> Unit
) {
    var titulo by remember { mutableStateOf("") }
    var contenido by remember { mutableStateOf("") }
    val fechaActual = SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva Nota") },
        text = {
            Column {
                OutlinedTextField(
                    value = titulo,
                    onValueChange = { titulo = it },
                    label = { Text("Título") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = contenido,
                    onValueChange = { contenido = it },
                    label = { Text("Contenido") },
                    modifier = Modifier.fillMaxWidth().height(150.dp)
                )
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    modifier = Modifier.weight(1f),
                    onClick = onDismiss,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEC615B))
                ) {
                    Text(
                        text = "Cancelar",
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
                Button(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        if (titulo.isNotBlank() && contenido.isNotBlank() && userId != null) {
                            onNotaCreada(
                                Nota(
                                    titulo = titulo,
                                    contenido = contenido,
                                    userId = userId,
                                    fechaCreacion = fechaActual,
                                    fechaModificacion = fechaActual
                                )
                            )
                        }
                    },
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Guardar")
                }
            }
        }

    )
}

/*
private fun formatTime(millis: Long): String {
    val minutes = millis / 60_000
    val seconds = (millis % 60_000) / 1_000
    return String.format("%02d:%02d", minutes, seconds)
}
*/
