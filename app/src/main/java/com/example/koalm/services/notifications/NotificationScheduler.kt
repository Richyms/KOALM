package com.example.koalm.services.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.koalm.services.timers.NotificationReceiverPers
import java.util.Calendar

object NotificationScheduler {

    /**
     * Programa el recordatorio para el próximo día válido según la frecuencia semanal.
     * frecuencia: lista de 7 booleanos [domingo, lunes, ..., sábado]
     */
    fun scheduleHabitReminder(
        context: Context,
        habitId: String,
        habitName: String,
        hour: Int,
        minute: Int,
        reminderIndex: Int,
        frecuencia: List<Boolean> = List(7) { true } // Por defecto todos los días activos
    ) {
        Log.d("NotificationScheduler", "Iniciando programación de alarma para habitId=$habitId, nombre=$habitName, hora=$hour, minuto=$minute, index=$reminderIndex")
        Log.d("NotificationScheduler", "Frecuencia recibida (domingo a sábado): ${frecuencia.joinToString()}")

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, NotificationReceiverPers::class.java).apply {
            putExtra("habitId", habitId)
            putExtra("habitName", habitName)
            putExtra("reminderIndex", reminderIndex)
            putExtra("hour", hour)
            putExtra("minute", minute)
            putExtra("frecuencia", frecuencia.toBooleanArray())
        }

        val requestCode = (habitId + reminderIndex.toString()).hashCode()
        Log.d("NotificationScheduler", "RequestCode para PendingIntent: $requestCode")

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val currentCalendar = Calendar.getInstance()
        val currentDayOfWeek = currentCalendar.get(Calendar.DAY_OF_WEEK)  // 1=domingo ... 7=sábado
        val currentHour = currentCalendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = currentCalendar.get(Calendar.MINUTE)

        Log.d("NotificationScheduler", "Hora actual: díaSemana=$currentDayOfWeek, hora=$currentHour, minuto=$currentMinute")

        val receiver = NotificationReceiverPers()
        val nextDayOffset = receiver.getNextValidDayOffset(
            frecuencia,
            currentDayOfWeek,
            currentHour,
            currentMinute,
            hour,
            minute
        )
        Log.d("NotificationScheduler", "Días a añadir para siguiente día válido: $nextDayOffset")

        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            add(Calendar.DAY_OF_MONTH, nextDayOffset)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        Log.d("NotificationScheduler", "Hora programada para alarma (timestamp): ${calendar.timeInMillis} - Fecha: ${calendar.time}")

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )

        Log.d("NotificationScheduler", "Alarma programada correctamente para habitId=$habitId con hora ${calendar.time}")
    }


    fun cancelHabitReminder(
        context: Context,
        habitId: String,
        reminderIndex: Int
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, NotificationReceiverPers::class.java).apply {
            putExtra("habitId", habitId)
            putExtra("reminderIndex", reminderIndex)
        }

        val requestCode = (habitId + reminderIndex.toString()).hashCode()

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }



}
