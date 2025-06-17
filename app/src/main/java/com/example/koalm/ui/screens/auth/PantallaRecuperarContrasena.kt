package com.example.koalm.ui.screens.auth

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.koalm.R
import com.example.koalm.ui.theme.*

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.firestore.FirebaseFirestore


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaRecuperarContrasena(navController: NavController) {
    val context = LocalContext.current
    var correo by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recuperar contraseña") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            ImagenKoalaRecuperar()
            Spacer(modifier = Modifier.height(24.dp))
            CampoCorreoRecuperar(correo) { correo = it }
            MensajeExplicacion()
            Spacer(modifier = Modifier.height(16.dp))
            BotonEnviarCorreo(correo, navController, context)
            Spacer(modifier = Modifier.height(32.dp))
            TextoIrARegistro(navController)
        }
    }
}

@Composable
fun ImagenKoalaRecuperar() {
    val isDark = isSystemInDarkTheme()
    val tintColor = if (isDark) Color.White else Color.Black

    Image(
        painter = painterResource(id = R.drawable.query),
        contentDescription = "Koala pregunta",
        modifier = Modifier.size(300.dp)/*,
        colorFilter = ColorFilter.tint(tintColor)*/
    )
}

@Composable
fun CampoCorreoRecuperar(value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("Ingresa tu correo") },

        modifier = Modifier.fillMaxWidth(0.97f),
        shape = RoundedCornerShape(16.dp),

        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = VerdePrincipal,
            unfocusedBorderColor = GrisMedio,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    )
}


@Composable
fun MensajeExplicacion() {
    Text(
        text = "Te enviaremos un enlace de restablecimiento al correo asociado a tu cuenta.",
        fontSize = 12.sp,
        color = GrisMedio,
        modifier = Modifier
            .fillMaxWidth(0.85f)
            .padding(top = 4.dp)
    )
}

@Composable
fun BotonEnviarCorreo(
    correo: String,
    navController: NavController,
    context: android.content.Context
) {
    var correoExiste by remember { mutableStateOf<Boolean?>(null) }
    var mostrarMensajeError by remember { mutableStateOf(false) }

    // Función de validación del correo
    fun validarCorreoExistente(correo: String) {
        FirebaseFirestore.getInstance()
            .collection("usuarios")
            .whereEqualTo("email", correo)
            .get()
            .addOnSuccessListener { docs ->
                correoExiste = !docs.isEmpty
                if (correoExiste == true) {
                    // Si existe, enviar correo de restablecimiento
                    val auth = FirebaseAuth.getInstance()
                    val actionCodeSettings = ActionCodeSettings.newBuilder()
                        .setUrl("https://koalm-94491.web.app")
                        .setHandleCodeInApp(false)
                        .setAndroidPackageName("com.example.koalm", true, "35")
                        .build()

                    auth.sendPasswordResetEmail(correo, actionCodeSettings)
                        .addOnSuccessListener {
                            Toast.makeText(
                                context,
                                "Revisa tu bandeja de entrada; te enviamos el enlace.",
                                Toast.LENGTH_LONG
                            ).show()
                            navController.navigate("restablecer")
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, it.localizedMessage, Toast.LENGTH_LONG).show()
                        }
                } else {
                    // Si no existe, mostrar mensaje de error
                    mostrarMensajeError = true
                    Toast.makeText(
                        context,
                        "No existe una cuenta asociada a este correo.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .addOnFailureListener {
                correoExiste = false
                mostrarMensajeError = true
            }
    }

    val emailValido = correo.isNotBlank() &&
            android.util.Patterns.EMAIL_ADDRESS.matcher(correo).matches()

    Button(
        enabled = emailValido,
        onClick = {
            // Al presionar el botón, validamos si el correo existe
            validarCorreoExistente(correo)
        },
        colors = ButtonDefaults.buttonColors(containerColor = VerdePrincipal)
    ) {
        Text("Enviar", color = Blanco)
    }
}

@Composable
fun TextoIrARegistro(navController: NavController) {
    Text(
        buildAnnotatedString {
            append("¿No tienes una cuenta? ")
            withStyle(SpanStyle(color = VerdeSecundario)) {
                append("Regístrate")
            }
        },
        fontSize = 14.sp,
        modifier = Modifier.clickable {
            navController.navigate("registro")
        }
    )
}
