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
import androidx.core.app.NotificationCompat
import com.example.koalm.MainActivity
import com.example.koalm.R
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
import java.util.Calendar

class NotificationService : Service() {
    companion object {
        private const val CHANNEL_ID = "escritura_habito"
        private const val NOTIFICATION_ID = 1
        private const val TAG = "KOALM_NOTIFICATIONS"
        const val NOTIFICATION_ACTION = "com.example.koalm.NOTIFICATION_ACTION"
    }

    override fun onCreate() {
        super.onCreate()
        Log.e(TAG, "onCreate: Servicio de notificaciones creado")
        createNotificationChannel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        Log.e(TAG, "createNotificationChannel: Creando canal de notificaciones")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.notification_channel_name)
            val descriptionText = getString(R.string.notification_channel_description)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableVibration(true)
                enableLights(true)
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            Log.e(TAG, "createNotificationChannel: Canal creado con importancia $importance")
        }
    }

    fun scheduleNotification(
        context: Context,
        diasSeleccionados: List<Boolean>,
        hora: LocalDateTime,
        descripcion: String,
        durationMinutes: Long
    ) {
        Log.e(TAG, "scheduleNotification: Programando notificaciones para los días seleccionados")
        Log.e(TAG, "scheduleNotification: Días seleccionados: $diasSeleccionados")
        Log.e(TAG, "scheduleNotification: Hora: $hora")
        Log.e(TAG, "scheduleNotification: Duración: $durationMinutes minutos")
        
        // Cancelar notificaciones existentes antes de programar nuevas
        cancelExistingNotifications(context)
        
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        var notificationCount = 0
        
        val now = LocalDateTime.now()
        
        // Programar para cada día seleccionado
        diasSeleccionados.forEachIndexed { index, seleccionado ->
            if (seleccionado) {
                val dayOfWeek = DayOfWeek.of(index + 1)

                // Calcular el próximo día de la semana
                var nextNotificationTime = now.with(hora.toLocalTime())
                    .with(TemporalAdjusters.nextOrSame(dayOfWeek))
                
                // Si la hora ya pasó hoy, programar para el próximo día
                if (nextNotificationTime.isBefore(now)) {
                    nextNotificationTime = nextNotificationTime.plusDays(1)
                }
                
                val notificationTimeMillis = nextNotificationTime
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()
                
                Log.e(TAG, "scheduleNotification: Programando notificación para día $dayOfWeek a las $nextNotificationTime")
                
                // Crear intent para el BroadcastReceiver
                val intent = Intent(context, NotificationReceiver::class.java).apply {
                    action = NOTIFICATION_ACTION
                    putExtra("descripcion", descripcion.ifEmpty { context.getString(R.string.notification_default_text) })
                    putExtra("dia_semana", index)
                    putExtra("duration_minutes", durationMinutes)
                }
                
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    NOTIFICATION_ID + index,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
                
                // Programar la alarma
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            notificationTimeMillis,
                            pendingIntent
                        )
                        Log.e(TAG, "scheduleNotification: Alarma programada con setExactAndAllowWhileIdle")
                    } else {
                        Log.e(TAG, "scheduleNotification: No se pueden programar alarmas exactas")
                    }
                } else {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        notificationTimeMillis,
                        pendingIntent
                    )
                    Log.e(TAG, "scheduleNotification: Alarma programada con setExactAndAllowWhileIdle")
                }
                
                notificationCount++
            }
        }
        
        Log.e(TAG, "scheduleNotification: Se programaron $notificationCount notificaciones")
    }
    
    private fun cancelExistingNotifications(context: Context) {
        Log.e(TAG, "cancelExistingNotifications: Cancelando notificaciones existentes")
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Cancelar todas las notificaciones existentes
        for (i in 0..6) {
            notificationManager.cancel(NOTIFICATION_ID + i)
        }
        
        // Cancelar alarmas existentes
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        for (i in 0..6) {
            val intent = Intent(context, NotificationReceiver::class.java).apply {
                action = NOTIFICATION_ACTION
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                NOTIFICATION_ID + i,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE
            )
            
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
                Log.e(TAG, "cancelExistingNotifications: Alarma cancelada para día $i")
            }
        }
    }
} 