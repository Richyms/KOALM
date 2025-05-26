package com.example.koalm.data

import android.annotation.SuppressLint
import android.util.Log
import com.example.koalm.model.HabitoPersonalizado
import com.example.koalm.model.ProgresoDiario
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

    // Incrementar el progreso de un hábito
    suspend fun incrementarProgresoHabito(email: String, habito: HabitoPersonalizado) {
        val idDocumento = habito.nombre.replace(" ", "_")
        val progresoRef = db.collection("habitos")
            .document(email)
            .collection("personalizados")
            .document(idDocumento)
            .collection("progreso")
            .document(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))

        val snapshot = progresoRef.get().await()
        val progresoActual = snapshot.toObject(ProgresoDiario::class.java)
        val total = habito.recordatorios?.horas?.size ?: 1

        // Inicializamos si no existía
        val nuevoProgreso = progresoActual ?: ProgresoDiario(
            realizados = 0,
            completado = false,
            totalRecordatoriosPorDia = total,
            fecha = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
            frecuencia = habito.frecuencia
        )

        // No hacer nada si ya estaba completo
        if (nuevoProgreso.completado) return

        nuevoProgreso.realizados += 1
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

    //Actualizar la racha con días activos
    private fun actualizarRacha(habito: HabitoPersonalizado, progreso: ProgresoDiario) {
        val hoy = LocalDate.now()
        val frecuencia = habito.frecuencia ?: List(7) { true }

        val diaDeLaSemanaHoy = (hoy.dayOfWeek.value + 6) % 7
        if (!frecuencia[diaDeLaSemanaHoy]) {
            Log.d("Racha", "Hoy no está en la frecuencia, no se actualiza racha.")
            return
        }

        val ultimoDia = habito.ultimoDiaCompletado?.let { LocalDate.parse(it) }
        Log.d("Racha", "Último día completado: $ultimoDia")

        if (ultimoDia == null) {
            habito.rachaActual = 1
            Log.d("Racha", "No había último día. Racha actual = 1")
        } else {
            // Verificamos si se rompió la racha ANTES de hoy
            var fechaEsperada = ultimoDia.plusDays(1)
            var rachaRota = false

            while (fechaEsperada.isBefore(hoy)) {
                val dia = (fechaEsperada.dayOfWeek.value + 6) % 7
                if (frecuencia[dia]) {
                    // Este día requería completarse pero no se hizo
                    rachaRota = true
                    break
                }
                fechaEsperada = fechaEsperada.plusDays(1)
            }

            if (rachaRota) {
                habito.rachaActual = 1
                Log.d("Racha", "Racha rota antes de hoy. Se reinicia a 1.")
            } else {
                // Si se mantiene la secuencia, aumentamos
                habito.rachaActual += 1
                Log.d("Racha", "Racha continúa. Nueva racha: ${habito.rachaActual}")
            }
        }

        if (habito.rachaActual > habito.rachaMaxima) {
            habito.rachaMaxima = habito.rachaActual
            Log.d("Racha", "Nueva racha máxima: ${habito.rachaMaxima}")
        }

        habito.ultimoDiaCompletado = hoy.toString()
    }
}