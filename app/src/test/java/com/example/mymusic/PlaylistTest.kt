package com.example.mymusic

import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for Playlist data class
 */
class PlaylistTest {
    
    @Test
    fun playlist_createsWithAllParameters() {
        val songIds = mutableListOf(1L, 2L, 3L)
        val playlist = Playlist(
            id = 1L,
            name = "My Favorites",
            songIds = songIds
        )
        
        assertEquals(1L, playlist.id)
        assertEquals("My Favorites", playlist.name)
        assertEquals(3, playlist.songIds.size)
        assertTrue(playlist.songIds.contains(1L))
        assertTrue(playlist.songIds.contains(2L))
        assertTrue(playlist.songIds.contains(3L))
    }
    
    @Test
    fun playlist_createsWithEmptySongList() {
        val playlist = Playlist(
            id = 1L,
            name = "Empty Playlist"
        )
        
        assertEquals(1L, playlist.id)
        assertEquals("Empty Playlist", playlist.name)
        assertEquals(0, playlist.songIds.size)
    }
    
    @Test
    fun playlist_songIdsAreMutable() {
        val playlist = Playlist(
            id = 1L,
            name = "Test Playlist"
        )
        
        // Initially empty
        assertEquals(0, playlist.songIds.size)
        
        // Add songs
        playlist.songIds.add(1L)
        playlist.songIds.add(2L)
        
        assertEquals(2, playlist.songIds.size)
        assertTrue(playlist.songIds.contains(1L))
        assertTrue(playlist.songIds.contains(2L))
    }
}
