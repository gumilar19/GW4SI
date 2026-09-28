package com.example.settings

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PreferenceManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)

    private val _unitSystem = MutableStateFlow(prefs.getString("unit_system", "Metric") ?: "Metric")
    val unitSystem: StateFlow<String> = _unitSystem

    private val _themeMode = MutableStateFlow(prefs.getString("theme_mode", "System") ?: "System")
    val themeMode: StateFlow<String> = _themeMode

    private val _language = MutableStateFlow(prefs.getString("language", "English") ?: "English")
    val language: StateFlow<String> = _language

    fun setUnitSystem(system: String) {
        prefs.edit().putString("unit_system", system).apply()
        _unitSystem.value = system
    }

    fun setThemeMode(mode: String) {
        prefs.edit().putString("theme_mode", mode).apply()
        _themeMode.value = mode
    }

    fun setLanguage(lang: String) {
        prefs.edit().putString("language", lang).apply()
        _language.value = lang
    }
}
