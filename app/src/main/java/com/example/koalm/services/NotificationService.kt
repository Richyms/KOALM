package com.example.koalm.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import com.example.koalm.R
import com.example.koalm.services.notifications.MeditationNotificationService
import com.example.koalm.services.notifications.NotificationBase
import com.example.koalm.services.notifications.WritingNotificationService
import java.time.LocalDateTime

class NotificationService : Service() {
    companion object {
        private const val TAG = "KOALM_NOTIFICATIONS"
    }

    private val notificationServices: Map<String, NotificationBase> = mapOf(
        "escritura" to WritingNotificationService(),
        "meditacion" to MeditationNotificationService()
    )

    override fun onCreate() {
        super.onCreate()
        Log.e(TAG, "onCreate: Servicio de notificaciones creado")
        createNotificationChannels()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannels() {
        Log.e(TAG, "createNotificationChannels: Creando canales de notificaciones")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            notificationServices.values.forEach { service ->
                val channel = NotificationChannel(
                    service.channelId,
                    getString(service.channelName),
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = getString(service.channelDescription)
                    enableVibration(true)
                    enableLights(true)
                }
                notificationManager.createNotificationChannel(channel)
            }
            
            Log.e(TAG, "createNotificationChannels: Canales creados")
        }
    }

    fun scheduleNotification(
        context: Context,
        diasSeleccionados: List<Boolean>,
        hora: LocalDateTime,
        descripcion: String,
        durationMinutes: Long,
        notasHabilitadas: Boolean,
        isMeditation: Boolean = false
    ) {
        val service = if (isMeditation) "meditacion" else "escritura"
        val additionalData = if (isMeditation) 
            mapOf("sonidos_habilitados" to notasHabilitadas)
        else 
            mapOf("notas_habilitadas" to notasHabilitadas)
            
        notificationServices[service]?.scheduleNotification(
            context = context,
            diasSeleccionados = diasSeleccionados,
            hora = hora,
            descripcion = descripcion,
            durationMinutes = durationMinutes,
            additionalData = additionalData
        )
    }
} 