/*PantallaPrincipalAjustes*/
package com.example.koalm.ui.screens.ajustes

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.material3.AlertDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import androidx.navigation.NavHostController
import com.example.koalm.R
import com.example.koalm.ui.theme.*
import com.google.android.gms.auth.api.identity.Identity
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.example.koalm.ui.viewmodels.InicioSesionPreferences
import com.lottiefiles.dotlottie.core.compose.ui.DotLottieAnimation
import com.lottiefiles.dotlottie.core.util.DotLottieSource
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.koalm.ui.components.ExitoDialogoGuardadoAnimado
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.DocumentReference

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaAjustes(navController: NavHostController) {

    AlertDialog(
        onDismissRequest = {
            navController.popBackStack()
        },
        title = {
            Box(modifier = Modifier
                .fillMaxWidth(),
                contentAlignment = Alignment.Center) {
                // Título
                Text(
                    text = "Ajustes",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,

                )
                // Botón "X" para cerrar
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Regresar"
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                BotonesAjustes(navController)
            }
        },
        confirmButton = {},
        dismissButton = {}
    )
}


@Suppress("DEPRECATION")
fun cerrarSesion(context: Context, navController: NavHostController) {
    FirebaseAuth.getInstance().signOut()
    InicioSesionPreferences(context).reiniciarAnimacion()

    @Suppress("DEPRECATION")
    Identity.getSignInClient(context)
        .signOut()
        .addOnCompleteListener {
            // …
        }
    // 3. Borra SharedPreferences con extensión KTX
    context.getSharedPreferences(
        context.getString(R.string.prefs_file),
        Context.MODE_PRIVATE
    ).edit {
        clear()
    }

    // 4. Redirige a la pantalla de inicio y limpia el back stack
    navController.navigate("iniciar") {
        popUpTo("menu") { inclusive = true }
    }
}

@Composable
private fun BotonesAjustes(navController: NavHostController) {
    val botonModifier = Modifier
        .fillMaxWidth(0.8f)
        .padding(vertical = 4.dp)
    val context = LocalContext.current

    var mostrarDialogoExito by remember{ mutableStateOf(false) }
    if (mostrarDialogoExito) {
        ExitoDialogoGuardadoAnimado(
            mensaje = "¡Cuenta eliminada exitosamente!",
            onDismiss = {
                mostrarDialogoExito = false
                navController.navigate("iniciar") {
                    popUpTo("home") { inclusive = true }
                }
            }
        )
    }

    Button(
        onClick = { navController.navigate("nosotros") },
        colors = ButtonDefaults.buttonColors(containerColor = VerdePrincipal),
        modifier = botonModifier
    ) {
        Text("Sobre Nosotros", color = Blanco)
    }

    Button(
        onClick = { navController.navigate("privacidad") },
        colors = ButtonDefaults.buttonColors(containerColor = VerdePrincipal),
        modifier = botonModifier
    ) {
        Text("Privacidad", color = Blanco)
    }

    Button(
        onClick = { navController.navigate("TyC") },
        colors = ButtonDefaults.buttonColors(containerColor = VerdePrincipal),
        modifier = botonModifier
    ) {
        Text("Términos y Condiciones", color = Blanco)
    }

    Button(
        onClick = { navController.navigate("cambiar_contrasena") },
        colors = ButtonDefaults.buttonColors(containerColor = VerdePrincipal),
        modifier = botonModifier
    ) {
        Text("Cambiar Contraseña", color = Blanco)
    }

    Button(
        onClick = { cerrarSesion(context, navController) },
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEC615B)),
        modifier = botonModifier
    ) {
        Text("Cerrar sesión", color = Blanco)
    }

    var mostrarDialogoEliminarCuenta by remember { mutableStateOf(false) }

    if (mostrarDialogoEliminarCuenta) {
        ConfirmacionDialogoEliminarCuenta(
            onConfirmar = {
                mostrarDialogoEliminarCuenta = false
                val user = FirebaseAuth.getInstance().currentUser
                val db = FirebaseFirestore.getInstance()

                user?.email?.let { email ->
                    val usuarioDoc = db.collection("usuarios").document(email)
                    val habitosDoc = db.collection("habitos").document(email)

                    fun borrarSubcoleccion(
                        docRef: DocumentReference,
                        subcoleccion: String,
                        onComplete: () -> Unit,
                        onError: (Exception) -> Unit
                    ) {
                        val subcolRef = docRef.collection(subcoleccion)
                        subcolRef.get()
                            .addOnSuccessListener { snapshot ->
                                val batch = db.batch()
                                snapshot.documents.forEach { batch.delete(it.reference) }
                                batch.commit()
                                    .addOnSuccessListener { onComplete() }
                                    .addOnFailureListener { e -> onError(e) }
                            }
                            .addOnFailureListener { e -> onError(e) }
                    }

                    fun borrarColeccionProgreso(
                        habitoDocRef: DocumentReference,
                        onComplete: () -> Unit,
                        onError: (Exception) -> Unit
                    ) {
                        borrarSubcoleccion(habitoDocRef, "progreso", onComplete, onError)
                    }

                    fun borrarHabitos(
                        habitosCollectionRef: CollectionReference,
                        onComplete: () -> Unit,
                        onError: (Exception) -> Unit
                    ) {
                        habitosCollectionRef.get()
                            .addOnSuccessListener { snapshot ->
                                if (snapshot.isEmpty) {
                                    onComplete()
                                    return@addOnSuccessListener
                                }
                                var deletedCount = 0
                                snapshot.documents.forEach { habitoDoc ->
                                    borrarColeccionProgreso(habitoDoc.reference, {
                                        habitoDoc.reference.delete()
                                            .addOnSuccessListener {
                                                deletedCount++
                                                if (deletedCount == snapshot.size()) onComplete()
                                            }
                                            .addOnFailureListener { e -> onError(e) }
                                    }, { e -> onError(e) })
                                }
                            }
                            .addOnFailureListener { e -> onError(e) }
                    }

                    // Primero borramos metasSalud dentro del usuario
                    borrarSubcoleccion(usuarioDoc, "metasSalud", {
                        // Seguido borramos notificaciones dentro del usuario
                        borrarSubcoleccion(usuarioDoc, "notificaciones", {
                            // Luego borramos el documento usuario
                            usuarioDoc.delete()
                                .addOnSuccessListener {
                                    // Después borramos hábitos personalizados
                                    borrarHabitos(habitosDoc.collection("personalizados"), {
                                        // Luego hábitos predeterminados
                                        borrarHabitos(habitosDoc.collection("predeterminados"), {
                                            // Finalmente borramos documento habitos
                                            habitosDoc.delete()
                                                .addOnSuccessListener {
                                                    // Y eliminamos usuario de Firebase Auth
                                                    user.delete()
                                                        .addOnCompleteListener { task ->
                                                            if (task.isSuccessful) {
                                                                mostrarDialogoExito = true
                                                            } else {
                                                                val error = task.exception?.localizedMessage ?: "Error desconocido"
                                                                Toast.makeText(
                                                                    context,
                                                                    "Error al eliminar autenticación: $error",
                                                                    Toast.LENGTH_LONG
                                                                ).show()
                                                            }
                                                        }
                                                }
                                                .addOnFailureListener { e ->
                                                    Toast.makeText(
                                                        context,
                                                        "Error al eliminar documento habitos: ${e.localizedMessage}",
                                                        Toast.LENGTH_LONG
                                                    ).show()
                                                }
                                        }, { e ->
                                            Toast.makeText(
                                                context,
                                                "Error al eliminar habitos predeterminados: ${e.localizedMessage}",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        })
                                    }, { e ->
                                        Toast.makeText(
                                            context,
                                            "Error al eliminar habitos personalizados: ${e.localizedMessage}",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    })
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(
                                        context,
                                        "Error al eliminar datos de usuario: ${e.localizedMessage}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                        }, { e ->
                            Toast.makeText(
                                context,
                                "Error al eliminar notificaciones: ${e.localizedMessage}",
                                Toast.LENGTH_LONG
                            ).show()
                        })
                    }, { e ->
                        Toast.makeText(
                            context,
                            "Error al eliminar metasSalud: ${e.localizedMessage}",
                            Toast.LENGTH_LONG
                        ).show()
                    })
                } ?: run {
                    Toast.makeText(context, "No se pudo obtener el correo del usuario", Toast.LENGTH_SHORT).show()
                }
            },
            onCancelar = {
                mostrarDialogoEliminarCuenta = false
            }
        )
    }

    Button(
        onClick = {
            mostrarDialogoEliminarCuenta = true
        },
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEC615B)),
        modifier = botonModifier
    ) {
        Text("Borrar Cuenta", color = Blanco)
    }
}

@Composable
fun ConfirmacionDialogoEliminarCuenta(
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
                    modifier = Modifier
                        .size(120.dp)
                        //.background(MaterialTheme.colorScheme.surface, shape = CircleShape)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "¿Eliminar cuenta?",
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "¿Estás seguro de que deseas eliminar tu cuenta? Esta acción es permanente y no se puede deshacer.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
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
                        Text("Cancelar", color = MaterialTheme.colorScheme.onPrimary)
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
