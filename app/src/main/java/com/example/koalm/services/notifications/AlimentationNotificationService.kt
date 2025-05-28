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
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

class AlimentationNotificationService : NotificationBase() {
    companion object {
        private const val TAG = "KOALM_NOTIFICATIONS"
        const val NOTIFICATION_ID = 16  // Cambia el ID para que no choque con sueño
    }

    override val notificationId: Int
        get() = NOTIFICATION_ID

    override val channelId = "alimentation_habito"
    override val channelName = R.string.alimentation_notification_channel_name
    override val channelDescription = R.string.alimentation_notification_channel_description
    override val defaultTitle = R.string.alimentation_notification_title
    override val defaultText = R.string.alimentation_notification_default_text

    override fun scheduleNotification(
        context: Context,
        diasSeleccionados: List<Boolean>,
        hora: LocalDateTime,
        descripcion: String,
        durationMinutes: Long,
        additionalData: Map<String, Any>
    ) {
        Log.d(TAG, "AlimentacionNotificationService: Iniciando programación de notificación")
        Log.d(TAG, "AlimentacionNotificationService: Días seleccionados: ${diasSeleccionados.joinToString()}")
        Log.d(TAG, "AlimentacionNotificationService: Hora programada: ${hora.format(DateTimeFormatter.ofPattern("HH:mm"))}")
        Log.d(TAG, "AlimentacionNotificationService: Duración: $durationMinutes minutos")

        // Cancelar notificaciones existentes antes de programar nuevas
        Log.d(TAG, "AlimentacionNotificationService: Cancelando notificaciones existentes")
        cancelNotifications(context)

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        var notificationCount = 0

        val now = LocalDateTime.now()
        Log.d(TAG, "AlimentacionNotificationService: Hora actual: ${now.format(DateTimeFormatter.ofPattern("HH:mm"))}")

        // Programar para cada día seleccionado
        diasSeleccionados.forEachIndexed { index, seleccionado ->
            if (seleccionado) {
                Log.d(TAG, "AlimentacionNotificationService: Programando notificación para día $index")
                val dayOfWeek = DayOfWeek.of(index + 1)
                var nextNotificationTime = now.with(hora.toLocalTime())
                    .with(TemporalAdjusters.nextOrSame(dayOfWeek))

                if (nextNotificationTime.isBefore(now)) {
                    Log.d(TAG, "AlimentacionNotificationService: La hora ya pasó hoy, programando para mañana")
                    nextNotificationTime = nextNotificationTime.plusDays(1)
                }

                val notificationTimeMillis = nextNotificationTime
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()

                Log.d(TAG, "AlimentacionNotificationService: Tiempo de notificación: ${nextNotificationTime.format(
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))}")

                val intent = Intent(context, NotificationReceiver::class.java).apply {
                    action = NotificationConstants.NOTIFICATION_ACTION
                    putExtra("descripcion", descripcion.ifEmpty { context.getString(defaultText) })
                    putExtra("dia_semana", index)
                    putExtra("is_alimentation", true)
                    putExtra("is_sleeping", false)
                    putExtra("is_hidratation", false)
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
                            Log.d(TAG, "AlimentacionNotificationService: Programando alarma exacta (Android 12+)")
                            alarmManager.setExactAndAllowWhileIdle(
                                AlarmManager.RTC_WAKEUP,
                                notificationTimeMillis,
                                pendingIntent
                            )
                        } else {
                            Log.e(TAG, "AlimentacionNotificationService: No se pueden programar alarmas exactas en Android 12+")
                        }
                    } else {
                        Log.d(TAG, "AlimentacionNotificationService: Programando alarma exacta (Android < 12)")
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            notificationTimeMillis,
                            pendingIntent
                        )
                    }
                    notificationCount++
                    Log.d(TAG, "AlimentacionNotificationService: Notificación programada exitosamente para día $index")
                } catch (e: Exception) {
                    Log.e(TAG, "AlimentacionNotificationService: Error al programar notificación para día $index: ${e.message}", e)
                }
            }
        }

        Log.d(TAG, "AlimentacionNotificationService: Se programaron $notificationCount notificaciones en total")
    }

    override fun cancelNotifications(context: Context) {
        Log.d(TAG, "AlimentacionNotificationService: Iniciando cancelación de notificaciones")

        try {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancelAll()
            Log.d(TAG, "AlimentacionNotificationService: Todas las notificaciones canceladas")

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            for (i in 0..6) {
                val intent = Intent(context, NotificationReceiver::class.java).apply {
                    action = NotificationConstants.NOTIFICATION_ACTION
                    putExtra("is_alimentation", true)
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
                    Log.d(TAG, "AlimentacionNotificationService: Alarma cancelada con ID ${NOTIFICATION_ID + i}")
                } catch (e: Exception) {
                    Log.e(TAG, "AlimentacionNotificationService: Error al cancelar alarma ${NOTIFICATION_ID + i}: ${e.message}")
                }
            }

            for (i in 0..6) {
                val intent = Intent(context, NotificationReceiver::class.java).apply {
                    action = NotificationConstants.START_TIMER_ACTION
                    putExtra("is_alimentation", true)
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
                    Log.d(TAG, "AlimentacionNotificationService: Alarma de acción cancelada con ID ${NOTIFICATION_ID + i + 100}")
                } catch (e: Exception) {
                    Log.e(TAG, "AlimentacionNotificationService: Error al cancelar alarma de acción ${NOTIFICATION_ID + i + 100}: ${e.message}")
                }
            }

            Log.d(TAG, "AlimentacionNotificationService: Cancelación de notificaciones completada exitosamente")
        } catch (e: Exception) {
            Log.e(TAG, "AlimentacionNotificationService: Error al cancelar notificaciones: ${e.message}", e)
        }
    }
}
