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
                val userEmail = auth.currentUser?.email
                Log.d(TAG, "UserID: $userId, Email: $userEmail")
                if (userId == null || userEmail == null) {
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
                if (habitos.documents.isEmpty()) {
                    Log.d(TAG, "No se encontraron hábitos de sueño")
                    return@launch
                }

                // Mapa para almacenar datos por día
                val horasObjetivoPorDia = mutableMapOf<Int, Float>()
                val horaInicioPorDia = mutableMapOf<Int, Int>()
                val horasRealesPorDia = mutableMapOf<Int, Float>()
                
                // Procesar configuración de hábitos
                for (doc in habitos.documents) {
                    try {
                        val data = doc.data ?: continue
                        Log.d(TAG, "Procesando hábito con ID: ${doc.id}")
                        Log.d(TAG, "Datos del hábito: $data")
                        
                        val diasSeleccionados = (data["diasSeleccionados"] as? List<*>)?.map { it as? Boolean ?: false } ?: continue
                        val horaStr = data["hora"] as? String ?: continue
                        val horaInicio = horaStr.split(":")[0].toInt()
                        val horasObjetivo = (data["objetivoHorasSueno"] as? Number)?.toFloat() ?: continue

                        Log.d(TAG, "Días seleccionados: $diasSeleccionados")
                        Log.d(TAG, "Hora inicio: $horaInicio")
                        Log.d(TAG, "Horas objetivo: $horasObjetivo")

                        // Guardar configuración para los días seleccionados
                        diasSeleccionados.forEachIndexed { index, seleccionado ->
                            if (seleccionado) {
                                horaInicioPorDia[index] = horaInicio
                                horasObjetivoPorDia[index] = horasObjetivo
                            }
                        }

                        // Obtener progreso real de los últimos 7 días
                        val habitoId = doc.id
                        for (i in 0..6) {
                            val fecha = fechaHace7Dias.plusDays(i.toLong())
                            val fechaStr = fecha.format(DateTimeFormatter.ISO_DATE)
                            val diaIndex = (fecha.dayOfWeek.value + 6) % 7 // Convertir a índice 0-6 empezando en lunes
                            
                            Log.d(TAG, "Buscando progreso para fecha: $fechaStr (día de semana: ${fecha.dayOfWeek}, índice: $diaIndex)")
                            
                            val progresoDoc = db.collection("habitos")
                                .document(userEmail)
                                .collection("predeterminados")
                                .document(habitoId)
                                .collection("progreso")
                                .document(fechaStr)
                                .get()
                                .await()

                            Log.d(TAG, "Ruta del documento: habitos/$userEmail/predeterminados/$habitoId/progreso/$fechaStr")
                            Log.d(TAG, "Documento existe: ${progresoDoc.exists()}")
                            if (progresoDoc.exists()) {
                                val datos = progresoDoc.data
                                Log.d(TAG, "Datos del progreso: $datos")
                                val realizados = progresoDoc.getLong("realizados") ?: 0L
                                Log.d(TAG, "Horas dormidas para $fechaStr: $realizados")
                                horasRealesPorDia[diaIndex] = realizados.toFloat()
                            } else {
                                Log.d(TAG, "No hay datos de progreso para $fechaStr")
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error procesando documento: ${e.message}")
                        e.printStackTrace()
                    }
                }

                // Crear el historial semanal
                val historialSemanal = mutableListOf<DiaSueno>()
                var horasTotales = 0f
                var minutosSemanaTotales = 0

                // Procesar cada día de la semana
                for (i in 0..6) {
                    val horasObjetivo = horasObjetivoPorDia[i]
                    val horaInicio = horaInicioPorDia[i]
                    val horasReales = horasRealesPorDia[i]

                    Log.d(TAG, "Día $i - Objetivo: $horasObjetivo, Inicio: $horaInicio, Real: $horasReales")

                    if (horasObjetivo != null && horaInicio != null) {
                        val horasDormidas = horasReales ?: 0f
                        historialSemanal.add(DiaSueno(
                            duracionHoras = horasDormidas,
                            horaInicio = horaInicio,
                            horasObjetivo = horasObjetivo
                        ))
                        horasTotales += horasDormidas
                        minutosSemanaTotales += (horasDormidas * 60).roundToInt()
                        Log.d(TAG, "Agregado al historial - Dormidas: $horasDormidas, Total acumulado: $horasTotales")
                    } else {
                        historialSemanal.add(DiaSueno(
                            duracionHoras = 0f,
                            horaInicio = 0,
                            horasObjetivo = 0f
                        ))
                        Log.d(TAG, "Día sin configuración, agregado con valores 0")
                    }
                }

                // Calcular puntos basados en el promedio diario
                val horasPromedioDiarias = horasTotales / 7f
                Log.d(TAG, "Promedio diario: $horasPromedioDiarias")
                val puntosTotales = when {
                    horasPromedioDiarias >= 8f -> 100
                    horasPromedioDiarias >= 7f -> 80
                    else -> 60
                }

                // Convertir los minutos totales a horas y minutos exactos
                val horasTotal = minutosSemanaTotales / 60
                val minutosRestantes = minutosSemanaTotales % 60

                Log.d(TAG, "Resumen final:")
                Log.d(TAG, "Total horas: $horasTotal")
                Log.d(TAG, "Total minutos restantes: $minutosRestantes")
                Log.d(TAG, "Puntos: $puntosTotales")
                Log.d(TAG, "Historial semanal: $historialSemanal")

                datosSueno = DatosSueno(
                    puntos = puntosTotales,
                    fecha = fechaHoy.format(DateTimeFormatter.ISO_DATE),
                    horas = horasTotal,
                    minutos = minutosRestantes,
                    duracionHoras = horasTotales,
                    historialSemanal = historialSemanal
                )

            } catch (e: Exception) {
                Log.e(TAG, "Error al cargar datos de sueño: ${e.message}")
                e.printStackTrace()
            }
        }
    }
}