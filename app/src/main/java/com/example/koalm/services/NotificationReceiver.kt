package com.example.koalm.services.timers

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
import com.example.koalm.services.timers.DigitalDisconnectTimerService
import com.example.koalm.services.timers.MeditationTimerService
import com.example.koalm.services.timers.ReadingTimerService
import com.example.koalm.services.timers.WritingTimerService

class NotificationReceiver : BroadcastReceiver() {
    companion object {
        private const val TAG = "KOALM_NOTIFICATIONS"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Received intent: ${intent.action}")
        Log.d(TAG, "Extras: ${intent.extras?.keySet()?.joinToString { "$it=${intent.extras?.get(it)}" }}")

        when (intent.action) {
            NotificationConstants.START_TIMER_ACTION -> {
                val duration = intent.getLongExtra("duration_minutes", 0)
                val isAlimentation= intent.getBooleanExtra("is_alimentation",false)
                val isReading = intent.getBooleanExtra("is_reading", false)
                val isMeditation = intent.getBooleanExtra("is_meditation", false)
                val isDigitalDisconnect = intent.getBooleanExtra("is_digital_disconnect", false)
                val isSleeping = intent.getBooleanExtra("is_sleeping", false)
                val descripcion = intent.getStringExtra("descripcion") ?: ""
                val diaSemana = intent.getIntExtra("dia_semana", 0)
                val notasHabilitadas = intent.getBooleanExtra("notas_habilitadas", false)
                val durationMinutes = intent.getLongExtra("duration_minutes", 0)
                when {
                    isSleeping -> showSleepNotification(context, descripcion, diaSemana)
                    isMeditation -> showMeditationNotification(context, descripcion, diaSemana, durationMinutes)
                    isReading -> showReadingNotification(context, descripcion, diaSemana, durationMinutes)
                    isDigitalDisconnect -> showDigitalDisconnectNotification(context, descripcion, diaSemana, durationMinutes)
                    isAlimentation -> showAlimentationNotification(context,descripcion)
                    else -> showWritingNotification(context, descripcion, diaSemana, durationMinutes, notasHabilitadas)
                }


                Log.d(TAG, "Starting timer with duration: $duration, isReading: $isReading, isMeditation: $isMeditation, isDigitalDisconnect: $isDigitalDisconnect")

                if (!isSleeping) {
                    when {
                        isReading -> startReadingTimer(context, duration)
                        isMeditation -> startMeditationTimer(context, duration)
                        isDigitalDisconnect -> startDigitalDisconnectTimer(context, duration)
                        else -> startWritingTimer(context, duration)
                    }
                }

            }

            NotificationConstants.OPEN_NOTES_ACTION -> {
                Log.d(TAG, "Opening notes")
                val intent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    putExtra("open_notes", true)
                }
                context.startActivity(intent)
            }
            NotificationConstants.NOTIFICATION_ACTION -> {
                val descripcion = intent.getStringExtra("descripcion") ?: ""
                val diaSemana = intent.getIntExtra("dia_semana", 0)
                val durationMinutes = intent.getLongExtra("duration_minutes", 0)
                val isMeditation = intent.getBooleanExtra("is_meditation", false)
                val isReading = intent.getBooleanExtra("is_reading", false)
                val isDigitalDisconnect = intent.getBooleanExtra("is_digital_disconnect", false)
                val notasHabilitadas = intent.getBooleanExtra("notas_habilitadas", false)
                val isAlimentation = intent.getBooleanExtra("is_alimentation",false)
                val isSleeping = intent.getBooleanExtra("is_sleeping", false)

                Log.d(TAG, "Flags de tipo: isMeditation=$isMeditation, isReading=$isReading, isDigitalDisconnect=$isDigitalDisconnect")

                val tipoNotificacion = when {
                    isMeditation -> "meditación"
                    isReading -> "lectura"
                    isDigitalDisconnect -> "desconexión digital"
                    isSleeping->"sueno"
                    isAlimentation->"alimentation"
                    else -> "escritura"
                }
                Log.d(TAG, "Tipo de notificación determinado: $tipoNotificacion")

                when {
                    isMeditation -> showMeditationNotification(context, descripcion, diaSemana, durationMinutes)
                    isReading -> showReadingNotification(context, descripcion, diaSemana, durationMinutes)
                    isDigitalDisconnect -> showDigitalDisconnectNotification(context, descripcion, diaSemana, durationMinutes)
                    isSleeping->showSleepNotification(context,descripcion,diaSemana)
                    else -> showWritingNotification(context, descripcion, diaSemana, durationMinutes, notasHabilitadas)
                }
            }
        }
    }
    private fun showAlimentationNotification(
        context: Context,
        descripcion: String
    ) {
        Log.d(TAG, "Mostrando notificación de alimentación")
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channelId = "alimentation_habito"
        val channelName = "Recordatorio de alimentación"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones para hábitos de alimentación"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("route", "alimentation")
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            addCategory(Intent.CATEGORY_LAUNCHER)
            action = Intent.ACTION_MAIN
        }

        val openPendingIntent = PendingIntent.getActivity(
            context,
            16,  // ID arbitrario para alimentación
            openIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Hora de alimentarse")
            .setContentText(descripcion)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(openPendingIntent)
            .build()

        notificationManager.notify(900, notification)
    }

    private fun showSleepNotification(
        context: Context,
        descripcion: String,
        diaSemana: Int
    ) {
        Log.d(TAG, "Mostrando notificación de sueño")
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "sueño_habito",  // Usar el mismo channelId que definiste en sueñoNotificationService
                "Recordatorio de sueño",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones para hábitos de sueño"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, "sueño_habito")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Es hora de dormir")
            .setContentText(descripcion)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(8 + diaSemana, notification)  // Usar el mismo ID base que en sueñoNotificationService
    }
    private fun showMeditationNotification(
        context: Context,
        descripcion: String,
        diaSemana: Int,
        durationMinutes: Long
    ) {
        Log.d(TAG, "Mostrando notificación de meditación")
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
            Log.d(TAG, "Canal de notificación de meditación creado")
        }

        val startTimerIntent = Intent(context, NotificationReceiver::class.java).apply {
            action = NotificationConstants.START_TIMER_ACTION
            putExtra("duration_minutes", durationMinutes)
            putExtra("is_meditation", true)
        }
        
        val startTimerPendingIntent = PendingIntent.getBroadcast(
            context,
            MeditationNotificationService.NOTIFICATION_ID + diaSemana,
            startTimerIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
/*
        val historyIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("route", "historial_meditacion")
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            addCategory(Intent.CATEGORY_LAUNCHER)
            action = Intent.ACTION_MAIN
        }

        
        val historyPendingIntent = PendingIntent.getActivity(
            context,
            MeditationNotificationService.NOTIFICATION_ID + diaSemana + 100,
            historyIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
         */
        val notification = NotificationCompat.Builder(context, MeditationNotificationService().channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(MeditationNotificationService().defaultTitle))
            .setContentText(descripcion)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .addAction(
                R.drawable.ic_timer,
                context.getString(R.string.start_timer),
                startTimerPendingIntent
            )
            /*
            .addAction(
                R.drawable.ic_notification,
                context.getString(R.string.historial_meditacion),
                historyPendingIntent
            )
             */
            .build()
        
        notificationManager.notify(MeditationNotificationService.NOTIFICATION_ID + diaSemana, notification)
        Log.d(TAG, "Notificación de meditación mostrada con ID: ${MeditationNotificationService.NOTIFICATION_ID + diaSemana}")
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

        /*
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
        */
        
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
            /*
            .addAction(More actions
                R.drawable.ic_notification,
                context.getString(R.string.notification_books_button),
                openBooksPendingIntent
            )
            */
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
        }
        
        val startTimerPendingIntent = PendingIntent.getBroadcast(
            context,
            WritingNotificationService.NOTIFICATION_ID + diaSemana,
            startTimerIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        /*
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
        */
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
        /*
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
        */
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
            /*
            .addAction(
                R.drawable.ic_notification,
                context.getString(R.string.notification_disconnect_button),
                openDisconnectPendingIntent
            )
            */
            .build()
        
        notificationManager.notify(DigitalDisconnectNotificationService.NOTIFICATION_ID + diaSemana, notification)
    }

    private fun startDigitalDisconnectTimer(context: Context, duration: Long) {
        Log.d(TAG, "Starting digital disconnect timer for $duration minutes")
        val intent = Intent(context, DigitalDisconnectTimerService::class.java).apply {
            putExtra(NotificationConstants.EXTRA_DURATION, duration)
        }
        try {
            context.startForegroundService(intent)
            Log.d(TAG, "Digital disconnect timer service started successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error starting digital disconnect timer service", e)
        }
    }

    private fun startWritingTimer(context: Context, duration: Long) {
        Log.d(TAG, "Starting writing timer for $duration minutes")
        val intent = Intent(context, WritingTimerService::class.java).apply {
            putExtra(NotificationConstants.EXTRA_DURATION, duration)
        }
        try {
            context.startForegroundService(intent)
            Log.d(TAG, "Writing timer service started successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error starting writing timer service", e)
        }
    }

    private fun startMeditationTimer(context: Context, duration: Long) {
        Log.d(TAG, "Starting meditation timer for $duration minutes")
        val intent = Intent(context, MeditationTimerService::class.java).apply {
            putExtra(NotificationConstants.EXTRA_DURATION, duration)
        }
        try {
            context.startForegroundService(intent)
            Log.d(TAG, "Meditation timer service started successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error starting meditation timer service", e)
        }
    }

    private fun startReadingTimer(context: Context, duration: Long) {
        Log.d(TAG, "Starting reading timer for $duration minutes")
        try {
            val intent = Intent(context, ReadingTimerService::class.java).apply {
                putExtra("duration_minutes", duration)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
            Log.d(TAG, "Reading timer service started successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error starting reading timer service", e)
        }
    }
} 