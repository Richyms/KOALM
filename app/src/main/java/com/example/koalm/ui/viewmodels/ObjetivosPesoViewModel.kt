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
    private val usuarioEmail = FirebaseAuth.getInstance().currentUser?.email

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
            if (usuarioEmail == null) return@launch

            // 1) Leer pesoInicial y fechaInicial desde /usuarios/{email}
            var pesoDesdeUser = 0f
            var fechaCreacionDesdeUser = ""
            try {
                val userDoc = db.collection("usuarios")
                    .document(usuarioEmail)
                    .get()
                    .await()

                if (userDoc.exists()) {
                    pesoDesdeUser = userDoc.getDouble("peso")?.toFloat() ?: 0f
                    fechaCreacionDesdeUser = userDoc.getString("fechaCreacion") ?: ""
                }
            } catch (e: Exception) {
                // Si hay error, dejamos pesoDesdeUser=0f, fechaCreacionDesdeUser=""
            }

            // Asignar siempre pesoInicial y fechaInicial desde /usuarios
            _pesoInicial.value = pesoDesdeUser
            _fechaInicial.value = fechaCreacionDesdeUser

            // 2) Leer pesoActual, fechaActual y pesoObjetivo desde metasSalud/valores
            try {
                val refMetas = db.collection("usuarios")
                    .document(usuarioEmail)
                    .collection("metasSalud")
                    .document("valores")

                val snapMetas = refMetas.get().await()
                if (snapMetas.exists()) {
                    // Peso actual y fecha de medición actual
                    _pesoActual.value = snapMetas.getDouble("pesoActual")?.toFloat() ?: pesoDesdeUser
                    _fechaActual.value = snapMetas.getString("fechaMedicion") ?: fechaCreacionDesdeUser

                    // Si no hay pesoInicial o fechaInicial en metasSalud, inicializarlos
                    val inicialDesdeMetas = snapMetas.getDouble("pesoInicial")?.toFloat()
                    val fechaIniDesdeMetas = snapMetas.getString("fechaInicial")
                    if (inicialDesdeMetas == null || fechaIniDesdeMetas == null) {
                        // Formato "d MMMM, yyyy" en español, p. ej. "31 mayo, 2025"
                        val hoy = LocalDate.now()
                            .format(DateTimeFormatter.ofPattern("d MMMM, yyyy", Locale("es", "MX")))
                        refMetas.update(
                            mapOf(
                                "pesoInicial" to pesoDesdeUser,
                                "fechaInicial" to fechaCreacionDesdeUser.ifEmpty { hoy }
                            )
                        ).await()
                        // El _pesoInicial ya se asignó desde /usuarios
                    }

                    // Carga peso objetivo
                    _pesoObjetivo.value = snapMetas.getDouble("pesoObjetivo")?.toFloat() ?: 0f
                } else {
                    // Si no existe documento en metasSalud, crearlo con pesoInicial/fechaInicial
                    val hoy = LocalDate.now()
                        .format(DateTimeFormatter.ofPattern("d MMMM, yyyy", Locale("es", "MX")))
                    refMetas.set(
                        mapOf(
                            "pesoInicial" to pesoDesdeUser,
                            "fechaInicial" to fechaCreacionDesdeUser.ifEmpty { hoy },
                            "pesoActual" to pesoDesdeUser,
                            "fechaMedicion" to fechaCreacionDesdeUser.ifEmpty { hoy },
                            "pesoObjetivo" to 0f
                        )
                    ).await()
                    _pesoActual.value = pesoDesdeUser
                    _fechaActual.value = fechaCreacionDesdeUser.ifEmpty { hoy }
                    _pesoObjetivo.value = 0f
                }
            } catch (e: Exception) {
                // En caso de error, dejamos pesoActual/fechaActual/pesoObjetivo en sus valores actuales
            }
        }
    }

    /** Actualiza en Firestore sólo el campo pesoObjetivo */
    fun guardarObjetivo(onDone: () -> Unit) = viewModelScope.launch {
        if (usuarioEmail == null) return@launch

        val refMetas = db.collection("usuarios")
            .document(usuarioEmail)
            .collection("metasSalud")
            .document("valores")

        refMetas.update("pesoObjetivo", _pesoObjetivo.value).await()
        onDone()
    }

    /** Cambia sólo el estado local de pesoObjetivo */
    fun setObjetivo(nuevo: Float) {
        _pesoObjetivo.value = nuevo
    }
}
