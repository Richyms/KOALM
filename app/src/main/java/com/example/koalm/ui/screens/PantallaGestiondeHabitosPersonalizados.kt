// PantallaGestionHabitosPersonalizados.kt
package com.example.koalm.ui.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.koalm.R
import com.example.koalm.model.HabitoPersonalizado
import com.example.koalm.ui.components.BarraNavegacionInferior
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.runtime.setValue
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.example.koalm.data.HabitosRepository.obtenerHabitosPersonalizados
import com.example.koalm.ui.components.obtenerIconoPorNombre
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaGestionHabitosPersonalizados(navController: NavHostController) {
    // Obtén el correo del usuario autenticado
    val usuarioEmail = FirebaseAuth.getInstance().currentUser?.email
    if (usuarioEmail.isNullOrBlank()) {
        Log.e("PantaGestiondeHabitosPersonalizados", "El email del usuario es nulo o vacío.")
        return
    }

    // State para almacenar los hábitos obtenidos
    val habitos = remember { mutableStateOf<List<HabitoPersonalizado>>(emptyList()) }

    // Estado de carga
    val isLoading = remember { mutableStateOf(true) }

    // Llamar a la función para obtener los hábitos
    LaunchedEffect(usuarioEmail) {
        try {
            // La función de suspensión se llama aquí dentro de LaunchedEffect
            val listaHabitos = obtenerHabitosPersonalizados(usuarioEmail)
            habitos.value = listaHabitos
        } catch (e: Exception) {
            Log.e("Firestore", "Error al obtener hábitos: ${e.message}")
        } finally {
            isLoading.value = false // Cambiar el estado a no cargando
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.titulo_gestion_habitos_personalizados)) },
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
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
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

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(R.string.mensaje_no_habitos_subtexto),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(32.dp))

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
                habitos.value.forEach { habito ->
                    HabitoCardExpandible(
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
                Spacer(Modifier.weight(1f))

                /* ----------------------------  Agregar más hábitos --------------------------- */
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = { navController.navigate("configurar_habito_personalizado") },
                        modifier = Modifier
                            .width(200.dp)
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = stringResource(R.string.boton_agregar),color = MaterialTheme.colorScheme.onPrimary)
                    }

                }
            }
        }
    }
}

@Composable
fun HabitoCardExpandible(
    habito: HabitoPersonalizado,
    navController: NavHostController,
    onEliminarHabito: () -> Unit = {} // Se llama cuando el hábito es eliminado para actualizar la lista
) {
    var expanded by remember { mutableStateOf(false) }
    var expandedMenu by remember { mutableStateOf(false) }
    var mostrarDialogoConfirmacion by remember { mutableStateOf(false) }

    val diasSemana = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo")
    val diasActivos = habito.frecuencia
        ?.mapIndexedNotNull { index, activo -> if (activo) diasSemana.getOrNull(index) else null }
        ?.joinToString(", ")
        ?: "No definida"

    //Obtener el ícono desde FB
    val icono = obtenerIconoPorNombre(habito.iconoEtiqueta)

    // Extensión para oscurecer el color
    fun Color.darken(factor: Float): Color {
        return Color(
            red = (red * (1 - factor)).coerceIn(0f, 1f),
            green = (green * (1 - factor)).coerceIn(0f, 1f),
            blue = (blue * (1 - factor)).coerceIn(0f, 1f),
            alpha = alpha
        )
    }

    // Función para hacer el parseo de color desde FB
    fun parseColorFromFirebase(colorString: String, darken: Boolean = false, darkenFactor: Float = 0.15f): Color {
        val regex = Regex("""Color\(([\d.]+), ([\d.]+), ([\d.]+), ([\d.]+),.*\)""")
        val match = regex.find(colorString)
        return if (match != null) {
            val (r, g, b, a) = match.destructured
            val baseColor = Color(r.toFloat(), g.toFloat(), b.toFloat(), a.toFloat())
            if (darken) baseColor.darken(darkenFactor) else baseColor
        } else {
            Log.e("ColorParse", "No se pudo parsear el color: $colorString")
            Color.Gray
        }
    }

    val colorTarjeta = parseColorFromFirebase(habito.colorEtiqueta)
    val colorIcono = parseColorFromFirebase(habito.colorEtiqueta, darken = true)

    // Tarjeta Expandible
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colorTarjeta),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Column {
            // Parte comprimida
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
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
                    if(habito.descripcion.isNotEmpty() && !expanded)
                    {
                        Text(text = habito.nombre, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = habito.descripcion.take(50) + if (habito.descripcion.length > 50) "..." else "",
                            style = MaterialTheme.typography.bodySmall
                        )
                    } else if (habito.descripcion.isEmpty()){
                        Text(text = habito.nombre, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                        Spacer(modifier = Modifier.height(4.dp))
                    } else if (expanded && habito.descripcion.isNotEmpty()) {
                        Text(text = habito.nombre, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = habito.descripcion,
                            style = MaterialTheme.typography.bodySmall
                        )

                    }

                }

                // Menú de opciones
                Box {
                    IconButton(onClick = { expandedMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Opciones")
                    }

                    DropdownMenu(
                        expanded = expandedMenu,
                        onDismissRequest = { expandedMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Editar") },
                            onClick = {
                                expandedMenu = false
                                navController.navigate("editahabito/${habito.nombre}")
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Eliminar") },
                            onClick = {
                                expandedMenu = false
                                mostrarDialogoConfirmacion = true
                            }
                        )
                    }
                }

                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                        contentDescription = "Expandir"
                    )
                }
            }

            // Parte expandida
            if (expanded) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (diasActivos.isNotBlank()) {
                        Text("Frecuencia: $diasActivos", style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    habito.recordatorios?.horas?.takeIf { it.isNotEmpty() }?.let {
                        Text("Recordatorios: ${it.joinToString(", ")}", style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    habito.fechaInicio?.takeIf { it.isNotBlank() }?.let { fechaRaw ->
                        formatearFecha(fechaRaw)?.let { fechaFormateada ->
                            Text("Inicio: $fechaFormateada", style = MaterialTheme.typography.bodyMedium)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }

                    habito.fechaFin?.takeIf { it.isNotBlank() && it.lowercase() != "null" }?.let { fechaRaw ->
                        formatearFecha(fechaRaw)?.let { fechaFormateada ->
                            Text("Fin: $fechaFormateada", style = MaterialTheme.typography.bodyMedium)
                            Spacer(modifier = Modifier.height(8.dp))
                            }
                    }

                    if (habito.rachaActual > 0) {
                        Text("Racha Actual: ${habito.rachaActual} días", style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    if (habito.rachaMaxima > 0) {
                        Text("Racha Máxima: ${habito.rachaMaxima} días", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }

    // Diálogo de confirmación
    if (mostrarDialogoConfirmacion) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoConfirmacion = false },
            title = { Text("¿Eliminar hábito?") },
            text = { Text("¿Estás seguro de que deseas eliminar el hábito\"${habito.nombre}\"? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = {
                    mostrarDialogoConfirmacion = false
                    eliminarHabitoPersonalizado(
                        nombreHabito = habito.nombre,
                        usuarioEmail = FirebaseAuth.getInstance().currentUser?.email,
                        onSuccess = onEliminarHabito
                    )
                }) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoConfirmacion = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

//Eliminar el hábito
fun eliminarHabitoPersonalizado(
    nombreHabito: String,
    usuarioEmail: String?,
    onSuccess: () -> Unit = {}
) {
    if (usuarioEmail == null) return

    val idDocumento = nombreHabito.replace(" ", "_")

    val db = FirebaseFirestore.getInstance()

    val progresoRef = db.collection("habitos")
        .document(usuarioEmail)
        .collection("personalizados")
        .document(idDocumento)
        .collection("progreso")

    // Primero eliminamos los documentos de la subcolección "progreso"
    progresoRef.get()
        .addOnSuccessListener { snapshot ->
            val batch = db.batch()
            for (document in snapshot.documents) {
                batch.delete(document.reference)
            }

            // Ejecutar el batch para eliminar los documentos de "progreso"
            batch.commit().addOnSuccessListener {
                Log.d("Firestore", "Progreso eliminado correctamente.")

                // Ahora eliminamos el documento principal del hábito
                db.collection("habitos")
                    .document(usuarioEmail)
                    .collection("personalizados")
                    .document(idDocumento)
                    .delete()
                    .addOnSuccessListener {
                        Log.d("Firestore", "Hábito eliminado correctamente.")
                        onSuccess()
                    }
                    .addOnFailureListener { e ->
                        Log.e("Firestore", "Error al eliminar el hábito: ${e.message}")
                    }

            }.addOnFailureListener { e ->
                Log.e("Firestore", "Error al eliminar progreso: ${e.message}")
            }
        }
        .addOnFailureListener { e ->
            Log.e("Firestore", "Error al obtener progreso: ${e.message}")
        }
}

// Formatear fecha
fun formatearFecha(fechaStr: String): String? {
    return try {
        val fecha = LocalDate.parse(fechaStr) //
        val formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale("es", "ES"))
        fecha.format(formatter)
    } catch (e: Exception) {
        null
        }
}

