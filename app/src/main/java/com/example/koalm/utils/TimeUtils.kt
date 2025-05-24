package com.example.koalm.utils

object TimeUtils {
    fun formatearDuracion(minutos: Int): String = when {
        minutos < 60 -> "${minutos} min"
        minutos == 60 -> "1 hora"
        minutos % 60 == 0 -> "${minutos/60} h"
        else -> "${minutos/60} h ${minutos%60} min"
    }
} 