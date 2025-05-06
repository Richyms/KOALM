package com.example.koalm.model

data class ProgresoDiario(
    var realizados: Int = 0,                    // Cuántas veces se ha realizado el hábito en el día
    var completado: Boolean = false,            // Si el hábito está completado o no en el día
    val totalRecordatoriosPorDia: Int = 0           // Número total de veces que debe completarse en un día, dinámico
){
    fun toMap(): Map<String, Any> = mapOf(
        "realizados" to realizados,
        "completado" to completado,
        "totalRecordatoriosPorDia" to totalRecordatoriosPorDia
    )
}



