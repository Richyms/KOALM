package com.example.koalm.model


import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class Habito(

    val id: String = "",
    val titulo: String = "",
    val descripcion: String = "",
    val clase: ClaseHabito = ClaseHabito.MENTAL,
    val tipo: TipoHabito = TipoHabito.ESCRITURA,
    val diasSeleccionados: List<Boolean> = List(7) { false },
    val hora: String = "", // Hora principal (formato "HH:mm a")
    val horarios: List<String> = emptyList(), // Para hábitos con múltiples horarios como alimentación
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
        "horarios" to horarios,
        "duracionMinutos" to duracionMinutos,
        "notasHabilitadas" to notasHabilitadas,
        "userId" to userId,
        "fechaCreacion" to fechaCreacion,
        "fechaModificacion" to fechaModificacion,
        "rachaActual" to rachaActual,
        "rachaMaxima" to rachaMaxima,
        "ultimoDiaCompletado" to ultimoDiaCompletado
    )

    companion object {
        fun crearNuevoHabitoAlimentacion(
            userId: String,
            descripcion: String,
            horarios: List<String>
        ): Habito {
            return Habito(
                titulo = "Alimentación",
                descripcion = descripcion,
                clase = ClaseHabito.FISICO,
                tipo = TipoHabito.ALIMENTACION,
                horarios = horarios,
                hora = horarios.firstOrNull() ?: "",
                diasSeleccionados = List(7) { true }, // Todos los días por defecto
                duracionMinutos = 30, // 30 minutos por comida
                userId = userId,
                fechaCreacion = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME),
                fechaModificacion = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)
            )
        }
    }
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




