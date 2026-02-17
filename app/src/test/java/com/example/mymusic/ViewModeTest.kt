package com.example.mymusic

import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for ViewMode enum
 */
class ViewModeTest {
    
    @Test
    fun viewMode_hasAllRequiredValues() {
        val values = ViewMode.entries
        
        assertEquals(3, values.size)
        assertTrue(values.contains(ViewMode.ALL_SONGS))
        assertTrue(values.contains(ViewMode.FAVORITES))
        assertTrue(values.contains(ViewMode.PLAYLIST))
    }
    
    @Test
    fun viewMode_canBeCompared() {
        assertEquals(ViewMode.ALL_SONGS, ViewMode.ALL_SONGS)
        assertNotEquals(ViewMode.ALL_SONGS, ViewMode.FAVORITES)
        assertNotEquals(ViewMode.FAVORITES, ViewMode.PLAYLIST)
    }
}
