package com.example.koalm

import android.widget.Toast
import androidx.compose.foundation.Image

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import androidx.navigation.NavController
import com.example.koalm.ui.theme.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.Icons
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.foundation.clickable


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

    val buttonModifier = Modifier.width(200.dp)

    // Validación de email
    val isValidEmail = remember(email) {
        val validDomains = listOf(
            "gmail.com", "hotmail.com", "yahoo.com", "icloud.com", 
            "live.com", "outlook.com", "proton.me", "protonmail.com",
            "aol.com", "mail.com", "zoho.com", "yandex.com"
        )
        email.contains("@") && validDomains.any { email.endsWith("@$it") }
    }

    // Validación de contraseña
    val isValidPassword = remember(password) {
        password.length >= 8
    }

    // Validación de nombre de usuario
    val isValidUsername = remember(username) {
        username.isNotEmpty() && !username.contains(" ")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registrarse") },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.navigateUp()
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
                painter = painterResource(id = R.drawable.koalaregistrar),
                contentDescription = "Koalaregistrar",
                modifier = Modifier.size(200.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Correo") },
                modifier = Modifier.fillMaxWidth(0.85f),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = if (isValidEmail || email.isEmpty()) VerdePrincipal else Color.Red,
                    unfocusedBorderColor = if (isValidEmail || email.isEmpty()) GrisMedio else Color.Red
                ),
                supportingText = {
                    Text(
                        text = "Solo servicios de correo conocidos.",
                        color = GrisMedio,
                        fontSize = 12.sp
                    )
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Nombre de usuario") },
                modifier = Modifier.fillMaxWidth(0.85f),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = if (isValidUsername || username.isEmpty()) VerdePrincipal else Color.Red,
                    unfocusedBorderColor = if (isValidUsername || username.isEmpty()) GrisMedio else Color.Red
                ),
                supportingText = {
                    Text(
                        text = "No se permiten espacios en el nombre de usuario",
                        color = GrisMedio,
                        fontSize = 12.sp
                    )
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(0.85f),
                shape = RoundedCornerShape(16.dp),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val icon = if (passwordVisible)
                        painterResource(id = R.drawable.ic_eye_closed)
                    else
                        painterResource(id = R.drawable.ic_eye)
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(painter = icon, contentDescription = null)
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

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirmar contraseña") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(0.85f),
                shape = RoundedCornerShape(16.dp),
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val icon = if (confirmPasswordVisible)
                        painterResource(id = R.drawable.ic_eye_closed)
                    else
                        painterResource(id = R.drawable.ic_eye)
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(painter = icon, contentDescription = null)
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = VerdePrincipal,
                    unfocusedBorderColor = GrisMedio
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(0.85f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Checkbox(
                    checked = termsAccepted,
                    onCheckedChange = { termsAccepted = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = VerdePrincipal,
                        uncheckedColor = GrisMedio
                    )
                )
                Text(
                    text = buildAnnotatedString {
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

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    if (!isValidEmail) {
                        Toast.makeText(context, "Por favor ingresa un correo válido", Toast.LENGTH_SHORT).show()
                    } else if (!isValidUsername) {
                        Toast.makeText(context, "El nombre de usuario no debe contener espacios", Toast.LENGTH_SHORT).show()
                    } else if (!isValidPassword) {
                        Toast.makeText(context, "La contraseña debe tener al menos 8 caracteres", Toast.LENGTH_SHORT).show()
                    } else if (password != confirmPassword) {
                        Toast.makeText(context, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                    } else if (!termsAccepted) {
                        Toast.makeText(context, "Debes aceptar los términos y condiciones", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Registrado como $username", Toast.LENGTH_SHORT).show()
                        navController.navigate("personalizar")
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
                Text("Inciar con Google", color = MaterialTheme.colorScheme.onSurface)
            }

            Spacer(modifier = Modifier.height(16.dp))

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
                    navController.navigate("login")
                }
            )
        }
    }
}
