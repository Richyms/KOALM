package com.example.koalm.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class TimerUpdateReceiver : BroadcastReceiver() {
    companion object {
        private const val TAG = "TimerUpdateReceiver"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d(TAG, "Recibida actualización del temporizador: ${intent?.action}")
        if (intent?.action == WritingTimerService.TIMER_UPDATE_ACTION) {
            val remainingTime = intent.getLongExtra(WritingTimerService.EXTRA_REMAINING_TIME, 0)
            Log.d(TAG, "Tiempo restante: $remainingTime ms")
            
            // Reenviar la actualización localmente
            val localIntent = Intent(WritingTimerService.TIMER_UPDATE_ACTION).apply {
                putExtra(WritingTimerService.EXTRA_REMAINING_TIME, remainingTime)
            }
            context?.sendBroadcast(localIntent)
        }
    }
} 