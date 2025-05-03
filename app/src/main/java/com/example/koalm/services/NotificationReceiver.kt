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
import com.example.koalm.services.notifications.DigitalDisconnectNotificationService
import com.example.koalm.services.notifications.NotificationConstants
import com.example.koalm.services.notifications.MeditationNotificationService
import com.example.koalm.services.notifications.ReadingNotificationService
import com.example.koalm.services.notifications.WritingNotificationService

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
                val isReading = intent.getBooleanExtra("is_reading", false)
                val isDigitalDisconnect = intent.getBooleanExtra("is_digital_disconnect", false)
                
                when {
                    isMeditation -> showMeditationNotification(context, descripcion, diaSemana)
                    isReading -> showReadingNotification(context, descripcion, diaSemana, durationMinutes)
                    isDigitalDisconnect -> showDigitalDisconnectNotification(context, descripcion, diaSemana, durationMinutes)
                    else -> {
                        val notasHabilitadas = intent.getBooleanExtra("notas_habilitadas", false)
                        showWritingNotification(context, descripcion, diaSemana, durationMinutes, notasHabilitadas)
                    }
                }
            }
            NotificationConstants.START_TIMER_ACTION -> {
                val durationMinutes = intent.getLongExtra("duration_minutes", 0)
                val notasHabilitadas = intent.getBooleanExtra("notas_habilitadas", false)
                val isDigitalDisconnect = intent.getBooleanExtra("is_digital_disconnect", false)
                
                if (isDigitalDisconnect) {
                    startDigitalDisconnectTimer(context, durationMinutes)
                } else {
                    startWritingTimer(context, durationMinutes, notasHabilitadas)
                }
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

    private fun showReadingNotification(
        context: Context,
        descripcion: String,
        diaSemana: Int,
        durationMinutes: Long
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                ReadingNotificationService().channelId,
                context.getString(ReadingNotificationService().channelName),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(ReadingNotificationService().channelDescription)
            }
            notificationManager.createNotificationChannel(channel)
        }
        
        val startTimerIntent = Intent(context, NotificationReceiver::class.java).apply {
            action = NotificationConstants.START_TIMER_ACTION
            putExtra("duration_minutes", durationMinutes)
            putExtra("is_reading", true)
        }
        
        val startTimerPendingIntent = PendingIntent.getBroadcast(
            context,
            ReadingNotificationService.NOTIFICATION_ID + diaSemana,
            startTimerIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val openBooksIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("route", "libros")
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            addCategory(Intent.CATEGORY_LAUNCHER)
            action = Intent.ACTION_MAIN
        }
        
        val openBooksPendingIntent = PendingIntent.getActivity(
            context,
            ReadingNotificationService.NOTIFICATION_ID + diaSemana + 100,
            openBooksIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val notification = NotificationCompat.Builder(context, ReadingNotificationService().channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(ReadingNotificationService().defaultTitle))
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
                context.getString(R.string.notification_books_button),
                openBooksPendingIntent
            )
            .build()
        
        notificationManager.notify(ReadingNotificationService.NOTIFICATION_ID + diaSemana, notification)
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

    private fun showDigitalDisconnectNotification(
        context: Context,
        descripcion: String,
        diaSemana: Int,
        durationMinutes: Long
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                DigitalDisconnectNotificationService().channelId,
                context.getString(DigitalDisconnectNotificationService().channelName),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(DigitalDisconnectNotificationService().channelDescription)
            }
            notificationManager.createNotificationChannel(channel)
        }
        
        val startTimerIntent = Intent(context, NotificationReceiver::class.java).apply {
            action = NotificationConstants.START_TIMER_ACTION
            putExtra("duration_minutes", durationMinutes)
            putExtra("is_digital_disconnect", true)
        }
        
        val startTimerPendingIntent = PendingIntent.getBroadcast(
            context,
            DigitalDisconnectNotificationService.NOTIFICATION_ID + diaSemana,
            startTimerIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val openDisconnectIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("route", "desconexion")
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            addCategory(Intent.CATEGORY_LAUNCHER)
            action = Intent.ACTION_MAIN
        }
        
        val openDisconnectPendingIntent = PendingIntent.getActivity(
            context,
            DigitalDisconnectNotificationService.NOTIFICATION_ID + diaSemana + 100,
            openDisconnectIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val notification = NotificationCompat.Builder(context, DigitalDisconnectNotificationService().channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(DigitalDisconnectNotificationService().defaultTitle))
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
                context.getString(R.string.notification_disconnect_button),
                openDisconnectPendingIntent
            )
            .build()
        
        notificationManager.notify(DigitalDisconnectNotificationService.NOTIFICATION_ID + diaSemana, notification)
    }

    private fun startDigitalDisconnectTimer(context: Context, durationMinutes: Long) {
        val intent = Intent(context, DigitalDisconnectTimerService::class.java).apply {
            putExtra("duration_minutes", durationMinutes)
        }
        ContextCompat.startForegroundService(context, intent)
    }

    private fun startWritingTimer(context: Context, durationMinutes: Long, notasHabilitadas: Boolean) {
        val intent = Intent(context, WritingTimerService::class.java).apply {
            putExtra("duration_minutes", durationMinutes)
            putExtra("notas_habilitadas", notasHabilitadas)
            putExtra("is_reading", true)
        }
        ContextCompat.startForegroundService(context, intent)
    }
} 