package com.example.koalm.ui.screens.habitos.saludFisica

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
import com.example.koalm.repository.HabitoRepository
import com.example.koalm.ui.components.BarraNavegacionInferior
import com.example.koalm.ui.theme.*
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import com.example.koalm.services.notifications.AlimentationNotificationService

import com.google.firebase.auth.FirebaseAuth
import com.example.koalm.services.notifications.NotificationBase

private const val TAG = "PantallaSaludFisica"

private val diasSemana = listOf("L", "M", "X", "J", "V", "S", "D")

private fun formatearDuracion(minutos: Int): String {
    val horas = minutos / 60
    val mins = minutos % 60
    // Mostrar siempre en horas, redondeando hacia arriba si hay minutos restantes
    return "${horas}h"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaSaludFisica(navController: NavHostController) {
    val context = LocalContext.current
    val habitosRepository = remember { HabitoRepository() }
    val scope = rememberCoroutineScope()
    val auth = FirebaseAuth.getInstance()

    // Estado de la UI
    var habitosActivos by remember {
        mutableStateOf<List<Habito>>(emptyList())}
    var isLoading by remember { mutableStateOf(true) }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    // Función para recargar los hábitos físicos
    fun cargarHabitos() {
        scope.launch {
            try {
                val currentUser = auth.currentUser
                if (currentUser == null) {
                    Log.e(TAG, "No hay usuario autenticado")
                    errorMessage = "Debes iniciar sesión para ver tus hábitos"
                    showError = true
                    isLoading = false
                    return@launch
                }

                habitosRepository.obtenerHabitosFisicos(currentUser.uid).fold(
                    onSuccess = { habitos ->
                        habitosActivos = habitos.map { habito ->
                            Log.d(TAG, "Procesando habito: ${habito.titulo}, tipo: ${habito.tipo}, horarios: ${habito.horarios}, hora: ${habito.hora}")
                            val habitoConHorarios = if (habito.tipo == TipoHabito.ALIMENTACION && habito.horarios.isNullOrEmpty()) {
                                habito.copy(horarios = listOf(habito.hora))
                            } else {
                                habito
                            }

                            Log.d(TAG, "Resultado habito: ${habitoConHorarios.titulo}, Horarios: ${habitoConHorarios.horarios}")
                            habitoConHorarios
                        }

                        isLoading = false
                    },
                    onFailure = { error ->
                        errorMessage = "Error al cargar hábitos: ${error.message}"
                        showError = true
                        isLoading = false
                    }
                )
            } catch (e: Exception) {
                errorMessage = "Error inesperado: ${e.message}"
                showError = true
                isLoading = false
            }
        }
    }

    // Cargar hábitos al iniciar
    LaunchedEffect(Unit) {
        cargarHabitos()
    }

    val habitosPlantilla = listOf(
        Habito(
            titulo = "Sueño",
            descripcion = "Registra tus horas de sueño y mejora tu descanso.",
            clase = ClaseHabito.FISICO,
            tipo = TipoHabito.SUEÑO
        ),
        Habito(
            titulo = "Alimentación",
            descripcion = "Comer a tus horas, nutre más que los alimentos.",
            clase = ClaseHabito.FISICO,
            tipo = TipoHabito.ALIMENTACION
        ),
        Habito(
            titulo = "Hidratación",
            descripcion = "Recuerda hidratarte cada día",
            clase = ClaseHabito.FISICO,
            tipo = TipoHabito.HIDRATACION
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de hábitos de salud física") },
                navigationIcon = {
                    IconButton(onClick = navController::navigateUp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            BarraNavegacionInferior(
                navController = navController,
                rutaActual = "salud_fisica"
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Sección de plantilla de hábitos
            Text(
                text = "Configura tus hábitos",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            habitosPlantilla.forEach { habito ->
                HabitoPlantillaCardFisico(habito, navController)
            }

            // Sección de hábitos activos
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                if (habitosActivos.isNotEmpty()) {
                    Text(
                        text = "Mis hábitos físicos",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    habitosActivos.forEach { habito ->
                        HabitoActivoCardFisico(
                            habito = habito,
                            navController = navController,
                            onHabitDeleted = { cargarHabitos() }
                        )
                    }
                } else {
                    Text(
                        text = "No tienes hábitos físicos configurados",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
            }
        }
    }

    // Diálogo de error
    if (showError) {
        AlertDialog(
            onDismissRequest = { showError = false },
            title = { Text("Error") },
            text = { Text(errorMessage) },
            confirmButton = {
                TextButton(onClick = { showError = false }) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
fun HabitoPlantillaCardFisico(habito: Habito, navController: NavHostController) {
    val context = LocalContext.current

    Card(
        onClick = {
            try {
                when (habito.tipo) {
                    TipoHabito.SUEÑO -> navController.navigate("configurar_habito_sueno") {
                        launchSingleTop = true
                        restoreState = true
                    }

                    TipoHabito.ALIMENTACION -> navController.navigate("configurar_habito_alimentacion") {
                        launchSingleTop = true
                        restoreState = true
                    }

                    TipoHabito.HIDRATACION -> navController.navigate("configurar_habito_hidratacion") {
                        launchSingleTop = true
                        restoreState = true
                    }
                    TipoHabito.LECTURA -> { /* No hacer nada o manejar caso mental */ }
                    TipoHabito.MEDITACION -> { /* No hacer nada o manejar caso mental */ }
                    TipoHabito.DESCONEXION_DIGITAL -> { /* No hacer nada o manejar caso mental */ }
                    TipoHabito.ESCRITURA -> { /* No hacer nada o manejar caso mental */ }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al navegar: ${e.message}", e)
                Toast.makeText(
                    context,
                    "Error al abrir la configuración: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, VerdeBorde, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = VerdeContenedor
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.AddCircle,
                contentDescription = "Agregar hábito",
                tint = VerdePrincipal,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = habito.titulo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = habito.descripcion,
                    style = MaterialTheme.typography.bodyMedium,
                    color = GrisMedio
                )
            }
        }
    }
}

@Composable
fun HabitoActivoCardFisico(
    habito: Habito,
    navController: NavHostController,
    onHabitDeleted: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val habitosRepository = remember { HabitoRepository() }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }

    // Calcular la hora de despertar para hábitos de sueño
    val horaDespertar = if (habito.tipo == TipoHabito.SUEÑO) {
        val horaDormir = habito.hora.split(":").let {
            (it[0].toInt() to it[1].toInt())
        }
        val duracionHoras = habito.duracionMinutos / 60
        val duracionMinutos = habito.duracionMinutos % 60
        
        var horaFinal = horaDormir.first + duracionHoras
        var minutosFinal = horaDormir.second + duracionMinutos
        
        if (minutosFinal >= 60) {
            horaFinal += 1
            minutosFinal -= 60
        }
        if (horaFinal >= 24) {
            horaFinal -= 24
        }
        
        String.format("%02d:%02d", horaFinal, minutosFinal)
    } else null

    Card(
        onClick = {
            try {
                when (habito.tipo) {
                    TipoHabito.SUEÑO -> navController.navigate("configurar_habito_sueno/${habito.id}") {
                        launchSingleTop = true
                        restoreState = true
                    }

                    TipoHabito.ALIMENTACION -> navController.navigate("configurar_habito_alimentacion/${habito.id}") {
                        launchSingleTop = true
                        restoreState = true
                    }

                    TipoHabito.HIDRATACION -> navController.navigate("configurar_habito_hidratacion/${habito.id}") {
                        launchSingleTop = true
                        restoreState = true
                    }
                    else -> {
                        // Manejar otros tipos o no hacer nada
                        Log.w(TAG, "Tipo de hábito no manejado en salud física: ${habito.tipo}")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al navegar: ${e.message}", e)
                Toast.makeText(
                    context,
                    "Error al abrir la configuración: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, VerdeBorde, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = VerdeContenedor.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = when (habito.tipo) {
                    TipoHabito.SUEÑO -> Icons.Default.Nightlight
                    TipoHabito.ALIMENTACION -> Icons.Default.Restaurant
                    TipoHabito.HIDRATACION -> Icons.Default.LocalDrink
                    else -> Icons.Default.FitnessCenter
                },
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = habito.titulo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = habito.descripcion,
                    style = MaterialTheme.typography.bodyMedium,
                    color = GrisMedio,
                    maxLines = 1
                )
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Mostrar horario
                    if (habito.tipo == TipoHabito.SUEÑO) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${habito.hora} - $horaDespertar",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = habito.hora,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Mostrar días
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (habito.diasSeleccionados.all { it }) {
                            "L-D"  // Todos los días seleccionados
                        } else {
                            habito.diasSeleccionados.mapIndexed { index, seleccionado ->
                                if (seleccionado) diasSemana[index] else ""
                            }.filter { it.isNotEmpty() }.joinToString("")
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Mostrar duración para hábitos de sueño
                    if (habito.tipo == TipoHabito.SUEÑO) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${habito.duracionMinutos / 60}h",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            IconButton(
                onClick = { showMenu = true }
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Más opciones",
                    tint = GrisMedio
                )
            }
        }
    }

    if (showMenu) {
        AlertDialog(
            onDismissRequest = { showMenu = false },
            title = { Text("Opciones del hábito") },
            text = {
                Column {
                    TextButton(
                        onClick = {
                            showMenu = false
                            showDeleteDialog = true
                        }
                    ) {
                        Text("Eliminar hábito")
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showMenu = false }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar hábito") },
            text = { Text("¿Estás seguro de que deseas eliminar este hábito?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        isProcessing = true
                        scope.launch {
                            try {
                                val result = habitosRepository.eliminarHabito(habito.id)
                                result.onSuccess {
                                    Toast.makeText(
                                        context,
                                        "Hábito eliminado exitosamente",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    onHabitDeleted()
                                }.onFailure { e ->
                                    Log.e(TAG, "Error al eliminar hábito: ${e.message}", e)
                                    Toast.makeText(
                                        context,
                                        "Error al eliminar hábito: ${e.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "Error inesperado: ${e.message}", e)
                                Toast.makeText(
                                    context,
                                    "Error inesperado: ${e.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } finally {
                                isProcessing = false
                            }
                        }
                    }
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}
