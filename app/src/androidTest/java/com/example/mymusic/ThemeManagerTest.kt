package com.example.mymusic

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test for ThemeManager
 * Tests theme persistence using SharedPreferences
 */
@RunWith(AndroidJUnit4::class)
class ThemeManagerTest {
    
    private lateinit var context: Context
    private lateinit var themeManager: ThemeManager
    
    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        themeManager = ThemeManager(context)
        // Clear any existing theme preference
        context.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()
    }
    
    @After
    fun tearDown() {
        // Clean up after tests
        context.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()
    }
    
    @Test
    fun themeManager_defaultTheme_isSpotify() {
        val defaultTheme = themeManager.getTheme()
        assertEquals(ThemeType.SPOTIFY, defaultTheme)
    }
    
    @Test
    fun themeManager_saveAndRetrieveSpotifyTheme() {
        themeManager.saveTheme(ThemeType.SPOTIFY)
        val retrievedTheme = themeManager.getTheme()
        assertEquals(ThemeType.SPOTIFY, retrievedTheme)
    }
    
    @Test
    fun themeManager_saveAndRetrieveAppleMusicTheme() {
        themeManager.saveTheme(ThemeType.APPLE_MUSIC)
        val retrievedTheme = themeManager.getTheme()
        assertEquals(ThemeType.APPLE_MUSIC, retrievedTheme)
    }
    
    @Test
    fun themeManager_saveAndRetrieveIosGlassTheme() {
        themeManager.saveTheme(ThemeType.IOS_GLASS)
        val retrievedTheme = themeManager.getTheme()
        assertEquals(ThemeType.IOS_GLASS, retrievedTheme)
    }
    
    @Test
    fun themeManager_persistsAcrossInstances() {
        themeManager.saveTheme(ThemeType.APPLE_MUSIC)
        
        // Create a new instance of ThemeManager
        val newThemeManager = ThemeManager(context)
        val retrievedTheme = newThemeManager.getTheme()
        
        assertEquals(ThemeType.APPLE_MUSIC, retrievedTheme)
    }
    
    @Test
    fun themeManager_overwritesPreviousTheme() {
        themeManager.saveTheme(ThemeType.SPOTIFY)
        assertEquals(ThemeType.SPOTIFY, themeManager.getTheme())
        
        themeManager.saveTheme(ThemeType.IOS_GLASS)
        assertEquals(ThemeType.IOS_GLASS, themeManager.getTheme())
    }
}
