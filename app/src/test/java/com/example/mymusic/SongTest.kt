package com.example.mymusic

import android.net.Uri
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for Song data class
 */
class SongTest {
    
    @Test
    fun song_createsWithAllParameters() {
        val song = Song(
            id = 1L,
            title = "Test Song",
            path = "/path/to/song.mp3",
            duration = 180000L,
            dateAdded = 1234567890L,
            albumId = 100L,
            artist = "Test Artist",
            album = "Test Album"
        )
        
        assertEquals(1L, song.id)
        assertEquals("Test Song", song.title)
        assertEquals("/path/to/song.mp3", song.path)
        assertEquals(180000L, song.duration)
        assertEquals(1234567890L, song.dateAdded)
        assertEquals(100L, song.albumId)
        assertEquals("Test Artist", song.artist)
        assertEquals("Test Album", song.album)
    }
    
    @Test
    fun song_usesDefaultArtistWhenNotProvided() {
        val song = Song(
            id = 1L,
            title = "Test Song",
            path = "/path/to/song.mp3",
            duration = 180000L,
            dateAdded = 1234567890L,
            albumId = 100L
        )
        
        assertEquals("Unknown Artist", song.artist)
    }
    
    @Test
    fun song_usesDefaultAlbumWhenNotProvided() {
        val song = Song(
            id = 1L,
            title = "Test Song",
            path = "/path/to/song.mp3",
            duration = 180000L,
            dateAdded = 1234567890L,
            albumId = 100L
        )
        
        assertEquals("Unknown Album", song.album)
    }
    
    @Test
    fun song_getAlbumArtUri_createsCorrectUri() {
        val song = Song(
            id = 1L,
            title = "Test Song",
            path = "/path/to/song.mp3",
            duration = 180000L,
            dateAdded = 1234567890L,
            albumId = 123L
        )
        
        val expectedUri = Uri.parse("content://media/external/audio/albumart/123")
        assertEquals(expectedUri, song.getAlbumArtUri())
    }
}
