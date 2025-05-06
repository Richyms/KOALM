package com.example.koalm.services

/* ---------- IMPORTS ---------- */
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.koalm.R
import com.example.koalm.data.StepCounterRepository
import java.util.concurrent.TimeUnit

/**
 * Escucha el sensor STEP_DETECTOR y publica los pasos en StepCounterRepository.
 * **Ya no envía nada a Firestore.**
 */
class MovimientoService : Service(), SensorEventListener {

    /* ---------- ATRIBUTOS ---------- */
    private lateinit var sensorManager: SensorManager
    private var stepDetector: Sensor? = null

    private var lastStepTime = 0L
    private var secondsActive = 0

    /* Inactividad: si pasan 2 min sin pasos reiniciamos el cronómetro */
    private val inactivityTimeout = TimeUnit.MINUTES.toMillis(2)
    private val handler = Handler(Looper.getMainLooper())
    private var inactivityRunnable: Runnable? = null

    /* ---------- CICLO DE VIDA ---------- */
    override fun onCreate() {
        super.onCreate()
        createChannel()
        startForeground(1, buildNotification())

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        stepDetector =
            sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR).also { sensor ->
                if (sensor == null) {
                    Log.w("KOALM_DEBUG", "Sensor STEP_DETECTOR no disponible")
                } else {
                    sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
                }
            }
    }

    /* ---------- SENSOR ---------- */
    override fun onSensorChanged(event: SensorEvent?) {
        val now = System.currentTimeMillis()

        // 1) Guardar paso
        StepCounterRepository.addStep()
        Log.d("KOALM_DEBUG", "Paso detectado")

        // 2) Segundos activos desde el paso previo (máx 120 s)
        if (lastStepTime != 0L) {
            val diffSec = ((now - lastStepTime) / 1000).toInt()
            if (diffSec < 120) {
                secondsActive += diffSec
                StepCounterRepository.addSeconds(diffSec)
            }
        }
        lastStepTime = now

        restartInactivityTimer()
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    /* ---------- INACTIVIDAD ---------- */
    private fun restartInactivityTimer() {
        inactivityRunnable?.let { handler.removeCallbacks(it) }
        inactivityRunnable = Runnable {
            Log.d("KOALM_DEBUG", "Inactividad >2 min → reinicio cronómetro")
            secondsActive = 0
            lastStepTime = 0L
        }
        handler.postDelayed(inactivityRunnable!!, inactivityTimeout)
    }

    /* ---------- NOTIFICACIÓN ---------- */
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

    /* ---------- LIMPIEZA ---------- */
    override fun onDestroy() {
        sensorManager.unregisterListener(this)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
