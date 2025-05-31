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
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ActividadDiariaViewModel : ViewModel() {

    private val _actividades = MutableStateFlow<List<ActividadDiaria>>(emptyList())
    val actividades: StateFlow<List<ActividadDiaria>> = _actividades

    // Si quieres sacar las metas de Firestore, cambialo aquí. Sino deja valores hardcodeados:
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
            val usuario = auth.currentUser
            if (usuario == null) {
                // No hay usuario logueado, no cargamos nada
                return@launch
            }
            val correo = usuario.email ?: return@launch

            // *** 1) (Opcional) Leer metas desde Firestore, si es que existe esa colección/documento ***
            // Supongamos que tienes un documento "metasSalud" con campos: metaPasos, metaCalorias, metaTiempo
            try {
                val docMeta = firestore
                    .collection("usuarios")
                    .document(correo)
                    .collection("metasSalud")
                    .document("valores")  // o sólo .document("metasSalud")
                    .get()
                    .await()
                if (docMeta.exists()) {
                    // Ajusta el nombre de los campos según tengas en Firestore
                    docMeta.getLong("metaPasos")?.let { metaPasos = it.toFloat() }
                    docMeta.getLong("metaCalorias")?.let { metaCalorias = it.toFloat() }
                    docMeta.getLong("metaTiempo")?.let { metaTiempo = it.toFloat() }
                }
            } catch (e: Exception) {
                // Si algo falla, mantenemos los valores por defecto
                e.printStackTrace()
            }

            // *** 2) Leer la sub-colección "metricasDiarias" y ordenar por ID de documento (YYYY-MM-DD) ***
            try {
                // Suponemos que cada documento en "metricasDiarias" tiene ID = "2025-05-28", "2025-05-27", etc.
                val refMetricas = firestore
                    .collection("usuarios")
                    .document(correo)
                    .collection("metricasDiarias")

                // Traer todos los documentos (o podrías limitar con .orderBy("fechaTimestamp", Query.Direction.DESCENDING).limit(7))
                val snapshot = refMetricas
                    .orderBy("__name__", Query.Direction.ASCENDING) // ordena por nombre de doc (lexicográfico)
                    .get()
                    .await()

                // Extraer pareja (fechaString, DocumentSnapshot)
                val docsConFecha = snapshot.documents.mapNotNull { doc ->
                    val fechaId = doc.id // ej. "2025-05-25"
                    // Validar que el ID sea una fecha válida
                    try {
                        // Sólo para comprobar que es parseable si usas DateTimeFormatter.ISO_LOCAL_DATE
                        LocalDate.parse(fechaId, DateTimeFormatter.ISO_LOCAL_DATE)
                        fechaId to doc
                    } catch (e: Exception) {
                        null
                    }
                }

                // Tomar los últimos 7 elementos (si hay menos, llenar con 7 últimos o con ceros)
                val ultimosDocs = if (docsConFecha.size <= 7) {
                    docsConFecha
                } else {
                    docsConFecha.takeLast(7)
                }

                // Crear listas temporales para pasos, calorías y tiempo (Long o Float)
                val listaPasos = mutableListOf<Float>()
                val listaCalorias = mutableListOf<Float>()
                val listaTiempo = mutableListOf<Float>()

                // Si hay menos de 7 días, rellenamos con 0f al principio para que tenga longitud 7
                val faltantes = 7 - ultimosDocs.size
                repeat(faltantes) {
                    listaPasos.add(0f)
                    listaCalorias.add(0f)
                    listaTiempo.add(0f)
                }

                // Ahora agregamos los valores de cada documento en orden cronológico
                for ((_, doc) in ultimosDocs) {
                    val pasosDoc = doc.getLong("pasos")?.toFloat() ?: 0f
                    val caloriasDoc = doc.getLong("calorias")?.toFloat() ?: 0f
                    val tiempoDoc = doc.getLong("tiempoActividad")?.toFloat() ?: 0f

                    listaPasos.add(pasosDoc)
                    listaCalorias.add(caloriasDoc)
                    listaTiempo.add(tiempoDoc)
                }

                // *** 3) Construir la lista de ActividadDiaria ***
                val actividadesFirebase = listOf(
                    ActividadDiaria(tipo = "Pasos", meta = metaPasos, datos = listaPasos),
                    ActividadDiaria(tipo = "Calorías quemadas", meta = metaCalorias, datos = listaCalorias),
                    ActividadDiaria(tipo = "Tiempo activo", meta = metaTiempo, datos = listaTiempo)
                )

                // Publicar en el StateFlow
                _actividades.value = actividadesFirebase

            } catch (e: Exception) {
                e.printStackTrace()
                // Si falla, podrías dejar el arreglo vacío o con algún fallback
                _actividades.value = emptyList()
            }
        }
    }
}
