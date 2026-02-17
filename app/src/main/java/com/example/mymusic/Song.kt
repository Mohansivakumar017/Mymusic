package com.example.mymusic

import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata

data class Song(
    val id: Long,
    val title: String,
    val path: String,
    val duration: Long,
    val dateAdded: Long,
    val albumId: Long,
    val artist: String = "Unknown Artist",
    val album: String = "Unknown Album"
) {
    fun getAlbumArtUri(): Uri {
        return Uri.parse("content://media/external/audio/albumart/$albumId")
    }
    
    fun toMediaItem(): MediaItem {
        return MediaItem.Builder()
            .setUri(path)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(title)
                    .setArtist(artist)
                    .setAlbumTitle(album)
                    .build()
            )
            .build()
    }
}
