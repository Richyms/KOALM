package com.example.koalm.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

import com.example.koalm.ui.theme.*
import com.example.koalm.R


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaCodigoRecuperarContrasena(navController: NavController) {
    val context = LocalContext.current
    val codigo = remember { mutableStateListOf("", "", "", "") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recuperar contraseña") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
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
            ImagenKoalaRecuperarCodigo()

            Spacer(modifier = Modifier.height(32.dp))

            Text("Código", style = MaterialTheme.typography.labelLarge)

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                for (i in 0..3) {
                    OutlinedTextField(
                        value = codigo[i],
                        onValueChange = {
                            if (it.length <= 1 && it.all { char -> char.isDigit() }) {
                                codigo[i] = it
                            }
                        },
                        modifier = Modifier.size(60.dp),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true,
                        textStyle = TextStyle(fontSize = 24.sp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VerdePrincipal,
                            unfocusedBorderColor = GrisMedio,
                            unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Ingresa los 4 dígitos que se enviaron al correo asociado a tu cuenta.",
                fontSize = 12.sp,
                color = GrisMedio,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (codigo.all { it.length == 1 }) {
                        Toast.makeText(context, "Código correcto", Toast.LENGTH_SHORT).show()
                        navController.navigate("PantallaRestablecerContrasena")
                    } else {
                        Toast.makeText(context, "Código incompleto", Toast.LENGTH_SHORT).show()
                        navController.navigate("restablecer")
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = VerdePrincipal)
            ) {
                Text("Verificar", color = Blanco)
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                buildAnnotatedString {
                    withStyle(SpanStyle(color = MaterialTheme.colorScheme.onSurface)) {
                        append("¿No tienes una cuenta? ")
                    }
                    withStyle(SpanStyle(color = VerdeSecundario)) {
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

@Composable
fun ImagenKoalaRecuperarCodigo() {
    val isDark = isSystemInDarkTheme()
    val tintColor = if (isDark) Color.White else Color.Black

    Image(
        painter = painterResource(id = R.drawable.query),
        contentDescription = "Koala pregunta",
        modifier = Modifier.size(300.dp),
        colorFilter = ColorFilter.tint(tintColor)
    )
}
