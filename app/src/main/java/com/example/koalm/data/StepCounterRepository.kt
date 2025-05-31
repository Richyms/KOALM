package com.example.koalm.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Repositorio singleton en memoria para compartir los pasos y el tiempo activo
 * entre el service y cualquier pantalla.
 */
object StepCounterRepository {

    private val _steps = MutableStateFlow(0)
    private val _activeSeconds = MutableStateFlow(0)

    /** Pasos totales desde que se lanzó la app */
    val steps: StateFlow<Int> = _steps.asStateFlow()

    /** Segundos activos acumulados (se convierte a minutos en la UI) */
    val activeSeconds: StateFlow<Int> = _activeSeconds.asStateFlow()

    /* ---- Métodos internos para que el Service actualice los valores ---- */
    fun addStep() = _steps.update { it + 1 }
    fun addSeconds(sec: Int) = _activeSeconds.update { it + sec }

    /** Reinicia el conteo al cambiar de día */
    fun reset() {
        _steps.value = 0
        _activeSeconds.value = 0
    }
}
