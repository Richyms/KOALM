package com.example.koalm

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
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
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.koalapregunta),
                contentDescription = "Koala pregunta",
                modifier = Modifier.size(150.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            /*Text("Ingresa tu correo", fontSize = 16.sp)*/

            OutlinedTextField(
                value = correo,
                onValueChange = { correo = it }, // actualiza el estado
                label = { Text("Ingresa tu correo") }, // etiqueta del campo
                modifier = Modifier.fillMaxWidth(0.85f),
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = VerdePrincipal,
                    unfocusedBorderColor = GrisMedio
                )
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Enviaremos un link al correo asociado a tu cuenta para que restablezcas la contraseña.",
                fontSize = 12.sp,
                color = GrisMedio,
                modifier = Modifier
                        .padding(top = 4.dp, start = 20.dp, end = 20.dp) // ligero margen
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    Toast.makeText(context, "Link enviado a $correo", Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = VerdePrincipal)
            ) {
                Text("Enviar", color = Blanco)
            }

            Spacer(modifier = Modifier.height(32.dp))

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
    }
}
