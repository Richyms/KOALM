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
                composable("menu") {
                    PantallaMenu(navController)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaLogin(navController: NavHostController) {
    val context = LocalContext.current // Para mostrar Toast
    var emailOrUsername by remember { mutableStateOf("") } // Estado para el correo o nombre de usuario
    var password by remember { mutableStateOf("") } // Estado para la contraseña
    var passwordVisible by remember { mutableStateOf(false) } // Estado para ver/ocultar contraseña

    val buttonModifier = Modifier.width(200.dp) // Tamaño común de botones

    // Validación de correo o nombre de usuario
    val isEmail = remember(emailOrUsername) {
        emailOrUsername.contains("@")
    }

    val isValidEmail = remember(emailOrUsername) {
        if (isEmail) {
            val validDomains = listOf(
                "gmail.com", "hotmail.com", "yahoo.com", "icloud.com", 
                "live.com", "outlook.com", "proton.me", "protonmail.com",
                "aol.com", "mail.com", "zoho.com", "yandex.com"
            )
            emailOrUsername.contains("@") && validDomains.any { emailOrUsername.endsWith("@$it") }
        } else {
            true // Si no es un correo, no se valida como correo
        }
    }

    val isValidUsername = remember(emailOrUsername) {
        if (!isEmail) {
            emailOrUsername.isNotBlank() && !emailOrUsername.contains(" ")
        } else {
            true // Si es un correo, no se valida como nombre de usuario
        }
    }

    val isValidInput = isValidEmail && isValidUsername

    // Validación de contraseña
    val isValidPassword = remember(password) {
        password.length >= 8
    }

    // Scaffold: estructura base con barra superior
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Iniciar sesión") }
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
            // Logo de koala
            Image(
                painter = painterResource(id = R.drawable.koala),
                contentDescription = "Koala",
                modifier = Modifier.size(200.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Campo de texto para el correo o nombre de usuario
            OutlinedTextField(
                value = emailOrUsername,
                onValueChange = { emailOrUsername = it },
                label = { Text("Correo o nombre de usuario") },
                modifier = Modifier.fillMaxWidth(0.85f),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = if (isValidInput || emailOrUsername.isEmpty()) VerdePrincipal else Color.Red,
                    unfocusedBorderColor = if (isValidInput || emailOrUsername.isEmpty()) GrisMedio else Color.Red
                ),
                supportingText = {
                    if (isEmail) {
                        Text(
                            text = "Solo servicios de correo conocidos.",
                            color = GrisMedio,
                            fontSize = 12.sp
                        )
                    } else {
                        Text(
                            text = "El nombre de usuario no debe contener espacios.",
                            color = GrisMedio,
                            fontSize = 12.sp
                        )
                    }
                }
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
                    focusedBorderColor = if (isValidPassword || password.isEmpty()) VerdePrincipal else Color.Red,
                    unfocusedBorderColor = if (isValidPassword || password.isEmpty()) GrisMedio else Color.Red
                ),
                supportingText = {
                    Text(
                        text = "La contraseña debe tener al menos 8 caracteres",
                        color = GrisMedio,
                        fontSize = 12.sp
                    )
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Botón principal de inicio de sesión
            Button(
                onClick = {
                    if (!isValidInput) {
                        val errorMessage = if (isEmail) {
                            "Por favor ingresa un correo válido"
                        } else {
                            "El nombre de usuario no debe contener espacios"
                        }
                        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                    } else if (!isValidPassword) {
                        Toast.makeText(context, "La contraseña debe tener al menos 8 caracteres", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Bienvenido $emailOrUsername", Toast.LENGTH_SHORT).show()
                        navController.navigate("menu")
                    }
                },
                modifier = buttonModifier,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Iniciar sesión", color = MaterialTheme.colorScheme.onPrimary)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Botón de inicio con Google
            OutlinedButton(
                onClick = {
                    Toast.makeText(context, "Google login", Toast.LENGTH_SHORT).show()
                },
                modifier = buttonModifier,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Text("Iniciar con Google", color = MaterialTheme.colorScheme.onSurface)
            }

            Spacer(modifier = Modifier.height(24.dp))

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
                    navController.navigate("recuperar")
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
