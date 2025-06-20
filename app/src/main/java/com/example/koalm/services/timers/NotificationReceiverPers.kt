package com.example.koalm.services.timers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import android.app.NotificationManager
import android.app.NotificationChannel
import android.app.PendingIntent
import android.os.Build
import android.util.Log
import com.example.koalm.MainActivity
import com.example.koalm.R
import com.example.koalm.services.notifications.NotificationScheduler
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class NotificationReceiverPers : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val habitId = intent.getStringExtra("habitId") ?: return
        val habitName = intent.getStringExtra("habitName") ?: "Tu hábito"
        val reminderIndex = intent.getIntExtra("reminderIndex", 0)
        val hour = intent.getIntExtra("hour", -1)
        val minute = intent.getIntExtra("minute", -1)
        val frecuenciaArray = intent.getBooleanArrayExtra("frecuencia") ?: BooleanArray(7) { false }

        val userEmail = FirebaseAuth.getInstance().currentUser?.email ?: return
        val db = FirebaseFirestore.getInstance()
        val habitDocRef = db.collection("habitos")
            .document(userEmail)
            .collection("personalizados")
            .document(habitId)

        // Verifica si el hábito sigue activo
        habitDocRef.get().addOnSuccessListener { doc ->
            if (doc.exists() && doc.getBoolean("estaActivo") == true) {
                Log.d("NotificationReceiverPers", "Hábito activo, enviando notificación habitId=$habitId")

                val notificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                val channelId = "habit_reminder_channel"
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val channel = NotificationChannel(
                        channelId,
                        "Recordatorios de hábitos",
                        NotificationManager.IMPORTANCE_HIGH
                    )
                    notificationManager.createNotificationChannel(channel)
                }

                val intentToMain = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    putExtra("startDestination", "menu")
                }

                val pendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    intentToMain,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                val notification = NotificationCompat.Builder(context, channelId)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle("🐨 ¡Hora del hábito!")
                    .setContentText("No olvides registrar tu progreso diario. ¡Es momento de \"$habitName\"! 🌿✨")
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)
                    .addAction(
                        R.drawable.ic_notification,
                        "Ir al Dashboard",
                        pendingIntent
                    )
                    .build()

                notificationManager.notify(habitId.hashCode() + reminderIndex, notification)

                // Guardar notificación en Firestore
                val notificacion = hashMapOf(
                    "habitId" to habitId,
                    "habitName" to habitName,
                    "mensaje" to "Es momento de \"$habitName\"",
                    "timestamp" to System.currentTimeMillis(),
                    "leido" to false
                )
                db.collection("usuarios")
                    .document(userEmail)
                    .collection("notificaciones")
                    .add(notificacion)

            } else {
                Log.d("NotificationReceiverPers", "Hábito inactivo o no encontrado, no se envía notificación habitId=$habitId")
            }

            // Reprogramar si es necesario
            if (hour >= 0 && minute >= 0) {
                val frecuenciaList = frecuenciaArray.toList()
                NotificationScheduler.scheduleHabitReminder(
                    context,
                    habitId,
                    habitName,
                    hour,
                    minute,
                    reminderIndex,
                    frecuenciaList
                )
            }
        }
    }


    /**
     * frecuencia: lista de 7 booleanos (0=domingo, 1=lunes... 6=sábado)
     * currentDayOfWeek: Calendar.DAY_OF_WEEK (1=domingo,... 7=sábado)
     * Retorna días a sumar para siguiente día activo (incluye hoy si aplica)
     */
    fun getNextValidDayOffset(
        frecuencia: List<Boolean>,
        currentDayOfWeek: Int,
        currentHour: Int,
        currentMinute: Int,
        targetHour: Int,
        targetMinute: Int
    ): Int {
        // Ajuste para que lunes=0 ... domingo=6
        val todayIndex = (currentDayOfWeek + 5) % 7

        for (offset in 0..6) {
            val dayIndex = (todayIndex + offset) % 7
            if (frecuencia[dayIndex]) {
                if (offset == 0) {
                    // Si es hoy, revisar si ya pasó la hora de recordatorio
                    if (currentHour > targetHour || (currentHour == targetHour && currentMinute >= targetMinute)) {
                        // Ya pasó, buscar el siguiente día activo después de hoy
                        continue
                    } else {
                        return 0
                    }
                }
                return offset
            }
        }
        return 7 // Si no encontró día activo, reprograma en 7 días (la próxima semana)
    }

}
