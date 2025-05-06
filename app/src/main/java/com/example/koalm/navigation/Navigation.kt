package com.example.koalm.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.koalm.ui.screens.*
import com.example.koalm.ui.screens.PantallaConfigurarDesconexionDigital

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
        screenWithSlide("iniciar") {
            PantallaIniciarSesion(
                navController = navController,
                onGoogleSignInClick = onGoogleSignInClick
            )
        }
        screenWithSlide("registro") {
            PantallaRegistro(
                navController = navController,
                onGoogleSignInClick = onGoogleSignInClick
            )
        }

        screenWithSlide("recuperar") { PantallaRecuperarContrasena(navController) }
        screenWithSlide("restablecer") { PantallaRestablecerContrasena(navController) }
        screenWithSlide("recuperarCodigo") { PantallaCodigoRecuperarContrasena(navController) }
        screenWithSlide("personalizar") { PantallaPersonalizarPerfil(navController) }
        screenWithSlide("habitos") { PantallaGustosUsuario(navController) }
        screenWithSlide("menu") { PantallaMenuPrincipal(navController) }
        screenWithSlide("tipos_habitos") { PantallaHabitos(navController) }
        screenWithSlide("salud_mental") { PantallaSaludMental(navController) }
        screenWithSlide("salud_fisica") { PantallaSaludFisica(navController) }
        screenWithSlide("configurar_habito_escritura") { PantallaConfiguracionHabitoEscritura(navController) }
        screenWithSlide("configurar_habito_meditacion") { PantallaConfiguracionHabitoMeditacion(navController) }
        screenWithSlide("configurar_habito_sueno") { PantallaConfiguracionHabitoSueno(navController) }
        screenWithSlide("configurar_habito_hidratacion") { PantallaConfiguracionHabitoHidratacion(navController) }
        screenWithSlide("configurar_habito_alimentacion") { PantallaConfiguracionHabitoAlimentacion(navController)}
        screenWithSlide("configurar_habito_lectura") { PantallaConfiguracionHabitoLectura(navController)}
        screenWithSlide("estadisticas") { PantallaParametrosSalud(navController) }
        screenWithSlide("gestion_habitos_personalizados") { PantallaGestionHabitosPersonalizados(navController) }
        screenWithSlide("configurar_habito_personalizado") { PantallaConfigurarHabitoPersonalizado(navController) }
        screenWithSlide("configurar_habito_desconexion_digital") { PantallaConfigurarDesconexionDigital(navController) }
        screenWithSlide("notas") { PantallaNotas(navController) }
        screenWithSlide("libros") { PantallaLibros(navController) }
        screenWithSlide("temporizador_meditacion") { PantallaTemporizadorMeditacion(navController) }
        screenWithSlide("ritmo-cardiaco") { PantallaRitmoCardiaco(navController) }
        screenWithSlide("sueño-de-anoche") { PantallaSueno(navController) }
        screenWithSlide("nivel-de-estres") { PantallaEstres(navController) }
        screenWithSlide("objetivos-peso") { PantallaObjetivosPeso(navController) }
        screenWithSlide("actividad-diaria") { PantallaActividadDiaria(navController) }
        screenWithSlide("actualizar-peso") { PantallaActualizarPeso(navController) }
        screenWithSlide("meta-diaria-pasos") { PantallaMetaPasos(navController) }
        screenWithSlide("meta-diaria-movimiento") { PantallaMetaMovimiento(navController) }
        screenWithSlide("meta-diaria-calorias") { PantallaMetaCalorias(navController) }
        screenWithSlide("racha_habitos") { PantallaRachaHabitos(navController) }
    }
}

// Animación personalizada (opcional)s
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