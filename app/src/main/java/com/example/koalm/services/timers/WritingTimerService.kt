package com.example.koalm.services.timers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.CountDownTimer
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.koalm.MainActivity
import com.example.koalm.R
import com.example.koalm.services.notifications.NotificationConstants
import com.example.koalm.services.notifications.WritingNotificationService

class WritingTimerService : Service() {
    companion object {
        private const val CHANNEL_ID = NotificationConstants.WRITING_CHANNEL_ID
        private const val NOTIFICATION_ID = NotificationConstants.NOTIFICATION_ID
        private const val TAG = "KOALM_WRITING_TIMER"
    }

    private var timer: CountDownTimer? = null
    private var durationMinutes: Long = 0
    private var notasHabilitadas: Boolean = false

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "WritingTimerService creado")
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand llamado")
        
        durationMinutes = intent?.getLongExtra(NotificationConstants.EXTRA_DURATION, 0) ?: 0
        notasHabilitadas = intent?.getBooleanExtra(NotificationConstants.EXTRA_NOTES_ENABLED, false) ?: false
        
        if (durationMinutes <= 0) {
            Log.e(TAG, "Duración inválida: $durationMinutes")
            stopSelf()
            return START_NOT_STICKY
        }
        
        startForeground(NOTIFICATION_ID, createNotification())
        startTimer()
        
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
        Log.d(TAG, "Servicio detenido")
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.timer_channel_name)
            val descriptionText = getString(R.string.timer_channel_description)
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableVibration(false)
                enableLights(true)
                setVibrationPattern(null)
                setShowBadge(false)
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            Log.d(TAG, "Canal de notificación creado: $CHANNEL_ID")
        }
    }

    private fun createNotification(): android.app.Notification {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                WritingNotificationService().channelId,
                getString(WritingNotificationService().channelName),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(WritingNotificationService().channelDescription)
            }
            notificationManager.createNotificationChannel(channel)
        }
        
        val activityIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            action = NotificationConstants.START_TIMER_ACTION
            putExtra(NotificationConstants.EXTRA_DURATION, durationMinutes)
            putExtra(NotificationConstants.EXTRA_NOTES_ENABLED, notasHabilitadas)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            activityIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        return NotificationCompat.Builder(this, WritingNotificationService().channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(getString(R.string.timer_notification_title))
            .setContentText("$durationMinutes:00")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .build()
    }

    private fun startTimer() {
        Log.d(TAG, "Iniciando temporizador de $durationMinutes minutos")
        
        timer?.cancel()
        timer = object : CountDownTimer(durationMinutes * 60 * 1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val minutes = millisUntilFinished / 1000 / 60
                val seconds = (millisUntilFinished / 1000) % 60
                updateNotification("$minutes:${String.format("%02d", seconds)}")
            }

            override fun onFinish() {
                Log.d(TAG, "Temporizador finalizado")
                showCompletionNotification()
                stopSelf()
            }
        }.start()
    }

    private fun updateNotification(timeLeft: String) {
        val notification = NotificationCompat.Builder(this, WritingNotificationService().channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(getString(R.string.timer_notification_title))
            .setContentText(timeLeft)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
            
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun showCompletionNotification() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Cancelar la notificación del temporizador
        notificationManager.cancel(NOTIFICATION_ID)
        
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Crear un canal de notificación específico para la finalización con alta prioridad
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val completionChannelId = "completion_channel"
            val name = "Finalización del temporizador"
            val descriptionText = "Canal para notificaciones de finalización del temporizador"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(completionChannelId, name, importance).apply {
                description = descriptionText
                enableVibration(true)
                enableLights(true)
                setVibrationPattern(longArrayOf(0, 500))
            }
            notificationManager.createNotificationChannel(channel)
            
            // Usar el canal de alta prioridad para la notificación de finalización
            val builder = NotificationCompat.Builder(this, completionChannelId)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(getString(R.string.timer_completion_title))
                .setContentText(getString(R.string.timer_completion_text))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setVibrate(longArrayOf(0, 500))
                .setDefaults(NotificationCompat.DEFAULT_ALL)

            notificationManager.notify(NOTIFICATION_ID + 1, builder.build())
        } else {
            // Para versiones anteriores a Android O
            val builder = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(getString(R.string.timer_completion_title))
                .setContentText(getString(R.string.timer_completion_text))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setVibrate(longArrayOf(0, 500))
                .setDefaults(NotificationCompat.DEFAULT_ALL)

            notificationManager.notify(NOTIFICATION_ID + 1, builder.build())
        }
        
        Log.d(TAG, "Notificación de finalización mostrada")
    }
} 