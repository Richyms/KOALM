@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.koalm.ui.screens.parametroSalud.niveles.peso

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.koalm.ui.components.BarraNavegacionInferior
import com.example.koalm.ui.components.ExitoDialogoGuardadoAnimado
import com.example.koalm.viewmodels.PesoViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*

@Composable
fun PantallaActualizarPeso(
    navController: NavHostController,
    viewModel: PesoViewModel = viewModel()
) {
    val peso by viewModel.peso.collectAsState()
    val fecha by viewModel.fecha.collectAsState()
    val correo = FirebaseAuth.getInstance().currentUser?.email

    val firestore = Firebase.firestore
    val storage   = Firebase.storage

    val coroutineScope   = rememberCoroutineScope()
    var showPhotoCheck by remember { mutableStateOf(false) }
    var pesoText by remember { mutableStateOf(TextFieldValue(if (peso == 0f) "" else peso.toString())) }

    var mostrarDialogoExito by remember{ mutableStateOf(false) }
    if (mostrarDialogoExito) {
        ExitoDialogoGuardadoAnimado(
            mensaje = "¡Subido correctamente!",
            onDismiss = {
                mostrarDialogoExito = false
            }
        )
    }

    val imagePicker = rememberLauncherForActivityResult(GetContent()) { uri: Uri? ->
        uri?.let {
            coroutineScope.launch {
                try {
                    val path = "usuarios/$correo/fotos/${System.currentTimeMillis()}.jpg"
                    val ref  = storage.reference.child(path)
                    ref.putFile(it).await()
                    val url = ref.downloadUrl.await().toString()
                    firestore.collection("usuarios")
                        .document(correo!!)
                        .update("photoUrl", url)
                        .await()
                    mostrarDialogoExito = true
                } catch(e: Exception) {
                    Log.e("DEBUG_PESO","Error subiendo foto",e)
                }
            }
        }
    }

    val green = Color(0xFF4CAF50)
    val shape = RoundedCornerShape(8.dp)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Actualizar peso") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val nuevo = pesoText.text.toFloatOrNull() ?: 0f
                        viewModel.actualizarPeso(nuevo) {
                            coroutineScope.launch {
                                guardarPesoEnHistorial(nuevo, fecha, correo!!)
                            }
                            navController.navigateUp()
                        }
                    }) {
                        Icon(Icons.Default.Check, contentDescription = "Guardar")
                    }
                }
            )
        },
        bottomBar = { BarraNavegacionInferior(navController, "inicio") }
    ) { inner ->
        Column(
            Modifier
                .padding(inner)
                .padding(24.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Peso grande centrado
            Text(
                text = if (peso == 0f) "—" else String.format(Locale.getDefault(), "%.1f kg", peso),
                style = MaterialTheme.typography.headlineMedium.copy(fontSize = 48.sp),
                color = green
            )

            Spacer(Modifier.height(32.dp))

            // Row: "Peso actual", input recuadro, "kg el ..."
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Peso actual", fontSize = 16.sp, color = Color.Black)
                val pesoRegex = Regex("^\\d{0,3}(\\.\\d{0,2})?$")

                // recuadro para ingresar peso
                Box(
                    modifier = Modifier
                        .width(80.dp)
                        .height(30.dp)
                        .border(BorderStroke(1.dp, green), shape)
                        .clip(shape)
                        .padding(horizontal = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    BasicTextField(
                        value = pesoText,
                        onValueChange = { newValue ->
                            // Filtrar SOLO si pasa el regex O está vacío
                            if (newValue.text.isEmpty() || pesoRegex.matches(newValue.text)) {
                                pesoText = newValue
                            }
                        },
                        singleLine = true,
                        textStyle = TextStyle(fontSize = 16.sp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }

                // Fecha y unidad
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("kg el $fecha", color = green, fontSize = 14.sp)
                    Spacer(Modifier.width(4.dp))
                    Icon(Icons.Default.CalendarToday, contentDescription = "Fecha", tint = green)
                }
            }

            Spacer(Modifier.height(48.dp))

            // Subir foto
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(shape)
                    .clickable { imagePicker.launch("image/*") }
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Subir foto", color = green, fontSize = 16.sp)
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Seleccionar imagen", tint = green)
                }
            }

        }
    }
}

//Función para historial de pesos
suspend fun guardarPesoEnHistorial(peso: Float, fecha: String, correo: String) {
    val firestore = Firebase.firestore
    val historialRef = firestore.collection("usuarios")
        .document(correo)
        .collection("historialPeso")

    try {
        // Obtener todos los documentos actuales
        val documentos = historialRef.get().await()

        // Contar cuántos registros hay
        val siguienteNumero = documentos.size() + 1
        val idPersonalizado = "peso$siguienteNumero"

        val registro = hashMapOf(
            "peso" to peso,
            "fecha" to fecha
        )

        // Guardar usando el ID personalizado
        historialRef.document(idPersonalizado).set(registro).await()
        Log.d("DEBUG_PESO", "Historial guardado con ID: $idPersonalizado")

    } catch (e: Exception) {
        Log.e("DEBUG_PESO", "Error al guardar historial de peso", e)
    }
}


