package com.example.mymusic

import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for ThemeType enum
 */
class ThemeTypeTest {
    
    @Test
    fun themeType_hasThreeValues() {
        val themes = ThemeType.values()
        assertEquals(3, themes.size)
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
    fun themeType_containsIosGlass() {
        val themes = ThemeType.values()
        assertTrue(themes.contains(ThemeType.IOS_GLASS))
    }
    
    @Test
    fun themeType_valueOf_worksCorrectly() {
        assertEquals(ThemeType.SPOTIFY, ThemeType.valueOf("SPOTIFY"))
        assertEquals(ThemeType.APPLE_MUSIC, ThemeType.valueOf("APPLE_MUSIC"))
        assertEquals(ThemeType.IOS_GLASS, ThemeType.valueOf("IOS_GLASS"))
    }
}
