package com.example.koalm.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.compose.ui.unit.dp
import com.example.koalm.ui.theme.*

@Composable
fun BarraNavegacionInferior(
    navController: NavHostController,
    rutaActual: String
) {
    NavigationBar(
        containerColor = GrisClaro,
        contentColor = GrisMedio,
        tonalElevation = 8.dp
    ) {
        listOf(
            Triple("Inicio", Icons.Default.Home, "menu"),
            Triple("Hábitos", Icons.Default.List, "tipos_habitos"),
            Triple("Perfil", Icons.Default.Person, "perfil")
        ).forEach { (label, icon, route) ->
            NavigationBarItem(
                selected = rutaActual == route,
                onClick = { 
                    if (rutaActual != route) {
                        navController.navigate(route) {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    }
                },
                icon = { 
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = if (rutaActual == route) VerdePrincipal else GrisMedio
                    )
                },
                label = { 
                    Text(
                        text = label,
                        color = if (rutaActual == route) VerdePrincipal else GrisMedio
                    ) 
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = VerdePrincipal,
                    unselectedIconColor = GrisMedio,
                    selectedTextColor = VerdePrincipal,
                    unselectedTextColor = GrisMedio,
                    indicatorColor = VerdePrincipal.copy(alpha = 0.1f)
                )
            )
        }
    }
} 