package com.example.koalm.model

data class Libro(
    val id: String = "",
    val titulo: String = "",
    val autor: String = "",
    val paginaActual: Int = 0,
    val calificacion: Int = 0, // 0-5 estrellas
    val terminado: Boolean = false,
    val userId: String? = null,
    val fechaCreacion: String? = null,
    val fechaModificacion: String? = null
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "titulo" to titulo,
        "autor" to autor,
        "paginaActual" to paginaActual,
        "calificacion" to calificacion,
        "terminado" to terminado,
        "userId" to userId,
        "fechaCreacion" to fechaCreacion,
        "fechaModificacion" to fechaModificacion
    )
} 