package com.example.koalm.ui.viewmodels

import android.content.Context
import android.content.SharedPreferences

class InicioSesionPreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("inicio_sesion_prefs", Context.MODE_PRIVATE)

    private val key = "animacion_mostrada"

    fun fueMostradaAnimacion(): Boolean {
        return prefs.getBoolean(key, false)
    }

    fun marcarAnimacionComoMostrada() {
        prefs.edit().putBoolean(key, true).apply()
    }

    fun reiniciarAnimacion() {
        prefs.edit().remove(key).apply()
    }
}

