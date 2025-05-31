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

class ObjetivosPesoViewModel : ViewModel() {
    private val db = Firebase.firestore
    private val usuario = FirebaseAuth.getInstance().currentUser?.email

    private val _pesoInicial = MutableStateFlow(0f)
    val pesoInicial: StateFlow<Float> = _pesoInicial

    private val _fechaInicial = MutableStateFlow("")
    val fechaInicial: StateFlow<String> = _fechaInicial

    private val _pesoActual = MutableStateFlow(0f)
    val pesoActual: StateFlow<Float> = _pesoActual

    private val _fechaActual = MutableStateFlow("")
    val fechaActual: StateFlow<String> = _fechaActual

    private val _pesoObjetivo = MutableStateFlow(0f)
    val pesoObjetivo: StateFlow<Float> = _pesoObjetivo

    init {
        viewModelScope.launch {
            if (usuario == null) return@launch
            val ref = db.collection("usuarios")
                .document(usuario)
                .collection("metasSalud")
                .document("valores")
            val snap = ref.get().await()

            // Carga peso actual y fecha
            val actual = snap.getDouble("pesoActual")?.toFloat() ?: 0f
            _pesoActual.value = actual
            _fechaActual.value = snap.getString("fechaMedicion") ?: ""

            // Carga o inicializa peso inicial y fecha
            val inicial = snap.getDouble("pesoInicial")?.toFloat()
            val fechaIni = snap.getString("fechaInicial")
            if (inicial == null || fechaIni == null) {
                val hoy = LocalDate.now()
                    .format(DateTimeFormatter.ofPattern("d MMMM, yyyy", Locale("es", "MX")))
                ref.update(
                    mapOf(
                        "pesoInicial" to actual,
                        "fechaInicial" to hoy
                    )
                ).await()
                _pesoInicial.value = actual
                _fechaInicial.value = hoy
            } else {
                _pesoInicial.value = inicial
                _fechaInicial.value = fechaIni
            }

            // Carga peso objetivo
            _pesoObjetivo.value = snap.getDouble("pesoObjetivo")?.toFloat() ?: 0f
        }
    }

    /** Actualiza en Firestore sólo el campo pesoObjetivo */
    fun guardarObjetivo(onDone: () -> Unit) = viewModelScope.launch {
        if (usuario == null) return@launch
        val ref = db.collection("usuarios")
            .document(usuario)
            .collection("metasSalud")
            .document("valores")
        ref.update("pesoObjetivo", _pesoObjetivo.value).await()
        onDone()
    }

    /** Cambia sólo el estado local de pesoObjetivo */
    fun setObjetivo(nuevo: Float) {
        _pesoObjetivo.value = nuevo
    }
}
