package com.example.koalm.model

data class HabitosPredeterminados(
    val id: String = "",
    val titulo: String = "",
    val descripcion: String = "",
    val clase: ClaseHabito = ClaseHabito.MENTAL,
    val tipo: TipoHabito = TipoHabito.ESCRITURA,
    val diasSeleccionados: List<Boolean> = List(7) { false },
    val hora: String = "", // Formato "HH:mm"
    val duracionMinutos: Int = 15,
    val notasHabilitadas: Boolean = false,
    val userId: String? = null,
    val fechaCreacion: String? = null,
    val fechaModificacion: String? = null,
    var rachaActual: Int = 0,
    var rachaMaxima: Int = 0,
    var ultimoDiaCompletado: String? = null
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "titulo" to titulo,
        "descripcion" to descripcion,
        "clase" to clase.name,
        "tipo" to tipo.name,
        "diasSeleccionados" to diasSeleccionados,
        "hora" to hora,
        "duracionMinutos" to duracionMinutos,
        "notasHabilitadas" to notasHabilitadas,
        "userId" to userId,
        "fechaCreacion" to fechaCreacion,
        "fechaModificacion" to fechaModificacion,
        "rachaActual" to rachaActual,
        "rachaMaxima" to rachaMaxima,
        "ultimoDiaCompletado" to ultimoDiaCompletado
    )
}

enum class ClaseHabito {
    MENTAL,
    FISICO,
    PERSONALIZADO
}

enum class TipoHabito {
    LECTURA,
    MEDITACION,
    DESCONEXION_DIGITAL,
    ESCRITURA,
    SUEÑO,
    ALIMENTACION,
    HIDRATACION
}



