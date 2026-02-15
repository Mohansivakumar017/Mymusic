package com.example.mymusic

data class Song(
    val id: Long,
    val title: String,
    val path: String,
    val duration: Long,
    val dateAdded: Long
)
