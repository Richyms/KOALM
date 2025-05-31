package com.example.koalm.model

import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class HabitoPersonalizado(
    val nombre: String = "",
    val colorEtiqueta: String = "",
    val iconoEtiqueta: String = "",
    val descripcion: String = "",
    val frecuencia: List<Boolean>? = null,          // Ej: ["true", "false"] (los 7 días)
    val recordatorios: Recordatorios? = null,       // Subcolección
    val fechaInicio: String? = null,
    val fechaFin: String? = null,
    val modoFin: String? = null,                    // "calendario" o "dias"
    var rachaActual: Int = 0,
    var rachaMaxima: Int = 0,
    var ultimoDiaCompletado: String? = null,
    val objetivoDiario: Int = 0,
    val clase: ClaseHabito = ClaseHabito.PERSONALIZADO
) {

    // Metodo para convertir a un mapa, útil para Firestore u otros usos
    fun toMap(): Map<String, Any?> = mapOf(
        "nombre" to nombre,
        "colorEtiqueta" to colorEtiqueta,
        "iconoEtiqueta" to iconoEtiqueta,
        "descripcion" to descripcion,
        "frecuencia" to frecuencia,
        "recordatorios" to recordatorios?.toMap(),
        "fechaInicio" to fechaInicio,
        "fechaFin" to fechaFin,
        "modoFin" to modoFin,
        "rachaActual" to rachaActual,
        "rachaMaxima" to rachaMaxima,
        "ultimoDiaCompletado" to ultimoDiaCompletado,
        "objetivoDiario" to objetivoDiario,
        "clase" to clase.name
    )

    companion object {
        // Metodo para calcular la fecha de inicio
        fun calcularFechaInicio(): String {
            return LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        }

        // Metodo para calcular la fecha de fin dependiendo de si es "calendario" o "días"
        fun calcularFechaFin(modoFecha: Boolean, fechaSeleccionada: String?, diasDuracion: String): String? {
            return if (modoFecha) {
                fechaSeleccionada  // Si se eligió una fecha específica
            } else {
                diasDuracion.let {
                    val fechaFin = LocalDate.now().plusDays(it.toLong()) // Sumar días a la fecha actual
                    fechaFin.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                }
            }
        }

        // Función para generar recordatorios automáticos
        fun generarHorasAutomaticas(): List<String> {
            return listOf("08:00", "14:00", "20:00") // Predefinidos: Mañana, tarde, noche
        }

    }
}
