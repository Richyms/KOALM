package com.example.koalm.data

import android.annotation.SuppressLint
import android.util.Log
import com.example.koalm.model.HabitoPersonalizado
import com.example.koalm.model.ProgresoDiario
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

object HabitosRepository {

    @SuppressLint("StaticFieldLeak")
    private val db = FirebaseFirestore.getInstance()

    // Obtener hábitos personalizados
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

    // Obtener progreso del hábito
    suspend fun obtenerProgresoDelHabito(usuarioEmail: String, nombreHabito: String): ProgresoDiario? {
        val progresoRef = db.collection("usuarios")
            .document(usuarioEmail)
            .collection("personalizados")
            .document(nombreHabito)
            .collection("progreso")
            .document("progresoDelDia")

        val doc = progresoRef.get().await()
        return doc.toObject(ProgresoDiario::class.java)
    }

    // Actualizar progreso del hábito
    suspend fun incrementarProgresoHabito(email: String, habito: HabitoPersonalizado) {
        val idDocumento = habito.nombre.replace(" ", "_")
        val docRef = db.collection("usuarios")
            .document(email)
            .collection("personalizados")
            .document(idDocumento) // Suponiendo que el nombre es único

        // Obtener el progreso actual
        val progreso = obtenerProgresoDelHabito(email, idDocumento)

        // Si el progreso no existe, lo inicializamos
        val progresoActualizado = progreso ?: ProgresoDiario(realizados = 0, completado = false, totalRecordatoriosPorDia = habito.recordatorios?.horas?.size ?: 0)

        // Si el progreso no está completado y aún hay espacio, incrementamos
        if (progresoActualizado.realizados < progresoActualizado.totalRecordatoriosPorDia) {
            progresoActualizado.realizados += 1
            progresoActualizado.completado = progresoActualizado.realizados >= progresoActualizado.totalRecordatoriosPorDia
        }

        // Actualizar racha
        actualizarRacha(habito, progresoActualizado)

        // Subir datos actualizados
        docRef.collection("progreso").document("progresoDelDia")
            .set(progresoActualizado, SetOptions.merge())
            .await()

        Log.d("Firestore", "Progreso actualizado para el hábito: ${idDocumento}")
    }

    private fun actualizarRacha(habito: HabitoPersonalizado, progreso: ProgresoDiario) {
        val fechaHoy = java.time.LocalDate.now()
        val ultimoDia = habito.ultimoDiaCompletado?.let { java.time.LocalDate.parse(it) }

        // Si el progreso fue completado hoy
        if (progreso.completado) {
            if (ultimoDia == null || !ultimoDia.plusDays(1).isEqual(fechaHoy)) {
                habito.rachaActual = 1
            } else {
                habito.rachaActual += 1
            }

            // Actualizar la racha máxima
            if (habito.rachaActual > habito.rachaMaxima) {
                habito.rachaMaxima = habito.rachaActual
            }

            habito.ultimoDiaCompletado = fechaHoy.toString()
        } else {
            habito.rachaActual = 0
        }
    }
}