package com.example.koalmV1.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.koalmV1.ui.screens.*

@Composable
fun AppNavigation(
    navController: NavHostController,
    onGoogleSignInClick: () -> Unit,
    startDestination: String
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable("iniciar") {
            PantallaIniciarSesion(
                navController = navController,
                onGoogleSignInClick = onGoogleSignInClick
            )
        }
        composable("registro") { PantallaRegistro(navController) }
        composable("recuperar") { PantallaRecuperarContrasena(navController) }
        composable("restablecer") { PantallaRestablecerContrasena(navController) }
        composable("personalizar") { PantallaPersonalizarPerfil(navController) }
        composable("habitos") { PantallaGustosUsuario(navController) }
        composable("menu") { PantallaMenuPrincipal(navController) }
    }
}

// Animación personalizada (opcional)
private fun androidx.navigation.NavGraphBuilder.screenWithSlide(
    route: String,
    content: @Composable () -> Unit
) {
    composable(
        route = route,
        enterTransition = {
            slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = tween(300)
            )
        },
        exitTransition = {
            slideOutHorizontally(
                targetOffsetX = { -it },
                animationSpec = tween(300)
            )
        },
        popEnterTransition = {
            slideInHorizontally(
                initialOffsetX = { -it },
                animationSpec = tween(300)
            )
        },
        popExitTransition = {
            slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = tween(300)
            )
        }
    ) {
        content()
    }
}
