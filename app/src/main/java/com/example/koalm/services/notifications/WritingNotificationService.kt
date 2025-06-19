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

class WritingNotificationService : NotificationBase() {
    companion object {
        private const val TAG = "WritingNotificationService"
        const val NOTIFICATION_ID = 1
    }

    override val channelId = "escritura_habito"
    override val channelName = R.string.notification_channel_name
    override val channelDescription = R.string.notification_channel_description
    override val defaultTitle = R.string.notification_title
    override val defaultText = R.string.notification_default_text
    override val notificationId = NOTIFICATION_ID

    override fun createNotificationIntent(
        context: Context,
        descripcion: String,
        diaSemana: Int,
        durationMinutes: Long,
        additionalData: Map<String, Any>
    ): Intent {
        return super.createNotificationIntent(context, descripcion, diaSemana, durationMinutes, additionalData).apply {
            putExtra("is_meditation", false)
            putExtra("is_reading", false)
            putExtra("is_digital_disconnect", false)
            putExtra("is_writing", true)
            putExtra("notification_title", context.getString(defaultTitle))
            putExtra("notification_action_button", context.getString(R.string.notification_notes_button))
        }
    }

    override fun scheduleNotification(
        context: Context,
        diasSeleccionados: List<Boolean>,
        hora: LocalDateTime,
        descripcion: String,
        durationMinutes: Long,
        additionalData: Map<String, Any>
    ) {
        Log.d(TAG, "Iniciando programación de notificación de escritura")
        Log.d(TAG, "Días seleccionados: ${diasSeleccionados.joinToString()}")
        Log.d(TAG, "Hora programada: ${hora.format(DateTimeFormatter.ofPattern("HH:mm"))}")
        Log.d(TAG, "Duración: $durationMinutes minutos")

        cancelNotifications(context)

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val now = LocalDateTime.now()
        var notificationCount = 0

        diasSeleccionados.forEachIndexed { index, seleccionado ->
            if (seleccionado) {
                Log.d(TAG, "Programando notificación para día $index")
                val dayOfWeek = DayOfWeek.of(index + 1)
                var nextNotificationTime = now.with(hora.toLocalTime())
                    .with(TemporalAdjusters.nextOrSame(dayOfWeek))

                if (nextNotificationTime.isBefore(now)) {
                    Log.d(TAG, "La hora ya pasó hoy, programando para la siguiente semana")
                    nextNotificationTime = nextNotificationTime.plusDays(1)
                }

                val notificationTimeMillis = nextNotificationTime
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()

                Log.d(TAG, "Tiempo de notificación: ${nextNotificationTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))}")

                val intent = Intent(context, NotificationReceiver::class.java).apply {
                    action = NotificationConstants.NOTIFICATION_ACTION
                    putExtra("descripcion", descripcion.ifEmpty { context.getString(defaultText) })
                    putExtra("dia_semana", index)
                    putExtra("duration_minutes", durationMinutes)
                    putExtra("is_meditation", false)
                    putExtra("is_reading", false)
                    putExtra("is_digital_disconnect", false)
                    putExtra("is_writing", true)
                    putExtra("notification_title", context.getString(defaultTitle))
                    putExtra("notification_action_button", context.getString(R.string.notification_notes_button))
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
                            Log.d(TAG, "Programando alarma exacta (Android 12+)")
                            alarmManager.setExactAndAllowWhileIdle(
                                AlarmManager.RTC_WAKEUP,
                                notificationTimeMillis,
                                pendingIntent
                            )
                        } else {
                            Log.e(TAG, "No se pueden programar alarmas exactas en Android 12+")
                        }
                    } else {
                        Log.d(TAG, "Programando alarma exacta (Android < 12)")
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            notificationTimeMillis,
                            pendingIntent
                        )
                    }

                    notificationCount++
                    Log.d(TAG, "Notificación programada exitosamente para día $index")
                } catch (e: Exception) {
                    Log.e(TAG, "Error al programar notificación para día $index: ${e.message}", e)
                }
            }
        }

        Log.d(TAG, "Se programaron $notificationCount notificaciones en total")
    }

    override fun cancelNotifications(context: Context) {
        Log.d(TAG, "Iniciando cancelación de notificaciones de escritura")

        try {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancelAll()
            Log.d(TAG, "Todas las notificaciones canceladas")

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            for (i in 0..6) {
                val intent = Intent(context, NotificationReceiver::class.java).apply {
                    action = NotificationConstants.NOTIFICATION_ACTION
                    putExtra("is_writing", true)
                }

                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    NOTIFICATION_ID + i,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )

                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
                Log.d(TAG, "Alarma cancelada con ID ${NOTIFICATION_ID + i}")
            }

            // Cancelar posibles alarmas de acción
            for (i in 0..6) {
                val actionIntent = Intent(context, NotificationReceiver::class.java).apply {
                    action = NotificationConstants.START_TIMER_ACTION
                    putExtra("is_writing", true)
                }

                val actionPendingIntent = PendingIntent.getBroadcast(
                    context,
                    NOTIFICATION_ID + i + 100,
                    actionIntent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )

                alarmManager.cancel(actionPendingIntent)
                actionPendingIntent.cancel()
                Log.d(TAG, "Alarma de acción cancelada con ID ${NOTIFICATION_ID + i + 100}")
            }

            Log.d(TAG, "Cancelación de notificaciones de escritura completada")
        } catch (e: Exception) {
            Log.e(TAG, "Error al cancelar notificaciones de escritura: ${e.message}", e)
        }
    }
}
