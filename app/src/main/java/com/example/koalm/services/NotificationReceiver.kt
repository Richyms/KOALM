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
        val notasHabilitadas = intent.getBooleanExtra("notas_habilitadas", false)
        
        Log.e(TAG, "onReceive: Descripción: $descripcion, Día: $diaSemana, Duración: $durationMinutes minutos, Notas habilitadas: $notasHabilitadas")
        
        // Verificar si el temporizador ya está activo
        val checkTimerIntent = Intent(context, WritingTimerService::class.java).apply {
            action = WritingTimerService.CHECK_TIMER_ACTION
        }
        
        // Iniciar el servicio para verificar el estado
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(checkTimerIntent)
        } else {
            context.startService(checkTimerIntent)
        }
        
        // Crear intent para abrir la app
        val activityIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, activityIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Crear intent para iniciar el temporizador
        val startTimerIntent = Intent(context, WritingTimerService::class.java).apply {
            action = WritingTimerService.START_TIMER_ACTION
            putExtra("duration_minutes", durationMinutes)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        Log.e(TAG, "onReceive: Creando intent para temporizador con duración: $durationMinutes minutos")
        
        // Crear PendingIntent para la notificación
        val startTimerPendingIntent = PendingIntent.getService(
            context,
            NOTIFICATION_ID + diaSemana,
            startTimerIntent,
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
                startTimerPendingIntent
            )
            .setVibrate(longArrayOf(0, 500)) // Vibra una sola vez por 500ms

        // Agregar botón de notas si está habilitado
        if (notasHabilitadas) {
            val notesIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                action = "com.example.koalm.START_TIMER"
                putExtra("duration_minutes", durationMinutes)
                addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
            }
            Log.d(TAG, "Creando intent para notas con duración: $durationMinutes minutos")
            
            val notesPendingIntent = PendingIntent.getActivity(
                context,
                NOTIFICATION_ID + diaSemana + 1,
                notesIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            
            builder.addAction(
                R.drawable.ic_notification,
                context.getString(R.string.open_notes_action),
                notesPendingIntent
            )
        }

        // Mostrar la notificación
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID + diaSemana, builder.build())
        
        Log.e(TAG, "onReceive: Notificación mostrada con ID ${NOTIFICATION_ID + diaSemana}")
    }
} 