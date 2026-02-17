package com.example.mymusic

import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for ThemeType enum
 */
class ThemeTypeTest {
    
    @Test
    fun themeType_hasFiveValues() {
        val themes = ThemeType.values()
        assertEquals(5, themes.size)
    }
    
    @Test
    fun themeType_containsSpotify() {
        val themes = ThemeType.values()
        assertTrue(themes.contains(ThemeType.SPOTIFY))
    }
    
    @Test
    fun themeType_containsAppleMusic() {
        val themes = ThemeType.values()
        assertTrue(themes.contains(ThemeType.APPLE_MUSIC))
    }
    
    @Test
    fun themeType_containsYouTubeMusic() {
        val themes = ThemeType.values()
        assertTrue(themes.contains(ThemeType.YOUTUBE_MUSIC))
    }
    
    @Test
    fun themeType_containsSoundCloud() {
        val themes = ThemeType.values()
        assertTrue(themes.contains(ThemeType.SOUNDCLOUD))
    }
    
    @Test
    fun themeType_containsTidal() {
        val themes = ThemeType.values()
        assertTrue(themes.contains(ThemeType.TIDAL))
    }
    
    @Test
    fun themeType_valueOf_worksCorrectly() {
        assertEquals(ThemeType.SPOTIFY, ThemeType.valueOf("SPOTIFY"))
        assertEquals(ThemeType.APPLE_MUSIC, ThemeType.valueOf("APPLE_MUSIC"))
        assertEquals(ThemeType.YOUTUBE_MUSIC, ThemeType.valueOf("YOUTUBE_MUSIC"))
        assertEquals(ThemeType.SOUNDCLOUD, ThemeType.valueOf("SOUNDCLOUD"))
        assertEquals(ThemeType.TIDAL, ThemeType.valueOf("TIDAL"))
    }
}
