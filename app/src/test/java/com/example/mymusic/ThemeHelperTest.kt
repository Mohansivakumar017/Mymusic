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
        assertEquals(0xFF121212.toInt(), colors.background)
        assertEquals(0xFF1DB954.toInt(), colors.primary)
    }
    
    @Test
    fun getThemeColors_returnsAppleMusicColors() {
        val colors = ThemeHelper.getThemeColors(ThemeType.APPLE_MUSIC)
        
        assertNotNull(colors)
        assertEquals(0xFFFAFAFA.toInt(), colors.background)
        assertEquals(0xFFFC3C44.toInt(), colors.primary)
    }
    
    @Test
    fun getThemeColors_returnsIosGlassColors() {
        val colors = ThemeHelper.getThemeColors(ThemeType.IOS_GLASS)
        
        assertNotNull(colors)
        assertEquals(0xFFF2F2F7.toInt(), colors.background)
        assertEquals(0xFF007AFF.toInt(), colors.primary)
    }
    
    @Test
    fun getThemeName_returnsCorrectNames() {
        assertEquals("Spotify", ThemeHelper.getThemeName(ThemeType.SPOTIFY))
        assertEquals("Apple Music", ThemeHelper.getThemeName(ThemeType.APPLE_MUSIC))
        assertEquals("iOS Glass", ThemeHelper.getThemeName(ThemeType.IOS_GLASS))
    }
    
    @Test
    fun getThemeDescription_returnsNonEmptyDescriptions() {
        val spotifyDesc = ThemeHelper.getThemeDescription(ThemeType.SPOTIFY)
        val appleMusicDesc = ThemeHelper.getThemeDescription(ThemeType.APPLE_MUSIC)
        val iosGlassDesc = ThemeHelper.getThemeDescription(ThemeType.IOS_GLASS)
        
        assertTrue(spotifyDesc.isNotEmpty())
        assertTrue(appleMusicDesc.isNotEmpty())
        assertTrue(iosGlassDesc.isNotEmpty())
    }
    
    @Test
    fun spotifyTheme_hasGradientDisabled() {
        val colors = ThemeHelper.getThemeColors(ThemeType.SPOTIFY)
        assertFalse(colors.useGradient)
    }
    
    @Test
    fun appleMusicTheme_hasGradientEnabled() {
        val colors = ThemeHelper.getThemeColors(ThemeType.APPLE_MUSIC)
        assertTrue(colors.useGradient)
        assertNotNull(colors.gradientStart)
        assertNotNull(colors.gradientEnd)
    }
    
    @Test
    fun iosGlassTheme_hasGradientDisabled() {
        val colors = ThemeHelper.getThemeColors(ThemeType.IOS_GLASS)
        assertFalse(colors.useGradient)
    }
}
