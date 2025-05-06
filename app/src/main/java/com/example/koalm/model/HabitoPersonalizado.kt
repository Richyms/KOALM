package com.example.koalm.model

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import com.google.firebase.firestore.FirebaseFirestore

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
    val unaVezPorHabito: Int = 0,                   // cuando solo es una vez la que el usuario hace el hábito por día
    var rachaActual: Int = 0,
    var rachaMaxima: Int = 0,
    var ultimoDiaCompletado: String? = null,
    val tipo: String = "personalizado"
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
        "unaVezPorHabito" to unaVezPorHabito,
        "rachaActual" to rachaActual,
        "rachaMaxima" to rachaMaxima,
        "ultimoDiaCompletado" to ultimoDiaCompletado,
        "tipo" to tipo
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

        // Función para actualizar el progreso diario en Firestore
        fun actualizarProgresoDiario(
            email: String,          // Email del usuario
            habito: HabitoPersonalizado,
            completadoHoy: Boolean
        ) {
            val progresoRef = FirebaseFirestore.getInstance()
                .collection("habitos") // Asumiendo que habitos es la colección de hábitos
                .document(email) // El documento del usuario por email
                .collection("personalizados") // La subcolección personalizada dentro del usuario
                .document(habito.nombre.replace(" ", "_")) // ID del hábito
                .collection("progreso") // Subcolección de progreso
                .document(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))) // Progreso de hoy (usamos la fecha actual)

            progresoRef.get().addOnSuccessListener { document ->
                val progreso = document.toObject(ProgresoDiario::class.java) ?: ProgresoDiario()

                // Si el hábito fue completado hoy, incrementar los "realizados"
                if (completadoHoy) {
                    progreso.realizados += 1
                }

                // Si los realizados alcanzan el total de recordatorios por día, marcar como completado
                progreso.completado = progreso.realizados >= habito.unaVezPorHabito

                // Manejo de la racha actual
                val fechaHoy = LocalDate.now()
                val ultimoDia = habito.ultimoDiaCompletado?.let { LocalDate.parse(it) }

                if (progreso.completado) {
                    // Si el último día completado no es el día anterior, reiniciar la racha
                    if (ultimoDia == null || !ultimoDia.plusDays(1).isEqual(fechaHoy)) {
                        habito.rachaActual = 1
                    } else {
                        // Si es consecutivo, aumentar la racha
                        habito.rachaActual += 1
                    }

                    // Actualizar la racha máxima si la racha actual es mayor
                    if (habito.rachaActual > habito.rachaMaxima) {
                        habito.rachaMaxima = habito.rachaActual
                    }

                    // Actualizar el último día completado
                    habito.ultimoDiaCompletado = fechaHoy.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                } else {
                    // Si no completó el hábito hoy, reiniciar la racha
                    habito.rachaActual = 0
                }

                // Actualizar el documento de progreso de Firestore
                val progresoMap = progreso.toMap()

                // Aquí actualizamos los datos en Firestore
                progresoRef.set(progresoMap).addOnSuccessListener {
                    // Si el progreso se guardó correctamente, también podemos actualizar el hábito con la nueva racha, último día completado, etc.
                    val habitosRef = FirebaseFirestore.getInstance()
                        .collection("habitos")
                        .document(email)
                        .collection("personalizados")
                        .document(habito.nombre.replace(" ", "_"))

                    val habitosMap = mapOf(
                        "rachaActual" to habito.rachaActual,
                        "rachaMaxima" to habito.rachaMaxima,
                        "ultimoDiaCompletado" to habito.ultimoDiaCompletado
                    )

                    // Actualizar el documento del hábito con la nueva información
                    habitosRef.update(habitosMap)
                }
            }
        }

    }
}
