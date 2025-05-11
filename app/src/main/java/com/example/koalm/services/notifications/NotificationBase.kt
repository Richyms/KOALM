package com.example.koalm.services.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.koalm.services.NotificationReceiver
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

abstract class NotificationBase {
    companion object {
        private const val TAG = "KOALM_NOTIFICATIONS"
    }

    abstract val channelId: String
    abstract val channelName: Int
    abstract val channelDescription: Int
    abstract val defaultTitle: Int
    abstract val defaultText: Int
    abstract val notificationId: Int

    open fun scheduleNotification(
        context: Context,
        diasSeleccionados: List<Boolean>,
        hora: LocalDateTime,
        descripcion: String,
        durationMinutes: Long,
        additionalData: Map<String, Any>
    ) {
        Log.d(TAG, "${javaClass.simpleName}: Iniciando programación de notificación")
        Log.d(TAG, "${javaClass.simpleName}: Días seleccionados: ${diasSeleccionados.joinToString()}")
        Log.d(TAG, "${javaClass.simpleName}: Hora programada: ${hora.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))}")
        Log.d(TAG, "${javaClass.simpleName}: Duración: $durationMinutes minutos")
        
        // Cancelar notificaciones existentes antes de programar nuevas
        Log.d(TAG, "${javaClass.simpleName}: Cancelando notificaciones existentes")
        cancelNotifications(context)
        
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        var notificationCount = 0
        
        val now = LocalDateTime.now()
        Log.d(TAG, "${javaClass.simpleName}: Hora actual: ${now.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))}")
        
        // Programar para cada día seleccionado
        diasSeleccionados.forEachIndexed { index, seleccionado ->
            if (seleccionado) {
                Log.d(TAG, "${javaClass.simpleName}: Programando notificación para día $index")
                val dayOfWeek = DayOfWeek.of(index + 1)
                var nextNotificationTime = now.with(hora.toLocalTime())
                    .with(TemporalAdjusters.nextOrSame(dayOfWeek))
                
                if (nextNotificationTime.isBefore(now)) {
                    Log.d(TAG, "${javaClass.simpleName}: La hora ya pasó hoy, programando para mañana")
                    nextNotificationTime = nextNotificationTime.plusDays(1)
                }
                
                val notificationTimeMillis = nextNotificationTime
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()
                
                Log.d(TAG, "${javaClass.simpleName}: Tiempo de notificación: ${nextNotificationTime.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))}")
                
                val intent = createNotificationIntent(context, descripcion, index, durationMinutes, additionalData)
                
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    notificationId + index,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
                
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        if (alarmManager.canScheduleExactAlarms()) {
                            Log.d(TAG, "${javaClass.simpleName}: Programando alarma exacta (Android 12+)")
                            alarmManager.setExactAndAllowWhileIdle(
                                AlarmManager.RTC_WAKEUP,
                                notificationTimeMillis,
                                pendingIntent
                            )
                        } else {
                            Log.e(TAG, "${javaClass.simpleName}: No se pueden programar alarmas exactas en Android 12+")
                        }
                    } else {
                        Log.d(TAG, "${javaClass.simpleName}: Programando alarma exacta (Android < 12)")
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            notificationTimeMillis,
                            pendingIntent
                        )
                    }
                    notificationCount++
                    Log.d(TAG, "${javaClass.simpleName}: Notificación programada exitosamente para día $index")
                } catch (e: Exception) {
                    Log.e(TAG, "${javaClass.simpleName}: Error al programar notificación para día $index: ${e.message}", e)
                }
            }
        }
        
        Log.d(TAG, "${javaClass.simpleName}: Se programaron $notificationCount notificaciones en total")
    }

    open fun cancelNotifications(context: Context) {
        Log.d(TAG, "${javaClass.simpleName}: Iniciando cancelación de notificaciones")
        
        try {
            // Cancelar notificaciones del NotificationManager
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            notificationManager.cancelAll() // Cancelar TODAS las notificaciones
            Log.d(TAG, "${javaClass.simpleName}: Todas las notificaciones canceladas")
            
            // Cancelar alarmas del AlarmManager
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            
            // Cancelar alarmas para cada día de la semana
            for (i in 0..6) {
                val intent = createNotificationIntent(context, "", i, 0, emptyMap())
                
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    notificationId + i,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
                
                try {
                    alarmManager.cancel(pendingIntent)
                    pendingIntent.cancel()
                    Log.d(TAG, "${javaClass.simpleName}: Alarma cancelada con ID ${notificationId + i}")
                } catch (e: Exception) {
                    Log.e(TAG, "${javaClass.simpleName}: Error al cancelar alarma ${notificationId + i}: ${e.message}")
                }
            }
            
            // Cancelar también las alarmas con IDs adicionales (para los botones de acción)
            for (i in 0..6) {
                val intent = Intent(context, NotificationReceiver::class.java).apply {
                    action = NotificationConstants.START_TIMER_ACTION
                    putExtra("is_meditation", false)
                    putExtra("is_reading", false)
                    putExtra("is_digital_disconnect", false)
                }
                
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    notificationId + i + 100,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
                
                try {
                    alarmManager.cancel(pendingIntent)
                    pendingIntent.cancel()
                    Log.d(TAG, "${javaClass.simpleName}: Alarma de acción cancelada con ID ${notificationId + i + 100}")
                } catch (e: Exception) {
                    Log.e(TAG, "${javaClass.simpleName}: Error al cancelar alarma de acción ${notificationId + i + 100}: ${e.message}")
                }
            }
            
            Log.d(TAG, "${javaClass.simpleName}: Cancelación de notificaciones completada exitosamente")
        } catch (e: Exception) {
            Log.e(TAG, "${javaClass.simpleName}: Error al cancelar notificaciones: ${e.message}", e)
        }
    }

    protected open fun createNotificationIntent(
        context: Context,
        descripcion: String,
        diaSemana: Int,
        durationMinutes: Long,
        additionalData: Map<String, Any>
    ): Intent {
        return Intent(context, NotificationReceiver::class.java).apply {
            action = NotificationConstants.NOTIFICATION_ACTION
            putExtra("descripcion", descripcion.ifEmpty { context.getString(defaultText) })
            putExtra("dia_semana", diaSemana)
            putExtra("duration_minutes", durationMinutes)
            putExtra("is_meditation", false)
            putExtra("is_reading", false)
            putExtra("is_digital_disconnect", false)
            putExtra("notas_habilitadas", false)
        }
    }
} 