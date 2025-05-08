package com.example.koalm.services.notifications

import android.content.Context
import java.time.LocalDateTime

interface NotificationBase {
    val channelId: String
    val channelName: Int
    val channelDescription: Int
    val defaultTitle: Int
    val defaultText: Int
    
    fun scheduleNotification(
        context: Context,
        diasSeleccionados: List<Boolean>,
        hora: LocalDateTime,
        descripcion: String,
        durationMinutes: Long,
        additionalData: Map<String, Any> = emptyMap()
    )
    
    fun cancelNotifications(context: Context)
} 