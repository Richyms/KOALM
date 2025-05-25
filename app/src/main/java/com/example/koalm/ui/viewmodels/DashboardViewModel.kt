package com.example.koalm.ui.viewmodels

import android.util.Log
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.koalm.data.HabitosRepository
import com.example.koalm.model.HabitoPersonalizado
import com.example.koalm.model.HabitosPredeterminados
import com.example.koalm.model.ProgresoDiario
import com.example.koalm.repository.HabitoRepository
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class DashboardViewModel : ViewModel() {

    //Para hábitos personalizados
    var habitos by mutableStateOf<List<HabitoPersonalizado>>(emptyList())
        private set
    //Para hábitos predeterminados
    var habitosPre by mutableStateOf<List<HabitosPredeterminados>>(emptyList())
        private set

    var progresos by mutableStateOf<Map<String, ProgresoDiario>>(emptyMap())
        private set

    var progresosPre by mutableStateOf<Map<String, ProgresoDiario>>(emptyMap())
        private set

    var cargando by mutableStateOf(true)
        private set

    fun cargarHabitos(email: String, userId: String) {
        viewModelScope.launch {
            try {
                Log.d("DashboardViewModel", "Iniciando carga de hábitos para email: $email, userId: $userId")
                cargando = true

                // Cargar hábitos personalizados y sus progresos
                Log.d("DashboardViewModel", "Cargando hábitos personalizados...")
                val habitosCargados = HabitosRepository.obtenerHabitosPersonalizados(email)
                Log.d("DashboardViewModel", "Hábitos personalizados cargados: ${habitosCargados.size}")
                habitos = habitosCargados

                Log.d("DashboardViewModel", "Cargando progresos de hábitos personalizados...")
                cargarProgresos(email, habitosCargados)
                Log.d("DashboardViewModel", "Progresos de hábitos personalizados cargados")

                // Cargar hábitos predeterminados y sus progresos
                Log.d("DashboardViewModel", "Cargando hábitos predeterminados...")
                val resultadoHabitosPre = HabitoRepository().obtenerHabitosActivos(userId)
                if (resultadoHabitosPre.isSuccess) {
                    val habitosPreCargados = resultadoHabitosPre.getOrNull() ?: emptyList()
                    Log.d("DashboardViewModel", "Hábitos predeterminados cargados: ${habitosPreCargados.size}")
                    habitosPre = habitosPreCargados

                    Log.d("DashboardViewModel", "Cargando progresos de hábitos predeterminados...")
                    cargarProgresosPre(email, habitosPreCargados)
                    Log.d("DashboardViewModel", "Progresos de hábitos predeterminados cargados")
                } else {
                    Log.e("DashboardViewModel", "Error al obtener hábitos predeterminados: ${resultadoHabitosPre.exceptionOrNull()?.message}")
                    habitosPre = emptyList()
                }
            } catch (e: Exception) {
                Log.e("DashboardViewModel", "Error al cargar hábitos: ${e.message}", e)
                Log.e("DashboardViewModel", "Stack trace: ${e.stackTraceToString()}")
                habitos = emptyList()
                habitosPre = emptyList()
                progresos = emptyMap()
                progresosPre = emptyMap()
            } finally {
                Log.d("DashboardViewModel", "Finalizando carga de hábitos")
                cargando = false
            }
        }
    }

    private suspend fun cargarProgresos(email: String, habitos: List<HabitoPersonalizado>) {
        try {
            Log.d("DashboardViewModel", "Iniciando carga de progresos personalizados")
            val progresosMap = mutableMapOf<String, ProgresoDiario>()
            for (habito in habitos) {
                try {
                    Log.d("DashboardViewModel", "Cargando progreso para hábito: ${habito.nombre}")
                    val ref = obtenerProgresoRef(email, habito)
                    val progreso = obtenerProgresoDelDia(ref)
                    if (progreso != null) {
                        val habitoId = habito.nombre.replace(" ", "_")
                        progresosMap[habitoId] = progreso
                        Log.d("DashboardViewModel", "Progreso cargado para hábito: ${habito.nombre}")
                    }
                } catch (e: Exception) {
                    Log.e("DashboardViewModel", "Error al cargar progreso para hábito ${habito.nombre}: ${e.message}")
                }
            }
            progresos = progresosMap
            Log.d("DashboardViewModel", "Finalizada carga de progresos personalizados")
        } catch (e: Exception) {
            Log.e("DashboardViewModel", "Error general al cargar progresos personalizados: ${e.message}")
        }
    }

    private suspend fun cargarProgresosPre(email: String, habitos: List<HabitosPredeterminados>) {
        try {
            Log.d("DashboardViewModel", "Iniciando carga de progresos predeterminados")
            val progresosMap = mutableMapOf<String, ProgresoDiario>()
            for (habito in habitos) {
                try {
                    Log.d("DashboardViewModel", "Cargando progreso para hábito predeterminado: ${habito.titulo}")
                    val ref = obtenerProgresoRefPre(email, habito)
                    val progreso = obtenerProgresoDelDia(ref)
                    if (progreso != null) {
                        val habitoId = habito.id
                        progresosMap[habitoId] = progreso
                        Log.d("DashboardViewModel", "Progreso cargado para hábito predeterminado: ${habito.titulo}")
                    }
                } catch (e: Exception) {
                    Log.e("DashboardViewModel", "Error al cargar progreso para hábito predeterminado ${habito.titulo}: ${e.message}")
                }
            }
            progresosPre = progresosMap
            Log.d("DashboardViewModel", "Finalizada carga de progresos predeterminados")
        } catch (e: Exception) {
            Log.e("DashboardViewModel", "Error general al cargar progresos predeterminados: ${e.message}")
        }
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

    fun incrementarProgresoPre(email: String, habito: HabitosPredeterminados) {
        viewModelScope.launch {
            try {
                HabitoRepository.incrementarProgresoHabito(email, habito)
                cargarProgresosPre(email, habitosPre)
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

    private fun obtenerProgresoRefPre(email: String, habito: HabitosPredeterminados): DocumentReference {
        return FirebaseFirestore.getInstance()
            .collection("habitos")
            .document(email)
            .collection("predeterminados")
            .document(habito.id)
            .collection("progreso")
            .document(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
    }

    private suspend fun obtenerProgresoDelDia(ref: DocumentReference): ProgresoDiario? {
        val snapshot = ref.get().await()
        return snapshot.toObject(ProgresoDiario::class.java)
    }
}