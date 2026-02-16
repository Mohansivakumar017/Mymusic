package com.example.mymusic

import android.graphics.Color

object ThemeHelper {
    
    fun getThemeColors(theme: ThemeType): ThemeColors {
        return when (theme) {
            ThemeType.SPOTIFY -> getSpotifyTheme()
            ThemeType.APPLE_MUSIC -> getAppleMusicTheme()
            ThemeType.IOS_GLASS -> getIOSGlassTheme()
        }
    }
    
    private fun getSpotifyTheme(): ThemeColors {
        return ThemeColors(
            primary = Color.parseColor("#1DB954"),
            primaryDark = Color.parseColor("#128C3E"),
            background = Color.parseColor("#121212"),
            surface = Color.parseColor("#1E1E1E"),
            onBackground = Color.parseColor("#FFFFFF"),
            onSurface = Color.parseColor("#B3B3B3"),
            accent = Color.parseColor("#1DB954"),
            cardBackground = Color.parseColor("#1E1E1E"),
            useGradient = false,
            useBlur = false
        )
    }
    
    private fun getAppleMusicTheme(): ThemeColors {
        return ThemeColors(
            primary = Color.parseColor("#FC3C44"),
            primaryDark = Color.parseColor("#C41E25"),
            background = Color.parseColor("#FAFAFA"),
            surface = Color.parseColor("#FFFFFF"),
            onBackground = Color.parseColor("#000000"),
            onSurface = Color.parseColor("#666666"),
            accent = Color.parseColor("#FC3C44"),
            cardBackground = Color.parseColor("#FFFFFF"),
            useGradient = true,
            useBlur = false,
            gradientStart = Color.parseColor("#FFE5E7"),
            gradientEnd = Color.parseColor("#FFF5F6")
        )
    }
    
    private fun getIOSGlassTheme(): ThemeColors {
        return ThemeColors(
            primary = Color.parseColor("#007AFF"),
            primaryDark = Color.parseColor("#0051D5"),
            background = Color.parseColor("#F2F2F7"),
            surface = Color.parseColor("#FFFFFF"),
            onBackground = Color.parseColor("#000000"),
            onSurface = Color.parseColor("#3C3C43"),
            accent = Color.parseColor("#007AFF"),
            cardBackground = Color.parseColor("#F9F9FB"),
            useGradient = false,
            useBlur = true
        )
    }
    
    fun getThemeName(theme: ThemeType): String {
        return when (theme) {
            ThemeType.SPOTIFY -> "Spotify"
            ThemeType.APPLE_MUSIC -> "Apple Music"
            ThemeType.IOS_GLASS -> "iOS Glass"
        }
    }
    
    fun getThemeDescription(theme: ThemeType): String {
        return when (theme) {
            ThemeType.SPOTIFY -> "Dark theme with green accents"
            ThemeType.APPLE_MUSIC -> "Light theme with vibrant gradients"
            ThemeType.IOS_GLASS -> "Frosted glass effect with iOS design"
        }
    }
}
