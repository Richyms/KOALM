package com.example.koalm.services

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import com.example.koalm.R
import com.example.koalm.repository.HabitoRepository
import com.example.koalm.services.notifications.DigitalDisconnectNotificationService
import com.example.koalm.services.notifications.MeditationNotificationService
import com.example.koalm.services.notifications.NotificationBase
import com.example.koalm.services.notifications.ReadingNotificationService
import com.example.koalm.services.notifications.WritingNotificationService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class NotificationService : Service() {
    companion object {
        private const val TAG = "KOALM_NOTIFICATIONS"
    }

    private val notificationServices: Map<String, NotificationBase> = mapOf(
        "escritura" to WritingNotificationService(),
        "meditacion" to MeditationNotificationService(),
        "lectura" to ReadingNotificationService(),
        "desconexion" to DigitalDisconnectNotificationService()
    )

    private val habitosRepository = HabitoRepository()
    private val serviceScope = CoroutineScope(Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        Log.e(TAG, "onCreate: Servicio de notificaciones creado")
        createNotificationChannels()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannels() {
        Log.e(TAG, "createNotificationChannels: Creando canales de notificaciones")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            notificationServices.values.forEach { service ->
                val channel = NotificationChannel(
                    service.channelId,
                    getString(service.channelName),
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = getString(service.channelDescription)
                    enableVibration(true)
                    enableLights(true)
                }
                notificationManager.createNotificationChannel(channel)
            }
            
            Log.e(TAG, "createNotificationChannels: Canales creados")
        }
    }

    fun scheduleNotification(
        context: Context,
        habitoId: String,
        diasSeleccionados: List<Boolean>,
        hora: LocalDateTime,
        descripcion: String,
        durationMinutes: Long,
        notasHabilitadas: Boolean,
        isMeditation: Boolean = false,
        isReading: Boolean = false,
        isDigitalDisconnect: Boolean = false
    ) {
        val scope = CoroutineScope(Dispatchers.IO + Job())
        scope.launch {
            try {
                val habitosRepository = HabitoRepository()
                val habitoResult = habitosRepository.obtenerHabito(habitoId)
                
                habitoResult.onSuccess { habito ->
                    if (!habito.activo) {
                        Log.d(TAG, "Hábito $habitoId está inactivo, no se programarán notificaciones")
                        return@onSuccess
                    }
                    
                    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                    val intent = Intent(context, NotificationReceiver::class.java).apply {
                        putExtra("habitoId", habitoId)
                        putExtra("descripcion", descripcion)
                        putExtra("duracionMinutos", durationMinutes)
                        putExtra("notasHabilitadas", notasHabilitadas)
                        putExtra("isMeditation", isMeditation)
                        putExtra("isReading", isReading)
                        putExtra("isDigitalDisconnect", isDigitalDisconnect)
                    }
                    
                    val pendingIntent = PendingIntent.getBroadcast(
                        context,
                        habitoId.hashCode(),
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    
                    val calendar = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, hora.hour)
                        set(Calendar.MINUTE, hora.minute)
                        set(Calendar.SECOND, 0)
                        
                        if (before(Calendar.getInstance())) {
                            add(Calendar.DAY_OF_YEAR, 1)
                        }
                    }
                    
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            calendar.timeInMillis,
                            pendingIntent
                        )
                    } else {
                        alarmManager.setExact(
                            AlarmManager.RTC_WAKEUP,
                            calendar.timeInMillis,
                            pendingIntent
                        )
                    }
                    
                    Log.d(TAG, "Notificación programada para el hábito $habitoId a las ${hora.format(DateTimeFormatter.ofPattern("HH:mm"))}")
                }.onFailure { e ->
                    Log.e(TAG, "Error al obtener hábito para programar notificación: ${e.message}", e)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al programar notificación: ${e.message}", e)
            }
        }
    }
} 