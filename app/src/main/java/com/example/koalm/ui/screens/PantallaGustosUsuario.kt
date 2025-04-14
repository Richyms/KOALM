package com.example.koalm.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.*
import androidx.navigation.NavController
import com.example.koalm.R
import com.example.koalm.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaGustosUsuario(navController: NavController) {
    var correr by remember { mutableStateOf(true) }
    var leer by remember { mutableStateOf(true) }
    var meditar by remember { mutableStateOf(true) }
    var nadar by remember { mutableStateOf(false) }
    var selectedIndex by remember { mutableStateOf(1) }

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
        },
        bottomBar = {
            BarraInferiorGustos(selectedIndex) { nuevo ->
                selectedIndex = nuevo
                when (nuevo) {
                    0 -> navController.navigate("menu")
                    1 -> navController.navigate("gustos")
                    2 -> navController.navigate("personalize")
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
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
            BotonGuardarGustos { navController.navigate("menu") }
        }
    }
}


@Composable
fun ImagenKoalaGustos() {
    Image(
        painter = painterResource(id = R.drawable.koala_ejercicio),
        contentDescription = "Koala haciendo ejercicio",
        modifier = Modifier
            .size(200.dp)
            .padding(vertical = 24.dp)
    )
}




@Composable
fun TextoTituloGustos() {
    Text(
        text = "Marca tus hábitos a mejorar",
        fontSize = 16.sp,
        modifier = Modifier.padding(bottom = 16.dp)
    )
}




@Composable
fun BarraInferiorGustos(seleccionado: Int, onSelect: (Int) -> Unit) {
    val items = listOf("Inicio", "Hábitos", "Perfil")
    val icons = listOf(Icons.Default.Home, Icons.Default.List, Icons.Default.Person)

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        items.forEachIndexed { index, item ->
            NavigationBarItem(
                selected = seleccionado == index,
                onClick = { onSelect(index) },
                icon = { Icon(icons[index], contentDescription = item) },
                label = { Text(item) }
            )
        }
    }
}





@Composable
fun BotonGuardarGustos(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .width(200.dp)
            .padding(vertical = 16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = VerdePrincipal)
    ) {
        Text("Guardar", color = Color.White)
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
            .padding(vertical = 4.dp)
            .border(
                width = 1.dp,
                color = VerdeBorde,
                shape = RoundedCornerShape(8.dp)
            ),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = VerdeContenedor
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



