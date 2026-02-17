package com.example.mymusic

data class Playlist(
    val id: Long,
    val name: String,
    val songIds: MutableList<Long> = mutableListOf()
)
