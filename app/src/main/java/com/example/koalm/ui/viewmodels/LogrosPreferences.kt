package com.example.koalm.ui.viewmodels

import android.content.Context
import android.content.SharedPreferences
import java.time.LocalDate

class LogrosPreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("logros_prefs", Context.MODE_PRIVATE)

    private fun getKey(habitoId: String): String {
        val hoy = LocalDate.now().toString() // yyyy-MM-dd
        return "$habitoId-$hoy"
    }

    fun fueMostrado(habitoId: String): Boolean {
        return prefs.getBoolean(getKey(habitoId), false)
    }

    fun marcarComoMostrado(habitoId: String) {
        prefs.edit().putBoolean(getKey(habitoId), true).apply()
    }
}