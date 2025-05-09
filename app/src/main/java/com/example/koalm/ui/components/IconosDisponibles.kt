package com.example.koalm.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*

val iconosDisponibles = listOf(
    "favorite" to Icons.Default.Favorite,
    "fitness_center" to Icons.Default.FitnessCenter,
    "self_improvement" to Icons.Default.SelfImprovement,
    "spa" to Icons.Default.Spa,
    "accessibility_new" to Icons.Default.AccessibilityNew,
    "monitor_heart" to Icons.Default.MonitorHeart,

    "book" to Icons.Default.Book,
    "menu_book" to Icons.AutoMirrored.Filled.MenuBook,
    "school" to Icons.Default.School,
    "lightbulb" to Icons.Default.Lightbulb,

    "work" to Icons.Default.Work,
    "check" to Icons.Default.Check,
    "task" to Icons.Default.Task,
    "event" to Icons.Default.Event,
    "schedule" to Icons.Default.Schedule,
    "list" to Icons.AutoMirrored.Filled.List,

    "phone" to Icons.Default.Phone,
    "email" to Icons.Default.Email,
    "notifications" to Icons.Default.Notifications,
    "smartphone" to Icons.Default.Smartphone,
    "devices" to Icons.Default.Devices,

    "home" to Icons.Default.Home,
    "bedtime" to Icons.Default.Bedtime,
    "kitchen" to Icons.Default.Kitchen,
    "wb_sunny" to Icons.Default.WbSunny,
    "shopping_cart" to Icons.Default.ShoppingCart,

    "face" to Icons.Default.Face,
    "mood" to Icons.Default.Mood,
    "emoji_emotions" to Icons.Default.EmojiEmotions,
    "thumb_up" to Icons.Default.ThumbUp,
    "people" to Icons.Default.People,

    "star" to Icons.Default.Star,
    "music_note" to Icons.Default.MusicNote,
    "pets" to Icons.Default.Pets,
    "explore" to Icons.Default.Explore,
    "travel_explore" to Icons.Default.TravelExplore,
    "directions_run" to Icons.AutoMirrored.Filled.DirectionsRun
)

// Función para obtener el icono a partir del nombre guardado en Firebase
fun obtenerIconoPorNombre(nombre: String): ImageVector {
    return iconosDisponibles.firstOrNull { it.first == nombre }?.second ?: Icons.Default.Favorite
}

// Composable del AlertDialog
@Composable
fun SelectorDeIconoDialog(
    iconoSeleccionadoNombre: String,
    onSeleccionar: (String) -> Unit,
    onCerrar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCerrar,
        title = { Text("Selecciona un icono") },
        text = {
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier
                    .height(200.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                userScrollEnabled = true
            ) {
                items(iconosDisponibles) { (nombre, icono) ->
                    IconButton(
                        onClick = {
                            onSeleccionar(nombre)
                            onCerrar()
                        },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = icono,
                            contentDescription = nombre,
                            tint = if (nombre == iconoSeleccionadoNombre) Color.Green else Color.Gray
                        )
                    }
                }
            }
        },
        confirmButton = {}
    )
}