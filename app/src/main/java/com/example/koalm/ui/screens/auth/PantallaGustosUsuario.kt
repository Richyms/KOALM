package com.example.koalm.ui.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.material3.Button
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import com.example.koalm.R
import com.example.koalm.ui.theme.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaGustosUsuario(navController: NavController) {
    var correr by remember { mutableStateOf(true) }
    var leer by remember { mutableStateOf(true) }
    var meditar by remember { mutableStateOf(true) }
    var nadar by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("¿Qué te gusta hacer?") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .fillMaxSize()
                .padding(horizontal = 16.dp),

            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ImagenKoalaGustos()
            TextoTituloGustos()

            HabitoCard("Correr", "Tengo el hábito de correr.", correr) { correr = it }
            HabitoCard("Leer", "Suelo leer constantemente.", leer) { leer = it }
            HabitoCard("Meditar", "Tomo un tiempo para meditar.", meditar) { meditar = it }
            HabitoCard("Nadar", "Me gusta nadar.", nadar) { nadar = it }

            Spacer(modifier = Modifier.weight(1f))
            BotonGuardarGustos { navController.navigate("personalizar") }
        }
    }
}

@Composable
fun ImagenKoalaGustos() {
    val isDark = isSystemInDarkTheme()
    val tintColor = if (isDark) Color.White else Color.Black
    Image(
        painter = painterResource(id = R.drawable.training),
        contentDescription = "Koala haciendo ejercicio",
        modifier = Modifier
            .size(300.dp)
            .padding(vertical = 24.dp)/*,
        colorFilter = ColorFilter.tint(tintColor)*/
    )
}

@Composable
fun TextoTituloGustos() {
    Text(
        text = "Marca tus hábitos a mejorar",
        fontSize = 16.sp,
        modifier = Modifier.padding(bottom = 16.dp),
        style = MaterialTheme.typography.titleMedium
    )
}

@Composable
fun BotonGuardarGustos(onClick: () -> Unit) {
    val buttonModifier = Modifier
        .width(200.dp)
        .padding(20.dp)
    Button(
        onClick = onClick,
        modifier = buttonModifier,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
        contentPadding = PaddingValues(15.dp)) {
        Text("Guardar", color = MaterialTheme.colorScheme.onPrimary)
    }

}

@Composable
fun HabitoCard(
    titulo: String,
    descripcion: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = VerdePrincipal,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = titulo,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = descripcion,
                        style = MaterialTheme.typography.bodyMedium,
                        color = GrisMedio
                    )
                }
            }

            Checkbox(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = CheckboxDefaults.colors(
                    checkedColor = VerdePrincipal,
                    checkmarkColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    }
}
@Composable
fun PantallaConScroll() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Aquí puedes poner muchos elementos
        repeat(50) {
            Text("Elemento $it", modifier = Modifier.padding(8.dp))
        }
    }
}

