// ActividadDiariaViewModel.kt
package com.example.koalm.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.koalm.ui.screens.parametroSalud.niveles.actividadDiaria.ActividadDiaria
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ActividadDiariaViewModel : ViewModel() {

    private val _actividades = MutableStateFlow<List<ActividadDiaria>>(emptyList())
    val actividades: StateFlow<List<ActividadDiaria>> = _actividades

    // Valores por defecto; se pueden actualizar desde Firestore si el documento existe
    private var metaPasos = 8000f
    private var metaCalorias = 500f
    private var metaTiempo = 180f

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    init {
        cargarActividades()
    }

    private fun cargarActividades() {
        viewModelScope.launch {
            val usuario = auth.currentUser ?: return@launch
            val correo = usuario.email ?: return@launch

            // 1) (OPCIONAL) Leer metas desde Firestore (si existen en "usuarios/{correo}/metasSalud/valores")
            try {
                val docMeta = firestore
                    .collection("usuarios")
                    .document(correo)
                    .collection("metasSalud")
                    .document("valores")
                    .get()
                    .await()
                if (docMeta.exists()) {
                    docMeta.getLong("metaPasos")?.let { metaPasos = it.toFloat() }
                    docMeta.getLong("metaCalorias")?.let { metaCalorias = it.toFloat() }
                    docMeta.getLong("metaTiempo")?.let { metaTiempo = it.toFloat() }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // 2) Construir la lista de 7 días exactos: de lunes a domingo de la semana actual
            try {
                val hoy = LocalDate.now()
                // Obtener el lunes de la semana actual
                val lunesSemana = hoy.with(DayOfWeek.MONDAY)
                // Domingo = lunes + 6 días
                val domingoSemana = lunesSemana.plusDays(6)

                // Rango de IDs (fechas) que queremos de Firestore:
                //   desde "lunesSemana" hasta "domingoSemana" (ambos inclusive)
                val formateador = DateTimeFormatter.ISO_LOCAL_DATE
                val idLunes  = lunesSemana.format(formateador)   // ej. "2025-05-26"
                val idDomingo = domingoSemana.format(formateador) // ej. "2025-06-01"

                // 2.1) Hacemos UNA consulta para traer todos los documentos
                //     cuya clave (document ID) esté entre lunesSemana y domingoSemana (lexicográficamente).
                val snapshot = firestore
                    .collection("usuarios")
                    .document(correo)
                    .collection("metricasDiarias")
                    .orderBy("__name__", Query.Direction.ASCENDING)
                    .whereGreaterThanOrEqualTo("__name__", idLunes)
                    .whereLessThanOrEqualTo("__name__", idDomingo)
                    .get()
                    .await()

                // 2.2) Creamos un mapa: fechaString → DocumentSnapshot
                val mapDocsPorFecha = snapshot.documents.associateBy { it.id }
                //    Ejemplo: { "2025-05-26" → docQueTienePasos, "2025-05-28" → otroDoc, ... }

                // 2.3) Iteramos exactamente 7 veces (i=0..6) para cada día de la semana:
                val listaPasos = mutableListOf<Float>()
                val listaCalorias = mutableListOf<Float>()
                val listaTiempo = mutableListOf<Float>()

                for (i in 0..6) {
                    val fechaIterada = lunesSemana.plusDays(i.toLong())
                    val claveFecha    = fechaIterada.format(formateador) // ej. "2025-05-26", etc.

                    if (mapDocsPorFecha.containsKey(claveFecha)) {
                        // El documento existe → leemos sus campos
                        val doc = mapDocsPorFecha[claveFecha]!!
                        val pasosDoc     = doc.getLong("pasos")?.toFloat() ?: 0f
                        val caloriasDoc  = doc.getLong("calorias")?.toFloat() ?: 0f
                        val tiempoDoc    = doc.getLong("tiempoActividad")?.toFloat() ?: 0f

                        listaPasos.add(pasosDoc)
                        listaCalorias.add(caloriasDoc)
                        listaTiempo.add(tiempoDoc)
                    } else {
                        // No hay documento para esa fecha → asignamos 0f
                        listaPasos.add(0f)
                        listaCalorias.add(0f)
                        listaTiempo.add(0f)
                    }
                }

                // 3) Creamos la lista de ActividadDiaria, sabiendo que
                //    índice 0 = lunes, índice 1 = martes, …, índice 6 = domingo
                val actividadesFirebase = listOf(
                    ActividadDiaria(tipo = "Pasos", meta = metaPasos, datos = listaPasos),
                    ActividadDiaria(tipo = "Calorías quemadas", meta = metaCalorias, datos = listaCalorias),
                    ActividadDiaria(tipo = "Tiempo activo", meta = metaTiempo, datos = listaTiempo)
                )

                _actividades.value = actividadesFirebase

            } catch (e: Exception) {
                e.printStackTrace()
                _actividades.value = emptyList()
            }
        }
    }
}
