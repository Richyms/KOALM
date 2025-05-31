package com.example.koalm.model

data class ProgresoDiario(
    var realizados: Int = 0,                    // Cuántas veces se ha realizado el hábito en el día
    var completado: Boolean = false,            // Si el hábito está completado o no en el día
    var totalObjetivoDiario: Int = 0,           // Número total de veces que debe completarse en un día, dinámico
    var fecha: String = "",
    val frecuencia: List<Boolean>? = null
){
    fun toMap(): Map<String, Any?> = mapOf(
        "realizados" to realizados,
        "completado" to completado,
        "totalObjetivoDiario" to totalObjetivoDiario,
        "fecha" to fecha,
        "frecuencia" to frecuencia
    )
}



