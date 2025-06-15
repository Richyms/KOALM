package com.example.koalm.ui.screens.habitos.personalizados

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.koalm.model.ClaseHabito
import com.example.koalm.model.Habito
import com.example.koalm.model.TipoHabito
import com.example.koalm.ui.components.BarraNavegacionInferior
import com.example.koalm.ui.theme.*
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import com.example.koalm.data.HabitosRepository.obtenerHabitosPersonalizados
import com.example.koalm.services.notifications.AlimentationNotificationService
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.koalm.R
import com.example.koalm.model.HabitoPersonalizado
import com.example.koalm.ui.components.BarraNavegacionInferior
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.runtime.setValue
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.koalm.ui.components.obtenerIconoPorNombre
import com.lottiefiles.dotlottie.core.compose.ui.DotLottieAnimation
import com.lottiefiles.dotlottie.core.util.DotLottieSource
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import com.dotlottie.dlplayer.Mode
import com.example.koalm.ui.components.ExitoDialogoGuardadoAnimado

import com.google.firebase.auth.FirebaseAuth
import com.example.koalm.services.notifications.NotificationBase
import com.example.koalm.ui.screens.HabitoCardPersonalizado

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaNotificacionesPersonalizados(navController: NavHostController){
    val context = LocalContext.current

    val usuarioEmail = FirebaseAuth.getInstance().currentUser?.email
    if (usuarioEmail.isNullOrBlank()) {
        Log.e("PantaNotificacionesPersonalizados", "El email del usuario es nulo o vacío.")
        return
    }

    // State para almacenar los hábitos obtenidos
    val habitos = remember { mutableStateOf<List<HabitoPersonalizado>>(emptyList()) }

    // Estado de carga
    val isLoading = remember { mutableStateOf(true) }

    // Llamar a la función para obtener los hábitos
    LaunchedEffect(usuarioEmail) {
        isLoading.value = true
        val hoy = LocalDate.now().toString()

        try {
            /**/val listaHabitos = obtenerHabitosPersonalizados(usuarioEmail)

            // Verifica y actualiza hábitos finalizados
            val habitosActualizados = listaHabitos.map { habito ->
                if (habito.fechaFin == hoy && habito.estaActivo) {
                    desactivarHabito(habito, usuarioEmail)
                    habito.copy(estaActivo = false)
                } else {
                    habito
                }
            }

            habitos.value = habitosActualizados
                        /**/
        } catch (e: Exception) {
            Log.e("Firestore", "Error al obtener hábitos: ${e.message}")
        } finally {
            isLoading.value = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
               title = {Text("Notificaciones de hábitos personalizados")},
                navigationIcon = {
                    IconButton(onClick = {navController.navigateUp()}) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        },
        bottomBar = {
            BarraNavegacionInferior(navController, "inicio")
        }
    ) { innerPadding  ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center)
        {
            Text(
                text = "Hábitos por realizar",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            val habitosActivos = habitos.value.filter { it.estaActivo }
            if (isLoading.value) {
                CircularProgressIndicator(modifier = Modifier.size(40.dp))
            } else if (habitos.value.isEmpty()) {
                // Si no hay hábitos, muestra el mensaje y el botón de agregar
                Image(
                    painter = painterResource(id = R.drawable.koala_triste),
                    contentDescription = null,
                    modifier = Modifier
                        .size(200.dp)
                        .padding(bottom = 16.dp)
                )

                Text(
                    text = stringResource(R.string.mensaje_no_habitos),
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = { navController.navigate("configurar_habito_personalizado") },
                    modifier = Modifier
                        .width(150.dp)
                        .align(Alignment.CenterHorizontally)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = stringResource(R.string.boton_agregar))
                }
            } else {
                val coroutineScope = rememberCoroutineScope()
                if (habitosActivos.isEmpty()) {
                    Text(
                        text = "No tienes hábitos activos",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    habitosActivos.forEach { habito ->
                        HabitoCardPerso(
                            habito = habito,
                            navController = navController,
                            onEliminarHabito = {
                                coroutineScope.launch {
                                    habitos.value = obtenerHabitosPersonalizados(usuarioEmail)
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun HabitoCardPerso(
    habito: HabitoPersonalizado,
    navController: NavHostController,
    onEliminarHabito: () -> Unit = {},
    colorPersonalizado: Color? = null
) {
    var mostrarDialogoConfirmacion by remember { mutableStateOf(false) }
    var mostrarDialogoExito by remember { mutableStateOf(false) }

    if (mostrarDialogoExito) {
        ExitoDialogoGuardadoAnimado(
            mensaje = "¡Hábito eliminado con éxito!",
            onDismiss = {
                mostrarDialogoExito = false
                onEliminarHabito()
            }
        )
    }

    val icono = obtenerIconoPorNombre(habito.iconoEtiqueta)
    val colorTarjeta = colorPersonalizado ?: parseColorFromFirebase(habito.colorEtiqueta)
    val colorIcono = colorPersonalizado ?: parseColorFromFirebase(habito.colorEtiqueta, darken = true)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colorTarjeta),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .shadow(4.dp, shape = RoundedCornerShape(8.dp), clip = false)
                        .background(Color.White, shape = RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icono,
                        contentDescription = "Icono del Hábito",
                        tint = colorIcono,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = habito.nombre,
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 24.sp ,fontWeight = FontWeight.Bold),
                    )
                    if (habito.descripcion.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = habito.descripcion,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

        }
    }

    if (mostrarDialogoConfirmacion) {
        ConfirmacionDialogoEliminarAnimado(
            habitoNombre = habito.nombre,
            onCancelar = { mostrarDialogoConfirmacion = false },
            onConfirmar = {
                mostrarDialogoConfirmacion = false
                eliminarHabitoPersonalizado(
                    nombreHabito = habito.nombre,
                    usuarioEmail = FirebaseAuth.getInstance().currentUser?.email,
                    onSuccess = { mostrarDialogoExito = true }
                )
            }
        )
    }
}