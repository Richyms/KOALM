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
import com.example.koalm.services.notifications.DigitalDisconnectNotificationService
import com.example.koalm.services.notifications.NotificationConstants

class DigitalDisconnectTimerService : Service() {
    companion object {
        private const val TAG = "DigitalDisconnectTimer"
        const val TIMER_UPDATE_ACTION = "com.example.koalm.DIGITAL_DISCONNECT_TIMER_UPDATE"
        const val EXTRA_REMAINING_TIME = "remaining_time"
        private const val NOTIFICATION_ID = 5000
    }

    private var timer: CountDownTimer? = null
    private var timeLeftInMillis: Long = 0

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate: Iniciando servicio de temporizador de desconexión digital")
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand: Recibido comando de inicio")
        val durationMinutes = intent?.getLongExtra("duration_minutes", 15) ?: 15
        Log.d(TAG, "onStartCommand: Duración configurada: $durationMinutes minutos")
        
        timeLeftInMillis = durationMinutes * 60 * 1000
        Log.d(TAG, "onStartCommand: Tiempo total en milisegundos: $timeLeftInMillis")

        startForeground(NOTIFICATION_ID, createNotification(timeLeftInMillis))
        startTimer()

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        Log.d(TAG, "createNotificationChannel: Creando canal de notificación")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                DigitalDisconnectNotificationService().channelId,
                getString(DigitalDisconnectNotificationService().channelName),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(DigitalDisconnectNotificationService().channelDescription)
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            Log.d(TAG, "createNotificationChannel: Canal creado exitosamente")
        }
    }

    private fun createNotification(timeLeftInMillis: Long): android.app.Notification {
        Log.d(TAG, "createNotification: Creando notificación con tiempo restante: $timeLeftInMillis ms")
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("route", "desconexion")
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val minutes = (timeLeftInMillis / 1000) / 60
        val seconds = (timeLeftInMillis / 1000) % 60
        val timeLeftFormatted = String.format("%02d:%02d", minutes, seconds)
        Log.d(TAG, "createNotification: Tiempo formateado: $timeLeftFormatted")

        return NotificationCompat.Builder(this, DigitalDisconnectNotificationService().channelId)
            .setContentTitle(getString(R.string.digital_disconnect_timer_title))
            .setContentText("$timeLeftFormatted restantes")
            .setSmallIcon(R.drawable.ic_notification)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .build()
    }

    private fun startTimer() {
        Log.d(TAG, "startTimer: Iniciando temporizador con duración: $timeLeftInMillis ms")
        timer?.cancel()
        timer = object : CountDownTimer(timeLeftInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
                Log.d(TAG, "onTick: Tiempo restante: $millisUntilFinished ms")
                
                val notification = createNotification(millisUntilFinished)
                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.notify(NOTIFICATION_ID, notification)

                // Enviar actualización del temporizador
                val updateIntent = Intent(TIMER_UPDATE_ACTION).apply {
                    putExtra(EXTRA_REMAINING_TIME, millisUntilFinished)
                }
                Log.d(TAG, "onTick: Enviando broadcast de actualización")
                sendBroadcast(updateIntent)
            }

            override fun onFinish() {
                Log.d(TAG, "onFinish: Temporizador finalizado")
                showCompletionNotification()
                stopSelf()
            }
        }.start()
        Log.d(TAG, "startTimer: Temporizador iniciado exitosamente")
    }

    private fun showCompletionNotification() {
        Log.d(TAG, "showCompletionNotification: Mostrando notificación de finalización")
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Cancelar la notificación del temporizador
        notificationManager.cancel(NOTIFICATION_ID)
        Log.d(TAG, "showCompletionNotification: Notificación del temporizador cancelada")
        
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("route", "desconexion")
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Crear un canal de notificación específico para la finalización con alta prioridad
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val completionChannelId = "digital_disconnect_completion"
            val name = "Finalización de desconexión digital"
            val descriptionText = "Canal para notificaciones de finalización de desconexión digital"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(completionChannelId, name, importance).apply {
                description = descriptionText
                enableVibration(true)
                enableLights(true)
                setVibrationPattern(longArrayOf(0, 500))
            }
            notificationManager.createNotificationChannel(channel)
            Log.d(TAG, "showCompletionNotification: Canal de finalización creado")
            
            val builder = NotificationCompat.Builder(this, completionChannelId)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(getString(R.string.digital_disconnect_completion_title))
                .setContentText(getString(R.string.digital_disconnect_completion_text))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setVibrate(longArrayOf(0, 500))
                .setDefaults(NotificationCompat.DEFAULT_ALL)

            notificationManager.notify(NOTIFICATION_ID + 1, builder.build())
            Log.d(TAG, "showCompletionNotification: Notificación de finalización mostrada")
        }
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy: Deteniendo servicio de temporizador")
        timer?.cancel()
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NOTIFICATION_ID)
        Log.d(TAG, "onDestroy: Servicio detenido y notificaciones canceladas")
        super.onDestroy()
    }
} 