package com.example.koalm.ui.viewmodels

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.koalm.data.HabitosRepository.obtenerHabitosPersonalizados
import com.example.koalm.model.HabitoPersonalizado
import com.example.koalm.model.ProgresoDiario
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class DashboardViewModel : ViewModel() {

    var habitos by mutableStateOf<List<HabitoPersonalizado>>(emptyList())
        private set

    var cargando by mutableStateOf(true)
        private set

    fun cargarHabitos(email: String) {
        viewModelScope.launch {
            cargando = true
            habitos = obtenerHabitosPersonalizados(email)
            cargando = false
        }
    }

    fun incrementarProgreso(email: String, habito: HabitoPersonalizado) {
        viewModelScope.launch {
            // Obtenemos la referencia al documento de progreso del hábito
            val progresoRef = obtenerProgresoRef(email, habito)

            try {
                val progreso = obtenerProgresoDelDia(progresoRef)

                if (habito.unaVezPorHabito == 1) {
                    // Si el hábito es "una vez por día", comprobamos si ya está completado
                    if (progreso?.completado == true) return@launch // No hacer nada si ya está completado

                    // Marcar como completado si no lo está
                    progreso?.realizados = 1
                    progreso?.completado = true
                    progreso?.let {
                        actualizarProgresoEnFirestore(progresoRef, it)
                    }
                } else {
                    // Si el hábito tiene más de una vez por día, incrementamos el contador de "realizados"
                    progreso?.realizados = (progreso?.realizados ?: 0) + 1

                    // Comprobamos si el progreso está completo (completado si los realizados son suficientes)
                    progreso?.completado = progreso?.realizados ?: 0 >= (habito.recordatorios?.horas?.size ?: 0)
                    progreso?.let {
                        actualizarProgresoEnFirestore(progresoRef, it)
                    }
                }

                // Recargamos los hábitos después de actualizar el progreso
                cargarHabitos(email)
            } catch (e: Exception) {
                // Manejo de errores (en caso de que no se pueda obtener el progreso o actualizar)
                e.printStackTrace()
            }
        }
    }

    private fun obtenerProgresoRef(email: String, habito: HabitoPersonalizado) =
        FirebaseFirestore.getInstance()
            .collection("usuarios")
            .document(email)
            .collection("personalizados")
            .document(habito.nombre.replace(" ", "_")) // Usamos el nombre del hábito con guiones bajos como ID
            .collection("progreso")
            .document("progresoDelDia") // Aquí se guarda el progreso diario

    private suspend fun obtenerProgresoDelDia(progresoRef: DocumentReference): ProgresoDiario? {
        val doc = progresoRef.get().await()
        return doc.toObject(ProgresoDiario::class.java)
    }

    private suspend fun actualizarProgresoEnFirestore(progresoRef: DocumentReference, progreso: ProgresoDiario) {
        progresoRef.set(progreso).await()
    }
}