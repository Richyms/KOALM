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
import com.example.koalm.model.HabitosPredeterminados
import com.example.koalm.repository.HabitoRepository
import com.example.koalm.services.notifications.DigitalDisconnectNotificationService
import com.example.koalm.services.notifications.MeditationNotificationService
import com.example.koalm.services.notifications.NotificationBase
import com.example.koalm.services.notifications.ReadingNotificationService
import com.example.koalm.services.notifications.WritingNotificationService
import com.example.koalm.services.notifications.NotificationConstants
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.ZoneId
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
        Log.d(TAG, "onCreate: Servicio de notificaciones creado")
        createNotificationChannels()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannels() {
        Log.d(TAG, "createNotificationChannels: Creando canales de notificaciones")
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
            
            Log.d(TAG, "createNotificationChannels: Canales creados exitosamente")
        }
    }

    fun scheduleNotification(
        context: Context,
        habitoId: String,
        diasSeleccionados: List<Boolean>,
        hora: LocalDateTime,
        descripcion: String,
        durationMinutes: Long,
        notasHabilitadas: Boolean = false,
        isMeditation: Boolean = false,
        isReading: Boolean = false,
        isDigitalDisconnect: Boolean = false
    ) {
        Log.d(TAG, "Iniciando programación de notificación para hábito: $habitoId")
        Log.d(TAG, "Hora programada: ${hora.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))}")
        Log.d(TAG, "Duración: $durationMinutes minutos")
        Log.d(TAG, "Flags de tipo: isMeditation=$isMeditation, isReading=$isReading, isDigitalDisconnect=$isDigitalDisconnect")
        
        // Obtener el hábito de Firebase
        val db = FirebaseFirestore.getInstance()
        db.collection("habitos").document(habitoId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val habito = document.toObject(HabitosPredeterminados::class.java)
                    if (habito != null) {
                        Log.d(TAG, "Hábito encontrado. Tipo: ${habito.tipo}")
                        
                        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                        val intent = Intent(context, NotificationReceiver::class.java).apply {
                            action = NotificationConstants.NOTIFICATION_ACTION
                            putExtra("habito_id", habitoId)
                            putExtra("descripcion", descripcion)
                            putExtra("duration_minutes", durationMinutes)
                            putExtra("is_meditation", isMeditation)
                            putExtra("is_reading", isReading)
                            putExtra("is_digital_disconnect", isDigitalDisconnect)
                            putExtra("notas_habilitadas", notasHabilitadas)
                        }
                        
                        // Generar un ID único para la alarma basado en el ID del hábito
                        val alarmId = habitoId.hashCode()
                        
                        // Configurar el tiempo de la alarma
                        val calendar = Calendar.getInstance().apply {
                            set(Calendar.HOUR_OF_DAY, hora.hour)
                            set(Calendar.MINUTE, hora.minute)
                            set(Calendar.SECOND, 0)
                            
                            // Si la hora ya pasó hoy, programar para mañana
                            if (timeInMillis <= System.currentTimeMillis()) {
                                add(Calendar.DAY_OF_YEAR, 1)
                            }
                        }
                        
                        // Cancelar cualquier alarma existente para este hábito
                        val existingIntent = Intent(context, NotificationReceiver::class.java).apply {
                            action = NotificationConstants.NOTIFICATION_ACTION
                            putExtra("habito_id", habitoId)
                        }
                        val existingPendingIntent = PendingIntent.getBroadcast(
                            context,
                            alarmId,
                            existingIntent,
                            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                        )
                        alarmManager.cancel(existingPendingIntent)
                        Log.d(TAG, "Alarmas existentes canceladas para hábito: $habitoId")
                        
                        // Programar la nueva alarma
                        val pendingIntent = PendingIntent.getBroadcast(
                            context,
                            alarmId,
                            intent,
                            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                        )
                        
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            if (alarmManager.canScheduleExactAlarms()) {
                                alarmManager.setAlarmClock(
                                    AlarmManager.AlarmClockInfo(calendar.timeInMillis, pendingIntent),
                                    pendingIntent
                                )
                            } else {
                                alarmManager.setExactAndAllowWhileIdle(
                                    AlarmManager.RTC_WAKEUP,
                                    calendar.timeInMillis,
                                    pendingIntent
                                )
                            }
                        } else {
                            alarmManager.setExactAndAllowWhileIdle(
                                AlarmManager.RTC_WAKEUP,
                                calendar.timeInMillis,
                                pendingIntent
                            )
                        }
                        
                        Log.d(TAG, "Notificación programada exitosamente para hábito: $habitoId")
                    } else {
                        Log.e(TAG, "El hábito no existe o no está activo: $habitoId")
                    }
                } else {
                    Log.e(TAG, "No se encontró el hábito: $habitoId")
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error al obtener el hábito: ${e.message}", e)
            }
    }
} 