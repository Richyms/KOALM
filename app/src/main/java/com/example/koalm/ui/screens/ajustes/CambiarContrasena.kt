package com.example.koalm.ui.screens.ajustes

import android.content.Context
import android.widget.NumberPicker.OnValueChangeListener
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
import androidx.core.content.edit
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.example.koalm.R
import com.example.koalm.ui.components.ExitoDialogoGuardadoAnimado
import com.example.koalm.ui.components.ValidacionesDialogoAnimado
import com.example.koalm.ui.theme.*
import com.example.koalm.ui.viewmodels.InicioSesionPreferences
import com.google.android.gms.auth.api.identity.Identity
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaCambiarContrasena(navController: NavController){
    val context = LocalContext.current
    var password by remember { mutableStateOf("")}

    Scaffold(
        topBar = {
            TopAppBar(
                title = {Text("Cambiar contraseña")},
                navigationIcon = {
                    IconButton(onClick = {navController.navigateUp()}) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                }
            )
        }
    )
    {padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.koala_lupa),
                contentDescription = "Koala lupa",
                modifier = Modifier.size(300.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))
            ValidarContrasenaActual(navController = navController) {}
        }

    }
}

/*Se requerira la contraseña para enviar un correo de validación*/
@Composable
fun CampoValidarContrasena (value: String, onValueChange: (String) -> Unit){
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("Ingresa tu contraseña") },

        modifier = Modifier.fillMaxWidth(0.97f),
        shape = RoundedCornerShape(16.dp),

        //keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = VerdePrincipal,
            unfocusedBorderColor = GrisMedio,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    )
}

@Composable
fun MensajeExplicacionCambio(){
    Text(
        text = "Te enviaremos un enlace de restablecimiento al correo asociado a tu cuenta",
        fontSize = 12.sp,
        color = GrisMedio,
        modifier = Modifier
            .fillMaxWidth(0.85f)
            .padding(top = 4.dp)
    )
}

@Suppress("DEPRECATION")
fun cerrarSesion(context: Context, navController: NavController) {
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
fun ValidarContrasenaActual(
    navController: NavController,
    onValidacionExitosa: () -> Unit
) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser
    val correo = user?.email ?: ""

    var contrasenaActual by remember { mutableStateOf("") }
    var mensajeValidacion by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    var mostrarDialogoExito by remember{ mutableStateOf(false) }
    if (mostrarDialogoExito) {
        ExitoDialogoGuardadoAnimado(
            mensaje = "Revisa tu bandeja de entrada; te enviamos el enlace a tu correo ${correo}",
            onDismiss = {
                mostrarDialogoExito = false
                cerrarSesion(context, navController)
            }
        )
    }

        CampoValidarContrasena(
            value = contrasenaActual,
            onValueChange = { contrasenaActual = it }
        )
        MensajeExplicacionCambio()
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (correo.isBlank()) {
                    mensajeValidacion = "No se encontró correo del usuario actual"
                    return@Button
                }
                if (contrasenaActual.isBlank()) {
                    mensajeValidacion = "Ingresa tu contraseña actual"
                    return@Button
                }

                isLoading = true
                auth.signInWithEmailAndPassword(correo, contrasenaActual)
                    .addOnCompleteListener { task ->
                        isLoading = false
                        if (task.isSuccessful) {
                            onValidacionExitosa()
                            //Enviar correo de restablecimiento
                            val auth = FirebaseAuth.getInstance()
                            val actionCodeSettings = ActionCodeSettings.newBuilder()
                                .setUrl("https://koalm-94491.web.app")
                                .setHandleCodeInApp(false)
                                .setAndroidPackageName("com.example.koalm", true, "35")
                                .build()

                            auth.sendPasswordResetEmail(correo, actionCodeSettings)
                                .addOnSuccessListener {
                                    mostrarDialogoExito = true
                                }
                                .addOnFailureListener {
                                    Toast.makeText(context, it.localizedMessage, Toast.LENGTH_LONG).show()
                                }
                        } else {
                            mensajeValidacion = "La contraseña ingresada no es correcta"
                        }
                    }
            },
            colors = ButtonDefaults.buttonColors(containerColor = VerdePrincipal),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Blanco,
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text("Validar contraseña", color = Blanco)
            }
        }

        // Mostrar diálogo o mensaje de validación
        if (mensajeValidacion != null) {
            ValidacionesDialogoAnimado(
                mensaje = mensajeValidacion!!,
                onDismiss = { mensajeValidacion = null }
            )
        }

}
