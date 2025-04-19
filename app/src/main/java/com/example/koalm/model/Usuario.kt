package com.example.koalm.model

data class Usuario(
    val id: String?       = null,
    val userId: String?   = null,
    val email: String?    = null,
    val username: String? = null,
    val nombre: String?       = null,
    val apellido: String?     = null,
    val nacimiento: String?   = null,
    val peso: Int?            = null,
    val altura: Int?          = null,
    val genero: String?       = null

) {
    fun toMap(): Map<String, Any> = mapOf(
        "id"         to (id        ?: ""),
        "userId"     to (userId    ?: ""),
        "email"      to (email     ?: ""),
        "username"   to (username  ?: ""),
        "nombre"     to (nombre    ?: ""),
        "apellido"   to (apellido  ?: ""),
        "nacimiento" to (nacimiento ?: ""),
        "peso"       to (peso      ?: 0),
        "altura"     to (altura    ?: 0),
        "genero"     to (genero    ?: "")   
    )
}
