package com.example.mymusic

import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for ThemeHelper
 */
class ThemeHelperTest {
    
    @Test
    fun getThemeColors_returnsSpotifyColors() {
        val colors = ThemeHelper.getThemeColors(ThemeType.SPOTIFY)
        
        assertNotNull(colors)
        assertEquals(0xFF000000.toInt(), colors.background)
        assertEquals(0xFF1DB954.toInt(), colors.primary)
    }
    
    @Test
    fun getThemeColors_returnsAppleMusicColors() {
        val colors = ThemeHelper.getThemeColors(ThemeType.APPLE_MUSIC)
        
        assertNotNull(colors)
        assertEquals(0xFFFFFFFF.toInt(), colors.background)
        assertEquals(0xFFFF2D55.toInt(), colors.primary)
    }
    
    @Test
    fun getThemeColors_returnsYouTubeMusicColors() {
        val colors = ThemeHelper.getThemeColors(ThemeType.YOUTUBE_MUSIC)
        
        assertNotNull(colors)
        assertEquals(0xFF282828.toInt(), colors.background)
        assertEquals(0xFFFF0000.toInt(), colors.primary)
    }
    
    @Test
    fun getThemeColors_returnsSoundCloudColors() {
        val colors = ThemeHelper.getThemeColors(ThemeType.SOUNDCLOUD)
        
        assertNotNull(colors)
        assertEquals(0xFFFFFFFF.toInt(), colors.background)
        assertEquals(0xFFFF5500.toInt(), colors.primary)
    }
    
    @Test
    fun getThemeColors_returnsTidalColors() {
        val colors = ThemeHelper.getThemeColors(ThemeType.TIDAL)
        
        assertNotNull(colors)
        assertEquals(0xFF1A1A1A.toInt(), colors.background)
        assertEquals(0xFF00FFFF.toInt(), colors.primary)
    }
    
    @Test
    fun getThemeName_returnsCorrectNames() {
        assertEquals("Spotify", ThemeHelper.getThemeName(ThemeType.SPOTIFY))
        assertEquals("Apple Music", ThemeHelper.getThemeName(ThemeType.APPLE_MUSIC))
        assertEquals("YouTube Music", ThemeHelper.getThemeName(ThemeType.YOUTUBE_MUSIC))
        assertEquals("SoundCloud", ThemeHelper.getThemeName(ThemeType.SOUNDCLOUD))
        assertEquals("Tidal", ThemeHelper.getThemeName(ThemeType.TIDAL))
    }
    
    @Test
    fun getThemeDescription_returnsNonEmptyDescriptions() {
        val spotifyDesc = ThemeHelper.getThemeDescription(ThemeType.SPOTIFY)
        val appleMusicDesc = ThemeHelper.getThemeDescription(ThemeType.APPLE_MUSIC)
        val youtubeMusicDesc = ThemeHelper.getThemeDescription(ThemeType.YOUTUBE_MUSIC)
        val soundCloudDesc = ThemeHelper.getThemeDescription(ThemeType.SOUNDCLOUD)
        val tidalDesc = ThemeHelper.getThemeDescription(ThemeType.TIDAL)
        
        assertTrue(spotifyDesc.isNotEmpty())
        assertTrue(appleMusicDesc.isNotEmpty())
        assertTrue(youtubeMusicDesc.isNotEmpty())
        assertTrue(soundCloudDesc.isNotEmpty())
        assertTrue(tidalDesc.isNotEmpty())
    }
    
    @Test
    fun spotifyTheme_hasGradientDisabled() {
        val colors = ThemeHelper.getThemeColors(ThemeType.SPOTIFY)
        assertFalse(colors.useGradient)
    }
    
    @Test
    fun appleMusicTheme_hasGradientDisabled() {
        val colors = ThemeHelper.getThemeColors(ThemeType.APPLE_MUSIC)
        assertFalse(colors.useGradient)
    }
    
    @Test
    fun youtubeMusicTheme_hasGradientDisabled() {
        val colors = ThemeHelper.getThemeColors(ThemeType.YOUTUBE_MUSIC)
        assertFalse(colors.useGradient)
    }
}
