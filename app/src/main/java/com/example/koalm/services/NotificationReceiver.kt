package com.example.koalm.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.koalm.MainActivity
import com.example.koalm.R
import java.time.LocalDateTime

class NotificationReceiver : BroadcastReceiver() {
    companion object {
        private const val CHANNEL_ID = "escritura_habito"
        private const val NOTIFICATION_ID = 100
        private const val TAG = "KOALM_NOTIFICATIONS"
        const val START_TIMER_ACTION = "com.example.koalm.START_TIMER_ACTION"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.e(TAG, "onReceive: Recibido broadcast para mostrar notificación")
        
        val descripcion = intent.getStringExtra("descripcion") ?: context.getString(R.string.notification_default_text)
        val diaSemana = intent.getIntExtra("dia_semana", -1)
        val durationMinutes = intent.getLongExtra("duration_minutes", 15)
        
        Log.e(TAG, "onReceive: Descripción: $descripcion, Día: $diaSemana, Duración: $durationMinutes minutos")
        
        // Crear intent para abrir la app
        val activityIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, activityIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Crear intent para iniciar el temporizador
        val timerIntent = Intent(context, WritingTimerService::class.java).apply {
            action = START_TIMER_ACTION
            putExtra("duration_minutes", durationMinutes)
        }
        Log.e(TAG, "onReceive: Creando intent para temporizador con duración: $durationMinutes minutos")
        
        val timerPendingIntent = PendingIntent.getService(
            context,
            NOTIFICATION_ID + diaSemana,
            timerIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Crear la notificación
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.notification_title))
            .setContentText(descripcion)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(
                R.drawable.ic_notification,
                context.getString(R.string.start_writing_action, durationMinutes),
                timerPendingIntent
            )
            .setVibrate(longArrayOf(0, 500)) // Vibra una sola vez por 500ms

        // Mostrar la notificación
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID + diaSemana, builder.build())
        
        Log.e(TAG, "onReceive: Notificación mostrada con ID ${NOTIFICATION_ID + diaSemana}")
    }
} 