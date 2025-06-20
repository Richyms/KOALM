package com.example.koalm.services.notifications

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.koalm.R
import com.example.koalm.services.timers.NotificationReceiver
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

class HydrationNotificationService : NotificationBase() {
    companion object {
        private const val TAG = "KOALM_NOTIFICATIONS"
        const val NOTIFICATION_ID = 16  // Cambia el ID para que no choque con sueño
    }

    override val notificationId: Int
        get() = NOTIFICATION_ID

    override val channelId = "hidratacion_habito"
    override val channelName = R.string.hidratacion_notification_channel_name
    override val channelDescription = R.string.hidratacion_notification_channel_description
    override val defaultTitle = R.string.hidratacion_notification_default_text
    override val defaultText = R.string.hidratacion_notification_default_text


    fun scheduleNotification(
        context: Context,
        horarios: List<String>,
        descripcion: String,
        durationMinutes: Long,
        additionalData: Map<String, Any>
    ) {
        Log.d(TAG, "HidatacionNotificationService: Iniciando programación de notificación")

        cancelNotifications(context)

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val formatter = DateTimeFormatter.ofPattern("HH:mm")
        val now = LocalDateTime.now()
        var notificationCount = 0

        horarios.take(6).forEachIndexed { index, horaStr ->
            val horaLocalTime = LocalDateTime.of(
                now.toLocalDate(),
                LocalDateTime.parse("1970-01-01T$horaStr").toLocalTime()
            )

            var notificationTime = if (horaLocalTime.isBefore(now)) {
                horaLocalTime.plusDays(1)
            } else {
                horaLocalTime
            }

            val notificationMillis = notificationTime
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()

            Log.d(TAG, "Programando notificación para horario $horaStr a las ${notificationTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))}")

            val intent = Intent(context, NotificationReceiver::class.java).apply {
                action = NotificationConstants.NOTIFICATION_ACTION
                putExtra("descripcion", descripcion.ifEmpty { context.getString(defaultText) })
                putExtra("is_alimentation", false)
                putExtra("is_sleeping", false)
                putExtra("is_hydration", true)
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
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            notificationMillis,
                            pendingIntent
                        )
                    } else {
                        Log.e(TAG, "No se pueden programar alarmas exactas en Android 12+")
                    }
                } else {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        notificationMillis,
                        pendingIntent
                    )
                }

                notificationCount++
                Log.d(TAG, "Notificación programada correctamente para índice $index")
            } catch (e: Exception) {
                Log.e(TAG, "Error al programar notificación en índice $index: ${e.message}", e)
            }
        }

        Log.d(TAG, "Se programaron $notificationCount notificaciones de hidrataciónn.")
    }

    override fun cancelNotifications(context: Context) {
        Log.d(TAG, "HidratacionNotificationService: Iniciando cancelación de notificaciones")

        try {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancelAll()
            Log.d(TAG, "HidratacionNotificationService: Todas las notificaciones canceladas")

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            // Cancelar hasta 6 notificaciones (una por horario)
            for (i in 0..5) {
                val intent = Intent(context, NotificationReceiver::class.java).apply {
                    action = NotificationConstants.NOTIFICATION_ACTION
                    putExtra("is_hydration", true)
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
                    Log.d(TAG, "HidratacionNotificationService: Alarma cancelada con ID ${NOTIFICATION_ID + i}")
                } catch (e: Exception) {
                    Log.e(TAG, "HidratacionNotificationService: Error al cancelar alarma ${NOTIFICATION_ID + i}: ${e.message}")
                }
            }

            // Cancelar acciones tipo START_TIMER_ACTION para los mismos 6 horarios
            for (i in 0..5) {
                val intent = Intent(context, NotificationReceiver::class.java).apply {
                    action = NotificationConstants.START_TIMER_ACTION
                    putExtra("is_hydration", true)
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
                    Log.d(TAG, "HidratacionNotificationService: Alarma de acción cancelada con ID ${NOTIFICATION_ID + i + 100}")
                } catch (e: Exception) {
                    Log.e(TAG, "HidratacionNotificationService: Error al cancelar alarma de acción ${NOTIFICATION_ID + i + 100}: ${e.message}")
                }
            }

            Log.d(TAG, "HidratacionNotificationService: Cancelación de notificaciones completada exitosamente")
        } catch (e: Exception) {
            Log.e(TAG, "HidratacionNotificationService: Error al cancelar notificaciones: ${e.message}", e)
        }
    }

}
