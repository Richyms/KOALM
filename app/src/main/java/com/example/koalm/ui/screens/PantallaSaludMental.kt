package com.example.koalm.ui.screens

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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import com.google.firebase.auth.FirebaseAuth

private const val TAG = "PantallaSaludMental"

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

    // Cargar hábitos activos al iniciar
    LaunchedEffect(Unit) {
        Log.d(TAG, "Iniciando carga de hábitos activos")
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
                            Log.d(TAG, "Hábito: id=${habito.id}, titulo=${habito.titulo}, activo=${habito.activo}")
                        }
                        habitosActivos = habitos
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
            descripcion = "Tomate un tiempo para ti y tu cuaderno",
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
                text = "Plantilla de hábitos",
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
                        HabitoActivoCard(habito, navController)
                    }
                } else {
                    Log.d(TAG, "No hay hábitos activos para mostrar")
                    Text(
                        text = "No tienes hábitos activos",
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
private fun HabitoActivoCard(habito: Habito, navController: NavHostController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val habitosRepository = remember { HabitoRepository() }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showDeactivateDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }
    
    Card(
        onClick = { 
            try {
                when (habito.tipo) {
                    TipoHabito.ESCRITURA -> navController.navigate("configurar_habito_escritura/${habito.id}") {
                        launchSingleTop = true
                        restoreState = true
                    }
                    TipoHabito.MEDITACION -> navController.navigate("configurar_habito_meditacion/${habito.id}") {
                        launchSingleTop = true
                        restoreState = true
                    }
                    TipoHabito.DESCONEXION_DIGITAL -> navController.navigate("configurar_habito_desconexion_digital/${habito.id}") {
                        launchSingleTop = true
                        restoreState = true
                    }
                    TipoHabito.LECTURA -> navController.navigate("configurar_habito_lectura/${habito.id}") {
                        launchSingleTop = true
                        restoreState = true
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
            containerColor = VerdeContenedor.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (habito.activo) Icons.Default.CheckCircle else Icons.Default.Pause,
                    contentDescription = if (habito.activo) "Hábito activo" else "Hábito inactivo",
                    tint = if (habito.activo) VerdePrincipal else MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(24.dp)
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
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
                    Text(
                        text = "Horario: ${habito.hora}",
                        style = MaterialTheme.typography.bodySmall,
                        color = GrisMedio
                    )
                }

                // Menú de opciones
                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        enabled = !isProcessing
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Más opciones",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Editar") },
                            onClick = {
                                showMenu = false
                                try {
                                    when (habito.tipo) {
                                        TipoHabito.ESCRITURA -> navController.navigate("configurar_habito_escritura/${habito.id}") {
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                        TipoHabito.MEDITACION -> navController.navigate("configurar_habito_meditacion/${habito.id}") {
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                        TipoHabito.DESCONEXION_DIGITAL -> navController.navigate("configurar_habito_desconexion_digital/${habito.id}") {
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                        TipoHabito.LECTURA -> navController.navigate("configurar_habito_lectura/${habito.id}") {
                                            launchSingleTop = true
                                            restoreState = true
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
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null,
                                    tint = VerdePrincipal
                                )
                            }
                        )

                        DropdownMenuItem(
                            text = { Text(if (habito.activo) "Desactivar" else "Activar") },
                            onClick = {
                                showMenu = false
                                showDeactivateDialog = true
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (habito.activo) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = VerdePrincipal
                                )
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Eliminar") },
                            onClick = {
                                showMenu = false
                                showDeleteDialog = true
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        )
                    }
                }
            }
        }
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
                                habitosRepository.eliminarHabito(habito.id)
                                    .onSuccess {
                                        Toast.makeText(
                                            context,
                                            "Hábito eliminado exitosamente",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                    .onFailure { e ->
                                        Log.e("PantallaSaludMental", "Error al eliminar hábito: ${e.message}", e)
                                        Toast.makeText(
                                            context,
                                            "Error al eliminar hábito: ${e.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
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
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showDeactivateDialog) {
        AlertDialog(
            onDismissRequest = { showDeactivateDialog = false },
            title = { Text(if (habito.activo) "Desactivar hábito" else "Activar hábito") },
            text = { Text(if (habito.activo) "¿Estás seguro de que deseas desactivar este hábito?" else "¿Estás seguro de que deseas activar este hábito?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeactivateDialog = false
                        isProcessing = true
                        scope.launch {
                            try {
                                val result = if (habito.activo) {
                                    habitosRepository.desactivarHabito(habito.id, context)
                                } else {
                                    habitosRepository.activarHabito(habito.id, context)
                                }
                                
                                result.onSuccess {
                                    Toast.makeText(
                                        context,
                                        if (habito.activo) "Hábito desactivado exitosamente" else "Hábito activado exitosamente",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }.onFailure { e ->
                                    Log.e("PantallaSaludMental", "Error al ${if (habito.activo) "desactivar" else "activar"} hábito: ${e.message}", e)
                                    Toast.makeText(
                                        context,
                                        "Error al ${if (habito.activo) "desactivar" else "activar"} hábito: ${e.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } finally {
                                isProcessing = false
                            }
                        }
                    }
                ) {
                    Text(if (habito.activo) "Desactivar" else "Activar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeactivateDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
} 