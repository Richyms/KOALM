package com.example.koalm.ui.viewmodels

import android.util.Log
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.koalm.data.HabitosRepository
import com.example.koalm.model.HabitoPersonalizado
import com.example.koalm.model.Habito
import com.example.koalm.model.ProgresoDiario
import com.example.koalm.repository.HabitoRepository
import com.example.koalm.ui.screens.habitos.personalizados.desactivarHabito
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
    var habitosPre by mutableStateOf<List<Habito>>(emptyList())
        private set

    var progresos by mutableStateOf<Map<String, ProgresoDiario>>(emptyMap())
        private set

    var progresosPre by mutableStateOf<Map<String, ProgresoDiario>>(emptyMap())
        private set

    var cargando by mutableStateOf(true)
        private set

    var rachaSemanal by mutableStateOf<List<Pair<String, Boolean>>>(emptyList())
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
                val hoy = LocalDate.now().toString()

                // Marcar como inactivos los hábitos que ya finalizaron
                val habitosActualizados = habitosCargados.map { habito ->
                    if (habito.fechaFin == hoy && habito.estaActivo) {
                        desactivarHabito(habito, email)
                        habito.copy(estaActivo = false)
                    } else {
                        habito
                    }
                }
                habitos = habitosActualizados

                Log.d("DashboardViewModel", "Cargando progresos de hábitos personalizados...")
                cargarProgresos(email, habitosActualizados)
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
                cargarRachaSemanal(email)

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

    private suspend fun cargarProgresosPre(email: String, habitos: List<Habito>) {
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

    fun incrementarProgreso(email: String, habito: HabitoPersonalizado, valor: Int) {
        viewModelScope.launch {
            try {
                HabitosRepository.incrementarProgresoHabito(email, habito, valor)
                cargarProgresos(email, habitos)
                cargarRachaSemanal(email)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun incrementarProgresoPre(email: String, habito: Habito, valor: Int) {
        viewModelScope.launch {
            try {
                HabitoRepository.incrementarProgresoHabito(email, habito, valor)
                cargarProgresosPre(email, habitosPre)
                cargarRachaSemanal(email)
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

    private fun obtenerProgresoRefPre(email: String, habito: Habito): DocumentReference {
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

    private fun obtenerFechasSemanaConLetras(): List<Pair<String, String>> {
        val hoy = LocalDate.now()
        val lunes = hoy.minusDays(((hoy.dayOfWeek.value + 6) % 7).toLong())
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val letras = listOf("L", "M", "X", "J", "V", "S", "D")

        return (0..6).map { i ->
            val dia = lunes.plusDays(i.toLong())
            letras[i] to dia.format(formatter)
        }
    }

    private fun cargarRachaSemanal(email: String) {
        viewModelScope.launch {
            try {
                val db = FirebaseFirestore.getInstance()
                val dias = obtenerFechasSemanaConLetras() // Lista (letra, fecha)
                val resultado = mutableListOf<Pair<String, Boolean>>()

                for ((index, diaData) in dias.withIndex()) {
                    val (letraDia, fecha) = diaData
                    val progresosConFrecuenciaActiva = mutableListOf<Boolean>()

                    // PERSONALIZADOS
                    for (habito in habitos) {
                        val doc = db.collection("habitos").document(email)
                            .collection("personalizados").document(habito.nombre.replace(" ", "_"))
                            .collection("progreso").document(fecha)

                        val snap = doc.get().await()
                        val progreso = snap.toObject(ProgresoDiario::class.java)

                        if (progreso != null && progreso.frecuencia?.getOrNull(index) == true) {
                            progresosConFrecuenciaActiva.add(progreso.completado)
                        } else if (progreso == null) {
                            // No hay progreso, pero revisamos si debió haber
                            val refActual = db.collection("habitos").document(email)
                                .collection("personalizados").document(habito.nombre.replace(" ", "_"))
                                .get().await()

                            val frecuencia = refActual.get("frecuencia") as? List<Boolean>
                            if (frecuencia?.getOrNull(index) == true) {
                                progresosConFrecuenciaActiva.add(false) // Esperado pero no hecho
                            }
                        }
                    }

                    // PREDETERMINADOS
                    for (habito in habitosPre) {
                        val doc = db.collection("habitos").document(email)
                            .collection("predeterminados").document(habito.id)
                            .collection("progreso").document(fecha)

                        val snap = doc.get().await()
                        val progreso = snap.toObject(ProgresoDiario::class.java)

                        if (progreso != null && progreso.frecuencia?.getOrNull(index) == true) {
                            progresosConFrecuenciaActiva.add(progreso.completado)
                        } else if (progreso == null) {
                            val refActual = db.collection("habitos").document(email)
                                .collection("predeterminados").document(habito.id)
                                .get().await()

                            val frecuencia = refActual.get("frecuencia") as? List<Boolean>
                            if (frecuencia?.getOrNull(index) == true) {
                                progresosConFrecuenciaActiva.add(false)
                            }
                        }
                    }

                    if (progresosConFrecuenciaActiva.isNotEmpty()) {
                        val diaCompletado = progresosConFrecuenciaActiva.all { it }
                        resultado.add(letraDia to diaCompletado)
                    }
                    // Si nada tenía frecuencia activa ese día, no se muestra
                }

                rachaSemanal = resultado
            } catch (e: Exception) {
                Log.e("DashboardViewModel", "Error al cargar racha semanal: ${e.message}")
            }
        }
    }
}