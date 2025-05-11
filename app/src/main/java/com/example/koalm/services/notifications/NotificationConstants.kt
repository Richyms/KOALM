package com.example.koalm.services.notifications

object NotificationConstants {
    // Acciones
    const val NOTIFICATION_ACTION = "com.example.koalm.NOTIFICATION_ACTION"
    const val START_TIMER_ACTION = "com.example.koalm.START_TIMER_ACTION"
    const val OPEN_NOTES_ACTION = "com.example.koalm.OPEN_NOTES"
    const val TIMER_UPDATE_ACTION = "com.example.koalm.TIMER_UPDATE_ACTION"
    const val CHECK_TIMER_ACTION = "com.example.koalm.CHECK_TIMER_ACTION"
    
    // Extras
    const val EXTRA_DURATION = "duration_minutes"
    const val EXTRA_IS_READING = "is_reading"
    const val EXTRA_IS_MEDITATION = "is_meditation"
    const val EXTRA_IS_DIGITAL_DISCONNECT = "is_digital_disconnect"
    const val EXTRA_NOTES_ENABLED = "notas_habilitadas"
    const val EXTRA_REMAINING_TIME = "remaining_time"
    const val EXTRA_IS_ACTIVE = "is_active"
    
    // IDs de canales
    const val WRITING_CHANNEL_ID = "escritura_timer"
    const val READING_CHANNEL_ID = "lectura_timer"
    const val MEDITATION_CHANNEL_ID = "meditacion_timer"
    const val DIGITAL_DISCONNECT_CHANNEL_ID = "desconexion_timer"
    
    // IDs de notificaciones
    const val NOTIFICATION_ID = 1
} 

