package com.example.koalm.services

import android.app.*
import android.content.*
import android.hardware.*
import android.os.*
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.koalm.R
import com.example.koalm.data.StepCounterRepository
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.time.LocalDate
import java.util.concurrent.TimeUnit

class MovimientoService : Service(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var stepDetector: Sensor? = null
    private var lastStepTime = 0L
    private val inactivityTimeout = TimeUnit.MINUTES.toMillis(2)
    private val handler = Handler(Looper.getMainLooper())
    private var inactivityRunnable: Runnable? = null

    override fun onCreate() {
        super.onCreate()
        createChannel()
        startForeground(1, buildNotification())

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        stepDetector = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)

        if (stepDetector != null) {
            sensorManager.registerListener(this, stepDetector, SensorManager.SENSOR_DELAY_NORMAL)
        } else {
            Log.w("KOALM_DEBUG", "Sensor STEP_DETECTOR no disponible")
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        val now = System.currentTimeMillis()

        StepCounterRepository.addStep()
        Log.d("KOALM_DEBUG", "Paso detectado")

        if (lastStepTime != 0L) {
            val diffSec = ((now - lastStepTime) / 1000).toInt()
            if (diffSec < 60) StepCounterRepository.addSeconds(1)
        }
        lastStepTime = now

        guardarMetricaEnFirestore(
            pasos = StepCounterRepository.steps.value,
            segundos = StepCounterRepository.activeSeconds.value
        )

        restartInactivityTimer()
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    private fun restartInactivityTimer() {
        inactivityRunnable?.let { handler.removeCallbacks(it) }
        inactivityRunnable = Runnable {
            lastStepTime = 0L
        }
        handler.postDelayed(inactivityRunnable!!, inactivityTimeout)
    }

    private fun getUserEmail(): String? {
        val prefs = getSharedPreferences("koalm_prefs", Context.MODE_PRIVATE)
        return prefs.getString("correo", null)
    }

    private fun guardarMetricaEnFirestore(pasos: Int, segundos: Int) {
        val correo = getUserEmail() ?: return
        val fecha = LocalDate.now().toString()

        val data = mapOf(
            "pasos" to pasos,
            "tiempoActividad" to segundos
        )

        Firebase.firestore
            .collection("usuarios")
            .document(correo)
            .collection("metricasDiarias")
            .document(fecha)
            .set(data, com.google.firebase.firestore.SetOptions.merge())
            .addOnSuccessListener {
                Log.d("KOALM_FIREBASE", "Guardado exitoso: $data")
            }
            .addOnFailureListener { e ->
                Log.e("KOALM_FIREBASE", "Error al guardar: ${e.message}", e)
            }
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "mov",
                "Monitoreo",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification =
        NotificationCompat.Builder(this, "mov")
            .setContentTitle("KOALM")
            .setContentText("Contando pasos…")
            .setSmallIcon(R.drawable.ic_notification)
            .build()

    override fun onDestroy() {
        sensorManager.unregisterListener(this)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
