/*PantallaPrincipalAjustes*/
package com.example.koalm.ui.screens.ajustes

import android.content.Context
import androidx.compose.material3.AlertDialog
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.example.koalm.R
import com.example.koalm.ui.theme.*
import com.google.android.gms.auth.api.identity.Identity
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaAjustes(navController: NavHostController) {

    AlertDialog(
        onDismissRequest = {
            navController.popBackStack()
        },
        title = {
            Box(modifier = Modifier.fillMaxWidth()) {
                // Título
                Text(
                    text = "Ajustes",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                // Botón "X" para cerrar
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Regresar"
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                BotonesAjustes(navController)
            }
        },
        confirmButton = {},
        dismissButton = {}
    )
}


@Suppress("DEPRECATION")
fun cerrarSesion(context: Context, navController: NavHostController) {
    FirebaseAuth.getInstance().signOut()

    @Suppress("DEPRECATION")
    Identity.getSignInClient(context)
        .signOut()
        .addOnCompleteListener {
            // …
        }
    // 3. Borra SharedPreferences con extensión KTX
    context.getSharedPreferences(
        context.getString(R.string.prefs_file),
        Context.MODE_PRIVATE
    ).edit {
        clear()
    }

    // 4. Redirige a la pantalla de inicio y limpia el back stack
    navController.navigate("iniciar") {
        popUpTo("menu") { inclusive = true }
    }
}

@Composable
private fun BotonesAjustes(navController: NavHostController) {
    val botonModifier = Modifier
        .fillMaxWidth(0.8f)
        .padding(vertical = 4.dp)
    val context = LocalContext.current

    Button(
        onClick = { navController.navigate("nosotros") },
        colors = ButtonDefaults.buttonColors(containerColor = VerdePrincipal),
        modifier = botonModifier
    ) {
        Text("Sobre Nosotros", color = Blanco)
    }

    Button(
        onClick = { navController.navigate("privacidad") },
        colors = ButtonDefaults.buttonColors(containerColor = VerdePrincipal),
        modifier = botonModifier
    ) {
        Text("Privacidad", color = Blanco)
    }

    Button(
        onClick = { navController.navigate("TyC") },
        colors = ButtonDefaults.buttonColors(containerColor = VerdePrincipal),
        modifier = botonModifier
    ) {
        Text("Términos y Condiciones", color = Blanco)
    }

    Button(
        onClick = { navController.navigate("cambiar_contrasena") },
        colors = ButtonDefaults.buttonColors(containerColor = VerdePrincipal),
        modifier = botonModifier
    ) {
        Text("Cambiar Contraseña", color = Blanco)
    }

    Button(
        onClick = { cerrarSesion(context, navController) },
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEC615B)),
        modifier = botonModifier
    ) {
        Text("Cerrar sesión", color = Blanco)
    }

    Button(
        onClick = { /*Agregar Logica*/ },
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEC615B)),
        modifier = botonModifier
    ) {
        Text("Borrar Cuenta", color = Blanco)
    }
}