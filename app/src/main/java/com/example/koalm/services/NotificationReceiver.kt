package com.example.koalm.services.timers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.koalm.MainActivity
import com.example.koalm.R
import com.example.koalm.services.notifications.DigitalDisconnectNotificationService
import com.example.koalm.services.notifications.NotificationConstants
import com.example.koalm.services.notifications.MeditationNotificationService
import com.example.koalm.services.notifications.ReadingNotificationService
import com.example.koalm.services.notifications.WritingNotificationService
import com.example.koalm.services.notifications.suenoNotificationService
import com.example.koalm.services.notifications.AlimentationNotificationService
import com.example.koalm.services.notifications.HydrationNotificationService
import com.example.koalm.services.timers.DigitalDisconnectTimerService
import com.example.koalm.services.timers.MeditationTimerService
import com.example.koalm.services.timers.ReadingTimerService
import com.example.koalm.services.timers.WritingTimerService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class NotificationReceiver : BroadcastReceiver() {
    companion object {
        private const val TAG = "KOALM_NOTIFICATIONS"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Received intent: ${intent.action}")
        Log.d(TAG, "Extras: ${intent.extras?.keySet()?.joinToString { "$it=${intent.extras?.get(it)}" }}")

        when (intent.action) {
            NotificationConstants.START_TIMER_ACTION -> {
                val duration = intent.getLongExtra("duration_minutes", 0)
                val isAlimentation= intent.getBooleanExtra("is_alimentation",false)
                val isWriting = intent.getBooleanExtra("is_writing", false)
                val isReading = intent.getBooleanExtra("is_reading", false)
                val isMeditation = intent.getBooleanExtra("is_meditation", false)
                val isDigitalDisconnect = intent.getBooleanExtra("is_digital_disconnect", false)
                val isSleeping = intent.getBooleanExtra("is_sleeping", false)
                val isHydration = intent.getBooleanExtra("is_hydration", false)
                val descripcion = intent.getStringExtra("descripcion") ?: ""
                val diaSemana = intent.getIntExtra("dia_semana", 0)
                //val notasHabilitadas = intent.getBooleanExtra("notas_habilitadas", false)
                val durationMinutes = intent.getLongExtra("duration_minutes", 0)
                /*when {
                    isSleeping -> showSleepNotification(context, descripcion, diaSemana)
                    isMeditation -> showMeditationNotification(context, descripcion, diaSemana, durationMinutes)
                    isReading -> showReadingNotification(context, descripcion, diaSemana, durationMinutes)
                    isDigitalDisconnect -> showDigitalDisconnectNotification(context, descripcion, diaSemana, durationMinutes)
                    isAlimentation -> showAlimentationNotification(context,descripcion)
                    else -> showWritingNotification(context, descripcion, diaSemana, durationMinutes, notasHabilitadas)
                }

                 */

                Log.d(TAG, "Starting timer with duration: $duration, isReading: $isReading, isMeditation: $isMeditation, isDigitalDisconnect: $isDigitalDisconnect")

                if (!isSleeping) {
                    when {
                        isReading -> startReadingTimer(context, duration)
                        isMeditation -> startMeditationTimer(context, duration)
                        isDigitalDisconnect -> startDigitalDisconnectTimer(context, duration)
                        isWriting -> startWritingTimer(context, duration)
                    }
                }

            }

            NotificationConstants.OPEN_NOTES_ACTION -> {
                Log.d(TAG, "Opening notes")
                val intent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    putExtra("open_notes", true)
                }
                context.startActivity(intent)
            }
            NotificationConstants.NOTIFICATION_ACTION -> {
                val descripcion = intent.getStringExtra("descripcion") ?: ""
                val diaSemana = intent.getIntExtra("dia_semana", 0)
                val durationMinutes = intent.getLongExtra("duration_minutes", 0)
                val isMeditation = intent.getBooleanExtra("is_meditation", false)
                val isReading = intent.getBooleanExtra("is_reading", false)
                val isDigitalDisconnect = intent.getBooleanExtra("is_digital_disconnect", false)
                val isHydration = intent.getBooleanExtra("is_hydration", false)
                val isAlimentation = intent.getBooleanExtra("is_alimentation",false)
                val isSleeping = intent.getBooleanExtra("is_sleeping", false)
                val isWriting = intent.getBooleanExtra("is_writing", false)

                Log.d(TAG, "Flags de tipo: isMeditation=$isMeditation, isReading=$isReading, isDigitalDisconnect=$isDigitalDisconnect")

                val tipoNotificacion = when {
                    isMeditation -> "meditación"
                    isReading -> "lectura"
                    isDigitalDisconnect -> "desconexión digital"
                    isSleeping->"sueno"
                    isAlimentation->"alimentation"
                    isWriting -> "escritura"
                    isHydration -> "hydration"
                    else -> "otro"
                }
                Log.d(TAG, "Tipo de notificación determinado: $tipoNotificacion")

                when {
                    isMeditation -> showMeditationNotification(context, descripcion, diaSemana, durationMinutes)
                    isReading -> showReadingNotification(context, descripcion, diaSemana, durationMinutes)
                    isDigitalDisconnect -> showDigitalDisconnectNotification(context, descripcion, diaSemana, durationMinutes)
                    isSleeping->showSleepNotification(context,descripcion,diaSemana)
                    isWriting -> showWritingNotification(context, descripcion, diaSemana, durationMinutes)
                    isAlimentation-> showAlimentationNotification(
                        context = context,
                        descripcion = descripcion,
                        notificationId = AlimentationNotificationService.NOTIFICATION_ID + diaSemana
                    )
                    isHydration-> showHydrationNotification(
                        context = context,
                        descripcion = descripcion,
                        notificationId = HydrationNotificationService.NOTIFICATION_ID + diaSemana
                    )

                    else -> Log.w(TAG, "Tipo de notificación no reconocido. No se mostró ninguna.")
                }
            }
        }
    }
    // Mostrar notificaciones de hábitos de salud física
    private fun showAlimentationNotification(
        context: Context,
        descripcion: String,
        notificationId: Int = AlimentationNotificationService.NOTIFICATION_ID // ID dinámico si necesitas varios
    ) {
        Log.d(TAG, "Mostrando notificación de alimentación")

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Intento para abrir el Dashboard
        val openDashboardIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("startDestination", "menu")
        }

        val openDashboardPendingIntent = PendingIntent.getActivity(
            context,
            notificationId + 100, // ID único por cada acción
            openDashboardIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val channelId = AlimentationNotificationService().channelId
        val channelName = context.getString(AlimentationNotificationService().channelName)

        // Crear canal de notificación (solo API 26+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(AlimentationNotificationService().channelDescription)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Intento para abrir directamente sección de alimentación
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("route", "alimentation")
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            addCategory(Intent.CATEGORY_LAUNCHER)
            action = Intent.ACTION_MAIN
        }

        val openPendingIntent = PendingIntent.getActivity(
            context,
            notificationId + 200, // ID diferente
            openIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Construcción de la notificación
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(AlimentationNotificationService().defaultTitle))
            .setContentText( "No olvides registrar tu progreso diario. 🌿✨" )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .addAction(
                R.drawable.ic_notification,
                "Ir al Dashboard",
                openDashboardPendingIntent
            )
            .setContentIntent(openPendingIntent)
            .build()

        notificationManager.notify(notificationId, notification)
        Log.d(TAG, "Notificación de alimentación mostrada con ID: $notificationId")

        // Guardar notificación en Firestore
        val userEmail = FirebaseAuth.getInstance().currentUser?.email ?: return
        val db = FirebaseFirestore.getInstance()
        val notificacion = hashMapOf(
            "mensaje" to "¡Hora de alimentarse!",
            "timestamp" to System.currentTimeMillis(),
            "leido" to false
        )
        db.collection("usuarios")
            .document(userEmail)
            .collection("notificaciones")
            .add(notificacion)
    }

    private fun showHydrationNotification(
        context: Context,
        descripcion: String,
        notificationId: Int = HydrationNotificationService.NOTIFICATION_ID // ID dinámico si necesitas varios
    ) {
        Log.d(TAG, "Mostrando notificación de hidratación")

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Intento para abrir el Dashboard
        val openDashboardIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("startDestination", "menu")
        }

        val openDashboardPendingIntent = PendingIntent.getActivity(
            context,
            notificationId + 100, // ID único por cada acción
            openDashboardIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val channelId = HydrationNotificationService().channelId
        val channelName = context.getString(HydrationNotificationService().channelName)

        // Crear canal de notificación (solo API 26+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(HydrationNotificationService().channelDescription)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Intento para abrir directamente sección de alimentación
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("route", "hydration")
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            addCategory(Intent.CATEGORY_LAUNCHER)
            action = Intent.ACTION_MAIN
        }

        val openPendingIntent = PendingIntent.getActivity(
            context,
            notificationId + 200, // ID diferente
            openIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Construcción de la notificación
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(HydrationNotificationService().defaultTitle))
            .setContentText( "No olvides registrar tu progreso diario. 🌿✨" )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .addAction(
                R.drawable.ic_notification,
                "Ir al Dashboard",
                openDashboardPendingIntent
            )
            .setContentIntent(openPendingIntent)
            .build()

        notificationManager.notify(notificationId, notification)
        Log.d(TAG, "Notificación de hidratación mostrada con ID: $notificationId")

        // Guardar notificación en Firestore
        val userEmail = FirebaseAuth.getInstance().currentUser?.email ?: return
        val db = FirebaseFirestore.getInstance()
        val notificacion = hashMapOf(
            "mensaje" to "¡Hora de hidratarse!",
            "timestamp" to System.currentTimeMillis(),
            "leido" to false
        )
        db.collection("usuarios")
            .document(userEmail)
            .collection("notificaciones")
            .add(notificacion)
    }

    private fun showSleepNotification(
        context: Context,
        descripcion: String,
        diaSemana: Int
    ) {
        Log.d(TAG, "Mostrando notificación de sueño")
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val openDashboardIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("startDestination", "menu") // Esto es lo que usas para ir al Dashboard
        }

        val openDashboardPendingIntent = PendingIntent.getActivity(
            context,
            suenoNotificationService.NOTIFICATION_ID + diaSemana + 200, // ID diferente al del timer
            openDashboardIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                suenoNotificationService().channelId,
                context.getString(suenoNotificationService().channelName),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(suenoNotificationService().channelDescription)
            }
            notificationManager.createNotificationChannel(channel)
            Log.d(TAG, "Canal de notificación de sueño creado")
        }



        val notification = NotificationCompat.Builder(context,suenoNotificationService().channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(suenoNotificationService().defaultTitle))
            .setContentText("No olvides registrar tu progreso diario. 🌿✨")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .addAction(
                R.drawable.ic_notification,
                "Ir al Dashboard", // Acción para dashboard
                openDashboardPendingIntent
            )
            .build()
        notificationManager.notify(suenoNotificationService.NOTIFICATION_ID + diaSemana, notification) // Usar el mismo ID base que en sueñoNotificationService
        Log.d(TAG, "Notificación de sueño mostrada con ID: ${suenoNotificationService.NOTIFICATION_ID + diaSemana}")
        val userEmail = FirebaseAuth.getInstance().currentUser?.email ?: return
        val db = FirebaseFirestore.getInstance()
        // Guardar notificación en Firestore
        val notificacion = hashMapOf(
            "mensaje" to "¡Hora de dormir!",
            "timestamp" to System.currentTimeMillis(),
            "leido" to false
        )
        db.collection("usuarios")
            .document(userEmail)
            .collection("notificaciones")
            .add(notificacion)
    }

    // Mostrar notificaciones de hábitos se salud mental
    private fun showMeditationNotification(
        context: Context,
        descripcion: String,
        diaSemana: Int,
        durationMinutes: Long
    ) {
        Log.d(TAG, "Mostrando notificación de meditación")
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val openDashboardIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("startDestination", "menu") // Esto es lo que usas para ir al Dashboard
        }

        val openDashboardPendingIntent = PendingIntent.getActivity(
            context,
            MeditationNotificationService.NOTIFICATION_ID + diaSemana + 200, // ID diferente al del timer
            openDashboardIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                MeditationNotificationService().channelId,
                context.getString(MeditationNotificationService().channelName),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(MeditationNotificationService().channelDescription)
            }
            notificationManager.createNotificationChannel(channel)
            Log.d(TAG, "Canal de notificación de meditación creado")
        }

        val startTimerIntent = Intent(context, NotificationReceiver::class.java).apply {
            action = NotificationConstants.START_TIMER_ACTION
            putExtra("duration_minutes", durationMinutes)
            putExtra("is_meditation", true)
        }
        
        val startTimerPendingIntent = PendingIntent.getBroadcast(
            context,
            MeditationNotificationService.NOTIFICATION_ID + diaSemana,
            startTimerIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
/*
        val historyIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("route", "historial_meditacion")
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            addCategory(Intent.CATEGORY_LAUNCHER)
            action = Intent.ACTION_MAIN
        }

        
        val historyPendingIntent = PendingIntent.getActivity(
            context,
            MeditationNotificationService.NOTIFICATION_ID + diaSemana + 100,
            historyIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
         */
        val notification = NotificationCompat.Builder(context, MeditationNotificationService().channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(MeditationNotificationService().defaultTitle))
            .setContentText("No olvides registrar tu progreso diario. 🌿✨")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .addAction(
                R.drawable.ic_timer,
                context.getString(R.string.start_timer), // Acción para temporizador
                startTimerPendingIntent
            )
            .addAction(
                R.drawable.ic_notification,
                "Ir al Dashboard", // Acción para dashboard
                openDashboardPendingIntent
            )
            .build()
        
        notificationManager.notify(MeditationNotificationService.NOTIFICATION_ID + diaSemana, notification)
        Log.d(TAG, "Notificación de meditación mostrada con ID: ${MeditationNotificationService.NOTIFICATION_ID + diaSemana}")
        val userEmail = FirebaseAuth.getInstance().currentUser?.email ?: return
        val db = FirebaseFirestore.getInstance()
        // Guardar notificación en Firestore
        val notificacion = hashMapOf(
            "mensaje" to "¡Hora de meditar!",
            "timestamp" to System.currentTimeMillis(),
            "leido" to false
        )
        db.collection("usuarios")
            .document(userEmail)
            .collection("notificaciones")
            .add(notificacion)
    }

    private fun showReadingNotification(
        context: Context,
        descripcion: String,
        diaSemana: Int,
        durationMinutes: Long
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val openDashboardIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("startDestination", "menu") // Esto es lo que usas para ir al Dashboard
        }

        val openDashboardPendingIntent = PendingIntent.getActivity(
            context,
            ReadingNotificationService.NOTIFICATION_ID + diaSemana + 200, // ID diferente al del timer
            openDashboardIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                ReadingNotificationService().channelId,
                context.getString(ReadingNotificationService().channelName),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(ReadingNotificationService().channelDescription)
            }
            notificationManager.createNotificationChannel(channel)
        }
        
        val startTimerIntent = Intent(context, NotificationReceiver::class.java).apply {
            action = NotificationConstants.START_TIMER_ACTION
            putExtra("duration_minutes", durationMinutes)
            putExtra("is_reading", true)
        }
        
        val startTimerPendingIntent = PendingIntent.getBroadcast(
            context,
            ReadingNotificationService.NOTIFICATION_ID + diaSemana,
            startTimerIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        /*
        val openBooksIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("route", "libros")
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            addCategory(Intent.CATEGORY_LAUNCHER)
            action = Intent.ACTION_MAIN
        }

        val openBooksPendingIntent = PendingIntent.getActivity(
            context,
            ReadingNotificationService.NOTIFICATION_ID + diaSemana + 100,
            openBooksIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        */
        
        val notification = NotificationCompat.Builder(context, ReadingNotificationService().channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(ReadingNotificationService().defaultTitle))
            .setContentText("No olvides registrar tu progreso diario. 🌿✨")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .addAction(
                R.drawable.ic_timer,
                context.getString(R.string.start_timer), // Acción para temporizador
                startTimerPendingIntent
            )
            .addAction(
                R.drawable.ic_notification,
                "Ir al Dashboard",
                openDashboardPendingIntent
            )
            .build()
        
        notificationManager.notify(ReadingNotificationService.NOTIFICATION_ID + diaSemana, notification)

        val userEmail = FirebaseAuth.getInstance().currentUser?.email ?: return
        val db = FirebaseFirestore.getInstance()
        // Guardar notificación en Firestore
        val notificacion = hashMapOf(
            "mensaje" to "¡Hora de leer!",
            "timestamp" to System.currentTimeMillis(),
            "leido" to false
        )
        db.collection("usuarios")
            .document(userEmail)
            .collection("notificaciones")
            .add(notificacion)
    }

    private fun showWritingNotification(
        context: Context,
        descripcion: String,
        diaSemana: Int,
        durationMinutes: Long
    ) {
        Log.d(TAG, "Mostrando notificación de escritura")

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val openDashboardIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("startDestination", "menu") // Ir al Dashboard
        }

        val openDashboardPendingIntent = PendingIntent.getActivity(
            context,
            WritingNotificationService.NOTIFICATION_ID + diaSemana + 200,
            openDashboardIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                WritingNotificationService().channelId,
                context.getString(WritingNotificationService().channelName),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(WritingNotificationService().channelDescription)
            }
            notificationManager.createNotificationChannel(channel)
            Log.d(TAG, "Canal de notificación de escritura creado")
        }

        val startTimerIntent = Intent(context, NotificationReceiver::class.java).apply {
            action = NotificationConstants.START_TIMER_ACTION
            putExtra("duration_minutes", durationMinutes)
            putExtra("is_writing", true)
        }

        val startTimerPendingIntent = PendingIntent.getBroadcast(
            context,
            WritingNotificationService.NOTIFICATION_ID + diaSemana,
            startTimerIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, WritingNotificationService().channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(WritingNotificationService().defaultTitle))
            .setContentText("No olvides registrar tu progreso diario. 🌿✨")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .addAction(
                R.drawable.ic_timer,
                context.getString(R.string.start_timer), // Acción para temporizador
                startTimerPendingIntent
            )
            .addAction(
                R.drawable.ic_notification,
                "Ir al Dashboard",
                openDashboardPendingIntent
            )
            .build()

        notificationManager.notify(WritingNotificationService.NOTIFICATION_ID + diaSemana, notification)
        Log.d(TAG, "Notificación de escritura mostrada con ID: ${WritingNotificationService.NOTIFICATION_ID + diaSemana}")

        val userEmail = FirebaseAuth.getInstance().currentUser?.email ?: return
        val db = FirebaseFirestore.getInstance()

        val notificacion = hashMapOf(
            "mensaje" to "¡Hora de escribir!",
            "timestamp" to System.currentTimeMillis(),
            "leido" to false
        )

        db.collection("usuarios")
            .document(userEmail)
            .collection("notificaciones")
            .add(notificacion)
    }


    private fun showDigitalDisconnectNotification(
        context: Context,
        descripcion: String,
        diaSemana: Int,
        durationMinutes: Long
    ) {

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val openDashboardIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("startDestination", "menu") // Esto es lo que usas para ir al Dashboard
        }

        val openDashboardPendingIntent = PendingIntent.getActivity(
            context,
            DigitalDisconnectNotificationService.NOTIFICATION_ID + diaSemana + 200, // ID diferente al del timer
            openDashboardIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                DigitalDisconnectNotificationService().channelId,
                context.getString(DigitalDisconnectNotificationService().channelName),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(DigitalDisconnectNotificationService().channelDescription)
            }
            notificationManager.createNotificationChannel(channel)
        }
        
        val startTimerIntent = Intent(context, NotificationReceiver::class.java).apply {
            action = NotificationConstants.START_TIMER_ACTION
            putExtra("duration_minutes", durationMinutes)
            putExtra("is_digital_disconnect", true)
        }
        
        val startTimerPendingIntent = PendingIntent.getBroadcast(
            context,
            DigitalDisconnectNotificationService.NOTIFICATION_ID + diaSemana,
            startTimerIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        /*
        val openDisconnectIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("route", "desconexion")
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            addCategory(Intent.CATEGORY_LAUNCHER)
            action = Intent.ACTION_MAIN
        }
        
        val openDisconnectPendingIntent = PendingIntent.getActivity(
            context,
            DigitalDisconnectNotificationService.NOTIFICATION_ID + diaSemana + 100,
            openDisconnectIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        */
        val notification = NotificationCompat.Builder(context, DigitalDisconnectNotificationService().channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(DigitalDisconnectNotificationService().defaultTitle))
            .setContentText("No olvides registrar tu progreso diario. 🌿✨")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .addAction(
                R.drawable.ic_timer,
                context.getString(R.string.start_timer), // Acción para temporizador
                startTimerPendingIntent
            )
            .addAction(
                R.drawable.ic_notification,
                "Ir al Dashboard", // Acción para dashboard
                openDashboardPendingIntent
            )
            .build()


        notificationManager.notify(DigitalDisconnectNotificationService.NOTIFICATION_ID + diaSemana, notification)

        val userEmail = FirebaseAuth.getInstance().currentUser?.email ?: return
        val db = FirebaseFirestore.getInstance()
        // Guardar notificación en Firestore
        val notificacion = hashMapOf(
            "mensaje" to "¡Hora de desconectarse!",
            "timestamp" to System.currentTimeMillis(),
            "leido" to false
        )
        db.collection("usuarios")
            .document(userEmail)
            .collection("notificaciones")
            .add(notificacion)
    }

    private fun startDigitalDisconnectTimer(context: Context, duration: Long) {
        Log.d(TAG, "Starting digital disconnect timer for $duration minutes")
        val intent = Intent(context, DigitalDisconnectTimerService::class.java).apply {
            putExtra(NotificationConstants.EXTRA_DURATION, duration)
        }
        try {
            context.startForegroundService(intent)
            Log.d(TAG, "Digital disconnect timer service started successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error starting digital disconnect timer service", e)
        }
    }

    private fun startWritingTimer(context: Context, duration: Long) {
        Log.d(TAG, "Starting writing timer for $duration minutes")
        val intent = Intent(context, WritingTimerService::class.java).apply {
            putExtra(NotificationConstants.EXTRA_DURATION, duration)
        }
        try {
            context.startForegroundService(intent)
            Log.d(TAG, "Writing timer service started successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error starting writing timer service", e)
        }
    }

    private fun startMeditationTimer(context: Context, duration: Long) {
        Log.d(TAG, "Starting meditation timer for $duration minutes")
        val intent = Intent(context, MeditationTimerService::class.java).apply {
            putExtra(NotificationConstants.EXTRA_DURATION, duration)
        }
        try {
            context.startForegroundService(intent)
            Log.d(TAG, "Meditation timer service started successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error starting meditation timer service", e)
        }
    }

    private fun startReadingTimer(context: Context, duration: Long) {
        Log.d(TAG, "Starting reading timer for $duration minutes")
        try {
            val intent = Intent(context, ReadingTimerService::class.java).apply {
                putExtra("duration_minutes", duration)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
            Log.d(TAG, "Reading timer service started successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error starting reading timer service", e)
        }
    }
} 