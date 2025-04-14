package com.example.koalm.ui.screens

import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.koalm.R
import com.example.koalm.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaRegistro(navController: NavController) {
    val context = LocalContext.current

    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var termsAccepted by remember { mutableStateOf(false) }

    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    val isValidEmail = email.contains("@") && listOf(
        "gmail.com", "hotmail.com", "yahoo.com", "icloud.com", 
        "live.com", "outlook.com", "proton.me", "protonmail.com",
        "aol.com", "mail.com", "zoho.com", "yandex.com"
    ).any { email.endsWith("@$it") }

    val isValidPassword = password.length >= 8
    val isValidUsername = username.isNotEmpty() && !username.contains(" ")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registrarse") },
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
            LogoRegistro()
            Spacer(modifier = Modifier.height(16.dp))
            CampoCorreo(email, isValidEmail) { email = it }
            Spacer(modifier = Modifier.height(8.dp))
            CampoNombreUsuario(username, isValidUsername) { username = it }
            Spacer(modifier = Modifier.height(8.dp))
            CampoContraseña(password, passwordVisible, isValidPassword, onValueChange = { password = it }) {
                passwordVisible = !passwordVisible
            }
            Spacer(modifier = Modifier.height(8.dp))
            CampoConfirmarContraseña(confirmPassword, confirmPasswordVisible, onValueChange = { confirmPassword = it }) {
                confirmPasswordVisible = !confirmPasswordVisible
            }
            Spacer(modifier = Modifier.height(8.dp))
            CheckboxTerminos(termsAccepted) { termsAccepted = it }
            Spacer(modifier = Modifier.height(12.dp))
            BotonesRegistro(
                email, username, password, confirmPassword,
                isValidEmail, isValidUsername, isValidPassword, termsAccepted,
                navController, context
            )
            Spacer(modifier = Modifier.height(16.dp))
            TextoIrIniciarSesion(navController)
        }
    }
}


@Composable
fun LogoRegistro() {
    Image(
        painter = painterResource(id = R.drawable.koala_registrar),
        contentDescription = "Koala registrarse",
        modifier = Modifier.size(200.dp)
    )
}



@Composable
fun CampoCorreo(value: String, valido: Boolean, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("Correo") },
        modifier = Modifier.fillMaxWidth(0.85f),
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = if (valido || value.isEmpty()) VerdePrincipal else Color.Red,
            unfocusedBorderColor = if (valido || value.isEmpty()) GrisMedio else Color.Red
        ),
        supportingText = {
            Text("Solo servicios de correo conocidos.", color = GrisMedio, fontSize = 12.sp)
        }
    )
}



@Composable
fun CampoNombreUsuario(value: String, valido: Boolean, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("Nombre de usuario") },
        modifier = Modifier.fillMaxWidth(0.85f),
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = if (valido || value.isEmpty()) VerdePrincipal else Color.Red,
            unfocusedBorderColor = if (valido || value.isEmpty()) GrisMedio else Color.Red
        ),
        supportingText = {
            Text("No se permiten espacios en el nombre de usuario", color = GrisMedio, fontSize = 12.sp)
        }
    )
}


@Composable
fun CampoContraseña(
    value: String,
    visible: Boolean,
    valido: Boolean,
    onValueChange: (String) -> Unit,
    onToggle: () -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("Contraseña") },
        modifier = Modifier.fillMaxWidth(0.85f),
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            val icon = if (visible) R.drawable.ic_eye_closed else R.drawable.ic_eye
            IconButton(onClick = onToggle) {
                Icon(painter = painterResource(id = icon), contentDescription = null)
            }
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = if (valido || value.isEmpty()) VerdePrincipal else Color.Red,
            unfocusedBorderColor = if (valido || value.isEmpty()) GrisMedio else Color.Red
        ),
        supportingText = {
            Text("La contraseña debe tener al menos 8 caracteres", color = GrisMedio, fontSize = 12.sp)
        }
    )
}



@Composable
fun CampoConfirmarContraseña(
    value: String,
    visible: Boolean,
    onValueChange: (String) -> Unit,
    onToggle: () -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("Confirmar contraseña") },
        modifier = Modifier.fillMaxWidth(0.85f),
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            val icon = if (visible) R.drawable.ic_eye_closed else R.drawable.ic_eye
            IconButton(onClick = onToggle) {
                Icon(painter = painterResource(id = icon), contentDescription = null)
            }
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = VerdePrincipal,
            unfocusedBorderColor = GrisMedio
        )
    )
}



@Composable
fun CheckboxTerminos(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    val context = LocalContext.current
    Row(
        modifier = Modifier.fillMaxWidth(0.85f),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = VerdePrincipal,
                uncheckedColor = GrisMedio
            )
        )
        Text(
            buildAnnotatedString {
                withStyle(
                    SpanStyle(
                        color = VerdeSecundario,
                        textDecoration = TextDecoration.Underline
                    )
                ) {
                    append("Acepto los términos y condiciones")
                }
            },
            fontSize = 14.sp,
            modifier = Modifier.clickable {
                Toast.makeText(context, "Mostrando términos y condiciones", Toast.LENGTH_SHORT).show()
            }
        )
    }
}




@Composable
fun BotonesRegistro(
    email: String, username: String, password: String, confirmPassword: String,
    isValidEmail: Boolean, isValidUsername: Boolean, isValidPassword: Boolean,
    termsAccepted: Boolean,
    navController: NavController, context: android.content.Context
) {
    val buttonModifier = Modifier.width(200.dp)

    Button(
        onClick = {
            when {
                !isValidEmail -> Toast.makeText(context, "Por favor ingresa un correo válido", Toast.LENGTH_SHORT).show()
                !isValidUsername -> Toast.makeText(context, "El nombre de usuario no debe contener espacios", Toast.LENGTH_SHORT).show()
                !isValidPassword -> Toast.makeText(context, "La contraseña debe tener al menos 8 caracteres", Toast.LENGTH_SHORT).show()
                password != confirmPassword -> Toast.makeText(context, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                !termsAccepted -> Toast.makeText(context, "Debes aceptar los términos y condiciones", Toast.LENGTH_SHORT).show()
                else -> {
                    Toast.makeText(context, "Registrado como $username", Toast.LENGTH_SHORT).show()
                    navController.navigate("personalizar")
                }
            }
        },
        modifier = buttonModifier,
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Text("Registrar", color = MaterialTheme.colorScheme.onPrimary)
    }

    Spacer(modifier = Modifier.height(12.dp))

    OutlinedButton(
        onClick = {
            Toast.makeText(context, "Google login", Toast.LENGTH_SHORT).show()
        },
        modifier = buttonModifier,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Text("Iniciar con Google", color = MaterialTheme.colorScheme.onSurface)
    }
}



@Composable
fun TextoIrIniciarSesion(navController: NavController) {
    Text(
        buildAnnotatedString {
            withStyle(SpanStyle(color = MaterialTheme.colorScheme.onSurface)) {
                append("¿Ya tienes una cuenta? ")
            }
            withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                append("Iniciar sesión")
            }
        },
        fontSize = 14.sp,
        modifier = Modifier.clickable {
            navController.navigate("iniciar")
        }
    )
}
