package com.example.koalm.model

data class HabitoPredeterminado(
    val nombre: String = "",
    val categoria: String = "",                    // Ej: "sueño", "meditacion", etc.
    val descripcion: String = "",
    val icono: String = "",
    val color: String = "",
    val parametros: Map<String, Any>? = null,      // Campos específicos para cada tipo
    val tipo: String = ""                 // "fisico" o "mental"
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "nombre" to nombre,
        "categoria" to categoria,
        "descripcion" to descripcion,
        "icono" to icono,
        "color" to color,
        "parametros" to parametros,
        "tipo" to tipo
    )
}
