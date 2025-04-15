package com.example.koalmV1.ui.screens


import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.* // Incluye: fillMaxSize, fillMaxWidth, padding, etc.
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController


import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.graphics.ColorFilter


import com.example.koalmV1.R
import com.example.koalmV1.ui.theme.* // Incluye: VerdePrincipal, GrisMedio, Blanco, etc.


enum class ProviderType{
    GOOGLE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaIniciarSesion(
    navController: NavHostController,
    onGoogleSignInClick: () -> Unit
) {
    val context = LocalContext.current
    var emailOrUsername by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val isEmail = emailOrUsername.contains("@")
    val isValidEmail = isEmail && listOf(
        "gmail.com", "hotmail.com", "yahoo.com", "icloud.com",
        "live.com", "outlook.com", "proton.me", "protonmail.com",
        "aol.com", "mail.com", "zoho.com", "yandex.com"
    ).any { emailOrUsername.endsWith("@$it") }

    val isValidUsername = !isEmail && emailOrUsername.isNotBlank() && !emailOrUsername.contains(" ")
    val isValidInput = if (isEmail) isValidEmail else isValidUsername
    val isValidPassword = password.length >= 8

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Iniciar sesión") })
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
            LoginLogo()
            Spacer(modifier = Modifier.height(32.dp))
            EmailOrUsernameField(emailOrUsername, isValidInput, isEmail) {
                emailOrUsername = it
            }
            Spacer(modifier = Modifier.height(16.dp))
            PasswordField(password, passwordVisible, isValidPassword, onPasswordChange = {
                password = it
            }, onVisibilityToggle = {
                passwordVisible = !passwordVisible
            })
            Spacer(modifier = Modifier.height(24.dp))
            LoginButtons(
                isValidInput = isValidInput,
                isValidPassword = isValidPassword,
                emailOrUsername = emailOrUsername,
                context = context,
                navController = navController,
                onGoogleSignInClick = onGoogleSignInClick
            )
            Spacer(modifier = Modifier.height(24.dp))
            LoginFooterText(navController)
        }
    }
}


@Composable
fun LoginLogo() {
    val isDark = isSystemInDarkTheme()
    val tintColor = if (isDark) Color.White else Color.Black

    Image(
        painter = painterResource(id = R.drawable.login),
        contentDescription = "Koala",
        modifier = Modifier.size(300.dp),
        colorFilter = ColorFilter.tint(tintColor)
    )
}



@Composable
fun EmailOrUsernameField(
    value: String,
    isValid: Boolean,
    isEmail: Boolean,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("Correo o nombre de usuario") },
        modifier = Modifier.fillMaxWidth(0.97f),
        singleLine = true,
        shape = RoundedCornerShape(6.dp),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = if (isValid || value.isEmpty()) VerdePrincipal else Color.Red,
            unfocusedBorderColor = if (isValid || value.isEmpty()) GrisMedio else Color.Red,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        ),
        supportingText = {
            Text(
                text = if (isEmail)
                    "Solo servicios de correo conocidos."
                else
                    "El nombre de usuario no debe contener espacios.",
                color = GrisMedio,
                fontSize = 12.sp
            )
        }
    )
}



@Composable
fun PasswordField(
    value: String,
    passwordVisible: Boolean,
    isValidPassword: Boolean,
    onPasswordChange: (String) -> Unit,
    onVisibilityToggle: () -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onPasswordChange,
        label = { Text("Contraseña") },
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth(0.97f)
            .clip(RoundedCornerShape(6.dp)),
        shape = RoundedCornerShape(6.dp),
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            val icon = if (passwordVisible)
                painterResource(id = R.drawable.ic_eye_closed)
            else
                painterResource(id = R.drawable.ic_eye)
            IconButton(onClick = onVisibilityToggle) {
                Icon(painter = icon, contentDescription = null)
            }
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = if (isValidPassword || value.isEmpty()) VerdePrincipal else Color.Red,
            unfocusedBorderColor = if (isValidPassword || value.isEmpty()) GrisMedio else Color.Red,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        ),
        supportingText = {
            Text(
                text = "La contraseña debe tener al menos 8 caracteres",
                color = GrisMedio,
                fontSize = 12.sp
            )
        }
    )
}





@Composable
fun LoginButtons(
    isValidInput: Boolean,
    isValidPassword: Boolean,
    emailOrUsername: String,
    context: Context,
    navController: NavHostController,
    onGoogleSignInClick: () -> Unit
) {
    val buttonModifier = Modifier.width(200.dp)

    Button(
        onClick = {
            when {
                !isValidInput -> {
                    val msg = if (emailOrUsername.contains("@"))
                        "Por favor ingresa un correo válido"
                    else
                        "El nombre de usuario no debe contener espacios"
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
                !isValidPassword -> {
                    Toast.makeText(context, "La contraseña debe tener al menos 8 caracteres", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    val prefs = context.getSharedPreferences(
                        context.getString(R.string.prefs_file),
                        Context.MODE_PRIVATE
                    )
                    with(prefs.edit()) {
                        putString("emailOrUsername", emailOrUsername)
                        apply()
                    }
                    Toast.makeText(context, "Bienvenido $emailOrUsername", Toast.LENGTH_SHORT).show()
                    navController.navigate("menu")
                }
            }
        },
        modifier = buttonModifier,
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Text("Iniciar sesión", color = MaterialTheme.colorScheme.onPrimary)
    }

    Spacer(modifier = Modifier.height(12.dp))

    OutlinedButton(
        onClick = { onGoogleSignInClick() },
        modifier = buttonModifier,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Text("Iniciar con Google", color = MaterialTheme.colorScheme.onSurface)
    }
}





@Composable
fun LoginFooterText(navController: NavHostController) {
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
