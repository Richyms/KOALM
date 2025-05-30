package com.example.koalm.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class PesoViewModel : ViewModel() {
    private val db = Firebase.firestore
    private val correo = FirebaseAuth.getInstance().currentUser?.email

    // Estado público para UI
    private val _peso = MutableStateFlow(0f)
    val peso: StateFlow<Float> = _peso

    private val _fecha = MutableStateFlow(
        LocalDate.now().format(DateTimeFormatter.ofPattern("d MMMM, yyyy", Locale("es", "MX")))
    )
    val fecha: StateFlow<String> = _fecha

    init {
        cargarPeso()
    }

    /** Carga desde Firestore `pesoActual` y `fechaMedicion`. */
    fun cargarPeso() {
        viewModelScope.launch {
            if (correo == null) return@launch

            val docValores = db
                .collection("usuarios")
                .document(correo)
                .collection("metasSalud")
                .document("valores")
                .get()
                .await()

            // weight
            _peso.value = docValores
                .getDouble("pesoActual")
                ?.toFloat()
                ?: 0f

            // date
            docValores.getString("fechaMedicion")?.let { fechaStr ->
                _fecha.value = fechaStr
            }
        }
    }

    /**
     * Actualiza en Firestore:
     *  - `/usuarios/{correo}` → campo "peso" y opcionalmente "pesoFecha"
     *  - `/usuarios/{correo}/metasSalud/valores` → campos "pesoActual" y "fechaMedicion"
     */
    fun actualizarPeso(
        nuevoPeso: Float,
        nuevaFecha: String,
        onFinish: () -> Unit
    ) {
        viewModelScope.launch {
            if (correo == null) return@launch

            // 1) documento raíz
            db.collection("usuarios")
                .document(correo)
                .update(
                    mapOf(
                        "peso" to nuevoPeso,
                        "pesoFecha" to nuevaFecha
                    )
                )
                .await()

            // 2) subdocumento valores
            db.collection("usuarios")
                .document(correo)
                .collection("metasSalud")
                .document("valores")
                .update(
                    mapOf(
                        "pesoActual" to nuevoPeso,
                        "fechaMedicion" to nuevaFecha
                    )
                )
                .await()

            // refrescar estado local
            _peso.value = nuevoPeso
            _fecha.value = nuevaFecha

            onFinish()
        }
    }
}
