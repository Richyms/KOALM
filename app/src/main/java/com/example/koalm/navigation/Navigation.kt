package com.example.koalm.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.koalm.ui.screens.habitos.saludMental.PantallaConfigurarDesconexionDigital
import com.example.koalm.ui.screens.auth.PantallaCodigoRecuperarContrasena
import com.example.koalm.ui.screens.auth.PantallaGustosUsuario
import com.example.koalm.ui.screens.auth.PantallaIniciarSesion
import com.example.koalm.ui.screens.auth.PantallaPersonalizarPerfil
import com.example.koalm.ui.screens.auth.PantallaRecuperarContrasena
import com.example.koalm.ui.screens.auth.PantallaRegistro
import com.example.koalm.ui.screens.auth.PantallaRestablecerContrasena
/* Quitar comentario cuando se reemplacen las pantallas
import com.example.koalm.ui.screens.estaditicas.PantallaEstadísticasSaludMental
import com.example.koalm.ui.screens.estaditicas.PantallaEstadisticasSaludFisica
 */
import com.example.koalm.ui.screens.estaditicas.PantallaEstadisticasHabitoPersonalizado
import com.example.koalm.ui.screens.habitos.saludMental.PantallaSaludMental
import com.example.koalm.ui.screens.habitos.saludFisica.PantallaSaludFisica

import com.example.koalm.ui.screens.habitos.saludFisica.PantallaConfiguracionHabitoAlimentacion
import com.example.koalm.ui.screens.habitos.saludMental.PantallaConfiguracionHabitoEscritura
import com.example.koalm.ui.screens.habitos.saludFisica.PantallaConfiguracionHabitoHidratacion
import com.example.koalm.ui.screens.habitos.saludMental.PantallaConfiguracionHabitoLectura
import com.example.koalm.ui.screens.habitos.saludMental.PantallaConfiguracionHabitoMeditacion
import com.example.koalm.ui.screens.habitos.saludFisica.PantallaConfiguracionHabitoSueno
import com.example.koalm.ui.screens.habitos.personalizados.PantallaConfigurarHabitoPersonalizado
import com.example.koalm.ui.screens.habitos.personalizados.PantallaGestionHabitosPersonalizados
import com.example.koalm.ui.screens.habitos.PantallaHabitos
import com.example.koalm.ui.screens.habitos.saludMental.PantallaLibros
import com.example.koalm.ui.screens.PantallaMenuPrincipal
import com.example.koalm.ui.screens.habitos.racha.PantallaRachaHabitos
import com.example.koalm.ui.screens.habitos.saludMental.*
import com.example.koalm.ui.screens.habitos.saludMental.PantallaTemporizadorMeditacion
import com.example.koalm.ui.screens.parametroSalud.niveles.actividadDiaria.PantallaActividadDiaria
import com.example.koalm.ui.screens.parametroSalud.niveles.peso.PantallaActualizarPeso
import com.example.koalm.ui.screens.parametroSalud.niveles.estres.PantallaEstres
import com.example.koalm.ui.screens.parametroSalud.niveles.actividadDiaria.PantallaMetaCalorias
import com.example.koalm.ui.screens.parametroSalud.niveles.actividadDiaria.PantallaMetaMovimiento
import com.example.koalm.ui.screens.parametroSalud.niveles.actividadDiaria.PantallaMetaPasos
import com.example.koalm.ui.screens.habitos.saludMental.PantallaNotas
import com.example.koalm.ui.screens.parametroSalud.niveles.peso.PantallaObjetivosPeso
import com.example.koalm.ui.screens.parametroSalud.PantallaParametrosSalud
import com.example.koalm.ui.screens.parametroSalud.niveles.peso.PantallaControlPeso
import com.example.koalm.ui.screens.parametroSalud.niveles.ritmoCardiaco.PantallaRitmoCardiaco
import com.example.koalm.ui.screens.parametroSalud.niveles.sueno.PantallaSueno
import com.example.koalm.ui.screens.tests.PantallaTestAnsiedad
import com.example.koalm.ui.screens.ajustes.PantallaCambiarContrasena
import com.example.koalm.ui.screens.ajustes.PantallaTyC
import com.example.koalm.ui.screens.ajustes.PantallaPrivacidad
import com.example.koalm.ui.screens.ajustes.PantallaNosotros

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
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
        screenWithSlide("configurar_habito_alimentacion") { PantallaConfiguracionHabitoAlimentacion(navController) }
        screenWithSlide("configurar_habito_lectura") { PantallaConfiguracionHabitoLectura(navController) }
        screenWithSlide("estadisticas") { PantallaParametrosSalud(navController) }
        screenWithSlide("gestion_habitos_personalizados") { PantallaGestionHabitosPersonalizados(navController) }
        screenWithSlide("configurar_habito_personalizado") { PantallaConfigurarHabitoPersonalizado(navController) }
        composable(
            "configurar_habito_personalizado/{nombreHabitoEditar}",
            arguments = listOf(navArgument("nombreHabitoEditar") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) { backStackEntry ->
            PantallaConfigurarHabitoPersonalizado(
                navController = navController,
                nombreHabitoEditar = backStackEntry.arguments?.getString("nombreHabitoEditar")
                )
        }
        screenWithSlide("configurar_habito_desconexion_digital") { PantallaConfigurarDesconexionDigital(navController) }
       // screenWithSlide("estadisticas_salud_mental") { PantallaEstadísticasSaludMental(navController) }
        //screenWithSlide("estadisticas_salud_fisica") { PantallaEstadisticasSaludFisica(navController) }
        screenWithSlide("notas") { PantallaNotas(navController) }
        screenWithSlide("libros") { PantallaLibros(navController) }
        screenWithSlide("ritmo-cardiaco") { PantallaRitmoCardiaco(navController) }
        screenWithSlide("sueño-de-anoche") { PantallaSueno(navController) }
        screenWithSlide("nivel-de-estres") { PantallaEstres(navController) }
        screenWithSlide("objetivos-peso") { PantallaObjetivosPeso(navController) }
        screenWithSlide("actividad-diaria") { PantallaActividadDiaria(navController) }
        screenWithSlide("objetivos-peso") { PantallaObjetivosPeso(navController) }
        screenWithSlide("control-peso") { PantallaControlPeso(navController) }
        screenWithSlide("actualizar-peso") { PantallaActualizarPeso(navController) }
        screenWithSlide("meta-diaria-pasos") { PantallaMetaPasos(navController) }
        screenWithSlide("meta-diaria-movimiento") { PantallaMetaMovimiento(navController) }
        screenWithSlide("meta-diaria-calorias") { PantallaMetaCalorias(navController) }
        screenWithSlide("racha_habitos") { PantallaRachaHabitos(navController) }
        screenWithSlide("test_de_ansiedad") { PantallaTestAnsiedad(navController) }
        screenWithSlide("estadisticas_habito_perzonalizado") { PantallaEstadisticasHabitoPersonalizado(navController)}
        screenWithSlide("cambiar_contrasena") { PantallaCambiarContrasena(navController)}
        screenWithSlide("TyC") { PantallaTyC(navController)}
        screenWithSlide("privacidad") { PantallaPrivacidad(navController)}
        screenWithSlide("nosotros") { PantallaNosotros(navController)}

        composable(
            route = "temporizador_meditacion/{duracion}",
            arguments = listOf(navArgument("duracion") {
                type = NavType.IntType
                defaultValue = 15
            })
        ) { backStackEntry ->
            val duracion = backStackEntry.arguments?.getInt("duracion") ?: 15
            PantallaTemporizadorMeditacion(navController, duracion)
        }
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