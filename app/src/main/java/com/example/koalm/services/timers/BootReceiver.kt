package com.example.koalm.services.timers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.koalm.services.notifications.NotificationScheduler

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Aquí reprogramamos todas las alarmas guardadas
            //NotificationScheduler.scheduleAllSavedNotifications(context)
        }
    }
}

