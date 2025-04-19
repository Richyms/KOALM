package com.example.koalm.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.navigation.NavHostController
import com.example.koalm.R
import com.example.koalm.ui.theme.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.example.koalm.model.Usuario

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaIniciarSesion(
    navController: NavHostController,
    onGoogleSignInClick: () -> Unit
) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    // Estados de input
    var email by remember { mutableStateOf("") }
    val isValidEmail = email.contains("@") && listOf(
        "gmail.com","hotmail.com","yahoo.com","icloud.com",
        "live.com","outlook.com","proton.me","protonmail.com",
        "aol.com","mail.com","zoho.com","yandex.com"
    ).any { domain -> email.endsWith("@$domain") }

    var password by remember { mutableStateOf("") }
    val isValidPassword = password.length >= 8
    var passwordVisible by remember { mutableStateOf(false) }

    // Al montar: si ya logueado y verificado, checar perfil y navegar
    LaunchedEffect(auth.currentUser) {
        auth.currentUser?.let { user ->
            if (!user.isEmailVerified) {
                Toast.makeText(context, "Verifica tu correo antes de continuar", Toast.LENGTH_LONG).show()
                return@let
            }
            db.collection("usuarios").document(user.uid).get()
                .addOnSuccessListener { doc ->
                    if (!doc.exists()) return@addOnSuccessListener
                    val nombre     = doc.getString("nombre").orEmpty()
                    val apellido   = doc.getString("apellido").orEmpty()
                    val nacimiento = doc.getString("nacimiento").orEmpty()
                    val genero     = doc.getString("genero").orEmpty()
                    val peso       = doc.getLong("peso") ?: 0L
                    val altura     = doc.getLong("altura") ?: 0L

                    val completo = listOf(
                        nombre.isNotBlank(),
                        apellido.isNotBlank(),
                        nacimiento.isNotBlank(),
                        genero.isNotBlank(),
                        peso > 0L,
                        altura > 0L
                    ).all { it }

                    val destino = if (completo) "menu" else "personalizar"
                    navController.navigate(destino) {
                        popUpTo("iniciar") { inclusive = true }
                        launchSingleTop = true
                    }
                }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Iniciar sesión") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
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
            LoginLogo()
            Spacer(modifier = Modifier.height(32.dp))

            EmailOrUsernameField(
                value = email,
                isValid = isValidEmail,
                isEmail = true,
                onValueChange = { email = it }
            )
            Spacer(modifier = Modifier.height(16.dp))

            PasswordField(
                value = password,
                passwordVisible = passwordVisible,
                isValidPassword = isValidPassword,
                onPasswordChange = { password = it },
                onVisibilityToggle = { passwordVisible = !passwordVisible }
            )
            Spacer(modifier = Modifier.height(24.dp))

            // Botón de login solo si correo verificado
            Button(
                onClick = {
                    when {
                        !isValidEmail -> Toast.makeText(context, "Correo inválido", Toast.LENGTH_SHORT).show()
                        !isValidPassword -> Toast.makeText(context, "Contraseña muy corta", Toast.LENGTH_SHORT).show()
                        else -> {
                            auth.signInWithEmailAndPassword(email, password)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        val user = auth.currentUser!!
                                        if (!user.isEmailVerified) {
                                            Toast.makeText(context,
                                                "Por favor verifica tu correo antes de iniciar sesión",
                                                Toast.LENGTH_LONG
                                            ).show()
                                            return@addOnCompleteListener
                                        }
                                        val uid = user.uid
                                        // Actualizar usuario mínimo
                                        val uLogin = Usuario(
                                            id = uid,
                                            userId = uid,
                                            email = email,
                                            username = email.substringBefore("@")
                                        )
                                        db.collection("usuarios").document(uid)
                                            .set(uLogin.toMap(), SetOptions.merge())
                                            .addOnSuccessListener {
                                                // Leer y checar perfil completo
                                                db.collection("usuarios").document(uid).get()
                                                    .addOnSuccessListener { doc ->
                                                        val nombre     = doc.getString("nombre").orEmpty()
                                                        val apellido   = doc.getString("apellido").orEmpty()
                                                        val nacimiento = doc.getString("nacimiento").orEmpty()
                                                        val genero     = doc.getString("genero").orEmpty()
                                                        val peso       = doc.getLong("peso") ?: 0L
                                                        val altura     = doc.getLong("altura") ?: 0L
                                                        val completo = listOf(
                                                            nombre.isNotBlank(),
                                                            apellido.isNotBlank(),
                                                            nacimiento.isNotBlank(),
                                                            genero.isNotBlank(),
                                                            peso > 0L,
                                                            altura > 0L
                                                        ).all { it }
                                                        val destino = if (completo) "menu" else "personalizar"
                                                        Toast.makeText(context,
                                                            if (completo) "Bienvenid@ ${doc.getString("username")}"
                                                            else "Completa tu perfil antes de continuar",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                        navController.navigate(destino) {
                                                            popUpTo("iniciar") { inclusive = true }
                                                            launchSingleTop = true
                                                        }
                                                    }
                                            }
                                    } else {
                                        val err = task.exception
                                        val msg = if (err is FirebaseAuthInvalidCredentialsException)
                                            "Credenciales incorrectas"
                                        else "Error: ${err?.localizedMessage}"
                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    }
                                }
                        }
                    }
                },
                modifier = Modifier.width(200.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Iniciar sesión", color = MaterialTheme.colorScheme.onPrimary)
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onGoogleSignInClick,
                modifier = Modifier.width(200.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Text("Iniciar con Google", color = MaterialTheme.colorScheme.onSurface)
            }

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
        label = { Text("Correo electrónico") },
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
                text = "Solo servicios de correo electrónico conocidos.",
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
    password: String,
    context: Context,
    navController: NavHostController,
    onGoogleSignInClick: () -> Unit,
    auth: FirebaseAuth,
    db: FirebaseFirestore
) {
    val buttonModifier = Modifier.width(200.dp)

    Button(
        onClick = {
            when {
                !isValidInput -> Toast.makeText(context, "Correo inválido", Toast.LENGTH_SHORT).show()
                !isValidPassword -> Toast.makeText(context, "Contraseña muy corta", Toast.LENGTH_SHORT).show()
                else -> {
                    auth.signInWithEmailAndPassword(emailOrUsername, password)
                        .addOnCompleteListener { authTask ->
                            if (authTask.isSuccessful) {
                                val user = auth.currentUser!!
                                val uid = user.uid
                                val usuarioLogin = Usuario(
                                    id = uid,
                                    userId = uid,
                                    email = emailOrUsername,
                                    username = emailOrUsername.substringBefore("@")
                                )
                                db.collection("usuarios")
                                    .document(uid)
                                    .set(usuarioLogin.toMap(), SetOptions.merge())

                                db.collection("usuarios")
                                    .document(uid)
                                    .get()
                                    .addOnSuccessListener { snap ->
                                        val nickname = snap.getString("nickName") ?: ""
                                        Toast.makeText(context, "Bienvenid@ $nickname", Toast.LENGTH_SHORT).show()
                                        navController.navigate("menu") {
                                            popUpTo("iniciar") { inclusive = true }
                                            launchSingleTop = true
                                        }
                                    }
                            } else {
                                val error = authTask.exception
                                val msg = when {
                                    error is FirebaseAuthInvalidCredentialsException -> "Credenciales incorrectas"
                                    else -> "Error: ${error?.localizedMessage}"
                                }
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            }
                        }
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
        modifier = Modifier.clickable { navController.navigate("recuperar") }
    )

    Spacer(modifier = Modifier.height(8.dp))

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
        modifier = Modifier.clickable { navController.navigate("registro") }
    )
}
