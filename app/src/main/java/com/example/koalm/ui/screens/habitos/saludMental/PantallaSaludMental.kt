package com.example.koalm.ui.screens.habitos.saludMental

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import com.example.koalm.model.Habito
import com.example.koalm.model.TipoHabito
import com.example.koalm.repository.HabitoRepository
import com.example.koalm.ui.components.BarraNavegacionInferior
import com.example.koalm.ui.theme.*
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.dotlottie.dlplayer.Mode
import com.example.koalm.R
import com.example.koalm.ui.components.ExitoDialogoGuardadoAnimado
import com.example.koalm.ui.screens.habitos.personalizados.eliminarHabitoPersonalizado
import com.google.firebase.auth.FirebaseAuth
import com.example.koalm.utils.TimeUtils
import com.lottiefiles.dotlottie.core.compose.ui.DotLottieAnimation
import com.lottiefiles.dotlottie.core.util.DotLottieSource

private const val TAG = "PantallaSaludMental"

private val diasSemana = listOf("L", "M", "X", "J", "V", "S", "D")

private fun formatearDuracion(minutos: Int): String {
    return if (minutos < 60) {
        "${minutos}min"
    } else {
        val horas = minutos / 60
        val mins = minutos % 60
        if (mins == 0) "${horas}h" else "${horas}h ${mins}min"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaSaludMental(navController: NavHostController) {
    val context = LocalContext.current
    val habitosRepository = remember { HabitoRepository() }
    val scope = rememberCoroutineScope()
    val auth = FirebaseAuth.getInstance()

    // Estado de la UI
    var habitosActivos by remember { mutableStateOf<List<Habito>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    // Función para recargar los hábitos
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

                val userId = currentUser.uid
                Log.d(TAG, "Buscando hábitos para userId: $userId")
                
                habitosRepository.obtenerHabitosActivos(userId).fold(
                    onSuccess = { habitos ->
                        Log.d(TAG, "Hábitos encontrados: ${habitos.size}")
                        habitos.forEach { habito ->
                            Log.d(TAG, "Hábito: id=${habito.id}, titulo=${habito.titulo}")
                        }
                        // Filtrar solo los hábitos mentales
                        habitosActivos = habitos.filter {
                            it.tipo in listOf(
                                TipoHabito.MEDITACION,
                                TipoHabito.LECTURA,
                                TipoHabito.DESCONEXION_DIGITAL,
                                TipoHabito.ESCRITURA
                            )
                        }
                        isLoading = false
                    },
                    onFailure = { error ->
                        Log.e(TAG, "Error al cargar hábitos: ${error.message}", error)
                        errorMessage = "Error al cargar hábitos: ${error.message}"
                        showError = true
                        isLoading = false
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error inesperado al cargar hábitos: ${e.message}", e)
                errorMessage = "Error inesperado: ${e.message}"
                showError = true
                isLoading = false
            }
        }
    }

    // Cargar hábitos activos al iniciar
    LaunchedEffect(Unit) {
        cargarHabitos()
    }

    val habitosPlantilla = listOf(
        Habito(
            titulo = "Lectura",
            descripcion = "Registra y administra tus lecturas.",
            tipo = TipoHabito.LECTURA
        ),
        Habito(
            titulo = "Meditación",
            descripcion = "Tomate un tiempo para ti y tu mente.",
            tipo = TipoHabito.MEDITACION
        ),
        Habito(
            titulo = "Desconexión digital",
            descripcion = "Re-vive fuera de tu pantalla.",
            tipo = TipoHabito.DESCONEXION_DIGITAL
        ),
        Habito(
            titulo = "Escritura",
            descripcion = "Tomate un tiempo para ti y tu cuaderno.",
            tipo = TipoHabito.ESCRITURA
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de hábitos de salud mental") },
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
                rutaActual = "salud_mental"
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
                HabitoPlantillaCard(habito, navController)
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
                        text = "Mis hábitos",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    habitosActivos.forEach { habito ->
                        Log.d(TAG, "Renderizando hábito activo: ${habito.titulo}")
                        HabitoActivoCardMental(
                            habito = habito,
                            navController = navController,
                            onHabitDeleted = { cargarHabitos() }
                        )
                    }
                } else {
                    Log.d(TAG, "No tienes hábitos mentales configurados")
                    Text(
                        text = "No tienes hábitos mentales configurados",
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
private fun HabitoPlantillaCard(habito: Habito, navController: NavHostController) {
    val context = LocalContext.current

    Card(
        onClick = {
            try {
                when (habito.tipo) {
                    TipoHabito.ESCRITURA -> navController.navigate("configurar_habito_escritura") {
                        launchSingleTop = true
                        restoreState = true
                    }
                    TipoHabito.MEDITACION -> navController.navigate("configurar_habito_meditacion") {
                        launchSingleTop = true
                        restoreState = true
                    }
                    TipoHabito.DESCONEXION_DIGITAL -> navController.navigate("configurar_habito_desconexion_digital") {
                        launchSingleTop = true
                        restoreState = true
                    }
                    TipoHabito.LECTURA -> navController.navigate("configurar_habito_lectura") {
                        launchSingleTop = true
                        restoreState = true
                    }
                    else -> {
                        // Manejar otros tipos o no hacer nada
                        Log.w(TAG, "Tipo de hábito no manejado en salud mental: ${habito.tipo}")
                    }
                }
            } catch (e: Exception) {
                Log.e("PantallaSaludMental", "Error al navegar: ${e.message}", e)
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
private fun HabitoActivoCardMental(
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
    var mostrarDialogoConfirmacion by remember { mutableStateOf(false) }

    //Mensaje de exito
    var mostrarDialogoExito by remember{ mutableStateOf(false) }
    if (mostrarDialogoExito) {
        ExitoDialogoGuardadoAnimado(
            mensaje = "¡Hábito eliminado con éxito!",
            onDismiss = {
                mostrarDialogoExito = false
                onHabitDeleted()
            }
        )
    }
    Card(
        onClick = {},
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
                    TipoHabito.MEDITACION -> Icons.Default.SelfImprovement
                    TipoHabito.LECTURA -> Icons.Default.MenuBook
                    TipoHabito.DESCONEXION_DIGITAL -> Icons.Default.PhoneDisabled
                    TipoHabito.ESCRITURA -> Icons.Default.Edit
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
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = habito.diasSeleccionados.mapIndexed { index, seleccionado ->
                            if (seleccionado) diasSemana[index] else ""
                        }.filter { it.isNotEmpty() }.joinToString(""),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = if (habito.tipo == TipoHabito.ESCRITURA || habito.tipo == TipoHabito.LECTURA ) {
                            "${formatearDuracion(habito.duracionMinutos)} · ${habito.objetivoPaginas} pág/día"
                        } else {
                            formatearDuracion(habito.duracionMinutos)
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )


                }
            }

            // Menú de opciones
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Opciones")
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    if (habito.tipo == TipoHabito.MEDITACION) {
                        DropdownMenuItem(
                            text = { Text("Temporizador") },
                            onClick = {
                                showMenu = false
                                navController.navigate("temporizador_meditacion/${habito.duracionMinutos}")
                            }
                        )
                        Divider()
                    }

                    if (habito.tipo == TipoHabito.ESCRITURA) {
                        DropdownMenuItem(
                            text = { Text("Mis notas") },
                            onClick = {
                                showMenu = false
                                navController.navigate("notas")
                            }
                        )
                        Divider()
                    }

                    if (habito.tipo == TipoHabito.LECTURA) {
                        DropdownMenuItem(
                            text = { Text("Mis libros") },
                            onClick = {
                                showMenu = false
                                navController.navigate("libros")
                            }
                        )
                        Divider()
                    }

                    DropdownMenuItem(
                        text = { Text("Editar") },
                        onClick = {
                            showMenu = false
                            when (habito.tipo) {
                                TipoHabito.MEDITACION -> {
                                    navController.navigate("configurar_habito_meditacion/${habito.id}")
                                }
                                TipoHabito.LECTURA -> {
                                    navController.navigate("configurar_habito_lectura/${habito.id}")
                                }
                                TipoHabito.ESCRITURA -> {
                                    navController.navigate("configurar_habito_escritura/${habito.id}")
                                }
                                TipoHabito.DESCONEXION_DIGITAL -> {
                                    navController.navigate("configurar_habito_desconexion_digital/${habito.id}")
                                }
                                else -> {
                                    Log.w("EditarHabito", "Tipo no soportado: ${habito.tipo}")
                                }
                            }
                        }
                    )

                    DropdownMenuItem(
                        text = { Text("Eliminar") },
                        onClick = {
                            showMenu = false
                            mostrarDialogoConfirmacion = true
                        }
                    )
                }


            }
        }
    }
    // Diálogo de confirmación
    if (mostrarDialogoConfirmacion) {
        ConfirmacionDialogoEliminarAnimado(
            onCancelar = { mostrarDialogoConfirmacion = false },
            onConfirmar = {
                mostrarDialogoConfirmacion = false
                scope.launch {
                    try {
                        val result = habitosRepository.eliminarHabito(habito.id)
                        result.onSuccess {
                            mostrarDialogoExito = true
                        }.onFailure { e ->
                            Log.e("PantallaSaludMental", "Error al eliminar hábito: ${e.message}", e)
                            Toast.makeText(
                                context,
                                "Error al eliminar hábito: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } catch (e: Exception) {
                        Log.e("PantallaSaludMental", "Error inesperado: ${e.message}", e)
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
                                    Log.e("PantallaSaludMental", "Error al eliminar hábito: ${e.message}", e)
                                    Toast.makeText(
                                        context,
                                        "Error al eliminar hábito: ${e.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } catch (e: Exception) {
                                Log.e("PantallaSaludMental", "Error inesperado: ${e.message}", e)
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

//Confirmacion de eliminacion del habito
@Composable
fun ConfirmacionDialogoEliminarAnimado(
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit
) {
    Dialog(
        onDismissRequest = { /* Evitar cierre automático */ },
        properties = DialogProperties(dismissOnClickOutside = false, dismissOnBackPress = false)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier
                .padding(16.dp)
                .wrapContentSize()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Lottie de advertencia
                DotLottieAnimation(
                    source = DotLottieSource.Url("https://lottie.host/039fc5d3-fdaa-4025-9051-c2843ff5eab4/1RvypHYH4i.lottie"),
                    autoplay = true,
                    loop = true,
                    speed = 1f,
                    useFrameInterpolation = false,
                    playMode = Mode.FORWARD,
                    modifier = Modifier
                        .size(120.dp)
                        .background(MaterialTheme.colorScheme.surface, shape = CircleShape)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "¿Eliminar hábito?",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "¿Estás seguro de que deseas eliminar este hábito? Esta acción no se puede deshacer.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = onCancelar,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEC615B))
                    ) {
                        Text(
                            stringResource(R.string.boton_cancelar_modificaciones),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = onConfirmar
                    ) {
                        Text("Eliminar")
                    }
                }
            }
        }
    }
}