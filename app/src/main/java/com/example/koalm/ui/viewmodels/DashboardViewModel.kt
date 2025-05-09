package com.example.koalm.ui.viewmodels

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.koalm.data.HabitosRepository
import com.example.koalm.model.HabitoPersonalizado
import com.example.koalm.model.ProgresoDiario
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class DashboardViewModel : ViewModel() {

    var habitos by mutableStateOf<List<HabitoPersonalizado>>(emptyList())
        private set

    var progresos by mutableStateOf<Map<String, ProgresoDiario>>(emptyMap())
        private set

    var cargando by mutableStateOf(true)
        private set

    fun cargarHabitos(email: String) {
        viewModelScope.launch {
            cargando = true
            val habitosCargados = HabitosRepository.obtenerHabitosPersonalizados(email)
            habitos = habitosCargados
            cargarProgresos(email, habitosCargados)
            cargando = false
        }
    }

    private suspend fun cargarProgresos(email: String, habitos: List<HabitoPersonalizado>) {
        val progresosMap = mutableMapOf<String, ProgresoDiario>()
        for (habito in habitos) {
            val ref = obtenerProgresoRef(email, habito)
            val progreso = obtenerProgresoDelDia(ref)
            if (progreso != null) {
                val habitoId = habito.nombre.replace(" ", "_")
                progresosMap[habitoId] = progreso
            }
        }
        progresos = progresosMap
    }

    fun incrementarProgreso(email: String, habito: HabitoPersonalizado) {
        viewModelScope.launch {
            try {
                HabitosRepository.incrementarProgresoHabito(email, habito)
                cargarProgresos(email, habitos)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun obtenerProgresoRef(email: String, habito: HabitoPersonalizado): DocumentReference {
        return FirebaseFirestore.getInstance()
            .collection("habitos")
            .document(email)
            .collection("personalizados")
            .document(habito.nombre.replace(" ", "_"))
            .collection("progreso")
            .document(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
    }

    private suspend fun obtenerProgresoDelDia(ref: DocumentReference): ProgresoDiario? {
        val snapshot = ref.get().await()
        return snapshot.toObject(ProgresoDiario::class.java)
    }
}