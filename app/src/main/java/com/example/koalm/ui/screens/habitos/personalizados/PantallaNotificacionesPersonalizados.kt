package com.example.koalm.ui.screens.habitos.personalizados

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.koalm.ui.components.BarraNavegacionInferior
import com.example.koalm.ui.theme.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.ui.graphics.Color

import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaNotificacionesPersonalizados(navController: NavHostController) {
    val usuarioEmail = FirebaseAuth.getInstance().currentUser?.email
    val db = FirebaseFirestore.getInstance()
    val notificaciones = remember { mutableStateListOf<Map<String, Any>>() }
    val context = LocalContext.current

    // Marcar como leídas las no leídas al abrir pantalla
    LaunchedEffect(usuarioEmail) {
        if (usuarioEmail != null) {
            val notificacionesRef = db.collection("usuarios")
                .document(usuarioEmail)
                .collection("notificaciones")

            // Marcar como leídas las notificaciones no leídas
            notificacionesRef
                .whereEqualTo("leido", false)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    for (doc in querySnapshot.documents) {
                        doc.reference.update("leido", true)
                    }
                }

            // Listener para cargar todas las notificaciones ordenadas
            notificacionesRef
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .addSnapshotListener { snapshots, e ->
                    if (e != null) {
                        Log.e("Firestore", "Error al obtener notificaciones: ${e.message}")
                        return@addSnapshotListener
                    }

                    notificaciones.clear()
                    for (doc in snapshots?.documents ?: emptyList()) {
                        doc.data?.let { notificaciones.add(it) }
                    }
                }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notificaciones de hábitos personalizados") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        },
        bottomBar = {
            BarraNavegacionInferior(navController, "inicio")
        }
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
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(notificaciones.size) { index ->
                        val noti = notificaciones[index]
                        val habitName = noti["habitName"] as? String ?: "Hábito"
                        val mensaje = noti["mensaje"] as? String ?: "Tienes un nuevo recordatorio"
                        val timestamp = noti["timestamp"] as? Long
                        val fecha = timestamp?.let {
                            java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date(it))
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
                }
            }
        }
    }
}
