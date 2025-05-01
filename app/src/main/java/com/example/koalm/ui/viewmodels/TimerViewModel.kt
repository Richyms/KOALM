/*  TimerViewModel.kt  */
package com.example.koalm.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TimerViewModel : ViewModel() {
    private val _timeLeft = MutableStateFlow(0L)
    val timeLeft: StateFlow<Long> = _timeLeft.asStateFlow()

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private var timerJob: kotlinx.coroutines.Job? = null

    /** Inicia una nueva cuenta atrás */
    fun start(durationMillis: Long) {
        timerJob?.cancel()
        _timeLeft.value = durationMillis
        _isRunning.value = true

        timerJob = viewModelScope.launch {
            while (_timeLeft.value > 0) {
                delay(1000) // Actualizar cada segundo
                _timeLeft.value -= 1000
            }
            _isRunning.value = false
        }
    }

    fun stop() {
        timerJob?.cancel()
        _isRunning.value = false
        _timeLeft.value = 0
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }

    fun updateTimeLeft(newTime: Long) {
        viewModelScope.launch {
            _timeLeft.value = newTime
        }
    }

    fun updateIsRunning(isRunning: Boolean) {
        viewModelScope.launch {
            _isRunning.value = isRunning
        }
    }
}
