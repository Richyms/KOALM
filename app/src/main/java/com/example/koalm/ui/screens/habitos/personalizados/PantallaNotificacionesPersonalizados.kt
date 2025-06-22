package com.example.koalm.ui.screens.habitos.personalizados

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.koalm.ui.components.BarraNavegacionInferior
import com.example.koalm.ui.theme.VerdeContenedor
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.absoluteValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaNotificacionesPersonalizados(navController: NavHostController) {
    val usuarioEmail = FirebaseAuth.getInstance().currentUser?.email
    val db = FirebaseFirestore.getInstance()
    val notificaciones = remember { mutableStateListOf<Map<String, Any>>() }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var recentlyDeleted by remember { mutableStateOf<Map<String, Any>?>(null) }

    LaunchedEffect(usuarioEmail) {
        if (usuarioEmail != null) {
            val notificacionesRef = db.collection("usuarios")
                .document(usuarioEmail)
                .collection("notificaciones")

            notificacionesRef
                .whereEqualTo("leido", false)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    for (doc in querySnapshot.documents) {
                        doc.reference.update("leido", true)
                    }
                }

            notificacionesRef
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .addSnapshotListener { snapshots, e ->
                    if (e != null) {
                        Log.e("Firestore", "Error al obtener notificaciones: ${e.message}")
                        return@addSnapshotListener
                    }

                    notificaciones.clear()
                    for (doc in snapshots?.documents ?: emptyList()) {
                        val data = doc.data
                        if (data != null) {
                            data["id"] = doc.id
                            notificaciones.add(data)
                        }
                    }
                }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notificaciones de hábitos") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        },
        bottomBar = {
            BarraNavegacionInferior(navController, "inicio")
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            if (notificaciones.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay notificaciones aún 💤")
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Desliza la tarjeta hacia los lados para eliminar la notificación.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }

                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(notificaciones.size, key = { index ->
                        (notificaciones[index]["id"] ?: index.hashCode()) as Any
                    }) { index ->
                        val noti = notificaciones[index]
                        var offsetX by remember { mutableStateOf(0f) }
                        var dismissed by remember { mutableStateOf(false) }

                        if (!dismissed) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(IntrinsicSize.Min)
                            ) {
                                // Fondo rojo dinámico según desplazamiento
                                val alpha = (offsetX.absoluteValue / 300f).coerceIn(0f, 1f)

                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Red.copy(alpha = alpha), RoundedCornerShape(16.dp))
                                        .padding(horizontal = 20.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = if (offsetX < 0) Arrangement.End else Arrangement.Start
                                ) {
                                    if (offsetX != 0f) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = null,
                                            tint = Color.White.copy(alpha = alpha)
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .offset { IntOffset(offsetX.roundToInt(), 0) }
                                        .pointerInput(Unit) {
                                            detectHorizontalDragGestures(
                                                onDragEnd = {
                                                    if (offsetX < -100f || offsetX > 100f) {
                                                        dismissed = true
                                                        val eliminado = notificaciones.removeAt(index)
                                                        recentlyDeleted = eliminado

                                                        scope.launch {
                                                            val result = snackbarHostState.showSnackbar(
                                                                message = "Notificación eliminada",
                                                                actionLabel = "Deshacer",
                                                                duration = SnackbarDuration.Short
                                                            )
                                                            if (result != SnackbarResult.ActionPerformed) {
                                                                usuarioEmail?.let { email ->
                                                                    val id = eliminado["id"] as? String
                                                                    if (id != null) {
                                                                        db.collection("usuarios")
                                                                            .document(email)
                                                                            .collection("notificaciones")
                                                                            .document(id)
                                                                            .delete()
                                                                    }
                                                                }
                                                            } else {
                                                                recentlyDeleted?.let {
                                                                    notificaciones.add(index, it)
                                                                    recentlyDeleted = null
                                                                }
                                                            }
                                                        }
                                                    } else {
                                                        offsetX = 0f
                                                    }
                                                },
                                                onHorizontalDrag = { _, dragAmount ->
                                                    offsetX = (offsetX + dragAmount).coerceIn(-300f, 300f)
                                                }
                                            )
                                        }
                                ) {
                                    TarjetaNotificacion(noti)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TarjetaNotificacion(noti: Map<String, Any>) {
    val habitName = noti["habitName"] as? String ?: "Hábito koalístico"
    val mensaje = noti["mensaje"] as? String ?: "Tienes un nuevo recordatorio"
    val timestamp = noti["timestamp"] as? Long
    val fecha = timestamp?.let {
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(it))
    } ?: "Fecha desconocida"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = VerdeContenedor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("🔔 $habitName", style = MaterialTheme.typography.titleMedium)
            Text(mensaje, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = fecha,
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}
