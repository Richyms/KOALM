package com.example.koalmV1.ui.screens

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.koalmV1.R
import com.example.koalmV1.ui.theme.*

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
        modifier = Modifier.size(300.dp),
        colorFilter = ColorFilter.tint(tintColor)
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
            unfocusedBorderColor = GrisMedio,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    )
}


@Composable
fun MensajeExplicacion() {
    Text(
        text = "Enviaremos un código de 4 dígitos al correo asociado a tu cuenta para que restablezcas la contraseña.",
        fontSize = 12.sp,
        color = GrisMedio,
        modifier = Modifier
            .fillMaxWidth(0.85f)
            .padding(top = 4.dp)
    )
}


@Composable
fun BotonEnviarCorreo(correo: String, navController: NavController, context: android.content.Context) {
    val EmailConf = correo.contains("@")
    val isValidEmail = EmailConf && listOf(
        "gmail.com", "hotmail.com", "yahoo.com", "icloud.com",
        "live.com", "outlook.com", "proton.me", "protonmail.com",
        "aol.com", "mail.com", "zoho.com", "yandex.com"
    ).any { correo.endsWith("@$it") }

    val isValidConf = EmailConf && correo.isNotBlank() && !correo.contains(" ")
    val isValidInput = if (EmailConf) isValidEmail else isValidConf
    Button(
        onClick = {
            if (isValidInput) {
                Toast.makeText(context, "Código enviado a $correo", Toast.LENGTH_SHORT).show()
                navController.navigate("recuperarCodigo")
            } else {
                Toast.makeText(context, "Por favor, ingresa un correo válido.", Toast.LENGTH_SHORT).show()
            }
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
