package com.example.koalm.model

data class Usuario(
    val imagenBase64: String? = null,
    val userId: String?       = null,
    val email: String?        = null,
    val username: String?     = null,
    val nombre: String?       = null,
    val apellido: String?     = null,
    val nacimiento: String?   = null,
    val peso: Float?          = null,
    val altura: Int?          = null,
    val genero: String?       = null
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "imagenBase64" to imagenBase64,
        "userId"       to userId,
        "email"        to email,
        "username"     to username,
        "nombre"       to nombre,
        "apellido"     to apellido,
        "nacimiento"   to nacimiento,
        "peso"         to peso,
        "altura"       to altura,
        "genero"       to genero
    ).filterValues { it != null } // Elimina campos con null para que no se guarden en Firebase
}