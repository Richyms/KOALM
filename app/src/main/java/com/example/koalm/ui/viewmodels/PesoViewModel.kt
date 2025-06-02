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

    // 1) Estado público para UI : peso y fecha
    private val _peso = MutableStateFlow(0f)
    val peso: StateFlow<Float> = _peso

    private val formatoSalida = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale("es", "MX"))
    private val _fecha = MutableStateFlow(
        LocalDate.now().format(formatoSalida)
    )
    val fecha: StateFlow<String> = _fecha

    init {
        cargarPeso()
    }

    /**
     * Carga desde Firestore:
     * 1) peso y fechaCreacion de `/usuarios/{correo}` (primer registro)
     * 2) intenta leer `pesoActual` y `fechaMedicion` de `/usuarios/{correo}/metasSalud/valores`
     *    si existe, los usa; sino, deja el peso/fecha de `/usuarios/...`.
     */
    private fun cargarPeso() {
        viewModelScope.launch {
            if (correo == null) return@launch

            // (A) Leer peso y fechaCreacion desde documento principal /usuarios/{correo}
            val pesoDesdeUsuario: Float = try {
                val userDoc = db.collection("usuarios")
                    .document(correo)
                    .get()
                    .await()
                userDoc.getDouble("peso")?.toFloat() ?: 0f
            } catch (_: Exception) {
                0f
            }
            val fechaCreacionRaw: String = try {
                val userDoc = db.collection("usuarios")
                    .document(correo)
                    .get()
                    .await()
                userDoc.getString("fechaCreacion") ?: ""
            } catch (_: Exception) {
                ""
            }
            // Convertir fechaCreacionRaw a "dd/MM/yyyy", o usar hoy si no parsea
            val fechaInicialFormateada: String = try {
                val parsed = LocalDate.parse(fechaCreacionRaw, DateTimeFormatter.ISO_LOCAL_DATE)
                parsed.format(formatoSalida)
            } catch (_: Exception) {
                try {
                    val parsed2 = LocalDate.parse(fechaCreacionRaw, DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale("es", "MX")))
                    parsed2.format(formatoSalida)
                } catch (_: Exception) {
                    LocalDate.now().format(formatoSalida)
                }
            }

            // (B) Intentar leer metasalud/valores → pesoActual y fechaMedicion
            val refMetas = db.collection("usuarios")
                .document(correo)
                .collection("metasSalud")
                .document("valores")

            val metasDoc = try {
                refMetas.get().await()
            } catch (_: Exception) {
                null
            }

            val pesoDesdeMetas: Float? = metasDoc?.getDouble("pesoActual")?.toFloat()
            val fechaMedicionRaw: String? = metasDoc?.getString("fechaMedicion")

            // Determinar qué peso y fecha expongo a la UI:
            _peso.value = pesoDesdeMetas ?: pesoDesdeUsuario

            _fecha.value = if (!fechaMedicionRaw.isNullOrBlank()) {
                try {
                    val parsed = LocalDate.parse(fechaMedicionRaw, DateTimeFormatter.ISO_LOCAL_DATE)
                    parsed.format(formatoSalida)
                } catch (_: Exception) {
                    try {
                        val parsed2 = LocalDate.parse(fechaMedicionRaw, DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale("es", "MX")))
                        parsed2.format(formatoSalida)
                    } catch (_: Exception) {
                        fechaInicialFormateada
                    }
                }
            } else {
                fechaInicialFormateada
            }
        }
    }

    /**
     * Actualiza en Firestore:
     *  - `/usuarios/{correo}` → campo "peso" y "fechaCreacion" (solo si es el primer guardado)
     *  - `/usuarios/{correo}/metasSalud/valores` → campos "pesoActual" y "fechaMedicion"
     *
     * Si `nuevoPeso` es 0f (input vacío) y `_peso.value != 0f`, mantiene el valor anterior.
     * Siempre actualiza la fecha a hoy.
     */
    fun actualizarPeso(nuevoPeso: Float, onFinish: () -> Unit) {
        viewModelScope.launch {
            if (correo == null) return@launch

            // Si no ingresaron nada (nuevoPeso == 0f) y ya había un peso, mantenemos el anterior
            val pesoParaGuardar = if (nuevoPeso == 0f && _peso.value != 0f) {
                _peso.value
            } else {
                nuevoPeso
            }

            // Fecha de hoy en ISO (p.ej. "2025-05-31")
            val fechaHoyIso = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
            // Fecha de hoy formateada para UI ("dd/MM/yyyy")
            val fechaHoyFormateada = LocalDate.now().format(formatoSalida)

            // 1) Actualizar documento principal /usuarios/{correo} → "peso" y, si no existía "fechaCreacion", la guardamos
            try {
                // Intentamos leer el campo "fechaCreacion" actual:
                val userRef = db.collection("usuarios").document(correo)
                val userSnap = userRef.get().await()
                val creacionExistente = userSnap.getString("fechaCreacion")

                val updatesUsuario = mutableMapOf<String, Any>("peso" to pesoParaGuardar)
                if (creacionExistente.isNullOrBlank()) {
                    // Si no existía "fechaCreacion", la ponemos hoy
                    updatesUsuario["fechaCreacion"] = fechaHoyIso
                }
                userRef.update(updatesUsuario).await()
            } catch (_: Exception) {
                // Ignorar fallo en documento principal
            }

            // 2) Actualizar subdocumento /metasSalud/valores → "pesoActual" y "fechaMedicion"
            try {
                val refMetas = db.collection("usuarios")
                    .document(correo)
                    .collection("metasSalud")
                    .document("valores")

                // Si no existía aún este documento, se crea automáticamente con merge
                refMetas.set(
                    mapOf(
                        "pesoActual" to pesoParaGuardar,
                        "fechaMedicion" to fechaHoyIso
                    ), com.google.firebase.firestore.SetOptions.merge()
                ).await()
            } catch (_: Exception) {
                // Ignorar fallo en metasalud/valores
            }

            // 3) Refrescar estado local
            _peso.value = pesoParaGuardar
            _fecha.value = LocalDate.now().format(formatoSalida)

            onFinish()
        }
    }
}
