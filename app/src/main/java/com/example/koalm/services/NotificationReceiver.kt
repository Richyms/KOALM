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
import androidx.core.content.ContextCompat
import com.example.koalm.MainActivity
import com.example.koalm.R
import com.example.koalm.services.notifications.NotificationConstants
import com.example.koalm.services.notifications.WritingNotificationService
import com.example.koalm.services.notifications.MeditationNotificationService

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val TAG = "NotificationReceiver"
        Log.e(TAG, "onReceive: Recibida notificación")
        
        when (intent.action) {
            NotificationConstants.NOTIFICATION_ACTION -> {
                val descripcion = intent.getStringExtra("descripcion") ?: ""
                val diaSemana = intent.getIntExtra("dia_semana", 0)
                val durationMinutes = intent.getLongExtra("duration_minutes", 0)
                val isMeditation = intent.getBooleanExtra("is_meditation", false)
                
                if (isMeditation) {
                    showMeditationNotification(context, descripcion, diaSemana)
                } else {
                    val notasHabilitadas = intent.getBooleanExtra("notas_habilitadas", false)
                    showWritingNotification(context, descripcion, diaSemana, durationMinutes, notasHabilitadas)
                }
            }
            NotificationConstants.START_TIMER_ACTION -> {
                val durationMinutes = intent.getLongExtra("duration_minutes", 0)
                val notasHabilitadas = intent.getBooleanExtra("notas_habilitadas", false)
                startWritingTimer(context, durationMinutes, notasHabilitadas)
            }
        }
    }

    private fun showMeditationNotification(
        context: Context,
        descripcion: String,
        diaSemana: Int
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                MeditationNotificationService().channelId,
                context.getString(MeditationNotificationService().channelName),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(MeditationNotificationService().channelDescription)
            }
            notificationManager.createNotificationChannel(channel)
        }
        
        val notification = NotificationCompat.Builder(context, MeditationNotificationService().channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(MeditationNotificationService().defaultTitle))
            .setContentText(descripcion)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        
        notificationManager.notify(MeditationNotificationService.NOTIFICATION_ID + diaSemana, notification)
    }

    private fun showWritingNotification(
        context: Context,
        descripcion: String,
        diaSemana: Int,
        durationMinutes: Long,
        notasHabilitadas: Boolean
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                WritingNotificationService().channelId,
                context.getString(WritingNotificationService().channelName),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(WritingNotificationService().channelDescription)
            }
            notificationManager.createNotificationChannel(channel)
        }
        
        val startTimerIntent = Intent(context, NotificationReceiver::class.java).apply {
            action = NotificationConstants.START_TIMER_ACTION
            putExtra("duration_minutes", durationMinutes)
            putExtra("notas_habilitadas", notasHabilitadas)
        }
        
        val startTimerPendingIntent = PendingIntent.getBroadcast(
            context,
            WritingNotificationService.NOTIFICATION_ID + diaSemana,
            startTimerIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val openNotesIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("route", "notas")
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            addCategory(Intent.CATEGORY_LAUNCHER)
            action = Intent.ACTION_MAIN
        }
        
        val openNotesPendingIntent = PendingIntent.getActivity(
            context,
            WritingNotificationService.NOTIFICATION_ID + diaSemana + 100,
            openNotesIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val notification = NotificationCompat.Builder(context, WritingNotificationService().channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(WritingNotificationService().defaultTitle))
            .setContentText(descripcion)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .addAction(
                R.drawable.ic_timer,
                context.getString(R.string.start_timer),
                startTimerPendingIntent
            )
            .addAction(
                R.drawable.ic_notification,
                context.getString(R.string.notification_notes_button),
                openNotesPendingIntent
            )
            .build()
        
        notificationManager.notify(WritingNotificationService.NOTIFICATION_ID + diaSemana, notification)
    }

    private fun startWritingTimer(context: Context, durationMinutes: Long, notasHabilitadas: Boolean) {
        val intent = Intent(context, WritingTimerService::class.java).apply {
            putExtra("duration_minutes", durationMinutes)
            putExtra("notas_habilitadas", notasHabilitadas)
        }
        ContextCompat.startForegroundService(context, intent)
    }
} 