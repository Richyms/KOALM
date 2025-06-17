package com.example.koalm.ui.screens.auth

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
import androidx.compose.ui.draw.clip
import com.example.koalm.ui.components.ExitoDialogoGuardadoAnimado
import com.example.koalm.ui.components.FalloDialogoGuardadoAnimado
import com.example.koalm.ui.components.ValidacionesDialogoAnimado
import kotlinx.coroutines.*
import androidx.compose.runtime.*

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

    var mensajeValidacion by remember { mutableStateOf<String?>(null) }

    if (mensajeValidacion != null) {
        ValidacionesDialogoAnimado(
            mensaje = mensajeValidacion!!,
            onDismiss = {
                mensajeValidacion = null
            }
        )
    }

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

    // Para saber si el username es válido
    var username by remember { mutableStateOf("") }

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
                onValueChange = { nuevoValor ->
                   username = nuevoValor
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
            CheckboxTerminos(
                navController = navController,
                checked = termsAccepted,
                onCheckedChange = { termsAccepted = it }
            )
            Spacer(modifier = Modifier.height(12.dp))
            BotonesRegistro(
                email, username, password, confirmPassword,
                isValidEmail, yaExisteCorreo,
                termsAccepted, navController, context,
                onMensajeValidacionChange = { mensajeValidacion = it },
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
        modifier = Modifier.size(270.dp)/*,
        colorFilter = ColorFilter.tint(tintColor)*/
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
    onValueChange: (String) -> Unit
) {
    val regex = "^[a-zA-Z0-9_ ]*$".toRegex() // Letras, números, guion bajo y espacios
    val limpio = value.filter { it.code != 8203 } // Elimina caracteres invisibles (como \u200B)

    val valido = limpio.isNotBlank() &&
            limpio.trim().length >= 3 &&
            regex.matches(limpio)

    OutlinedTextField(
        value = value,
        onValueChange = { nuevoTexto ->
            if (nuevoTexto.matches(regex)) {
                onValueChange(nuevoTexto)
            }
        },
        label = { Text("Nombre de usuario *") },
        modifier = Modifier
            .fillMaxWidth(0.97f)
            .clip(RoundedCornerShape(16.dp)),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = if (valido || value.isEmpty()) VerdePrincipal else Color.Red,
            unfocusedBorderColor = if (valido || value.isEmpty()) GrisMedio else Color.Red,
            focusedLabelColor = if (valido || value.isEmpty()) VerdePrincipal else Color.Red,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            errorLabelColor = Color.Red
        ),
        supportingText = {
            when {
                value.all { it == ' ' } && value.length >= 3 -> { // Solo espacios
                    Text(
                        text = "El nombre no puede solo contener espacios.",
                        color = Color.Red,
                        fontSize = 12.sp
                    )
                }

                value.isBlank() -> {
                    Text(
                        text = "El nombre no puede estar vacío o solo contener espacios.",
                        color = GrisMedio,
                        fontSize = 12.sp
                    )
                }

                value.trim().length < 3 -> {
                    Text(
                        text = "Debe tener al menos 3 caracteres (excluyendo espacios).",
                        color = Color.Red,
                        fontSize = 12.sp
                    )
                }
                !regex.matches(value) -> {
                    Text(
                        text = "Solo se permiten letras, números, guion bajo y espacios.",
                        color = Color.Red,
                        fontSize = 12.sp
                    )
                }
                else -> {
                    Text(
                        text = "Nombre de usuario válido.",
                        color = GrisMedio,
                        fontSize = 12.sp
                    )
                }
            }
        }
    )

    Spacer(modifier = Modifier.height(12.dp))
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
fun CheckboxTerminos(checked: Boolean, navController: NavController, onCheckedChange: (Boolean) -> Unit) {
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
                navController.navigate("TyC")
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
    termsAccepted: Boolean,
    navController: NavController,
    context: Context,
    onMensajeValidacionChange: (String?) -> Unit,
    onGoogleSignInClick: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val buttonModifier = Modifier.width(200.dp)
    var isLoading by remember { mutableStateOf(false) }
    val TAG = "Registro"

    //Mensaje de exito de registro
    var mostrarDialogoExito by remember{ mutableStateOf(false) }
    if (mostrarDialogoExito) {
        ExitoDialogoGuardadoAnimado(
            mensaje = "¡Registro exitoso! Hemos enviado un enlace de verificación a tu correo electrónico.",
            onDismiss = {
                mostrarDialogoExito = false
                navController.navigate("iniciar") {
                    popUpTo("registro") { inclusive = true }
                    launchSingleTop = true
                }
            }
        )
    }

    //Mensaje de fallo de registro
    var mostrarDialogoFallo by remember{ mutableStateOf(false) }
    if (mostrarDialogoFallo) {
        FalloDialogoGuardadoAnimado(
            mensaje = "Hubo un error al enviar el enlace de verificación.",
            onDismiss = {
                mostrarDialogoFallo = false
            }
        )
    }

    Button(
        onClick = {
            android.util.Log.d(TAG, "Iniciando proceso de registro")
            android.util.Log.d(TAG, "Email: $email")
            android.util.Log.d(TAG, "Username: $username")
            android.util.Log.d(TAG, "Validaciones iniciales:")
            android.util.Log.d(TAG, "- Email válido: $isValidEmail")
            android.util.Log.d(TAG, "- Email ya existe: $yaExisteCorreo")
            android.util.Log.d(TAG, "- Términos aceptados: $termsAccepted")

            //Expresión regular: La regla de negocio que se definió para la contraseña.
            val passwordRegex = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#\$%^&*()\\-_=+\\[{\\]}|;:,.<>?/`~]).{8,}$")
            //Expresión regular: La regla de negocio que se definió para el nombre de usuarios (caracteres válidos)
            val usernameRegex = "^[a-zA-Z0-9_ ]*$".toRegex()
            val limpio = username.filter { it.code != 8203 } // Elimina caracteres invisibles (como \u200B)
            val valido = limpio.isNotBlank() &&
                    limpio.trim().length >= 3 &&
                    usernameRegex.matches(limpio)

            android.util.Log.d(TAG, "Validaciones adicionales:")
            android.util.Log.d(TAG, "- Username válido: $valido")
            android.util.Log.d(TAG, "- Password cumple regex: ${passwordRegex.matches(password)}")
            android.util.Log.d(TAG, "- Passwords coinciden: ${password == confirmPassword}")

            //Todas las validaciones generales
            when {
                email.isBlank() || username.isBlank() || password.isBlank() || confirmPassword.isBlank() -> {
                    android.util.Log.w(TAG, "Campos vacíos detectados")
                    onMensajeValidacionChange("Por favor completa todos los campos.")
                }
                !isValidEmail -> {
                    android.util.Log.w(TAG, "Email inválido")
                    onMensajeValidacionChange("El formato del correo no es válido.")
                }
                yaExisteCorreo -> {
                    android.util.Log.w(TAG, "Email ya existe en la base de datos")
                    onMensajeValidacionChange("El correo electrónico ya está en uso.")
                }
                !valido -> {
                    android.util.Log.w(TAG, "Username inválido")
                    onMensajeValidacionChange("El nombre de usuario no cumple con los requisitos.")
                }
                !passwordRegex.matches(password) -> {
                    android.util.Log.w(TAG, "Password no cumple con los requisitos")
                    onMensajeValidacionChange("La contraseña no cumple con los requisitos.")
                }
                password != confirmPassword -> {
                    android.util.Log.w(TAG, "Passwords no coinciden")
                    onMensajeValidacionChange("Las contraseñas no coinciden.")
                }
                !termsAccepted -> {
                    android.util.Log.w(TAG, "Términos no aceptados")
                    onMensajeValidacionChange( "Debes aceptar los términos y condiciones.")
                }
                else -> {
                    android.util.Log.d(TAG, "Todas las validaciones pasaron, iniciando registro en Firebase")
                    isLoading = true
                    // Usando el servicio de Firebase
                    // 1) Crear usuario en Firebase Auth
                    FirebaseAuth.getInstance()
                        .createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                android.util.Log.d(TAG, "Usuario creado exitosamente en Firebase Auth")
                                // 1.1) Obtener UserID
                                val auth = FirebaseAuth.getInstance()
                                val userId = auth.currentUser!!.uid
                                android.util.Log.d(TAG, "UserID obtenido: $userId")

                                // 2) Construir un objeto Usuario mínimo (los primeros 3 campos del registro)
                                val uLogin = Usuario(
                                    userId   = userId,
                                    email    = email,
                                    username = username
                                )

                                // 3) Guardar en Firestore con merge
                                val db = FirebaseFirestore.getInstance()
                                android.util.Log.d(TAG, "Intentando guardar usuario en Firestore")
                                db.collection("usuarios")
                                    .document(email)
                                    .set(uLogin.toMap(), SetOptions.merge())
                                    .addOnSuccessListener {
                                        android.util.Log.d(TAG, "Usuario guardado exitosamente en Firestore")
                                        // 4) Enviar verificación de correo
                                        auth.currentUser
                                            ?.sendEmailVerification()
                                            ?.addOnCompleteListener { verifyTask ->
                                                isLoading = false
                                                if (verifyTask.isSuccessful) {
                                                    android.util.Log.d(TAG, "Correo de verificación enviado exitosamente")
                                                    // Mostrar diálogo de éxito
                                                    mostrarDialogoExito = true

                                                } else {
                                                    android.util.Log.e(TAG, "Error al enviar correo de verificación", verifyTask.exception)
                                                    mostrarDialogoFallo = true
                                                }
                                            }
                                    }
                                    .addOnFailureListener { e ->
                                        isLoading = false
                                        android.util.Log.e(TAG, "Error al guardar usuario en Firestore", e)
                                        Toast.makeText(
                                            context,
                                            "Error guardando usuario: ${e.localizedMessage}",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                            } else {
                                isLoading = false
                                android.util.Log.e(TAG, "Error al crear usuario en Firebase Auth", task.exception)
                                Toast.makeText(
                                    context,
                                    "Error al crear usuario: ${task.exception?.localizedMessage}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                }
            }
        },
        modifier = buttonModifier,
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
        enabled = !isLoading
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = MaterialTheme.colorScheme.onPrimary
            )
        } else {
            Text("Registrar", color = MaterialTheme.colorScheme.onPrimary)
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

    OutlinedButton(
        onClick = { onGoogleSignInClick() },
        modifier = buttonModifier,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        enabled = !isLoading
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
