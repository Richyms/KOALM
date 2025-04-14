package com.example.koalm

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.koalm.ui.theme.VerdePrincipal
import com.example.koalm.ui.theme.GrisMedio
import com.example.koalm.ui.theme.VerdeContenedor
import com.example.koalm.ui.theme.VerdeBorde

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaHabitos(navController: NavController) {
    var correr by remember { mutableStateOf(true) }
    var leer by remember { mutableStateOf(true) }
    var meditar by remember { mutableStateOf(true) }
    var nadar by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("¿Qué te gusta hacer?") },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = true,
                    onClick = { navController.navigate("inicio") },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Inicio") },
                    label = { Text("Inicio") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        indicatorColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate("habitos") },
                    icon = { Icon(Icons.Default.List, contentDescription = "Hábitos") },
                    label = { Text("Hábitos") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        indicatorColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate("perfil") },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Perfil") },
                    label = { Text("Perfil") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        indicatorColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
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
            Image(
                painter = painterResource(id = R.drawable.koala_ejercicio),
                contentDescription = "Koala haciendo ejercicio",
                modifier = Modifier
                    .size(200.dp)
                    .padding(vertical = 24.dp)
            )

            Text(
                text = "Marca tus hábitos a mejorar",
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Hábito Card - Correr
            HabitoCard(
                titulo = "Correr",
                descripcion = "Tengo el hábito de correr.",
                checked = correr,
                onCheckedChange = { correr = it }
            )

            // Hábito Card - Leer
            HabitoCard(
                titulo = "Leer",
                descripcion = "Suelo leer constantemente.",
                checked = leer,
                onCheckedChange = { leer = it }
            )

            // Hábito Card - Meditar
            HabitoCard(
                titulo = "Meditar",
                descripcion = "Tomo un tiempo para meditar.",
                checked = meditar,
                onCheckedChange = { meditar = it }
            )

            // Hábito Card - Nadar
            HabitoCard(
                titulo = "Nadar",
                descripcion = "Me gusta nadar.",
                checked = nadar,
                onCheckedChange = { nadar = it }
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { navController.navigate("guardar") },
                modifier = Modifier
                    .width(200.dp)
                    .padding(vertical = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = VerdePrincipal
                )
            ) {
                Text("Guardar")
            }
        }
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
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
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