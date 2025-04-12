package com.example.koalm

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.koalm.ui.theme.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.graphics.Color

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KoalmTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = Blanco) {
                    PantallaLogin()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaLogin() {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val buttonModifier = Modifier.width(200.dp)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Iniciar sesión") },
                navigationIcon = {
                    IconButton(onClick = {
                        Toast.makeText(context, "Regresar", Toast.LENGTH_SHORT).show()
                    }) {
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
                painter = painterResource(id = R.drawable.koala),
                contentDescription = "Koala",
                modifier = Modifier.size(150.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Correo o nombre de usuario") },
                modifier = Modifier.fillMaxWidth(0.85f),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = VerdePrincipal,
                    unfocusedBorderColor = GrisMedio
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .clip(RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val image = if (passwordVisible)
                        painterResource(id = R.drawable.ic_eye_closed)
                    else
                        painterResource(id = R.drawable.ic_eye)

                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(painter = image, contentDescription = null)
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = VerdePrincipal,
                    unfocusedBorderColor = GrisMedio
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    Toast.makeText(context, "Bienvenido $email", Toast.LENGTH_SHORT).show()
                },
                modifier = buttonModifier,
                colors = ButtonDefaults.buttonColors(containerColor = VerdePrincipal)
            ) {
                Text("Iniciar sesión", color = Blanco)
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = {
                    Toast.makeText(context, "Google login", Toast.LENGTH_SHORT).show()
                },
                modifier = buttonModifier,
                border = BorderStroke(1.dp, Color.Gray)
            ) {
                Text("Sign in with Google", color = Negro)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                buildAnnotatedString {
                    withStyle(SpanStyle(color = Negro)) {
                        append("¿Olvidaste tu contraseña? ")
                    }
                    withStyle(SpanStyle(color = VerdeSecundario)) {
                        append("Aquí")
                    }
                },
                fontSize = 14.sp
            )

            Text(
                buildAnnotatedString {
                    withStyle(SpanStyle(color = Negro)) {
                        append("¿No tienes una cuenta? ")
                    }
                    withStyle(SpanStyle(color = VerdeSecundario)) {
                        append("Regístrate")
                    }
                },
                fontSize = 14.sp
            )
        }
    }
}


