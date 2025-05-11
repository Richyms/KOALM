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
import com.example.koalm.R
import com.example.koalm.services.notifications.NotificationConstants

class DigitalDisconnectTimerService : Service() {
    private var timer: CountDownTimer? = null
    private val TAG = "KOALM_DIGITAL_DISCONNECT_TIMER"

    companion object {
        private const val CHANNEL_ID = NotificationConstants.DIGITAL_DISCONNECT_CHANNEL_ID
        private const val NOTIFICATION_ID = NotificationConstants.NOTIFICATION_ID
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "DigitalDisconnectTimerService creado")
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand llamado")
        try {
            intent?.let {
                val duration = it.getLongExtra("duration_minutes", 0)
                Log.d(TAG, "Duración recibida: $duration minutos")
                
                if (duration <= 0) {
                    Log.e(TAG, "Duración inválida: $duration")
                    stopSelf()
                    return START_NOT_STICKY
                }
                
                val notification = createNotification(duration)
                startForeground(NOTIFICATION_ID, notification)
                startTimer(duration)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al iniciar el servicio", e)
            stopSelf()
            return START_NOT_STICKY
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        Log.d(TAG, "onDestroy llamado")
        timer?.cancel()
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.digital_disconnect_timer_channel_name)
            val descriptionText = getString(R.string.digital_disconnect_timer_channel_description)
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableVibration(false)
                enableLights(false)
                setShowBadge(false)
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            Log.d(TAG, "Canal de notificación creado: $CHANNEL_ID")
        }
    }

    private fun createNotification(durationMinutes: Long): android.app.Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.digital_disconnect_timer_title))
            .setSmallIcon(R.drawable.ic_notification)
            .setContentText(getString(R.string.digital_disconnect_timer_text, durationMinutes))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }

    private fun startTimer(durationMinutes: Long) {
        Log.d(TAG, "Iniciando temporizador para $durationMinutes minutos")
        val durationMillis = durationMinutes * 60 * 1000L
        Log.d(TAG, "Duración en milisegundos: $durationMillis")

        timer?.cancel()
        timer = object : CountDownTimer(durationMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val minutes = millisUntilFinished / 1000 / 60
                val seconds = millisUntilFinished / 1000 % 60
                val timeLeft = String.format("%02d:%02d", minutes, seconds)
                Log.d(TAG, "Tiempo restante: $timeLeft")
                updateNotification(timeLeft)
            }

            override fun onFinish() {
                Log.d(TAG, "Temporizador finalizado")
                showCompletionNotification()
                stopSelf()
            }
        }.start()
    }

    private fun updateNotification(timeLeft: String) {
        try {
            Log.d(TAG, "Actualizando notificación con tiempo restante: $timeLeft")
            val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(getString(R.string.digital_disconnect_timer_title))
                .setSmallIcon(R.drawable.ic_notification)
                .setContentText(getString(R.string.digital_disconnect_timer_remaining_text, timeLeft))
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .setSilent(true)
                .build()

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(NOTIFICATION_ID, notification)
            Log.d(TAG, "Notificación actualizada correctamente")
        } catch (e: Exception) {
            Log.e(TAG, "Error al actualizar la notificación", e)
        }
    }

    private fun showCompletionNotification() {
        try {
            Log.d(TAG, "Mostrando notificación de finalización")
            val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(getString(R.string.digital_disconnect_timer_completion_title))
                .setContentText(getString(R.string.digital_disconnect_timer_completion_text))
                .setSmallIcon(R.drawable.ic_notification)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .build()

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(NOTIFICATION_ID, notification)
            Log.d(TAG, "Notificación de finalización mostrada correctamente")
        } catch (e: Exception) {
            Log.e(TAG, "Error al mostrar la notificación de finalización", e)
        }
    }
} 