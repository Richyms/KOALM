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
import androidx.core.content.ContextCompat
import com.example.koalm.MainActivity
import com.example.koalm.R
import com.example.koalm.services.notifications.NotificationConstants
import java.util.concurrent.TimeUnit

class ReadingTimerService : Service() {
    companion object {
        private const val CHANNEL_ID = "lectura_timer"
        private const val NOTIFICATION_ID = 201
        private const val TAG = "KOALM_READING_TIMER"
        const val TIMER_ACTION = "com.example.koalm.READING_TIMER_ACTION"
        const val START_TIMER_ACTION = "com.example.koalm.START_READING_TIMER_ACTION"
        const val TIMER_UPDATE_ACTION = "com.example.koalm.READING_TIMER_UPDATE_ACTION"
        const val CHECK_TIMER_ACTION = "com.example.koalm.CHECK_READING_TIMER_ACTION"
        const val EXTRA_REMAINING_TIME = "remaining_time"
        const val EXTRA_IS_ACTIVE = "is_active"
    }

    private var timer: CountDownTimer? = null
    private var durationMinutes: Long = 0

    override fun onCreate() {
        super.onCreate()
        Log.e(TAG, "onCreate: Servicio de temporizador de lectura creado")
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.e(TAG, "onStartCommand: Iniciando servicio de temporizador de lectura")
        Log.e(TAG, "onStartCommand: Intent action: ${intent?.action}")
        Log.e(TAG, "onStartCommand: Intent extras: ${intent?.extras?.keySet()?.joinToString { "$it: ${intent.extras?.get(it)}" }}")
        
        try {
            durationMinutes = intent?.getLongExtra("duration_minutes", 0) ?: 0
            Log.e(TAG, "onStartCommand: Duración recibida: $durationMinutes minutos")
            
            if (durationMinutes <= 0) {
                Log.e(TAG, "onStartCommand: Duración inválida: $durationMinutes")
                stopSelf()
                return START_NOT_STICKY
            }
            
            Log.e(TAG, "onStartCommand: Iniciando servicio en primer plano")
            val notification = createNotification()
            startForeground(NOTIFICATION_ID, notification)
            Log.e(TAG, "onStartCommand: Servicio iniciado en primer plano, comenzando temporizador")
            startTimer()
            
            return START_NOT_STICKY
        } catch (e: Exception) {
            Log.e(TAG, "onStartCommand: Error al iniciar el servicio", e)
            stopSelf()
            return START_NOT_STICKY
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
        Log.e(TAG, "onDestroy: Servicio detenido")
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.reading_timer_channel_name)
            val descriptionText = getString(R.string.reading_timer_channel_description)
            Log.e(TAG, "createNotificationChannel: Creando canal con nombre: $name")
            Log.e(TAG, "createNotificationChannel: Descripción: $descriptionText")
            
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
            Log.e(TAG, "createNotificationChannel: Canal de notificación creado con ID: $CHANNEL_ID")
        }
    }

    private fun createNotification(): android.app.Notification {
        Log.e(TAG, "createNotification: Iniciando creación de notificación")
        
        try {
            val activityIntent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                action = START_TIMER_ACTION
                putExtra("duration_minutes", durationMinutes)
                putExtra("is_reading", true)
            }
            Log.e(TAG, "createNotification: Intent creado con action: ${activityIntent.action}")
            Log.e(TAG, "createNotification: Intent extras: ${activityIntent.extras?.keySet()?.joinToString { "$it: ${activityIntent.extras?.get(it)}" }}")
            
            val pendingIntent = PendingIntent.getActivity(
                this,
                0,
                activityIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            
            val notificationTitle = getString(R.string.reading_timer_notification_title)
            Log.e(TAG, "createNotification: Título de notificación: $notificationTitle")
            
            val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(notificationTitle)
                .setContentText("$durationMinutes:00")
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .setContentIntent(pendingIntent)
                .build()
                
            Log.e(TAG, "createNotification: Notificación creada exitosamente con canal: $CHANNEL_ID")
            return notification
        } catch (e: Exception) {
            Log.e(TAG, "createNotification: Error al crear la notificación: ${e.message}", e)
            throw e
        }
    }

    private fun startTimer() {
        Log.e(TAG, "startTimer: Iniciando temporizador de lectura de $durationMinutes minutos")
        Log.e(TAG, "startTimer: Tiempo total en milisegundos: ${durationMinutes * 60 * 1000}")
        
        timer?.cancel()
        timer = object : CountDownTimer(durationMinutes * 60 * 1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val minutes = millisUntilFinished / 1000 / 60
                val seconds = (millisUntilFinished / 1000) % 60
                val timeString = "$minutes:${String.format("%02d", seconds)}"
                Log.e(TAG, "onTick: Tiempo restante: $timeString")
                updateNotification(timeString)
            }

            override fun onFinish() {
                Log.e(TAG, "onFinish: Temporizador de lectura finalizado")
                showCompletionNotification()
                stopSelf()
            }
        }.start()
        Log.e(TAG, "startTimer: Temporizador iniciado correctamente")
    }

    private fun updateNotification(timeLeft: String) {
        Log.e(TAG, "updateNotification: Iniciando actualización de notificación")
        val notificationTitle = getString(R.string.reading_timer_notification_title)
        Log.e(TAG, "updateNotification: Título: $notificationTitle, Tiempo restante: $timeLeft")
        
        try {
            val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(notificationTitle)
                .setContentText(timeLeft)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .build()
                
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(NOTIFICATION_ID, notification)
            Log.e(TAG, "updateNotification: Notificación actualizada exitosamente con ID: $NOTIFICATION_ID")
        } catch (e: Exception) {
            Log.e(TAG, "updateNotification: Error al actualizar la notificación: ${e.message}", e)
        }
    }

    private fun showCompletionNotification() {
        Log.e(TAG, "showCompletionNotification: Mostrando notificación de finalización")
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
            val completionChannelId = "reading_completion_channel"
            val name = "Finalización del temporizador de lectura"
            val descriptionText = "Canal para notificaciones de finalización del temporizador de lectura"
            Log.e(TAG, "showCompletionNotification: Creando canal de finalización con nombre: $name")
            
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(completionChannelId, name, importance).apply {
                description = descriptionText
                enableVibration(true)
                enableLights(true)
                setVibrationPattern(longArrayOf(0, 500))
            }
            notificationManager.createNotificationChannel(channel)
            
            val completionTitle = getString(R.string.reading_timer_completion_title)
            val completionText = getString(R.string.reading_timer_completion_text)
            Log.e(TAG, "showCompletionNotification: Título de finalización: $completionTitle")
            Log.e(TAG, "showCompletionNotification: Texto de finalización: $completionText")
            
            // Usar el canal de alta prioridad para la notificación de finalización
            val builder = NotificationCompat.Builder(this, completionChannelId)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(completionTitle)
                .setContentText(completionText)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setVibrate(longArrayOf(0, 500))
                .setDefaults(NotificationCompat.DEFAULT_ALL)

            notificationManager.notify(NOTIFICATION_ID + 1, builder.build())
            Log.e(TAG, "showCompletionNotification: Notificación de finalización mostrada con canal: $completionChannelId")
        } else {
            val completionTitle = getString(R.string.reading_timer_completion_title)
            val completionText = getString(R.string.reading_timer_completion_text)
            Log.e(TAG, "showCompletionNotification: Título de finalización (Android < O): $completionTitle")
            
            // Para versiones anteriores a Android O
            val builder = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(completionTitle)
                .setContentText(completionText)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setVibrate(longArrayOf(0, 500))
                .setDefaults(NotificationCompat.DEFAULT_ALL)

            notificationManager.notify(NOTIFICATION_ID + 1, builder.build())
            Log.e(TAG, "showCompletionNotification: Notificación de finalización mostrada (Android < O)")
        }
    }
} 