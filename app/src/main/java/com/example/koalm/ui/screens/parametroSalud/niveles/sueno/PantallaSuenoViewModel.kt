package com.example.koalm.ui.screens.parametroSalud.niveles.sueno

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.koalm.model.Habito
import com.example.koalm.model.ClaseHabito
import com.example.koalm.model.TipoHabito
import com.example.koalm.ui.components.DatosSueno
import com.example.koalm.ui.components.DiaSueno
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

class PantallaSuenoViewModel : ViewModel() {
    private val TAG = "PantallaSuenoViewModel"
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    var datosSueno by mutableStateOf<DatosSueno?>(null)
        private set

    init {
        Log.d(TAG, "Inicializando ViewModel")
        cargarDatosSueno()
    }

    private fun cargarDatosSueno() {
        viewModelScope.launch {
            try {
                val userId = auth.currentUser?.uid
                Log.d(TAG, "UserID: $userId")
                if (userId == null) {
                    Log.e(TAG, "Usuario no autenticado")
                    return@launch
                }

                val fechaHoy = LocalDate.now()
                val fechaHace7Dias = fechaHoy.minusDays(6)
                Log.d(TAG, "Buscando datos desde $fechaHace7Dias hasta $fechaHoy")

                // Obtener hábitos de sueño
                val habitos = db.collection("habitos")
                    .whereEqualTo("userId", userId)
                    .whereEqualTo("tipo", "SUEÑO")
                    .get()
                    .await()

                Log.d(TAG, "Número de hábitos encontrados: ${habitos.documents.size}")

                // Mapa para almacenar la suma de minutos por día de la semana (0-6)
                val minutosPorDia = mutableMapOf<Int, Int>()
                
                // Procesar todos los hábitos
                for (doc in habitos.documents) {
                    try {
                        val data = doc.data
                        Log.d(TAG, "Procesando documento: ${doc.id}")
                        Log.d(TAG, "Datos del documento: $data")

                        val duracionMinutos = (data?.get("duracionMinutos") as? Number)?.toInt() ?: 0
                        val diasSeleccionados = (data?.get("diasSeleccionados") as? List<*>)?.map { it as? Boolean ?: false } ?: List(7) { false }

                        // Distribuir los minutos en los días seleccionados
                        diasSeleccionados.forEachIndexed { index, seleccionado ->
                            if (seleccionado) {
                                val minutosActuales = minutosPorDia[index] ?: 0
                                minutosPorDia[index] = minutosActuales + duracionMinutos
                                Log.d(TAG, "Día $index: Sumando $duracionMinutos minutos a $minutosActuales. Total: ${minutosPorDia[index]}")
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error procesando documento: ${e.message}")
                    }
                }

                // Crear el historial semanal y calcular el total
                val historialSemanal = mutableListOf<DiaSueno>()
                var minutosSemanaTotales = 0

                // Procesar cada día de la semana
                for (i in 0..6) {
                    val minutosDelDia = minutosPorDia[i] ?: 0
                    minutosSemanaTotales += minutosDelDia
                    // Convertir minutos a horas con decimales para la gráfica
                    val horasDelDia = minutosDelDia / 60f
                    historialSemanal.add(DiaSueno(horasDelDia))
                    Log.d(TAG, "Día $i: $minutosDelDia minutos = $horasDelDia horas")
                }

                // Calcular puntos basados en el promedio diario
                val horasPromedioDiarias = minutosSemanaTotales / 7f / 60f
                val puntosTotales = when {
                    horasPromedioDiarias >= 8f -> 100
                    horasPromedioDiarias >= 7f -> 80
                    else -> 60
                }

                // Convertir los minutos totales a horas y minutos exactos
                val horasTotal = minutosSemanaTotales / 60
                val minutosRestantes = minutosSemanaTotales % 60

                Log.d(TAG, "Total semanal: $minutosSemanaTotales minutos = $horasTotal horas y $minutosRestantes minutos")

                datosSueno = DatosSueno(
                    puntos = puntosTotales,
                    fecha = fechaHoy.format(DateTimeFormatter.ISO_DATE),
                    horas = horasTotal,
                    minutos = minutosRestantes,
                    duracionHoras = minutosSemanaTotales / 60f,
                    historialSemanal = historialSemanal
                )
                Log.d(TAG, "DatosSueno creado exitosamente: $datosSueno")

            } catch (e: Exception) {
                Log.e(TAG, "Error al cargar datos de sueño: ${e.message}")
                e.printStackTrace()
            }
        }
    }
}