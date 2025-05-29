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
import android.util.Log

class PesoViewModel : ViewModel() {
    private val _peso = MutableStateFlow(0f)
    val peso: StateFlow<Float> = _peso

    private val correo: String?
        get() = FirebaseAuth.getInstance().currentUser?.email

    init {
        cargarPeso()
    }

    fun cargarPeso() {
        viewModelScope.launch {
            try {
                correo?.let {
                    val snapshot = Firebase.firestore.collection("usuarios")
                        .document(it)
                        .collection("metasSalud")
                        .document("valores")
                        .get()
                        .await()

                    val pesoDb = snapshot.get("pesoActual")
                    _peso.value = when (pesoDb) {
                        is Long -> pesoDb.toFloat()
                        is Double -> pesoDb.toFloat()
                        is Int -> pesoDb.toFloat()
                        else -> 0f
                    }
                }
            } catch (e: Exception) {
                Log.e("PesoViewModel", "Error al cargar pesoActual", e)
            }
        }
    }

    fun actualizarPeso(nuevoPeso: Float, onFinish: () -> Unit) {
        viewModelScope.launch {
            try {
                correo?.let {
                    Firebase.firestore.collection("usuarios")
                        .document(it)
                        .collection("metasSalud")
                        .document("valores")
                        .update("pesoActual", nuevoPeso)
                        .await()
                    _peso.value = nuevoPeso
                }
            } catch (e: Exception) {
                Log.e("PesoViewModel", "Error al actualizar pesoActual", e)
            } finally {
                onFinish()
            }
        }
    }
}
