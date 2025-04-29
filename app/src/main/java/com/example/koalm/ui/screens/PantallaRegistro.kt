package com.example.koalm.ui.screens

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import android.content.Context

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.filled.Info
import androidx.compose.ui.graphics.ColorFilter

import com.example.koalm.R
import com.example.koalm.ui.theme.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

import com.example.koalm.model.Usuario
import com.google.firebase.firestore.SetOptions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaRegistro(
    navController: NavController,
    onGoogleSignInClick: () -> Unit
) {
    val context = LocalContext.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var termsAccepted by remember { mutableStateOf(false) }

    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    //Verificar si el correo es válido
    var isValidEmail by remember { mutableStateOf(true) }
    var yaExisteCorreo by remember { mutableStateOf(false) }

    val dominiosPermitidos = listOf(
        "gmail.com", "hotmail.com", "yahoo.com", "icloud.com",
        "live.com", "outlook.com", "proton.me", "protonmail.com",
        "aol.com", "mail.com", "zoho.com", "yandex.com"
    )

    //Verificar si el correo ya existe o no
    fun validarCorreoExistente(correo: String) {
        FirebaseFirestore.getInstance()
            .collection("usuarios")
            .whereEqualTo("email", correo)
            .get()
            .addOnSuccessListener { docs ->
                yaExisteCorreo = !docs.isEmpty
            }
            .addOnFailureListener {
                yaExisteCorreo = false
            }
    }

    // Para saber si el username está disponible o nadota
    val db = FirebaseFirestore.getInstance()
    var username by remember { mutableStateOf("") }
    var usuarioValido by remember { mutableStateOf(true) }
    var yaExisteUsuario by remember { mutableStateOf(false) }

    fun validarNombreUsuario(nombre: String) {
        if (nombre.isNotBlank()) {
            db.collection("usuarios")
                .whereEqualTo("username", nombre)
                .get()
                .addOnSuccessListener { documents ->
                    yaExisteUsuario = !documents.isEmpty
                }
                .addOnFailureListener {
                    yaExisteUsuario = false // En caso de error, entonceees asumimos que no existe
                }
        }
    }


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
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            LogoRegistro()
            Spacer(modifier = Modifier.height(16.dp))
            CampoCorreo(
                value = email,
                esValido = isValidEmail,
                yaExiste = yaExisteCorreo,
                onValueChange = {
                    email = it
                    isValidEmail = it.contains("@") &&
                            !it.contains(" ") &&
                            dominiosPermitidos.any { domain -> it.endsWith("@$domain") }

                    if (isValidEmail) {
                        validarCorreoExistente(it)
                    } else {
                        yaExisteCorreo = false
                    }
                }
            )

            Spacer(modifier = Modifier.height(8.dp))
            CampoNombreUsuario(
                value = username,
                valido = usuarioValido,
                yaExiste = yaExisteUsuario,
                onValueChange = {
                    username = it //actualizar nombre de usuario
                    usuarioValido = !it.contains(" ") //validar que no contenga espacios
                    validarNombreUsuario(it) //verificar si existe o no el nombre en la db
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
            CampoContrasena(password, passwordVisible, onValueChange = { password = it }) {
                passwordVisible = !passwordVisible
            }
            Spacer(modifier = Modifier.height(8.dp))
            CampoConfirmarContrasena(
                value = confirmPassword,
                visible = confirmPasswordVisible,
                coincideCon = confirmPassword == password,
                onValueChange = { confirmPassword = it },
                onToggle = { confirmPasswordVisible = !confirmPasswordVisible }
            )

            Spacer(modifier = Modifier.height(8.dp))
            CheckboxTerminos(termsAccepted) { termsAccepted = it }
            Spacer(modifier = Modifier.height(12.dp))
            BotonesRegistro(
                email, username, password, confirmPassword,
                isValidEmail, yaExisteCorreo, usuarioValido, yaExisteUsuario,
                termsAccepted, navController, context,
                onGoogleSignInClick = onGoogleSignInClick
            )
            Spacer(modifier = Modifier.height(16.dp))
            TextoIrIniciarSesion(navController)
        }
    }
}

@Composable
fun LogoRegistro() {
    val isDark = isSystemInDarkTheme()
    val tintColor = if (isDark) Color.White else Color.Black

    Image(
        painter = painterResource(id = R.drawable.greeting),
        contentDescription = "Koala registrarse",
        modifier = Modifier.size(270.dp),
        colorFilter = ColorFilter.tint(tintColor)
    )
}

@Composable
fun CampoCorreo(
    value: String,
    esValido: Boolean,
    yaExiste: Boolean,
    onValueChange: (String) -> Unit
) {
    val mostrarError = (!esValido || yaExiste) && value.isNotEmpty()

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("Correo electrónico *") },
        modifier = Modifier.fillMaxWidth(0.97f),
        singleLine = true,
        shape = RoundedCornerShape(6.dp),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = if (!mostrarError) VerdePrincipal else Color.Red,
            unfocusedBorderColor = if (!mostrarError) GrisMedio else Color.Red,
            focusedLabelColor = if (!mostrarError) VerdePrincipal else Color.Red,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            errorLabelColor = Color.Red
        ),
        supportingText = {
            when {
                yaExiste && value.isNotEmpty() -> {
                    Text("Este correo ya está en uso.", color = Color.Red, fontSize = 12.sp)
                }
                !esValido && value.isNotEmpty() -> {
                    Text("El formato del correo no es válido.", color = Color.Red, fontSize = 12.sp)
                }
                else -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Text(
                            "Solo servicios de correo electrónico permitidos.",
                            color = GrisMedio,
                            fontSize = 12.sp
                        )
                        AyudaDominios()
                    }
                }
            }
        }
    )
}




@Composable
fun AyudaDominios() {
    var mostrarDialogo by remember { mutableStateOf(false) }

    // Ícono de ayuda
    IconButton(onClick = { mostrarDialogo = true }) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = "Ayuda",
            tint = MaterialTheme.colorScheme.primary
        )
    }

    // Diálogo informativo
    if (mostrarDialogo) {
        AlertDialog(
            onDismissRequest = { mostrarDialogo = false },
            title = { Text("Dominios permitidos") },
            text = {
                Text("Puedes usar correos de los siguientes dominios:\n\n" +
                        "• gmail.com\n" +
                        "• hotmail.com\n" +
                        "• outlook.com\n" +
                        "• icloud.com\n" +
                        "• proton.me\n" +
                        "• yahoo.com\n" +
                        "• live.com\n" +
                        "• protonmail.com\n" +
                        "• aol.com\n" +
                        "• mail.com\n" +
                        "• zoho.com\n" +
                        "• yandex.com\n")
            },
            confirmButton = {
                TextButton(onClick = { mostrarDialogo = false }) {
                    Text("Entendido")
                }
            }
        )
    }
}


@Composable
fun CampoNombreUsuario(
    value: String,
    valido: Boolean,
    yaExiste: Boolean,
    onValueChange: (String) -> Unit
) {
    val minimoCaracteres = 3 // Número mínimo de caracteres

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("Nombre de usuario *") },
        modifier = Modifier.fillMaxWidth(0.97f),
        singleLine = true,
        shape = RoundedCornerShape(6.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = if ((valido && !yaExiste && value.length >= minimoCaracteres) || value.isEmpty()) VerdePrincipal else Color.Red,
            unfocusedBorderColor = if ((valido && !yaExiste && value.length >= minimoCaracteres) || value.isEmpty()) GrisMedio else Color.Red,
            focusedLabelColor = if ((valido && !yaExiste && value.length >= minimoCaracteres) || value.isEmpty()) VerdePrincipal else Color.Red,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            errorLabelColor = Color.Red
        ),
        supportingText = {
            if (yaExiste && value.isNotEmpty()) {
                Text(
                    text = "Este nombre de usuario ya está en uso.",
                    color = Color.Red,
                    fontSize = 12.sp
                )
            } else if (value.length < minimoCaracteres && value.isNotEmpty()) {
                Text(
                    text = "El nombre de usuario debe tener al menos $minimoCaracteres caracteres.",
                    color = Color.Red,
                    fontSize = 12.sp
                )
            } else if (!yaExiste && !valido && value.isNotEmpty()) {
                Text(
                    text = "No se permiten espacios en el nombre de usuario.",
                    color = Color.Red,
                    fontSize = 12.sp
                )
            } else if (!yaExiste && value.isNotEmpty()) {
                Text(
                    text = "Nombre de usuario válido.",
                    color = GrisMedio,
                    fontSize = 12.sp
                )
            } else {
                Text(
                    text = "No se permiten espacios en el nombre de usuario.",
                    color = GrisMedio,
                    fontSize = 12.sp
                )
            }
        }
    )
}



@Composable
fun CampoContrasena(
    value: String,
    visible: Boolean,
    onValueChange: (String) -> Unit,
    onToggle: () -> Unit
) {
    val passwordValidationMessage = remember(value) {
        when {
            value.isEmpty() -> "La contraseña debe tener al menos 8 caracteres, una letra minúscula, una mayúscula, un número y un carácter especial."
            value.length < 8 -> "La contraseña debe tener al menos 8 caracteres."
            !value.any { it.isLowerCase() } -> "Debe contener al menos una letra minúscula."
            !value.any { it.isUpperCase() } -> "Debe contener al menos una letra mayúscula."
            !value.any { it.isDigit() } -> "Debe contener al menos un número."
            !value.any { it in "!@#$%^&*()-_=+[{]}|;:,.<>?/`~" } -> "Debe contener al menos un carácter especial."
            else -> "Contraseña válida."
        }
    }

    val isValidPassword = passwordValidationMessage == "Contraseña válida."

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("Contraseña *") },
        modifier = Modifier.fillMaxWidth(0.97f),
        singleLine = true,
        shape = RoundedCornerShape(6.dp),
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            val icon = if (visible) R.drawable.ic_eye else R.drawable.ic_eye_closed
            IconButton(onClick = onToggle) {
                Icon(painter = painterResource(id = icon), contentDescription = null)
            }
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = if (isValidPassword || value.isEmpty()) VerdePrincipal else Color.Red,
            unfocusedBorderColor = if (isValidPassword || value.isEmpty()) GrisMedio else Color.Red,
            focusedLabelColor = if (isValidPassword || value.isEmpty()) VerdePrincipal else Color.Red,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            errorLabelColor = Color.Red
        ),
        supportingText = {
            val color = if (!isValidPassword && value.isNotEmpty()) Color.Red else GrisMedio

            Text(
                text = passwordValidationMessage,
                color = color,
                fontSize = 12.sp
            )
        }
    )
}


@Composable
fun CampoConfirmarContrasena(
    value: String,
    visible: Boolean,
    coincideCon: Boolean,
    onValueChange: (String) -> Unit,
    onToggle: () -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("Confirmar contraseña *") },
        modifier = Modifier.fillMaxWidth(0.97f),
        singleLine = true,
        shape = RoundedCornerShape(6.dp),
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            val icon = if (visible) R.drawable.ic_eye else R.drawable.ic_eye_closed
            IconButton(onClick = onToggle) {
                Icon(painter = painterResource(id = icon), contentDescription = null)
            }
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = if (coincideCon || value.isEmpty()) VerdePrincipal else Color.Red,
            unfocusedBorderColor = if (coincideCon || value.isEmpty()) GrisMedio else Color.Red,
            focusedLabelColor = if (coincideCon || value.isEmpty()) VerdePrincipal else Color.Red,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            errorLabelColor = Color.Red
        ),
        supportingText = {
            // Mensaje de validación inicial (cuando el campo está vacío)
            if (value.isEmpty()) {
                Text("Las contraseñas deben coincidir.", color = GrisMedio, fontSize = 12.sp)
            }

            // Mensaje de error si las contraseñas no coinciden
            if (!coincideCon && value.isNotEmpty()) {
                Text(
                    text = "Las contraseñas introducidas no coinciden.",
                    color = Color.Red,
                    fontSize = 12.sp
                )
            }

            // Mensaje que indica que las contraseñas coinciden
            if (coincideCon && value.isNotEmpty()) {
                Text(
                    text = "Las contraseñas coinciden.",
                    color = GrisMedio,
                    fontSize = 12.sp
                )
            }
        }
    )
}


@Composable
fun CheckboxTerminos(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    val context = LocalContext.current
    Row(
        modifier = Modifier.fillMaxWidth(0.97f),
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


@SuppressLint("SuspiciousIndentation")
@Composable
fun BotonesRegistro(
    email: String, username: String, password: String, confirmPassword: String,
    isValidEmail: Boolean,
    yaExisteCorreo: Boolean,
    usuarioValido: Boolean,
    yaExisteUsuario: Boolean,
    termsAccepted: Boolean,
    navController: NavController,
    context: Context,
    onGoogleSignInClick: () -> Unit
) {
    val buttonModifier = Modifier.width(200.dp)

    Button(
        onClick = {
            //Expresión regular: La regla de negocio que se definió para la contraseña.
            val passwordRegex = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#\$%^&*()\\-_=+\\[{\\]}|;:,.<>?/`~]).{8,}$")
            //Todas las validaciones generales
            when {
                email.isBlank() || username.isBlank() || password.isBlank() || confirmPassword.isBlank() -> {
                    Toast.makeText(context, "Por favor completa todos los campos.", Toast.LENGTH_SHORT).show()
                }
                !isValidEmail -> {
                    Toast.makeText(context, "El formato del correo no es válido.", Toast.LENGTH_SHORT).show()
                }
                yaExisteCorreo-> {
                    Toast.makeText(context, "El correo electrónico ya está en uso.", Toast.LENGTH_SHORT).show()
                }
                !usuarioValido -> {
                    Toast.makeText(context, "El nombre de usuario no puede contener espacios.", Toast.LENGTH_SHORT).show()
                }
                username.length < 3 -> {
                    Toast.makeText(context, "El nombre de usuario debe tener al menos 3 caracteres.", Toast.LENGTH_SHORT).show()
                }
                yaExisteUsuario -> {
                    Toast.makeText(context, "El nombre de usuario ya está en uso.", Toast.LENGTH_SHORT).show()
                }
                !passwordRegex.matches(password) -> {
                    Toast.makeText(context, "La contraseña no cumple con los requisitos.", Toast.LENGTH_SHORT).show()
                }
                password != confirmPassword -> {
                    Toast.makeText(context, "Las contraseñas no coinciden.", Toast.LENGTH_SHORT).show()
                }
                !termsAccepted -> {
                    Toast.makeText(context, "Debes aceptar los términos y condiciones.", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    // Usando el servicio de Firebase
                    // 1) Crear usuario en Firebase Auth
                    FirebaseAuth.getInstance()
                        .createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                // 1.1) Obtener UserID
                                val auth = FirebaseAuth.getInstance()
                                val userId = auth.currentUser!!.uid

                                // 2) Construir un objeto Usuario mínimo (los primeros 3 campos del registro)
                                val uLogin = Usuario(
                                    userId   = userId,
                                    email    = email,
                                    username = username
                                )

                                // 3) Guardar en Firestore con merge
                                val db = FirebaseFirestore.getInstance()
                                db.collection("usuarios")
                                    .document(email)
                                    .set(uLogin.toMap(), SetOptions.merge())
                                    .addOnSuccessListener {
                                        // 4) Enviar verificación de correo
                                        auth.currentUser
                                            ?.sendEmailVerification()
                                            ?.addOnCompleteListener { verifyTask ->
                                                if (verifyTask.isSuccessful) {
                                                    Toast.makeText(
                                                        context,
                                                        "Se envió un correo de verificación a $email",
                                                        Toast.LENGTH_LONG
                                                    ).show()
                                                } else {
                                                    Toast.makeText(
                                                        context,
                                                        "Error al enviar verificación: ${verifyTask.exception?.localizedMessage}",
                                                        Toast.LENGTH_LONG
                                                    ).show()
                                                }
                                                // 5) Navegar a login
                                                navController.navigate("iniciar") {
                                                    popUpTo("registro") { inclusive = true }
                                                    launchSingleTop = true
                                                }
                                            }
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(
                                            context,
                                            "Error guardando usuario: ${e.localizedMessage}",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                            }
                        }
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
        onClick = { onGoogleSignInClick() },

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
        modifier = Modifier
            .padding(30.dp)
            .clickable {
                navController.navigate("iniciar")
            }



    )
}
