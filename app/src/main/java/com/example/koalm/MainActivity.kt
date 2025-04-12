// Paquete base del proyecto
package com.example.koalm

// Importación de clases necesarias para Android y Jetpack Compose
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.* // Layouts como Column, Row, Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.* // Componentes de Material 3 (Botones, TextFields, etc.)
import androidx.compose.runtime.* // Funciones y estados de Compose
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext // Contexto para Toast, etc.
import androidx.compose.ui.res.painterResource // Carga de imágenes
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString // Texto con diferentes estilos
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation // Oculta contraseña
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.koalm.ui.theme.* // Acceso a colores y tema personalizado
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack // Ícono de flecha para regresar
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.graphics.Color

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KoalmTheme { // Usa el tema personalizado
                Surface(modifier = Modifier.fillMaxSize(), color = Blanco) { // Fondo de pantalla completo con color blanco
                    PantallaLogin() // Llama al Composable de login
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaLogin() {
    val context = LocalContext.current // Para mostrar Toast
    var email by remember { mutableStateOf("") } // Estado para el correo
    var password by remember { mutableStateOf("") } // Estado para la contraseña
    var passwordVisible by remember { mutableStateOf(false) } // Estado para ver/ocultar contraseña

    val buttonModifier = Modifier.width(200.dp) // Tamaño común de botones

    // Scaffold: estructura base con barra superior
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Iniciar sesión") }, // Título de la barra
                navigationIcon = { // Ícono de regresar
                    IconButton(onClick = {
                        Toast.makeText(context, "Regresar", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                }
            )
        }
    ) { innerPadding -> // innerPadding: espacio que deja la TopAppBar

        // Contenido principal centrado
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo de koala
            Image(
                painter = painterResource(id = R.drawable.koala),
                contentDescription = "Koala",
                modifier = Modifier.size(150.dp)
            )

            Spacer(modifier = Modifier.height(32.dp)) // Espacio

            // Campo de texto para el correo
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

            // Campo de texto para la contraseña
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .clip(RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(), // Ocultar o mostrar contraseña
                trailingIcon = {
                    val image = if (passwordVisible)
                        painterResource(id = R.drawable.ic_eye_closed) // Ícono cuando es visible
                    else
                        painterResource(id = R.drawable.ic_eye) // Ícono cuando está oculta

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

            // Botón principal de inicio de sesión
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

            // Botón de inicio con Google
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

            // Texto interactivo "¿Olvidaste tu contraseña?"
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

            // Texto interactivo "¿No tienes cuenta?"
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
