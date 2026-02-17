package com.example.mymusic

import android.graphics.Color

object ThemeHelper {
    
    fun getThemeColors(theme: ThemeType): ThemeColors {
        return when (theme) {
            ThemeType.SPOTIFY -> getSpotifyTheme()
            ThemeType.APPLE_MUSIC -> getAppleMusicTheme()
            ThemeType.YOUTUBE_MUSIC -> getYouTubeMusicTheme()
            ThemeType.SOUNDCLOUD -> getSoundCloudTheme()
            ThemeType.TIDAL -> getTidalTheme()
        }
    }
    
    private fun getSpotifyTheme(): ThemeColors {
        return ThemeColors(
            primary = Color.parseColor("#1DB954"),
            primaryDark = Color.parseColor("#000000"),
            background = Color.parseColor("#000000"),
            surface = Color.parseColor("#000000"),
            onBackground = Color.parseColor("#FFFFFF"),
            onSurface = Color.parseColor("#B3B3B3"),
            accent = Color.parseColor("#1DB954"),
            cardBackground = Color.parseColor("#181818"),
            useGradient = false,
            useBlur = false
        )
    }
    
    private fun getAppleMusicTheme(): ThemeColors {
        return ThemeColors(
            primary = Color.parseColor("#FF2D55"),
            primaryDark = Color.parseColor("#FF2D55"),
            background = Color.parseColor("#FFFFFF"),
            surface = Color.parseColor("#F5F5F5"),
            onBackground = Color.parseColor("#000000"),
            onSurface = Color.parseColor("#666666"),
            accent = Color.parseColor("#FF2D55"),
            cardBackground = Color.parseColor("#FFFFFF"),
            useGradient = false,
            useBlur = false
        )
    }
    
    private fun getYouTubeMusicTheme(): ThemeColors {
        return ThemeColors(
            primary = Color.parseColor("#FF0000"),
            primaryDark = Color.parseColor("#282828"),
            background = Color.parseColor("#282828"),
            surface = Color.parseColor("#282828"),
            onBackground = Color.parseColor("#FFFFFF"),
            onSurface = Color.parseColor("#AAAAAA"),
            accent = Color.parseColor("#FF0000"),
            cardBackground = Color.parseColor("#383838"),
            useGradient = false,
            useBlur = false
        )
    }
    
    private fun getSoundCloudTheme(): ThemeColors {
        return ThemeColors(
            primary = Color.parseColor("#FF5500"),
            primaryDark = Color.parseColor("#FF5500"),
            background = Color.parseColor("#FFFFFF"),
            surface = Color.parseColor("#F2F2F2"),
            onBackground = Color.parseColor("#333333"),
            onSurface = Color.parseColor("#666666"),
            accent = Color.parseColor("#FF5500"),
            cardBackground = Color.parseColor("#FFFFFF"),
            useGradient = false,
            useBlur = false
        )
    }
    
    private fun getTidalTheme(): ThemeColors {
        return ThemeColors(
            primary = Color.parseColor("#00FFFF"),
            primaryDark = Color.parseColor("#1A1A1A"),
            background = Color.parseColor("#1A1A1A"),
            surface = Color.parseColor("#1A1A1A"),
            onBackground = Color.parseColor("#FFFFFF"),
            onSurface = Color.parseColor("#999999"),
            accent = Color.parseColor("#00FFFF"),
            cardBackground = Color.parseColor("#2A2A2A"),
            useGradient = false,
            useBlur = false
        )
    }
    
    fun getThemeName(theme: ThemeType): String {
        return when (theme) {
            ThemeType.SPOTIFY -> "Spotify"
            ThemeType.APPLE_MUSIC -> "Apple Music"
            ThemeType.YOUTUBE_MUSIC -> "YouTube Music"
            ThemeType.SOUNDCLOUD -> "SoundCloud"
            ThemeType.TIDAL -> "Tidal"
        }
    }
    
    fun getThemeDescription(theme: ThemeType): String {
        return when (theme) {
            ThemeType.SPOTIFY -> "Pure black background with bright green accents"
            ThemeType.APPLE_MUSIC -> "Clean white design with soft pink accents"
            ThemeType.YOUTUBE_MUSIC -> "Dark theme with bold red accents"
            ThemeType.SOUNDCLOUD -> "Bright white theme with energetic orange"
            ThemeType.TIDAL -> "Premium dark theme with cyan accents"
        }
    }
}