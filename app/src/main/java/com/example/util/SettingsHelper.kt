package com.example.util

import android.content.Context
import android.content.SharedPreferences

class SettingsHelper(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "simple_reminder_settings",
        Context.MODE_PRIVATE
    )

    companion object {
        const val KEY_THEME = "prefs_theme" // "light", "dark", "system"
        const val KEY_SOUND = "prefs_sound"
        const val KEY_VIBE = "prefs_vibe"
    }

    var theme: String
        get() = prefs.getString(KEY_THEME, "system") ?: "system"
        set(value) = prefs.edit().putString(KEY_THEME, value).apply()

    var isSoundEnabled: Boolean
        get() = prefs.getBoolean(KEY_SOUND, true)
        set(value) = prefs.edit().putBoolean(KEY_SOUND, value).apply()

    var isVibrationEnabled: Boolean
        get() = prefs.getBoolean(KEY_VIBE, true)
        set(value) = prefs.edit().putBoolean(KEY_VIBE, value).apply()
}
