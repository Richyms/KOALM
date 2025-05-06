package com.example.koalm.model

data class Recordatorios(
    val tipo: String = "automatico",           // "automatico" o "personalizado"
    val horas: List<String>? = null            // Ej: ["08:00", "14:00", "20:00"]
) {

    fun toMap(): Map<String, Any?> = mapOf(
        "tipo" to tipo,
        "horas" to horas
    )
}
