package com.example.koalm.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
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
    Image(
        painter = painterResource(id = R.drawable.koala_pregunta),
        contentDescription = "Koala pregunta",
        modifier = Modifier.size(150.dp)
    )
}


@Composable
fun CampoCorreoRecuperar(value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("Ingresa tu correo") },
        modifier = Modifier.fillMaxWidth(0.85f),
        shape = RoundedCornerShape(16.dp),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = VerdePrincipal,
            unfocusedBorderColor = GrisMedio
        )
    )
}




@Composable
fun MensajeExplicacion() {
    Text(
        text = "Enviaremos un link al correo asociado a tu cuenta para que restablezcas la contraseña.",
        fontSize = 12.sp,
        color = GrisMedio,
        modifier = Modifier.padding(top = 4.dp, start = 20.dp, end = 20.dp)
    )
}




@Composable
fun BotonEnviarCorreo(correo: String, navController: NavController, context: android.content.Context) {
    Button(
        onClick = {
            Toast.makeText(context, "Link enviado a $correo", Toast.LENGTH_SHORT).show()
            navController.navigate("restablecer")
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



