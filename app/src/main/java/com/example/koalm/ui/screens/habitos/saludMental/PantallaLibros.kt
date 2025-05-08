package com.example.koalm.ui.screens.habitos.saludMental

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
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.koalm.model.Libro
import com.example.koalm.ui.components.BarraNavegacionInferior
import com.example.koalm.ui.theme.VerdeBorde
import com.example.koalm.ui.theme.VerdeContenedor
import com.example.koalm.ui.theme.VerdePrincipal
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaLibros(navController: NavHostController) {
    val context = LocalContext.current
    var libros by remember { mutableStateOf(listOf<Libro>()) }
    var mostrarDialogoNuevoLibro by remember { mutableStateOf(false) }
    var libroAEditar by remember { mutableStateOf<Libro?>(null) }
    
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    // Cargar libros del usuario actual
    LaunchedEffect(Unit) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            db.collection("libros")
                .whereEqualTo("userId", userId)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        val nuevosLibros = mutableListOf<Libro>()
                        for (doc in snapshot.documents) {
                            val libro = Libro(
                                id = doc.id,
                                titulo = doc.getString("titulo") ?: "",
                                autor = doc.getString("autor") ?: "",
                                paginaActual = doc.getLong("paginaActual")?.toInt() ?: 0,
                                calificacion = doc.getLong("calificacion")?.toInt() ?: 0,
                                terminado = doc.getBoolean("terminado") ?: false,
                                userId = doc.getString("userId"),
                                fechaCreacion = doc.getString("fechaCreacion"),
                                fechaModificacion = doc.getString("fechaModificacion")
                            )
                            nuevosLibros.add(libro)
                        }
                        libros = nuevosLibros
                    }
                }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Libros") },
                navigationIcon = {
                    IconButton(onClick = navController::navigateUp) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { mostrarDialogoNuevoLibro = true },
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Nuevo Libro") },
                containerColor = VerdePrincipal,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        },
        bottomBar = {
            BarraNavegacionInferior(navController, rutaActual = "libros")
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
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(libros) { libro ->
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
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = libro.titulo,
                                        style = MaterialTheme.typography.titleMedium,
                                        textDecoration = if (libro.terminado) TextDecoration.LineThrough else TextDecoration.None
                                    )
                                    Text(
                                        text = libro.autor,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                IconButton(
                                    onClick = { libroAEditar = libro }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Editar",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        db.collection("libros").document(libro.id).delete()
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Eliminar",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Página actual: ${libro.paginaActual}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Row {
                                    repeat(5) { index ->
                                        Icon(
                                            imageVector = if (index < libro.calificacion) Icons.Filled.Star else Icons.Outlined.Star,
                                            contentDescription = null,
                                            tint = if (index < libro.calificacion) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (mostrarDialogoNuevoLibro) {
        DialogoNuevoLibro(
            onDismiss = { mostrarDialogoNuevoLibro = false },
            onLibroCreado = { nuevoLibro ->
                val userId = auth.currentUser?.uid
                if (userId != null) {
                    val docRef = db.collection("libros").document()
                    val libroConId = nuevoLibro.copy(id = docRef.id)
                    
                    docRef.set(libroConId.toMap())
                        .addOnSuccessListener {
                            Log.d("PantallaLibros", "Libro creado con ID: ${docRef.id}")
                        }
                }
                mostrarDialogoNuevoLibro = false
            }
        )
    }

    if (libroAEditar != null) {
        DialogoEditarLibro(
            libro = libroAEditar!!,
            onDismiss = { libroAEditar = null },
            onLibroEditado = { libroEditado ->
                db.collection("libros").document(libroEditado.id)
                    .update(libroEditado.toMap())
                    .addOnSuccessListener {
                        libros = libros.map { if (it.id == libroEditado.id) libroEditado else it }
                    }
                libroAEditar = null
            }
        )
    }
}

@Composable
private fun DialogoNuevoLibro(
    onDismiss: () -> Unit,
    onLibroCreado: (Libro) -> Unit
) {
    var titulo by remember { mutableStateOf("") }
    var autor by remember { mutableStateOf("") }
    var paginaActual by remember { mutableStateOf("") }
    var calificacion by remember { mutableStateOf(0) }
    var terminado by remember { mutableStateOf(false) }
    val fechaActual = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo Libro") },
        text = {
            Column {
                OutlinedTextField(
                    value = titulo,
                    onValueChange = { titulo = it },
                    label = { Text("Título del libro") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = autor,
                    onValueChange = { autor = it },
                    label = { Text("Autor") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = paginaActual,
                    onValueChange = { paginaActual = it },
                    label = { Text("Página actual") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Calificación:")
                Row {
                    repeat(5) { index ->
                        IconButton(
                            onClick = { calificacion = index + 1 }
                        ) {
                            Icon(
                                imageVector = if (index < calificacion) Icons.Filled.Star else Icons.Outlined.Star,
                                contentDescription = null,
                                tint = if (index < calificacion) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = terminado,
                        onCheckedChange = { terminado = it }
                    )
                    Text("Libro terminado")
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (titulo.isNotBlank() && autor.isNotBlank() && paginaActual.isNotBlank() && userId != null) {
                        onLibroCreado(Libro(
                            titulo = titulo,
                            autor = autor,
                            paginaActual = paginaActual.toIntOrNull() ?: 0,
                            calificacion = calificacion,
                            terminado = terminado,
                            userId = userId,
                            fechaCreacion = fechaActual,
                            fechaModificacion = fechaActual
                        ))
                    }
                }
            ) { Text("Guardar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
private fun DialogoEditarLibro(
    libro: Libro,
    onDismiss: () -> Unit,
    onLibroEditado: (Libro) -> Unit
) {
    var paginaActual by remember { mutableStateOf(libro.paginaActual.toString()) }
    var calificacion by remember { mutableStateOf(libro.calificacion) }
    var terminado by remember { mutableStateOf(libro.terminado) }
    val fechaActual = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Libro") },
        text = {
            Column {
                Text(
                    text = libro.titulo,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = libro.autor,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = paginaActual,
                    onValueChange = { paginaActual = it },
                    label = { Text("Página actual") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Calificación:")
                Row {
                    repeat(5) { index ->
                        IconButton(
                            onClick = { calificacion = index + 1 }
                        ) {
                            Icon(
                                imageVector = if (index < calificacion) Icons.Filled.Star else Icons.Outlined.Star,
                                contentDescription = null,
                                tint = if (index < calificacion) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = terminado,
                        onCheckedChange = { terminado = it }
                    )
                    Text("Libro terminado")
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val nuevaPagina = paginaActual.toIntOrNull() ?: libro.paginaActual
                    onLibroEditado(libro.copy(
                        paginaActual = nuevaPagina,
                        calificacion = calificacion,
                        terminado = terminado,
                        fechaModificacion = fechaActual
                    ))
                }
            ) { Text("Guardar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
} 