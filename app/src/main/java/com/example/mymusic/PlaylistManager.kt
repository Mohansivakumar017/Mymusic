package com.example.mymusic

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

class PlaylistManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("playlist_prefs", Context.MODE_PRIVATE)
    
    companion object {
        private const val PLAYLISTS_KEY = "playlists"
        private const val NEXT_ID_KEY = "next_playlist_id"
    }
    
    fun getPlaylists(): List<Playlist> {
        val playlistsJson = prefs.getString(PLAYLISTS_KEY, "[]") ?: "[]"
        val playlists = mutableListOf<Playlist>()
        
        try {
            val jsonArray = JSONArray(playlistsJson)
            for (i in 0 until jsonArray.length()) {
                val json = jsonArray.getJSONObject(i)
                val id = json.getLong("id")
                val name = json.getString("name")
                val songIdsArray = json.getJSONArray("songIds")
                val songIds = mutableListOf<Long>()
                for (j in 0 until songIdsArray.length()) {
                    songIds.add(songIdsArray.getLong(j))
                }
                playlists.add(Playlist(id, name, songIds))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        return playlists
    }
    
    fun createPlaylist(name: String): Playlist {
        val id = getNextId()
        val playlist = Playlist(id, name, mutableListOf())
        val playlists = getPlaylists().toMutableList()
        playlists.add(playlist)
        savePlaylists(playlists)
        return playlist
    }
    
    fun updatePlaylist(playlist: Playlist) {
        val playlists = getPlaylists().toMutableList()
        val index = playlists.indexOfFirst { it.id == playlist.id }
        if (index != -1) {
            playlists[index] = playlist
            savePlaylists(playlists)
        }
    }
    
    fun deletePlaylist(playlistId: Long) {
        val playlists = getPlaylists().toMutableList()
        playlists.removeAll { it.id == playlistId }
        savePlaylists(playlists)
    }
    
    fun addSongToPlaylist(playlistId: Long, songId: Long) {
        val playlists = getPlaylists().toMutableList()
        val playlist = playlists.find { it.id == playlistId }
        if (playlist != null && !playlist.songIds.contains(songId)) {
            playlist.songIds.add(songId)
            updatePlaylist(playlist)
        }
    }
    
    fun removeSongFromPlaylist(playlistId: Long, songId: Long) {
        val playlists = getPlaylists().toMutableList()
        val playlist = playlists.find { it.id == playlistId }
        if (playlist != null) {
            playlist.songIds.remove(songId)
            updatePlaylist(playlist)
        }
    }
    
    private fun savePlaylists(playlists: List<Playlist>) {
        val jsonArray = JSONArray()
        playlists.forEach { playlist ->
            val json = JSONObject()
            json.put("id", playlist.id)
            json.put("name", playlist.name)
            val songIdsArray = JSONArray()
            playlist.songIds.forEach { songIdsArray.put(it) }
            json.put("songIds", songIdsArray)
            jsonArray.put(json)
        }
        prefs.edit().putString(PLAYLISTS_KEY, jsonArray.toString()).apply()
    }
    
    private fun getNextId(): Long {
        val nextId = prefs.getLong(NEXT_ID_KEY, 1)
        prefs.edit().putLong(NEXT_ID_KEY, nextId + 1).apply()
        return nextId
    }
}
