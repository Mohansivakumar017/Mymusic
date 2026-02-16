package com.example.mymusic

import android.content.Context
import android.content.SharedPreferences

class ThemeManager(context: Context) {
    private val prefs: SharedPreferences = 
        context.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)
    
    fun saveTheme(theme: ThemeType) {
        prefs.edit().putString("selected_theme", theme.name).apply()
    }
    
    fun getTheme(): ThemeType {
        val themeName = prefs.getString("selected_theme", ThemeType.SPOTIFY.name)
        return try {
            ThemeType.valueOf(themeName ?: ThemeType.SPOTIFY.name)
        } catch (e: IllegalArgumentException) {
            ThemeType.SPOTIFY
        }
    }
}

data class ThemeColors(
    val primary: Int,
    val primaryDark: Int,
    val background: Int,
    val surface: Int,
    val onBackground: Int,
    val onSurface: Int,
    val accent: Int,
    val cardBackground: Int,
    val useGradient: Boolean = false,
    val useBlur: Boolean = false,
    val gradientStart: Int? = null,
    val gradientEnd: Int? = null
)
