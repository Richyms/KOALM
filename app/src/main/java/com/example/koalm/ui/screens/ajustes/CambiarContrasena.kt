package com.example.koalm.ui.screens.ajustes

import android.widget.NumberPicker.OnValueChangeListener
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
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.koalm.R
import com.example.koalm.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaCambiarContrasena(navController: NavController){
    val context = LocalContext.current
    var password by remember { mutableStateOf("")}

    Scaffold(
        topBar = {
            TopAppBar(
                title = {Text("Cambiar contraseña")},
                navigationIcon = {
                    IconButton(onClick = {navController.navigateUp()}) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                }
            )
        }
    )
    {padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.koala_lupa),
                contentDescription = "Koala lupa",
                modifier = Modifier.size(300.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))
            CampoValidarContrasena(password) { password = it }
            MensajeExplicacionCambio()
            Spacer(modifier = Modifier.height(16.dp))
            BotonEnviarContrasena(password, navController, context)
        }

    }
}

/*Se requerira la contraseña para enviar un correo de validación*/
@Composable
fun CampoValidarContrasena (value: String, onValueChange: (String) -> Unit){
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("Ingresa tu contraseña") },

        modifier = Modifier.fillMaxWidth(0.97f),
        shape = RoundedCornerShape(16.dp),

        //keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = VerdePrincipal,
            unfocusedBorderColor = GrisMedio,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    )
}

@Composable
fun MensajeExplicacionCambio(){
    Text(
        text = "Te enviaremos un enlace de restablecimiento al correo asociado a tu cuenta",
        fontSize = 12.sp,
        color = GrisMedio,
        modifier = Modifier
            .fillMaxWidth(0.85f)
            .padding(top = 4.dp)
    )
}

@Composable
fun BotonEnviarContrasena(
    password: String,
    navController: NavController,
    context: android.content.Context
){
   /*Logica de recuperar el codigo con base en la contraseña de usuario*/
    Button(
        //enabled = emailValido,
        onClick = {
            // Al presionar el botón, validamos si el correo existe
            //validarCorreoExistente(correo)
        },
        colors = ButtonDefaults.buttonColors(containerColor = VerdePrincipal)
    ) {
        Text("Enviar", color = Blanco)
    }
}