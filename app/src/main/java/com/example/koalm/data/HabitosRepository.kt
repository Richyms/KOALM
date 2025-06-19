package com.example.koalm.data

import android.annotation.SuppressLint
import android.util.Log
import com.example.koalm.model.Habito
import com.example.koalm.model.HabitoPersonalizado
import com.example.koalm.model.ProgresoDiario
import com.example.koalm.model.TipoHabito
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object HabitosRepository {

    @SuppressLint("StaticFieldLeak")
    private val db = FirebaseFirestore.getInstance()

    // Obtener hábitos personalizados del usuario
    suspend fun obtenerHabitosPersonalizados(usuarioEmail: String): List<HabitoPersonalizado> {
        val habitosList = mutableListOf<HabitoPersonalizado>()
        try {
            val snapshot = db.collection("habitos")
                .document(usuarioEmail)
                .collection("personalizados")
                .get()
                .await()

            for (document in snapshot.documents) {
                val habito = document.toObject(HabitoPersonalizado::class.java)
                if (habito != null) {
                    // Verificar si se rompió la racha antes de agregarlo a la lista
                    val rachaRota = verificarYRomperRachaSiCorresponde(habito)
                    if (rachaRota) {
                        // Guardar el cambio en Firestore
                        val idDocumento = habito.nombre.replace(" ", "_")
                        db.collection("habitos")
                            .document(usuarioEmail)
                            .collection("personalizados")
                            .document(idDocumento)
                            .set(habito, SetOptions.merge())
                            .await()
                    }

                    habitosList.add(habito)
                }
            }
        } catch (e: Exception) {
            Log.e("Firestore", "Error al obtener hábitos: ${e.message}")
        }
        return habitosList
    }

    //Función auxiliar para los días pasivos del habito del usuario
    private fun verificarYRomperRachaSiCorresponde(habito: HabitoPersonalizado): Boolean {
        val hoy = LocalDate.now()
        val frecuencia = habito.frecuencia ?: List(7) { true }
        val ultimoDia = habito.ultimoDiaCompletado?.let { LocalDate.parse(it) } ?: return false

        var fechaEsperada = ultimoDia.plusDays(1)
        while (fechaEsperada.isBefore(hoy)) {
            val dia = (fechaEsperada.dayOfWeek.value + 6) % 7
            if (frecuencia[dia]) {
                habito.rachaActual = 0
                return true // Se rompió la racha y se debe guardar el cambio
            }
            fechaEsperada = fechaEsperada.plusDays(1)
        }
        return false // No se rompió la racha
    }

    // Incrementar el progreso de un hábito personalizado
    suspend fun incrementarProgresoHabito(email: String, habito: HabitoPersonalizado, valor: Int) {
        val idDocumento = habito.nombre.replace(" ", "_")
        val progresoRef = db.collection("habitos")
            .document(email)
            .collection("personalizados")
            .document(idDocumento)
            .collection("progreso")
            .document(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))

        val snapshot = progresoRef.get().await()
        val progresoActual = snapshot.toObject(ProgresoDiario::class.java)
        val total = habito.objetivoDiario

        // Inicializamos si no existía
        val nuevoProgreso = progresoActual ?: ProgresoDiario(
            realizados = 0,
            completado = false,
            totalObjetivoDiario = total,
            fecha = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
            frecuencia = habito.frecuencia
        )

        // No hacer nada si ya estaba completo
        if (nuevoProgreso.completado) return

        nuevoProgreso.realizados += valor
        nuevoProgreso.completado = nuevoProgreso.realizados >= total

        // Actualizamos la racha si se completó hoy
        if (nuevoProgreso.completado) {
            actualizarRacha(habito, nuevoProgreso)
        }

        // Guardamos progreso
        progresoRef.set(nuevoProgreso, SetOptions.merge()).await()

        // Guardamos cambios del hábito (racha y último día)
        db.collection("habitos")
            .document(email)
            .collection("personalizados")
            .document(idDocumento)
            .set(habito, SetOptions.merge())
            .await()

        Log.d("Firestore", "Progreso actualizado para el hábito: $idDocumento")
    }

    suspend fun incrementarProgresoHabitoPre(email: String, habito: Habito, valor: Int) {
        val progresoRef = db.collection("habitos")
            .document(email)
            .collection("predeterminados")
            .document(habito.id)
            .collection("progreso")
            .document(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))

        val snapshot = progresoRef.get().await()
        val progresoActual = snapshot.toObject(ProgresoDiario::class.java)

        val totalObjetivo = when (habito.tipo) {
            TipoHabito.LECTURA, TipoHabito.ESCRITURA -> habito.objetivoPaginas
            TipoHabito.MEDITACION, TipoHabito.DESCONEXION_DIGITAL -> habito.duracionMinutos
            else -> 1
        }

        val realizadosPrevios = progresoActual?.realizados ?: 0
        val nuevosRealizados = (realizadosPrevios + valor).coerceAtMost(totalObjetivo)
        val completado = nuevosRealizados >= totalObjetivo

        val nuevoProgreso = ProgresoDiario(
            realizados = nuevosRealizados,
            completado = completado,
            totalObjetivoDiario = totalObjetivo,
            fecha = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
            frecuencia = habito.diasSeleccionados
        )

        progresoRef.set(nuevoProgreso).await()
    }



    //Actualizar la racha con días activos
    private fun actualizarRacha(habito: HabitoPersonalizado, progreso: ProgresoDiario) {
        val hoy = LocalDate.now()
        val ultimoDia = habito.ultimoDiaCompletado?.let { LocalDate.parse(it) }

        if (ultimoDia == null || !ultimoDia.plusDays(1).isEqual(hoy)) {
            habito.rachaActual = 1
        } else {
            habito.rachaActual += 1
        }

        if (habito.rachaActual > habito.rachaMaxima) {
            habito.rachaMaxima = habito.rachaActual
        }

        habito.ultimoDiaCompletado = hoy.toString()
    }
}