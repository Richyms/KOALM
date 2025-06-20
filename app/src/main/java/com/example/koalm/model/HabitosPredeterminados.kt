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
    val userId: String? = null,
    val fechaCreacion: String? = null,
    val fechaModificacion: String? = null,
    var rachaActual: Int = 0,
    var rachaMaxima: Int = 0,
    var ultimoDiaCompletado: String? = null,
    // Métricas específicas por tipo de hábito
    val metricasEspecificas: MetricasHabito = MetricasHabito(),
    // Objetivos específicos por tipo de hábito
    val objetivoPaginas: Int = 0, // Objetivo de páginas para hábitos de escritura/lectura
    val objetivoHorasSueno: Int = 0 // Objetivo de horas para hábitos de sueño
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
        "userId" to userId,
        "fechaCreacion" to fechaCreacion,
        "fechaModificacion" to fechaModificacion,
        "rachaActual" to rachaActual,
        "rachaMaxima" to rachaMaxima,
        "ultimoDiaCompletado" to ultimoDiaCompletado,
        "metricasEspecificas" to metricasEspecificas.toMap(),
        "objetivoPaginas" to objetivoPaginas,
        "objetivoHorasSueno" to objetivoHorasSueno
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

data class MetricasHabito(
    // Métricas para hábitos de escritura
    val paginasEscritas: Int = 0,
    val palabrasEscritas: Int = 0,
    
    // Métricas para hábitos de lectura
    val paginasLeidas: Int = 0,
    val minutosLeidos: Int = 0,
    
    // Métricas para hábitos de meditación
    val minutosMeditados: Int = 0,
    val sesionesCompletadas: Int = 0,
    
    // Métricas para hábitos de desconexión digital
    val minutosDesconectado: Int = 0,
    val vecesDesbloqueado: Int = 0,
    
    // Métricas para hábitos de sueño
    val horasDormidas: Float = 0f,
    val calidadSueno: Int = 0, // 1-5
    
    // Métricas para hábitos de alimentación
    val comidasCompletadas: Int = 0,
    val caloriasConsumidas: Int = 0,
    
    // Métricas para hábitos de hidratación
    val vasosAgua: Int = 0,
    val mililitrosBebidos: Int = 0
) {
    fun toMap(): Map<String, Any> = mapOf(
        "paginasEscritas" to paginasEscritas,
        "palabrasEscritas" to palabrasEscritas,
        "paginasLeidas" to paginasLeidas,
        "minutosLeidos" to minutosLeidos,
        "minutosMeditados" to minutosMeditados,
        "sesionesCompletadas" to sesionesCompletadas,
        "minutosDesconectado" to minutosDesconectado,
        "vecesDesbloqueado" to vecesDesbloqueado,
        "horasDormidas" to horasDormidas,
        "calidadSueno" to calidadSueno,
        "comidasCompletadas" to comidasCompletadas,
        "caloriasConsumidas" to caloriasConsumidas,
        "vasosAgua" to vasosAgua,
        "mililitrosBebidos" to mililitrosBebidos
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




