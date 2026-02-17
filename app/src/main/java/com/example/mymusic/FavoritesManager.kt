package com.example.mymusic

import android.content.Context
import android.content.SharedPreferences

class FavoritesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("favorites_prefs", Context.MODE_PRIVATE)
    
    companion object {
        private const val FAVORITES_KEY = "favorite_songs"
    }
    
    fun getFavorites(): Set<Long> {
        val favoritesString = prefs.getString(FAVORITES_KEY, "") ?: ""
        return if (favoritesString.isEmpty()) {
            emptySet()
        } else {
            favoritesString.split(",").mapNotNull { it.toLongOrNull() }.toSet()
        }
    }
    
    fun isFavorite(songId: Long): Boolean {
        return getFavorites().contains(songId)
    }
    
    fun toggleFavorite(songId: Long): Boolean {
        val favorites = getFavorites().toMutableSet()
        val isFavorite = if (favorites.contains(songId)) {
            favorites.remove(songId)
            false
        } else {
            favorites.add(songId)
            true
        }
        saveFavorites(favorites)
        return isFavorite
    }
    
    private fun saveFavorites(favorites: Set<Long>) {
        val favoritesString = favorites.joinToString(",")
        prefs.edit().putString(FAVORITES_KEY, favoritesString).apply()
    }
}
