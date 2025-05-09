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
                    habitosList.add(habito)
                }
            }
        } catch (e: Exception) {
            Log.e("Firestore", "Error al obtener hábitos: ${e.message}")
        }
        return habitosList
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
            totalRecordatoriosPorDia = total
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

    // Actualizar racha del hábito
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