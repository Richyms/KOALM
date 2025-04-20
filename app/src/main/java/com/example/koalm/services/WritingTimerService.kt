package com.example.koalm.services

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
import java.util.concurrent.TimeUnit

class WritingTimerService : Service() {
    companion object {
        private const val CHANNEL_ID = "escritura_timer"
        private const val NOTIFICATION_ID = 200
        private const val TAG = "KOALM_TIMER"
        const val TIMER_ACTION = "com.example.koalm.TIMER_ACTION"
    }

    private var timer: CountDownTimer? = null
    private var durationMinutes: Long = 0

    override fun onCreate() {
        super.onCreate()
        Log.e(TAG, "onCreate: Servicio de temporizador creado")
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.e(TAG, "onStartCommand: Iniciando temporizador")
        
        try {
            // Extraer la duración inmediatamente
            durationMinutes = intent?.getLongExtra("duration_minutes", 15) ?: 15
            Log.e(TAG, "onStartCommand: Duración recibida: $durationMinutes minutos")
            
            // Crear y mostrar la notificación inicial inmediatamente
            val initialNotification = createInitialNotification(durationMinutes)
            startForeground(NOTIFICATION_ID, initialNotification)
            
            // Iniciar el temporizador en segundo plano
            startTimer(durationMinutes)
            
        } catch (e: Exception) {
            Log.e(TAG, "onStartCommand: Error al iniciar temporizador", e)
            stopSelf()
            return START_NOT_STICKY
        }

        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
        Log.e(TAG, "onDestroy: Servicio de temporizador destruido")
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
            Log.e(TAG, "createNotificationChannel: Canal de notificación creado")
        }
    }

    private fun createTimerNotification(
        minutes: Long,
        millisUntilFinished: Long
    ): android.app.Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val minutesRemaining = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)
        val secondsRemaining = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60
        val timeRemainingText = String.format("%02d:%02d", minutesRemaining, secondsRemaining)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(getString(R.string.timer_notification_title))
            .setContentText(getString(R.string.timer_remaining_text, timeRemainingText))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(false)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setVibrate(null)
            .setOnlyAlertOnce(true)
            .build()
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
        
        Log.e(TAG, "showCompletionNotification: Notificación de finalización mostrada")
    }

    private fun startTimer(minutes: Long) {
        val totalMillis = TimeUnit.MINUTES.toMillis(minutes)
        Log.e(TAG, "startTimer: Tiempo total en milisegundos: $totalMillis")
        
        // Cancelar temporizador existente si hay uno
        timer?.cancel()
        
        // Iniciar temporizador
        timer = object : CountDownTimer(totalMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val minutesRemaining = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)
                val secondsRemaining = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60
                
                Log.d(TAG, "onTick: Tiempo restante: $minutesRemaining:$secondsRemaining")
                
                // Actualizar la notificación con el tiempo restante
                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                val updatedNotification = createTimerNotification(
                    minutesRemaining,
                    millisUntilFinished
                )
                notificationManager.notify(NOTIFICATION_ID, updatedNotification)
            }

            override fun onFinish() {
                Log.e(TAG, "onFinish: Temporizador completado")
                // Mostrar notificación de finalización
                showCompletionNotification()
                stopSelf()
            }
        }.start()
        
        Log.e(TAG, "startTimer: Temporizador iniciado correctamente")
    }

    private fun createInitialNotification(minutes: Long): android.app.Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(getString(R.string.timer_notification_title))
            .setContentText(getString(R.string.timer_notification_text, minutes))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(false)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setVibrate(null)
            .setOnlyAlertOnce(true)
            .build()
    }
} 