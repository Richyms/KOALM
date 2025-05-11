package com.example.koalm.services.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.koalm.R
import com.example.koalm.services.NotificationReceiver
import com.example.koalm.services.notifications.NotificationConstants
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

class MeditationNotificationService : NotificationBase() {
    companion object {
        private const val TAG = "KOALM_NOTIFICATIONS"
        const val NOTIFICATION_ID = 3
    }

    override val channelId = "meditacion_habito"
    override val channelName = R.string.meditation_notification_channel_name
    override val channelDescription = R.string.meditation_notification_channel_description
    override val defaultTitle = R.string.meditation_notification_title
    override val defaultText = R.string.meditation_notification_default_text
    override val notificationId = NOTIFICATION_ID

    override fun scheduleNotification(
        context: Context,
        diasSeleccionados: List<Boolean>,
        hora: LocalDateTime,
        descripcion: String,
        durationMinutes: Long,
        additionalData: Map<String, Any>
    ) {
        Log.d(TAG, "MeditationNotificationService: Iniciando programación de notificación")
        Log.d(TAG, "MeditationNotificationService: Días seleccionados: ${diasSeleccionados.joinToString()}")
        Log.d(TAG, "MeditationNotificationService: Hora programada: ${hora.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))}")
        Log.d(TAG, "MeditationNotificationService: Duración: $durationMinutes minutos")
        
        // Cancelar notificaciones existentes antes de programar nuevas
        Log.d(TAG, "MeditationNotificationService: Cancelando notificaciones existentes")
        cancelNotifications(context)
        
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        var notificationCount = 0
        
        val now = LocalDateTime.now()
        Log.d(TAG, "MeditationNotificationService: Hora actual: ${now.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))}")
        
        // Programar para cada día seleccionado
        diasSeleccionados.forEachIndexed { index, seleccionado ->
            if (seleccionado) {
                Log.d(TAG, "MeditationNotificationService: Programando notificación para día $index")
                val dayOfWeek = DayOfWeek.of(index + 1)
                var nextNotificationTime = now.with(hora.toLocalTime())
                    .with(TemporalAdjusters.nextOrSame(dayOfWeek))
                
                if (nextNotificationTime.isBefore(now)) {
                    Log.d(TAG, "MeditationNotificationService: La hora ya pasó hoy, programando para mañana")
                    nextNotificationTime = nextNotificationTime.plusDays(1)
                }
                
                val notificationTimeMillis = nextNotificationTime
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()
                
                Log.d(TAG, "MeditationNotificationService: Tiempo de notificación: ${nextNotificationTime.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))}")
                
                val intent = Intent(context, NotificationReceiver::class.java).apply {
                    action = NotificationConstants.NOTIFICATION_ACTION
                    putExtra("descripcion", descripcion.ifEmpty { context.getString(defaultText) })
                    putExtra("dia_semana", index)
                    putExtra("duration_minutes", durationMinutes)
                    putExtra("is_meditation", true)
                    putExtra("is_reading", false)
                    putExtra("is_digital_disconnect", false)
                    putExtra("notas_habilitadas", false)
                    putExtra("notification_title", context.getString(defaultTitle))
                    putExtra("notification_action_button", context.getString(R.string.notification_start_timer))
                }
                
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    NOTIFICATION_ID + index,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
                
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        if (alarmManager.canScheduleExactAlarms()) {
                            Log.d(TAG, "MeditationNotificationService: Programando alarma exacta (Android 12+)")
                            alarmManager.setExactAndAllowWhileIdle(
                                AlarmManager.RTC_WAKEUP,
                                notificationTimeMillis,
                                pendingIntent
                            )
                        } else {
                            Log.e(TAG, "MeditationNotificationService: No se pueden programar alarmas exactas en Android 12+")
                        }
                    } else {
                        Log.d(TAG, "MeditationNotificationService: Programando alarma exacta (Android < 12)")
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            notificationTimeMillis,
                            pendingIntent
                        )
                    }
                    notificationCount++
                    Log.d(TAG, "MeditationNotificationService: Notificación programada exitosamente para día $index")
                } catch (e: Exception) {
                    Log.e(TAG, "MeditationNotificationService: Error al programar notificación para día $index: ${e.message}", e)
                }
            }
        }
        
        Log.d(TAG, "MeditationNotificationService: Se programaron $notificationCount notificaciones en total")
    }

    override fun cancelNotifications(context: Context) {
        Log.d(TAG, "MeditationNotificationService: Iniciando cancelación de notificaciones")
        
        try {
            // Cancelar notificaciones del NotificationManager
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            notificationManager.cancelAll() // Cancelar TODAS las notificaciones
            Log.d(TAG, "MeditationNotificationService: Todas las notificaciones canceladas")
            
            // Cancelar alarmas del AlarmManager
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            
            // Cancelar alarmas para cada día de la semana
            for (i in 0..6) {
                val intent = Intent(context, NotificationReceiver::class.java).apply {
                    action = NotificationConstants.NOTIFICATION_ACTION
                    putExtra("is_meditation", true)
                }
                
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    NOTIFICATION_ID + i,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
                
                try {
                    alarmManager.cancel(pendingIntent)
                    pendingIntent.cancel()
                    Log.d(TAG, "MeditationNotificationService: Alarma cancelada con ID ${NOTIFICATION_ID + i}")
                } catch (e: Exception) {
                    Log.e(TAG, "MeditationNotificationService: Error al cancelar alarma ${NOTIFICATION_ID + i}: ${e.message}")
                }
            }
            
            // Cancelar también las alarmas con IDs adicionales (para los botones de acción)
            for (i in 0..6) {
                val intent = Intent(context, NotificationReceiver::class.java).apply {
                    action = NotificationConstants.START_TIMER_ACTION
                    putExtra("is_meditation", true)
                }
                
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    NOTIFICATION_ID + i + 100,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
                
                try {
                    alarmManager.cancel(pendingIntent)
                    pendingIntent.cancel()
                    Log.d(TAG, "MeditationNotificationService: Alarma de acción cancelada con ID ${NOTIFICATION_ID + i + 100}")
                } catch (e: Exception) {
                    Log.e(TAG, "MeditationNotificationService: Error al cancelar alarma de acción ${NOTIFICATION_ID + i + 100}: ${e.message}")
                }
            }
            
            Log.d(TAG, "MeditationNotificationService: Cancelación de notificaciones completada exitosamente")
        } catch (e: Exception) {
            Log.e(TAG, "MeditationNotificationService: Error al cancelar notificaciones: ${e.message}", e)
        }
    }

    override fun createNotificationIntent(
        context: Context,
        descripcion: String,
        diaSemana: Int,
        durationMinutes: Long,
        additionalData: Map<String, Any>
    ): Intent {
        return super.createNotificationIntent(context, descripcion, diaSemana, durationMinutes, additionalData).apply {
            putExtra("is_meditation", true)
            putExtra("is_reading", false)
            putExtra("is_digital_disconnect", false)
            putExtra("notification_title", context.getString(defaultTitle))
            putExtra("notification_action_button", context.getString(R.string.notification_start_timer))
        }
    }
} 