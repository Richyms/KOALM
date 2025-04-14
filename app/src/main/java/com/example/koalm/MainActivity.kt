// Paquete base del proyecto
package com.example.koalm

// Importación de clases necesarias para Android y Jetpack Compose
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
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
import androidx.compose.ui.platform.LocalView
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
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.navigation.NavHostController
import androidx.compose.foundation.clickable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.core.view.WindowCompat
import androidx.compose.foundation.isSystemInDarkTheme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainApp()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp() {
    KoalmTheme {
        val navController = rememberNavController()
        val view = LocalView.current
        val isDarkTheme = isSystemInDarkTheme()
        
        DisposableEffect(isDarkTheme) {
            val window = (view.context as ComponentActivity).window
            window.statusBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !isDarkTheme
                isAppearanceLightNavigationBars = !isDarkTheme
            }
            onDispose {}
        }

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            NavHost(
                navController = navController,
                startDestination = "login"
            ) {
                composable("login") {
                    PantallaLogin(navController)
                }
                composable("registro") {
                    PantallaRegistro(navController)
                }
                composable("recuperar") {
                    PantallaRecuperarContrasena(navController)
                }
                composable("restablecer") {
                    PantallaRestablecerContrasena(navController)
                }
                composable("personalizar") {
                    PantallaPersonalizar(navController)
                }
                composable("habitos") {
                    PantallaHabitos(navController)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaLogin(navController: NavHostController) {
    val context = LocalContext.current // Para mostrar Toast
    var email by remember { mutableStateOf("") } // Estado para el correo
    var password by remember { mutableStateOf("") } // Estado para la contraseña
    var passwordVisible by remember { mutableStateOf(false) } // Estado para ver/ocultar contraseña

    val buttonModifier = Modifier.width(200.dp) // Tamaño común de botones

    // Scaffold: estructura base con barra superior
    Scaffold(
        /*topBar = {
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
        }*/
        topBar = {
            TopAppBar(
                title = { Text("Iniciar sesión") }
            )
        }

    ) { innerPadding -> // innerPadding: espacio que deja la TopAppBar

        // Contenido principal centrado
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize() // ocupatodo el espacio disponible
                .padding(horizontal = 24.dp), // margen lateral
            horizontalAlignment = Alignment.CenterHorizontally, // centra elementos horizontalmente
            verticalArrangement = Arrangement.Center // centra elementos verticalmente
        ) {
            // Logo de koala
            Image(
                painter = painterResource(id = R.drawable.koala),
                contentDescription = "Koala", // descripción para accesibilidad
                modifier = Modifier.size(200.dp) // tamaño de la imagen
            )

            Spacer(modifier = Modifier.height(32.dp)) // espacio vertical

            // Campo de texto para el correo
            OutlinedTextField(
                value = email,
                onValueChange = { email = it }, // actualiza el estado
                label = { Text("Correo o nombre de usuario") }, // etiqueta del campo
                modifier = Modifier.fillMaxWidth(0.85f), // ancho de 85%
                singleLine = true, // una sola línea
                shape = RoundedCornerShape(16.dp), // bordes redondeados
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), // teclado tipo email
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = VerdePrincipal, // borde activo verde
                    unfocusedBorderColor = GrisMedio // borde inactivo gris
                )
            )

            Spacer(modifier = Modifier.height(16.dp)) // espacio vertical

            // Campo de texto para la contraseña
            OutlinedTextField(
                value = password,
                onValueChange = { password = it }, // actualiza el estado
                label = { Text("Contraseña") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth(0.85f) // ancho 85%
                    .clip(RoundedCornerShape(16.dp)), // bordes redondeados
                shape = RoundedCornerShape(16.dp),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(), // muestra u oculta
                trailingIcon = {
                    val image = if (passwordVisible)
                        painterResource(id = R.drawable.ic_eye_closed) // ojo cerrado
                    else
                        painterResource(id = R.drawable.ic_eye) // ojo abierto

                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(painter = image, contentDescription = null) // alterna icono
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = VerdePrincipal,
                    unfocusedBorderColor = GrisMedio
                )
            )

            Spacer(modifier = Modifier.height(24.dp)) // espacio vertical

            // Botón principal de inicio de sesión
            Button(
                onClick = {
                    Toast.makeText(context, "Bienvenido $email", Toast.LENGTH_SHORT)
                        .show() // muestra Toast
                },
                modifier = buttonModifier, // ancho común
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary) // fondo verde
            ) {
                Text("Iniciar sesión", color = MaterialTheme.colorScheme.onPrimary) // texto blanco
            }

            Spacer(modifier = Modifier.height(12.dp)) // espacio

            // Botón de inicio con Google
            OutlinedButton(
                onClick = {
                    Toast.makeText(context, "Google login", Toast.LENGTH_SHORT).show()
                },
                modifier = buttonModifier,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline) // borde gris
            ) {
                Text("Iniciar con Google", color = MaterialTheme.colorScheme.onSurface) // texto negro
            }

            Spacer(modifier = Modifier.height(24.dp)) // espacio

            // Texto interactivo "¿Olvidaste tu contraseña?"
            Text(
                buildAnnotatedString {
                    withStyle(SpanStyle(color = MaterialTheme.colorScheme.onSurface)) {
                        append("¿Olvidaste tu contraseña? ")
                    }
                    withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                        append("Aquí")
                    }
                },
                fontSize = 14.sp,
                modifier = Modifier.clickable {
                    navController.navigate("recuperar") // <-- Esta es la conexión
                }
            )

            // Texto interactivo "¿No tienes cuenta?"
            Text(
                buildAnnotatedString {
                    withStyle(SpanStyle(color = MaterialTheme.colorScheme.onSurface)) {
                        append("¿No tienes una cuenta? ")
                    }
                    withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary)) {
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
