package com.example.koalm.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.koalm.R
import com.example.koalm.ui.theme.VerdePrincipal
import com.example.koalm.ui.theme.GrisMedio

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaRestablecerContrasena(navController: NavController) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recuperar contraseña") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Regresar"
                        )
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
            ImagenKoalaCorreo()
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "¡Revisa tu correo!",
                style = MaterialTheme.typography.headlineMedium,
                color = VerdePrincipal,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Te hemos enviado un enlace para que puedas crear una nueva contraseña.\nÁbrelo desde tu correo para continuar.",
                style = MaterialTheme.typography.bodyLarge,
                color = GrisMedio,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ImagenKoalaCorreo() {
    val isDark = isSystemInDarkTheme()
    val tintColor = if (isDark) Color.White else Color.Black

    Image(
        painter = painterResource(id = R.drawable.query), // Puedes usar otro como "mail", "koala_email", etc.
        contentDescription = "Koala correo enviado",
        modifier = Modifier.size(200.dp),
        colorFilter = ColorFilter.tint(tintColor)
    )
}
