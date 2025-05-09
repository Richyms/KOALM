package com.example.koalm.services.notifications

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.koalm.R
import com.example.koalm.services.NotificationReceiver
import com.example.koalm.services.WritingTimerService
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

class WritingNotificationService : NotificationBase {
    override val channelId = "escritura_habito"
    override val channelName = R.string.notification_channel_name
    override val channelDescription = R.string.notification_channel_description
    override val defaultTitle = R.string.notification_title
    override val defaultText = R.string.notification_default_text

    override fun scheduleNotification(
        context: Context,
        diasSeleccionados: List<Boolean>,
        hora: LocalDateTime,
        descripcion: String,
        durationMinutes: Long,
        additionalData: Map<String, Any>
    ) {
        val TAG = "WritingNotificationService"
        Log.e(TAG, "scheduleNotification: Programando notificaciones para los días seleccionados")
        
        // Cancelar notificaciones existentes antes de programar nuevas
        cancelNotifications(context)
        
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        var notificationCount = 0
        
        val now = LocalDateTime.now()
        
        // Programar para cada día seleccionado
        diasSeleccionados.forEachIndexed { index, seleccionado ->
            if (seleccionado) {
                val dayOfWeek = DayOfWeek.of(index + 1)
                var nextNotificationTime = now.with(hora.toLocalTime())
                    .with(TemporalAdjusters.nextOrSame(dayOfWeek))
                
                if (nextNotificationTime.isBefore(now)) {
                    nextNotificationTime = nextNotificationTime.plusDays(1)
                }
                
                val notificationTimeMillis = nextNotificationTime
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()
                
                val intent = Intent(context, NotificationReceiver::class.java).apply {
                    action = NotificationConstants.NOTIFICATION_ACTION
                    putExtra("descripcion", descripcion.ifEmpty { context.getString(defaultText) })
                    putExtra("dia_semana", index)
                    putExtra("duration_minutes", durationMinutes)
                    putExtra("notas_habilitadas", additionalData["notas_habilitadas"] as? Boolean ?: false)
                    putExtra("is_meditation", false)
                }
                
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    NOTIFICATION_ID + index,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            notificationTimeMillis,
                            pendingIntent
                        )
                    }
                } else {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        notificationTimeMillis,
                        pendingIntent
                    )
                }
                
                notificationCount++
            }
        }
        
        Log.e(TAG, "scheduleNotification: Se programaron $notificationCount notificaciones")
    }

    override fun cancelNotifications(context: Context) {
        val TAG = "WritingNotificationService"
        Log.e(TAG, "cancelNotifications: Cancelando notificaciones existentes")
        
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        for (i in 0..6) {
            notificationManager.cancel(NOTIFICATION_ID + i)
        }
        
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        for (i in 0..6) {
            val intent = Intent(context, NotificationReceiver::class.java).apply {
                action = NotificationConstants.NOTIFICATION_ACTION
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                NOTIFICATION_ID + i,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE
            )
            
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
            }
        }
    }

    companion object {
        const val NOTIFICATION_ID = 1
    }
} 