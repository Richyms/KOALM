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
    background = Blanco,
    onBackground = Color.Black,
    surface = Blanco,
    onSurface = Color.Black
)

// Paleta oscura (opcional, podrías personalizarla)
private val DarkColorScheme = darkColorScheme(
    primary = VerdePrincipal,
    onPrimary = Color.White,
    background = Color.Black,
    onBackground = Color.White,
    surface = Color.Black,
    onSurface = Color.White
)



