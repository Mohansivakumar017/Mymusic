package com.example.mymusic

import android.content.Context
import android.content.SharedPreferences
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever

/**
 * Unit tests for FavoritesManager
 */
@RunWith(MockitoJUnitRunner::class)
class FavoritesManagerTest {
    
    @Mock
    private lateinit var mockContext: Context
    
    @Mock
    private lateinit var mockSharedPreferences: SharedPreferences
    
    @Mock
    private lateinit var mockEditor: SharedPreferences.Editor
    
    private lateinit var favoritesManager: FavoritesManager
    
    @Before
    fun setup() {
        whenever(mockContext.getSharedPreferences("favorites_prefs", Context.MODE_PRIVATE))
            .thenReturn(mockSharedPreferences)
        whenever(mockSharedPreferences.edit()).thenReturn(mockEditor)
        whenever(mockEditor.putString(any(), any())).thenReturn(mockEditor)
        whenever(mockEditor.apply()).then { }
        
        favoritesManager = FavoritesManager(mockContext)
    }
    
    @Test
    fun getFavorites_returnsEmptySetWhenNoFavorites() {
        whenever(mockSharedPreferences.getString("favorite_songs", "")).thenReturn("")
        
        val favorites = favoritesManager.getFavorites()
        
        assertTrue(favorites.isEmpty())
    }
    
    @Test
    fun getFavorites_parsesFavoritesCorrectly() {
        whenever(mockSharedPreferences.getString("favorite_songs", "")).thenReturn("1,2,3")
        
        val favorites = favoritesManager.getFavorites()
        
        assertEquals(3, favorites.size)
        assertTrue(favorites.contains(1L))
        assertTrue(favorites.contains(2L))
        assertTrue(favorites.contains(3L))
    }
    
    @Test
    fun isFavorite_returnsTrueForFavoriteSong() {
        whenever(mockSharedPreferences.getString("favorite_songs", "")).thenReturn("1,2,3")
        
        assertTrue(favoritesManager.isFavorite(2L))
    }
    
    @Test
    fun isFavorite_returnsFalseForNonFavoriteSong() {
        whenever(mockSharedPreferences.getString("favorite_songs", "")).thenReturn("1,2,3")
        
        assertFalse(favoritesManager.isFavorite(5L))
    }
    
    @Test
    fun toggleFavorite_addsSongWhenNotFavorite() {
        whenever(mockSharedPreferences.getString("favorite_songs", "")).thenReturn("1,2,3")
        
        val result = favoritesManager.toggleFavorite(5L)
        
        assertTrue(result)
        verify(mockEditor).putString(eq("favorite_songs"), contains("5"))
        verify(mockEditor).apply()
    }
    
    @Test
    fun toggleFavorite_removesSongWhenAlreadyFavorite() {
        whenever(mockSharedPreferences.getString("favorite_songs", "")).thenReturn("1,2,3")
        
        val result = favoritesManager.toggleFavorite(2L)
        
        assertFalse(result)
        verify(mockEditor).apply()
    }
}
