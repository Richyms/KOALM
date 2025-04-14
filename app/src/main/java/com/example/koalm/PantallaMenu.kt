package com.example.koalm

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.koalm.ui.theme.VerdePrincipal
import com.example.koalm.ui.theme.GrisMedio

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaMenu(navController: NavHostController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("¡Hola, Kool! 🐨✨") },
                navigationIcon = {
                    IconButton(onClick = { /* Abrir barra lateral */ }) {
                        Icon(Icons.Default.Menu, contentDescription = "Menú")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Abrir configuración */ }) {
                        Icon(Icons.Default.Settings, contentDescription = "Configuración")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = true,
                    onClick = { /* Navegar a Inicio */ },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Inicio") },
                    label = { Text("Inicio") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { /* Navegar a Hábitos */ },
                    icon = { Icon(Icons.Default.List, contentDescription = "Hábitos") },
                    label = { Text("Hábitos") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { /* Navegar a Perfil */ },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Perfil") },
                    label = { Text("Perfil") }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.Top
        ) {
            // Carrusel de racha
            Text(
                text = "Racha",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(7) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (it < 5) VerdePrincipal else GrisMedio,
                        modifier = Modifier.size(40.dp)
                    ) {}
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Carrusel de hábitos koalísticos
            Text(
                text = "Hábitos koalísticos",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(5) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.size(200.dp, 120.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.koala),
                            contentDescription = "Koala",
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sección de hábitos
            Text(
                text = "Mis hábitos",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                repeat(3) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.koala),
                                contentDescription = "Hábito",
                                modifier = Modifier.size(60.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("Hábito ${it + 1}", fontWeight = FontWeight.Bold)
                                Text("Descripción del hábito", fontSize = 12.sp, color = GrisMedio)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sección de estadísticas
            Text(
                text = "Estadísticas",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.LightGray,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            ) {
                // Placeholder para estadísticas
            }
        }
    }
}