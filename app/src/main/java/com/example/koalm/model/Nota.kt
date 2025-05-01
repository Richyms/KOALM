package com.example.koalm.model

data class Nota(
    val id: String? = null,
    val titulo: String = "",
    val contenido: String = "",
    val userId: String? = null,
    val fechaCreacion: String? = null,
    val fechaModificacion: String? = null
) {
    fun toMap(): Map<String, Any> = mapOf(
        "id" to (id ?: ""),
        "titulo" to titulo,
        "contenido" to contenido,
        "userId" to (userId ?: ""),
        "fechaCreacion" to (fechaCreacion ?: ""),
        "fechaModificacion" to (fechaModificacion ?: "")
    )
} 