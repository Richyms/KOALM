package com.example.koalm.ui.screens.parametroSalud.niveles.peso

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.koalm.ui.components.BarraNavegacionInferior
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaActualizarPeso(
    navController: NavHostController
) {
    val pesoActual = remember { mutableStateOf(0f) }
    val fechaHoy = LocalDate
        .now()
        .format(DateTimeFormatter.ofPattern("d MMMM, yyyy", Locale("es", "MX")))
    val correo = FirebaseAuth.getInstance().currentUser?.email

    // 🔄 Cargar el pesoActual desde Firestore (subdocumento "valores")
    LaunchedEffect(correo) {
        if (correo != null) {
            val snapshot = Firebase
                .firestore
                .collection("usuarios")
                .document(correo)
                .collection("metasSalud")
                .document("valores")
                .get()
                .await()

            pesoActual.value = snapshot
                .getDouble("pesoActual")
                ?.toFloat()
                ?: 0f

            Log.d("DEBUG_PESO", "Peso cargado de Firestore: ${pesoActual.value}")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Actualizar peso") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Atrás"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        if (correo != null) {
                            val db = Firebase.firestore
                            db.collection("usuarios")
                                .document(correo)
                                .update("peso", pesoActual.value)
                            db.collection("usuarios")
                                .document(correo)
                                .collection("metasSalud")
                                .document("valores")
                                .update("pesoActual", pesoActual.value)
                        }
                        navController.navigateUp()
                    }) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "Guardar",
                            tint = Color.Black
                        )
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
                .padding(horizontal = 24.dp, vertical = 10.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // ←—— Aquí mostramos el pesoActual en grande y centrado
            Text(
                text = if (pesoActual.value == 0f) "—" else String.format(Locale.getDefault(), "%.1f kg", pesoActual.value),
                style = MaterialTheme.typography.headlineMedium.copy(fontSize = 48.sp),
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Campo de entrada con ComponenteInputs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ComponenteInputs("Peso deseado", pesoActual, fechaHoy)
            }
        }
    }
}
