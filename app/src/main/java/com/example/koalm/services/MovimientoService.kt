package com.example.koalm.services
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
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
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.time.LocalDate
import java.util.concurrent.TimeUnit

class MovimientoService : Service(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var stepDetector: Sensor? = null

    // Mientras no cambiaremos en esta versión el cálculo de segundos entre pasos,
    // dejamos un campo para “inactividad” pero no lo usamos para sumar tiempo activo.
    private var lastStepTime = 0L
    private val inactivityTimeout = TimeUnit.MINUTES.toMillis(2)
    private val handler = Handler(Looper.getMainLooper())
    private var inactivityRunnable: Runnable? = null

    // Guardamos la fecha “actual” para detectar transición de día
    private var currentDate: LocalDate = LocalDate.now()

    override fun onCreate() {
        super.onCreate()
        createChannel()
        startForeground(1, buildNotification())

        currentDate = LocalDate.now()
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepDetector = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)

        if (stepDetector != null) {
            sensorManager.registerListener(this, stepDetector, SensorManager.SENSOR_DELAY_NORMAL)
            Log.d("KOALM_DEBUG", "Se registra listener de STEP_DETECTOR")
        } else {
            Log.w("KOALM_DEBUG", "Sensor STEP_DETECTOR no disponible")
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        // --- 1) Verificamos cambio de día ---
        val today = LocalDate.now()
        if (today != currentDate) {
            // “Ayer” (fecha anterior) en String
            val ayerFecha = currentDate.toString()
            val pasosTotalesAyer = StepCounterRepository.steps.value

            if (pasosTotalesAyer > 0) {
                // Calculamos tiempoActivo en MINUTOS:
                val minutosAyer = pasosTotalesAyer / 60  // división entera
                Log.d(
                    "KOALM_DEBUG",
                    "Subiendo resto del día anterior ($ayerFecha): " +
                            "pasos=$pasosTotalesAyer, minutosActivos=$minutosAyer"
                )
                // Subimos usando “minutosAyer” como tiempoActivo
                uploadMetricsToFirestore(
                    fecha = ayerFecha,
                    pasos = pasosTotalesAyer,
                    minutosActivos = minutosAyer

                )
            }

            // Reiniciamos contadores en memoria para el nuevo día
            StepCounterRepository.reset()
            currentDate = today
            Log.d("KOALM_DEBUG", "Cambio de día: contadores reiniciados para $currentDate")
        }

        // --- 2) Cada vez que detectamos un paso, lo sumamos en memoria ---
        StepCounterRepository.addStep()
        val pasosHoy = StepCounterRepository.steps.value
        Log.d("KOALM_DEBUG", "Paso detectado → total pasos hoy: $pasosHoy")

        // --- 3) (Opcional) reiniciamos temporizador de inactividad ---
        val nowTimestamp = System.currentTimeMillis()
        if (lastStepTime != 0L) {
            val diffSec = ((nowTimestamp - lastStepTime) / 1000).toInt()
            if (diffSec < 60) {
                // Aunque antes usábamos esto para sumar segundos,
                // ahora calculamos tiempo en minutos a posteriori
                Log.d("KOALM_DEBUG", "Pasos seguidos sin más de 60s de pausa (diffSec=$diffSec)")
            }
        }
        lastStepTime = nowTimestamp

        // --- 4) Cada 60 pasos, subimos a Firestore ---
        if (pasosHoy > 0 && pasosHoy % 30 == 0) {
            // calculamos tiempoActivo en MINUTOS
            val minutosHoy = pasosHoy / 60
            Log.d(
                "KOALM_DEBUG",
                "Alcanzados $pasosHoy pasos (múltiplo de 30). " +
                        "Subiendo a Firestore → fecha=$currentDate, pasos=$pasosHoy, minutosActivos=$minutosHoy"
            )
            uploadMetricsToFirestore(
                fecha = currentDate.toString(),
                pasos = pasosHoy,
                minutosActivos = minutosHoy
            )
        }

        // --- 5) Reiniciamos temporizador de inactividad ---
        restartInactivityTimer()
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    private fun restartInactivityTimer() {
        inactivityRunnable?.let { handler.removeCallbacks(it) }
        inactivityRunnable = Runnable { lastStepTime = 0L }
        handler.postDelayed(inactivityRunnable!!, inactivityTimeout)
    }

    /**
     * Obtiene el correo del usuario de SharedPreferences (guardado en MainActivity).
     * Se asume que en MainActivity se hace:
     *   prefs.edit().putString("correo", user.email).apply()
     */
    private fun getUserEmail(): String? {
        val prefs = getSharedPreferences("koalm_prefs", Context.MODE_PRIVATE)
        return prefs.getString("correo", null)
    }

    /**
     * Sube a Firestore los valores de pasos, tiempoActivo (en minutos) y calorías
     * para la fecha dada. Usa el correo como ID de documento “usuarios/{correo}”.
     */
    private fun uploadMetricsToFirestore(fecha: String, pasos: Int, minutosActivos: Int) {
        val email = getUserEmail() ?: run {
            Log.w("KOALM_FIRESTORE", "No hay correo en SharedPreferences; no se suben métricas.")
            return
        }

        // Referencia al documento de usuario
        val usuarioDoc = Firebase.firestore
            .collection("usuarios")
            .document(email)

        usuarioDoc.get()
            .addOnSuccessListener { document ->
                // Obtenemos “peso” para calcular calorías
                val peso = document.getDouble("peso") ?: 0.0
                // fórmula original: calorías = pasos * peso * 0.0007
                val calorias = (pasos * peso * 0.0007).toInt()

                //guardado local
                guardarDatosLocales(pasos, minutosActivos, calorias)

                // Creamos el mapa con “pasos”, “tiempoActividad” EN MINUTOS y “calorias”
                val data = mapOf(
                    "pasos" to pasos,
                    "tiempoActividad" to minutosActivos,
                    "calorias" to calorias
                )

                usuarioDoc.collection("metricasDiarias")
                    .document(fecha)
                    .set(data, SetOptions.merge())
                    .addOnSuccessListener {
                        Log.d("KOALM_FIRESTORE", "Upload exitoso ($fecha): $data")
                    }
                    .addOnFailureListener { e ->
                        Log.e("KOALM_FIRESTORE", "Error al subir métricas ($fecha): ${e.message}", e)
                    }
            }
            .addOnFailureListener { e ->
                Log.e("KOALM_FIRESTORE", "Error al obtener usuario para cálculo de calorías: ${e.message}", e)
            }
    }

    /*
    Logica para guardar los datos de manera local para su posterior consulta
     */
    private fun guardarDatosLocales(pasos: Int, minutos:Int, calorias:Int){
        val prefs = getSharedPreferences("koalm_prefs", Context.MODE_PRIVATE)
        prefs.edit()
            .putInt("pasos_hoy", pasos)
            .putInt("minutos_activos", minutos)
            .putInt("calorias", calorias)
            .apply()
    }

    fun obtenerPasosLocales(): Int {
        val prefs = getSharedPreferences("koalm_prefs", Context.MODE_PRIVATE)
        return prefs.getInt("pasos_hoy", 0)
    }

    fun obtenerMinutosLocales(): Int {
        val prefs = getSharedPreferences("koalm_prefs", Context.MODE_PRIVATE)
        return prefs.getInt("minutos_activos", 0)
    }

    fun obtenerCaloriasLocales(): Int {
        val prefs = getSharedPreferences("koalm_prefs", Context.MODE_PRIVATE)
        return prefs.getInt("calorias", 0)
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "mov",
                "Monitoreo de actividad",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
    }

    private fun buildNotification() =
        NotificationCompat.Builder(this, "mov")
            .setContentTitle("KOALM")
            .setContentText("Contando pasos…")
            .setSmallIcon(R.drawable.ic_notification)
            .build()

    override fun onDestroy() {
        sensorManager.unregisterListener(this)
        super.onDestroy()
        Log.d("KOALM_DEBUG", "Servicio detenido, listener desregistrado")
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
