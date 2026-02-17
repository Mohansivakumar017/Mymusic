package com.example.mymusic

import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for SortMode enum
 */
class SortModeTest {
    
    @Test
    fun sortMode_hasFourValues() {
        val modes = SortMode.values()
        assertEquals(4, modes.size)
    }
    
    @Test
    fun sortMode_containsByName() {
        val modes = SortMode.values()
        assertTrue(modes.contains(SortMode.BY_NAME))
    }
    
    @Test
    fun sortMode_containsByDate() {
        val modes = SortMode.values()
        assertTrue(modes.contains(SortMode.BY_DATE))
    }
    
    @Test
    fun sortMode_containsByDuration() {
        val modes = SortMode.values()
        assertTrue(modes.contains(SortMode.BY_DURATION))
    }
    
    @Test
    fun sortMode_containsByArtist() {
        val modes = SortMode.values()
        assertTrue(modes.contains(SortMode.BY_ARTIST))
    }
    
    @Test
    fun sortMode_valueOf_worksCorrectly() {
        assertEquals(SortMode.BY_NAME, SortMode.valueOf("BY_NAME"))
        assertEquals(SortMode.BY_ARTIST, SortMode.valueOf("BY_ARTIST"))
        assertEquals(SortMode.BY_DATE, SortMode.valueOf("BY_DATE"))
        assertEquals(SortMode.BY_DURATION, SortMode.valueOf("BY_DURATION"))
    }
}
