package com.example.mymusic

import android.net.Uri

data class Song(
    val id: Long,
    val title: String,
    val path: String,
    val duration: Long,
    val dateAdded: Long,
    val albumId: Long
) {
    fun getAlbumArtUri(): Uri {
        return Uri.parse("content://media/external/audio/albumart/$albumId")
    }
}
