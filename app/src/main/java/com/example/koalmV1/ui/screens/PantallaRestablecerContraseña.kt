package com.example.koalmV1.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.koalmV1.R
import com.example.koalmV1.ui.theme.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaRestablecerContrasena(navController: NavController) {
    val context = LocalContext.current

    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Restablecer contraseña") },
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
            ImagenKoalaRestablecer()
            Spacer(modifier = Modifier.height(24.dp))
            CampoNuevaContrasena(password, passwordVisible,
                onValueChange = { password = it },
                onVisibilityToggle = { passwordVisible = !passwordVisible }
            )
            Spacer(modifier = Modifier.height(12.dp))
            CampoConfirmarContrasena(confirmPassword, confirmPasswordVisible,
                onValueChange = { confirmPassword = it },
                onVisibilityToggle = { confirmPasswordVisible = !confirmPasswordVisible }
            )
            Spacer(modifier = Modifier.height(24.dp))
            BotonRestablecer(password, confirmPassword, context)
        }
    }
}



@Composable
fun ImagenKoalaRestablecer() {
    Image(
        painter = painterResource(id = R.drawable.detective),
        contentDescription = "Koala Restablecer",
        modifier = Modifier.size(180.dp)
    )
}



@Composable
fun CampoNuevaContrasena(
    value: String,
    visible: Boolean,
    onValueChange: (String) -> Unit,
    onVisibilityToggle: () -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("Ingresa tu nueva contraseña") },
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth(0.85f)
            .clip(RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            val icon = if (visible) R.drawable.ic_eye_closed else R.drawable.ic_eye
            IconButton(onClick = onVisibilityToggle) {
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
fun CampoConfirmarContrasena(
    value: String,
    visible: Boolean,
    onValueChange: (String) -> Unit,
    onVisibilityToggle: () -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("Confirma tu nueva contraseña") },
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth(0.85f)
            .clip(RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            val icon = if (visible) R.drawable.ic_eye_closed else R.drawable.ic_eye
            IconButton(onClick = onVisibilityToggle) {
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
fun BotonRestablecer(
    password: String,
    confirmPassword: String,
    context: android.content.Context
) {
    Button(
        onClick = {
            if (password.length < 8) {
                Toast.makeText(context, "La contraseña debe tener al menos 8 caracteres", Toast.LENGTH_SHORT).show()
            } else if (password != confirmPassword) {
                Toast.makeText(context, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Contraseña actualizada", Toast.LENGTH_SHORT).show()
            }
        },
        colors = ButtonDefaults.buttonColors(containerColor = VerdePrincipal),
        modifier = Modifier.width(200.dp)
    ) {
        Text("Restablecer", color = Blanco)
    }
}



