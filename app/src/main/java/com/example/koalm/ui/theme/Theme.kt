package com.example.koalm.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Paleta clara
private val LightColorScheme = lightColorScheme(
    primary = VerdePrincipal,
    onPrimary = Color.White,
    secondary = VerdeSecundario,
    onSecondary = Color.White,
    tertiary = VerdeClaro,
    onTertiary = Color.Black,
    background = Blanco,
    onBackground = Color.Black,
    surface = Blanco,
    onSurface = Color.Black,
    surfaceVariant = GrisClaro,
    onSurfaceVariant = Color.Black,
    surfaceTint = VerdePrincipal
)

// Paleta oscura
private val DarkColorScheme = darkColorScheme(
    primary = VerdePrincipal,
    onPrimary = Color.White,
    secondary = VerdeSecundario,
    onSecondary = Color.White,
    tertiary = VerdeClaro,
    onTertiary = Color.Black,
    background = FondoOscuro,
    onBackground = Color.White,
    surface = Color.Black,
    onSurface = Color.White,
    surfaceVariant = GrisClaro,
    onSurfaceVariant = Color.White,
    surfaceTint = VerdePrincipal
)

@Composable
fun KoalmTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}



