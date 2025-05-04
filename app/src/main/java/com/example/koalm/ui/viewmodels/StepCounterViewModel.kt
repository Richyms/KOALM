package com.example.koalm.viewmodels

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class StepCounterViewModel(application: Application)
    : AndroidViewModel(application), SensorEventListener {

    private val sensorManager =
        application.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val stepSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

    // Preferencias para guardar el valor base
    private val prefs: SharedPreferences =
        application.getSharedPreferences("STEP_PREFS", Context.MODE_PRIVATE)
    private var baseline: Float
        get() = prefs.getFloat("baseline", -1f)
        set(value) = prefs.edit().putFloat("baseline", value).apply()

    private val _steps = MutableLiveData(0)
    val steps: LiveData<Int> = _steps

    init {
        stepSensor?.also { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
    }

    override fun onSensorChanged(event: SensorEvent) {
        val totalSinceBoot = event.values[0]              // pasos acumulados desde el último reinicio
        if (baseline < 0f) {                              // primera vez tras permiso
            baseline = totalSinceBoot
        }
        _steps.value = (totalSinceBoot - baseline).toInt()
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    override fun onCleared() {
        super.onCleared()
        sensorManager.unregisterListener(this)
    }

    /** Reinicia el conteo (por ejemplo al final del día) */
    fun resetBaseline(currentSensorValue: Float) {
        baseline = currentSensorValue
        _steps.value = 0
    }
}
