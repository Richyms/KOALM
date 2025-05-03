package com.example.koalm.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class DigitalDisconnectTimerUpdateReceiver : BroadcastReceiver() {
    companion object {
        private const val TAG = "DigitalDisconnectTimerUpdate"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d(TAG, "onReceive: Recibida actualización del temporizador")
        Log.d(TAG, "onReceive: Action: ${intent?.action}")
        Log.d(TAG, "onReceive: Extras: ${intent?.extras?.keySet()?.joinToString { "$it=${intent.extras?.get(it)}" }}")
        
        if (intent?.action == DigitalDisconnectTimerService.TIMER_UPDATE_ACTION) {
            val remainingTime = intent.getLongExtra(DigitalDisconnectTimerService.EXTRA_REMAINING_TIME, 0)
            Log.d(TAG, "onReceive: Tiempo restante: $remainingTime ms")
            
            // Reenviar la actualización localmente
            val localIntent = Intent(DigitalDisconnectTimerService.TIMER_UPDATE_ACTION).apply {
                putExtra(DigitalDisconnectTimerService.EXTRA_REMAINING_TIME, remainingTime)
            }
            Log.d(TAG, "onReceive: Reenviando broadcast local")
            context?.sendBroadcast(localIntent)
            Log.d(TAG, "onReceive: Broadcast local enviado")
        } else {
            Log.d(TAG, "onReceive: Action no reconocida: ${intent?.action}")
        }
    }
} 