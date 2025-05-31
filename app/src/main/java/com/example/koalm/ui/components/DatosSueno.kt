package com.example.koalm.ui.components

data class DiaSueno(
    val duracionHoras: Float,
    val horaInicio: Int, // Hora de inicio real en formato 24h
    val horasObjetivo: Float // Horas objetivo configuradas
)

data class DatosSueno(
    val puntos: Int,
    val fecha: String,
    val horas: Int,
    val minutos: Int,
    val duracionHoras: Float,
    val historialSemanal: List<DiaSueno>
) 