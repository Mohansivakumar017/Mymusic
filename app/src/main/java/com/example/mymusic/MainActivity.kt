package com.example.mymusic

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import coil.transform.RoundedCornersTransformation
import com.example.mymusic.databinding.ActivityMainBinding
import com.example.mymusic.databinding.ViewNowPlayingBinding
import com.example.mymusic.databinding.ViewMiniPlayerBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var nowPlayingBinding: ViewNowPlayingBinding
    private lateinit var miniPlayerBinding: ViewMiniPlayerBinding
    private lateinit var player: ExoPlayer
    private val songs = mutableListOf<Song>()
    private val filteredSongs = mutableListOf<Song>()
    private lateinit var adapter: SongAdapter
    private lateinit var themeManager: ThemeManager
    private lateinit var favoritesManager: FavoritesManager
    private lateinit var playlistManager: PlaylistManager
    private lateinit var sleepTimerManager: SleepTimerManager
    private var currentTheme: ThemeType = ThemeType.SPOTIFY
    private var isSearching = false
    private var currentSortMode = SortMode.BY_NAME
    private var currentViewMode = ViewMode.ALL_SONGS
    private var currentPlaylistId: Long? = null
    
    // Now Playing UI components
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    private var currentPlayingSong: Song? = null
    private val progressHandler = android.os.Handler(android.os.Looper.getMainLooper())
    private lateinit var progressUpdateRunnable: Runnable
    
    // Player Control View components
    private var nowPlayingTitle: TextView? = null
    private var nowPlayingArtist: TextView? = null
    private var nowPlayingAlbumArt: ImageView? = null
    private var favoriteButton: ImageView? = null
    private var sleepTimerButton: ImageView? = null
    private var miniShuffleButton: ImageView? = null
    private var miniRepeatButton: ImageView? = null
    
    // MediaSession for notifications and lock screen
    private var mediaSession: MediaSession? = null
    private var notificationHelper: NotificationHelper? = null

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                loadSongsAndStart()
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    
    private val requestNotificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (!isGranted) {
                Toast.makeText(this, "Notification permission denied. You won't see playback notifications.", Toast.LENGTH_LONG).show()
            }
        }
    
    private val themeSelectionLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                // Theme was changed, recreate activity
                currentTheme = themeManager.getTheme()
                recreate()
            }
        }

    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize theme manager
        themeManager = ThemeManager(this)
        currentTheme = themeManager.getTheme()
        
        // Initialize favorites and playlist managers
        favoritesManager = FavoritesManager(this)
        playlistManager = PlaylistManager(this)
        sleepTimerManager = SleepTimerManager()
        
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Initialize ViewBinding for now playing screen and mini player
        nowPlayingBinding = binding.nowPlayingContainer
        val miniPlayerView = binding.miniPlayerContainer.findViewById<View>(R.id.mini_player_root)
        miniPlayerBinding = ViewMiniPlayerBinding.bind(miniPlayerView)
        
        // Setup toolbar
        setSupportActionBar(binding.toolbar)
        
        // Apply theme
        applyTheme()

        try {
            player = ExoPlayer.Builder(this).build()
            
            // Setup MediaSession for notifications and lock screen
            setupMediaSession()
            
            // Set player to PlayerControlView
            binding.playerControlView.player = player
            // Disable auto-hide timeout to keep controller always visible
            binding.playerControlView.setShowTimeoutMs(0)
            binding.playerControlView.show()
            
            player.addListener(object : Player.Listener {
                override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                    super.onMediaItemTransition(mediaItem, reason)
                    
                    // Update current playing song when media item changes
                    // Use the actual MediaItem URI to find the song instead of relying on index
                    val currentUri = mediaItem?.localConfiguration?.uri?.path
                    if (currentUri != null) {
                        // Find the song by matching its path to avoid index mismatches
                        val song = filteredSongs.find { it.path == currentUri }
                        if (song != null) {
                            currentPlayingSong = song
                            updateNowPlayingInfoImmediate(song)
                        } else {
                            // Song not in filtered list - could have been filtered out
                            Log.w("MainActivity", "Playing song not found in filtered list")
                            currentPlayingSong = null
                            // Controller stays visible if songs are available
                        }
                    } else {
                        // No media item - clear current song
                        currentPlayingSong = null
                        // Controller stays visible if songs are available
                    }
                    updateNowPlayingUI()
                }
                
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    super.onIsPlayingChanged(isPlaying)
                    updatePlayPauseButtons()
                    
                    // Keep player control always visible when any songs exist in library
                    // Don't hide it even if filteredSongs is empty due to search/filter
                    updatePlayerControlVisibility()
                }
            })
            
            // Get references to now playing views (delayed until after songs are loaded)
            binding.playerControlView.post {
                nowPlayingTitle = binding.playerControlView.findViewById(R.id.now_playing_title)
                nowPlayingArtist = binding.playerControlView.findViewById(R.id.now_playing_artist)
                nowPlayingAlbumArt = binding.playerControlView.findViewById(R.id.now_playing_album_art)
                favoriteButton = binding.playerControlView.findViewById(R.id.btn_favorite)
                sleepTimerButton = binding.playerControlView.findViewById(R.id.btn_sleep_timer)
                miniShuffleButton = binding.playerControlView.findViewById(R.id.btn_shuffle)
                miniRepeatButton = binding.playerControlView.findViewById(R.id.btn_repeat)
                
                // Setup favorite button click
                favoriteButton?.setOnClickListener {
                    currentPlayingSong?.let { song ->
                        toggleFavorite(song)
                    }
                }
                
                // Setup sleep timer button click
                sleepTimerButton?.setOnClickListener {
                    showSleepTimerDialog()
                }
                
                // Setup shuffle button click
                miniShuffleButton?.setOnClickListener {
                    player.shuffleModeEnabled = !player.shuffleModeEnabled
                    // Update all shuffle buttons to keep them in sync
                    updateMiniShuffleButton()
                    updateMainShuffleButton()
                    updateShuffleButton()
                    val message = if (player.shuffleModeEnabled) getString(R.string.shuffle_on) else getString(R.string.shuffle_off)
                    Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
                }
                
                // Setup repeat button click
                miniRepeatButton?.setOnClickListener {
                    player.repeatMode = when (player.repeatMode) {
                        Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
                        Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
                        else -> Player.REPEAT_MODE_OFF
                    }
                    // Update all repeat buttons to keep them in sync
                    updateMiniRepeatButton()
                    updateRepeatButton()
                    val message = when (player.repeatMode) {
                        Player.REPEAT_MODE_ALL -> getString(R.string.repeat_all)
                        Player.REPEAT_MODE_ONE -> getString(R.string.repeat_one)
                        else -> getString(R.string.repeat_off)
                    }
                    Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
                }
                
                // Update now playing info will be called after songs load
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Error initializing player", e)
        }

        setupNowPlayingUI()
        
        // Hide mini player permanently - using PlayerControlView instead
        binding.miniPlayerContainer.visibility = View.GONE
        
        setupRecyclerView()
        setupButtons()
        checkPermissionAndLoad()
        
        // Request notification permission for Android 13+ (API 33+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) 
                != PackageManager.PERMISSION_GRANTED) {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun setupRecyclerView() {
        filteredSongs.addAll(songs)
        adapter = SongAdapter(
            songs = filteredSongs,
            theme = currentTheme,
            onSongClick = { position -> playSongAt(position) },
            onSongLongClick = { position -> 
                val song = filteredSongs[position]
                showSongOptionsDialog(song)
            }
        )
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }

    private fun setupButtons() {
        binding.btnSort.setOnClickListener {
            currentSortMode = when (currentSortMode) {
                SortMode.BY_NAME -> SortMode.BY_DATE
                SortMode.BY_DATE -> SortMode.BY_DURATION
                SortMode.BY_DURATION -> SortMode.BY_NAME
            }
            applySorting()
        }
        binding.btnShuffleMain.setOnClickListener {
            if (filteredSongs.isNotEmpty()) {
                // Toggle shuffle mode
                player.shuffleModeEnabled = !player.shuffleModeEnabled
                
                // Update visual state
                updateMainShuffleButton()
                
                // If no song is playing, start from first song
                if (player.mediaItemCount == 0 || player.currentMediaItemIndex == -1) {
                    player.seekTo(0, 0)
                    player.play()
                }
                
                val message = if (player.shuffleModeEnabled) "Shuffle Mode On" else "Shuffle Mode Off"
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun performSorting() {
        when (currentSortMode) {
            SortMode.BY_NAME -> filteredSongs.sortBy { it.title.lowercase() }
            SortMode.BY_ARTIST -> filteredSongs.sortBy { it.artist.lowercase() }
            SortMode.BY_DATE -> filteredSongs.sortByDescending { it.dateAdded }
            SortMode.BY_DURATION -> filteredSongs.sortByDescending { it.duration }
        }
    }
    
    private fun applySorting() {
        performSorting()
        
        // Update button text
        when (currentSortMode) {
            SortMode.BY_NAME -> binding.btnSort.text = getString(R.string.sort_by_name)
            SortMode.BY_ARTIST -> binding.btnSort.text = getString(R.string.sort_by_artist)
            SortMode.BY_DATE -> binding.btnSort.text = getString(R.string.sort_by_date)
            SortMode.BY_DURATION -> binding.btnSort.text = getString(R.string.sort_by_duration)
        }
        
        adapter.notifyDataSetChanged()
        // Don't update player when sorting - the same songs are still there, just reordered
        // This prevents playback interruption/blinking when sorting
    }

    private fun onDataChanged() {
        if (!isSearching) {
            filteredSongs.clear()
            filteredSongs.addAll(songs)
        }
        adapter.notifyDataSetChanged()
        updatePlayerWithFilteredSongs()
    }

    private fun checkPermissionAndLoad() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        when {
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED -> {
                loadSongsAndStart()
            }
            else -> {
                requestPermissionLauncher.launch(permission)
            }
        }
    }

    private fun loadSongsAndStart() {
        loadSongs()
        if (songs.isNotEmpty()) {
            filteredSongs.clear()
            filteredSongs.addAll(songs)
            currentSortMode = SortMode.BY_NAME // Ensure default sort
            applySorting() // Apply default sort
            updatePlayerPlaylist()
            // Show controller permanently once songs are loaded
            updatePlayerControlVisibility()
        } else {
            Toast.makeText(this, "No songs found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadSongs() {
        songs.clear()
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM
        )
        
        try {
            val cursor = contentResolver.query(uri, projection, null, null, null)

            cursor?.use {
                val idColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val dataColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
                val durationColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val dateAddedColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
                val albumIdColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                val artistColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)

                while (it.moveToNext()) {
                    val id = it.getLong(idColumn)
                    val title = it.getString(titleColumn) ?: "Unknown"
                    val path = it.getString(dataColumn) ?: ""
                    val duration = it.getLong(durationColumn)
                    val dateAdded = it.getLong(dateAddedColumn)
                    val albumId = it.getLong(albumIdColumn)
                    val artist = it.getString(artistColumn) ?: "Unknown Artist"
                    val album = it.getString(albumColumn) ?: "Unknown Album"

                    if (path.isNotEmpty()) {
                        songs.add(Song(id, title, path, duration, dateAdded, albumId, artist, album))
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Error loading songs", e)
        }
    }

    private fun updatePlayerPlaylist() {
        val mediaItems = filteredSongs.map { it.toMediaItem() }
        player.setMediaItems(mediaItems)
        player.prepare()
    }

    private fun updatePlayerWithFilteredSongs() {
        // Don't hide the controller even if filteredSongs is empty
        // Keep it visible as long as songs exist in the library
        if (songs.isEmpty()) {
            currentPlayingSong = null
            hidePlayerControlView()
            return
        }
        
        if (filteredSongs.isEmpty()) {
            // Filtered list is empty but library has songs
            // Stop playback but keep controller visible
            player.stop()
            player.clearMediaItems()
            currentPlayingSong = null
            updatePlayerControlVisibility()
            return
        }
        
        // Get current playing song and position if any
        val wasPlaying = player.isPlaying
        val currentPosition = player.currentPosition
        val currentSongPath = if (player.currentMediaItemIndex >= 0) {
            player.currentMediaItem?.localConfiguration?.uri?.path
        } else null
        
        // Update player with filtered songs
        val mediaItems = filteredSongs.map { it.toMediaItem() }
        player.setMediaItems(mediaItems)
        player.prepare()
        
        // If there was a song playing, try to continue it
        if (currentSongPath != null) {
            val newIndex = filteredSongs.indexOfFirst { it.path == currentSongPath }
            if (newIndex >= 0) {
                player.seekTo(newIndex, currentPosition)
                if (wasPlaying) {
                    player.play()
                }
                // currentPlayingSong will be updated via onMediaItemTransition
            } else {
                // Song was filtered out - controller stays visible
                currentPlayingSong = null
            }
        } else {
            // No song was playing
            currentPlayingSong = null
        }
        
        // Show player control when songs are available
        if (filteredSongs.isNotEmpty()) {
            binding.playerControlView.visibility = View.VISIBLE
        }
    }

    private fun playSongAt(index: Int) {
        if (index !in filteredSongs.indices) return
        
        try {
            player.seekTo(index, 0)
            player.play()
            
            // Show the player control view - onMediaItemTransition will update the info
            binding.playerControlView.visibility = View.VISIBLE
            
        } catch (e: Exception) {
            Log.e("MainActivity", "Error playing song", e)
            Toast.makeText(this, "Error playing song", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        progressHandler.removeCallbacks(progressUpdateRunnable)
        sleepTimerManager.cancelTimer()
        notificationHelper?.release()
        mediaSession?.release()
        if (::player.isInitialized) {
            player.release()
        }
    }
    
    override fun onPause() {
        super.onPause()
        // Save player state
        if (::player.isInitialized && currentPlayingSong != null) {
            val prefs = getSharedPreferences("MusicPlayerPrefs", MODE_PRIVATE)
            prefs.edit().apply {
                putBoolean("wasPlaying", player.isPlaying)
                putInt("lastPosition", player.currentPosition.toInt())
                putLong("lastSongId", currentPlayingSong?.id ?: -1L)
                apply()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Restore player visibility if music was playing or paused with content
        if (::player.isInitialized && player.currentMediaItemIndex >= 0) {
            binding.playerControlView.visibility = View.VISIBLE
            updateNowPlayingInfo()
        }
    }
    
    private fun setupNowPlayingUI() {
        // Setup bottom sheet behavior
        bottomSheetBehavior = BottomSheetBehavior.from(nowPlayingBinding.root)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        bottomSheetBehavior.isHideable = true
        
        // PlayerControlView click to expand bottom sheet
        binding.playerControlView.setOnClickListener {
            if (player.isPlaying || player.currentMediaItemIndex >= 0) {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }
        
        // Collapse button
        nowPlayingBinding.btnCollapse.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        }
        
        // Setup playback controls for mini player (kept for compatibility)
        miniPlayerBinding.miniPlayPause.setOnClickListener {
            if (player.isPlaying) {
                player.pause()
            } else {
                player.play()
            }
        }
        
        // Setup playback controls for full player
        nowPlayingBinding.fullPlayPause.setOnClickListener {
            if (player.isPlaying) {
                player.pause()
            } else {
                player.play()
            }
        }
        
        nowPlayingBinding.fullPrevious.setOnClickListener {
            if (player.hasPreviousMediaItem()) {
                player.seekToPreviousMediaItem()
                // Let onMediaItemTransition handle the UI update
            }
        }
        
        nowPlayingBinding.fullNext.setOnClickListener {
            if (player.hasNextMediaItem()) {
                player.seekToNextMediaItem()
                // Let onMediaItemTransition handle the UI update
            }
        }
        
        nowPlayingBinding.fullShuffle.setOnClickListener {
            player.shuffleModeEnabled = !player.shuffleModeEnabled
            updateShuffleButton()
            Toast.makeText(this, if (player.shuffleModeEnabled) "Shuffle On" else "Shuffle Off", Toast.LENGTH_SHORT).show()
        }
        
        nowPlayingBinding.fullRepeat.setOnClickListener {
            player.repeatMode = when (player.repeatMode) {
                Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
                Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
                else -> Player.REPEAT_MODE_OFF
            }
            updateRepeatButton()
        }
        
        // Setup favorite button
        nowPlayingBinding.fullFavorite.setOnClickListener {
            currentPlayingSong?.let { song ->
                toggleFavorite(song)
            }
        }
        
        // Setup sleep timer button
        nowPlayingBinding.fullSleepTimer.setOnClickListener {
            showSleepTimerDialog()
        }
        
        // Setup progress bar to control player
        nowPlayingBinding.fullProgressBar.addListener(object : androidx.media3.ui.TimeBar.OnScrubListener {
            override fun onScrubStart(timeBar: androidx.media3.ui.TimeBar, position: Long) {
            }
            
            override fun onScrubMove(timeBar: androidx.media3.ui.TimeBar, position: Long) {
            }
            
            override fun onScrubStop(timeBar: androidx.media3.ui.TimeBar, position: Long, canceled: Boolean) {
                if (!canceled) {
                    player.seekTo(position)
                }
            }
        })
        
        // Start updating progress
        updateProgress()
        
        // Bottom sheet callback - FIXED: Don't hide PlayerControlView
        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                // Don't change visibility - PlayerControlView stays visible
                // Only the bottom sheet (full player) expands/collapses
            }
            
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                // Optional: Implement fade effect or other animations
            }
        })
    }
    
    private fun updateProgress() {
        progressUpdateRunnable = object : Runnable {
            override fun run() {
                if (::player.isInitialized) {
                    val position = player.currentPosition
                    val duration = player.duration
                    
                    nowPlayingBinding.fullProgressBar.setPosition(position)
                    nowPlayingBinding.fullProgressBar.setDuration(duration)
                    
                    nowPlayingBinding.fullCurrentTime.text = formatTime(position)
                    nowPlayingBinding.fullTotalTime.text = formatTime(duration)
                }
                progressHandler.postDelayed(this, 500)
            }
        }
        progressHandler.post(progressUpdateRunnable)
    }
    
    private fun formatTime(millis: Long): String {
        if (millis < 0) return "0:00"
        val seconds = (millis / 1000).toInt()
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format("%d:%02d", minutes, remainingSeconds)
    }
    
    private fun updateNowPlayingUI() {
        val song = currentPlayingSong
        if (song != null) {
            // Update full player (bottom sheet)
            nowPlayingBinding.fullSongTitle.text = song.title
            nowPlayingBinding.fullSongArtist.text = song.artist
            nowPlayingBinding.fullSongAlbum.text = song.album
            nowPlayingBinding.fullAlbumArt.load(song.getAlbumArtUri()) {
                crossfade(true)
                placeholder(R.drawable.ic_music_note)
                error(R.drawable.ic_music_note)
            }
            
            updatePlayPauseButtons()
            updateShuffleButton()
            updateRepeatButton()
            updateMiniShuffleButton()
            updateMiniRepeatButton()
        }
    }
    
    private fun updatePlayPauseButtons() {
        val isPlaying = player.isPlaying
        
        val iconRes = if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play
        // Mini player is hidden - only update full player
        nowPlayingBinding.fullPlayPause.setImageResource(iconRes)
    }
    
    private fun updateShuffleButton() {
        val colors = ThemeHelper.getThemeColors(currentTheme)
        val tint = if (player.shuffleModeEnabled) colors.primary else colors.onSurface
        nowPlayingBinding.fullShuffle.setColorFilter(tint)
    }
    
    private fun updateMainShuffleButton() {
        val colors = ThemeHelper.getThemeColors(currentTheme)
        val tint = if (player.shuffleModeEnabled) colors.primary else colors.onSurface
        binding.btnShuffleMain.setColorFilter(tint)
    }
    
    private fun updateRepeatButton() {
        val colors = ThemeHelper.getThemeColors(currentTheme)
        val tint = when (player.repeatMode) {
            Player.REPEAT_MODE_OFF -> colors.onSurface
            else -> colors.primary
        }
        nowPlayingBinding.fullRepeat.setColorFilter(tint)
    }
    
    private fun updateMiniShuffleButton() {
        val colors = ThemeHelper.getThemeColors(currentTheme)
        val tint = if (player.shuffleModeEnabled) colors.primary else colors.onSurface
        miniShuffleButton?.setColorFilter(tint)
    }
    
    private fun updateMiniRepeatButton() {
        val colors = ThemeHelper.getThemeColors(currentTheme)
        val tint = when (player.repeatMode) {
            Player.REPEAT_MODE_OFF -> colors.onSurface
            else -> colors.primary
        }
        miniRepeatButton?.setColorFilter(tint)
    }
    
    private fun hidePlayerControlView() {
        binding.playerControlView.visibility = View.GONE
        // Also collapse the bottom sheet if it's expanded
        if (::bottomSheetBehavior.isInitialized && 
            bottomSheetBehavior.state != BottomSheetBehavior.STATE_HIDDEN) {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        }
    }
    
    private fun updatePlayerControlVisibility() {
        // Keep controller visible as long as songs exist in the library
        // Hide it only when library is completely empty
        if (songs.isNotEmpty()) {
            binding.playerControlView.visibility = View.VISIBLE
        } else {
            hidePlayerControlView()
        }
    }
    
    private fun updateNowPlayingInfoImmediate(song: Song) {
        // Make the player control view visible
        binding.playerControlView.visibility = View.VISIBLE
        
        // Update song info in PlayerControlView
        nowPlayingTitle?.text = song.title
        nowPlayingArtist?.text = song.artist
        nowPlayingAlbumArt?.load(song.getAlbumArtUri()) {
            placeholder(R.drawable.ic_music_note)
            error(R.drawable.ic_music_note)
            transformations(RoundedCornersTransformation(12f))
        }
        
        // Update favorite button
        val isFavorite = favoritesManager.isFavorite(song.id)
        updateFavoriteButton(isFavorite)
        
        // Update song info in full player (bottom sheet)
        nowPlayingBinding.fullSongTitle.text = song.title
        nowPlayingBinding.fullSongArtist.text = song.artist
        nowPlayingBinding.fullAlbumArt.load(song.getAlbumArtUri()) {
            placeholder(R.drawable.ic_music_note)
            error(R.drawable.ic_music_note)
        }
    }
    
    private fun updateNowPlayingInfo() {
        // Update now playing info using the current song reference
        // This is safer than looking up by index
        val song = currentPlayingSong
        if (song != null) {
            updateNowPlayingInfoImmediate(song)
        } else {
            // No song playing - try to get from player
            val currentUri = player.currentMediaItem?.localConfiguration?.uri?.path
            if (currentUri != null) {
                val foundSong = filteredSongs.find { it.path == currentUri }
                if (foundSong != null) {
                    currentPlayingSong = foundSong
                    updateNowPlayingInfoImmediate(foundSong)
                }
            }
        }
    }
    
    private fun applyTheme() {
        val colors = ThemeHelper.getThemeColors(currentTheme)
        
        // Apply background colors
        binding.root.setBackgroundColor(colors.background)
        binding.toolbar.setBackgroundColor(colors.surface)
        binding.toolbar.setTitleTextColor(colors.onBackground)
        binding.sortBar.setBackgroundColor(colors.surface)
        
        // Apply theme to mini player
        binding.miniPlayerContainer.setBackgroundColor(colors.surface)
        
        // Apply theme to now playing screen
        nowPlayingBinding.root.setBackgroundColor(colors.background)
        
        // Apply status bar color
        window.statusBarColor = colors.primaryDark
        
        // Apply gradient if needed
        if (colors.useGradient && colors.gradientStart != null && colors.gradientEnd != null) {
            val gradientDrawable = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(colors.gradientStart, colors.gradientEnd)
            )
            binding.root.background = gradientDrawable
            nowPlayingBinding.root.background = gradientDrawable
        }
        
        // Update button colors
        binding.btnSort.setTextColor(colors.onBackground)
        binding.btnShuffleMain.setColorFilter(colors.primary)
        
        // Recreate adapter to apply theme to items
        if (::adapter.isInitialized) {
            adapter = SongAdapter(
                songs = filteredSongs,
                theme = currentTheme,
                onSongClick = { position -> playSongAt(position) },
                onSongLongClick = { position -> 
                    val song = filteredSongs[position]
                    showSongOptionsDialog(song)
                }
            )
            binding.recyclerView.adapter = adapter
        }
    }
    
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_themes -> {
                val intent = Intent(this, ThemeSelectionActivity::class.java)
                themeSelectionLauncher.launch(intent)
                true
            }
            R.id.action_search -> {
                showSearchDialog()
                true
            }
            R.id.action_favorites -> {
                showFavorites()
                true
            }
            R.id.action_playlists -> {
                showPlaylistsDialog()
                true
            }
            R.id.action_all_songs -> {
                showAllSongs()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    private fun showSearchDialog() {
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Search Songs")
        
        val input = android.widget.EditText(this)
        input.hint = "Enter song, artist, or album name"
        val paddingHorizontal = resources.getDimensionPixelSize(R.dimen.search_input_padding_horizontal)
        val paddingVertical = resources.getDimensionPixelSize(R.dimen.search_input_padding_vertical)
        input.setPadding(paddingHorizontal, paddingVertical, paddingHorizontal, paddingVertical)
        builder.setView(input)
        
        builder.setPositiveButton("Search") { _, _ ->
            val query = input.text.toString()
            filterSongs(query)
        }
        
        builder.setNegativeButton("Clear") { _, _ ->
            clearFilter()
        }
        
        builder.setNeutralButton("Cancel") { dialog, _ ->
            dialog.cancel()
        }
        
        builder.show()
    }
    
    private fun filterSongs(query: String) {
        if (query.isEmpty()) {
            clearFilter()
            return
        }
        
        isSearching = true
        filteredSongs.clear()
        
        songs.forEach { song ->
            if (song.title.contains(query, ignoreCase = true) ||
                song.artist.contains(query, ignoreCase = true) ||
                song.album.contains(query, ignoreCase = true)) {
                filteredSongs.add(song)
            }
        }
        
        adapter.notifyDataSetChanged()
        updatePlayerWithFilteredSongs()
        Toast.makeText(this, "Found ${filteredSongs.size} songs", Toast.LENGTH_SHORT).show()
    }
    
    private fun clearFilter() {
        isSearching = false
        filteredSongs.clear()
        filteredSongs.addAll(songs)
        adapter.notifyDataSetChanged()
        updatePlayerWithFilteredSongs()
        Toast.makeText(this, "Showing all songs", Toast.LENGTH_SHORT).show()
    }
    
    // MediaSession setup for notifications and lock screen
    private fun setupMediaSession() {
        val sessionActivityIntent = Intent(this, MainActivity::class.java)
        val sessionActivityPendingIntent = android.app.PendingIntent.getActivity(
            this,
            0,
            sessionActivityIntent,
            android.app.PendingIntent.FLAG_IMMUTABLE or android.app.PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        mediaSession = MediaSession.Builder(this, player)
            .setSessionActivity(sessionActivityPendingIntent)
            .build()
        
        // Initialize notification helper to show media notifications
        notificationHelper = NotificationHelper(this, player, mediaSession!!)
    }
    
    // Favorites functionality
    private fun toggleFavorite(song: Song) {
        val isFavorite = favoritesManager.toggleFavorite(song.id)
        updateFavoriteButton(isFavorite)
        
        val message = if (isFavorite) {
            "Added to favorites"
        } else {
            "Removed from favorites"
        }
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
    
    private fun updateFavoriteButton(isFavorite: Boolean) {
        val iconRes = if (isFavorite) R.drawable.ic_favorite else R.drawable.ic_favorite_border
        favoriteButton?.setImageResource(iconRes)
        nowPlayingBinding.fullFavorite.setImageResource(iconRes)
        
        // Update button color
        val colors = ThemeHelper.getThemeColors(currentTheme)
        val tint = if (isFavorite) colors.primary else colors.onSurface
        favoriteButton?.setColorFilter(tint)
        nowPlayingBinding.fullFavorite.setColorFilter(tint)
    }
    
    private fun showFavorites() {
        currentViewMode = ViewMode.FAVORITES
        currentPlaylistId = null
        isSearching = false
        
        val favoriteIds = favoritesManager.getFavorites()
        filteredSongs.clear()
        filteredSongs.addAll(songs.filter { favoriteIds.contains(it.id) })
        
        applySorting()
        
        supportActionBar?.title = getString(R.string.favorites)
        Toast.makeText(this, "Showing ${filteredSongs.size} favorite songs", Toast.LENGTH_SHORT).show()
    }
    
    private fun showAllSongs() {
        currentViewMode = ViewMode.ALL_SONGS
        currentPlaylistId = null
        isSearching = false
        
        filteredSongs.clear()
        filteredSongs.addAll(songs)
        
        applySorting()
        
        supportActionBar?.title = getString(R.string.app_name)
        Toast.makeText(this, "Showing all songs", Toast.LENGTH_SHORT).show()
    }
    
    // Playlist functionality
    private fun showPlaylistsDialog() {
        val playlists = playlistManager.getPlaylists()
        val playlistNames = playlists.map { it.name }.toMutableList()
        playlistNames.add(0, "+ Create New Playlist")
        
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.playlists_long_press_hint))
        
        val listView = android.widget.ListView(this)
        val arrayAdapter = android.widget.ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            playlistNames
        )
        listView.adapter = arrayAdapter
        
        // Handle normal click - open playlist
        listView.setOnItemClickListener { _, _, position, _ ->
            if (position == 0) {
                // Create new playlist
                showCreatePlaylistDialog()
            } else {
                // Show playlist
                val playlist = playlists[position - 1]
                showPlaylist(playlist)
            }
        }
        
        // Handle long click - delete playlist
        listView.setOnItemLongClickListener { _, _, position, _ ->
            if (position > 0) {
                val playlist = playlists[position - 1]
                showDeletePlaylistConfirmation(playlist)
                true
            } else {
                false
            }
        }
        
        builder.setView(listView)
        builder.setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
            dialog.dismiss()
        }
        
        builder.show()
    }
    
    private fun showDeletePlaylistConfirmation(playlist: Playlist) {
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.delete_playlist))
        builder.setMessage(getString(R.string.delete_playlist_confirmation, playlist.name))
        
        builder.setPositiveButton(getString(R.string.delete)) { dialog, _ ->
            playlistManager.deletePlaylist(playlist.id)
            Toast.makeText(this, getString(R.string.playlist_deleted), Toast.LENGTH_SHORT).show()
            
            // If we're currently viewing this playlist, go back to all songs
            if (currentViewMode == ViewMode.PLAYLIST && currentPlaylistId == playlist.id) {
                showAllSongs()
            }
            
            dialog.dismiss()
        }
        
        builder.setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
            dialog.dismiss()
        }
        
        builder.show()
    }
    
    private fun showCreatePlaylistDialog() {
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.create_playlist))
        
        val input = android.widget.EditText(this)
        input.hint = getString(R.string.playlist_name)
        val paddingHorizontal = resources.getDimensionPixelSize(R.dimen.search_input_padding_horizontal)
        val paddingVertical = resources.getDimensionPixelSize(R.dimen.search_input_padding_vertical)
        input.setPadding(paddingHorizontal, paddingVertical, paddingHorizontal, paddingVertical)
        builder.setView(input)
        
        builder.setPositiveButton("Create") { _, _ ->
            val name = input.text.toString().trim()
            if (name.isNotEmpty()) {
                val playlist = playlistManager.createPlaylist(name)
                Toast.makeText(this, "Playlist '$name' created", Toast.LENGTH_SHORT).show()
                showPlaylist(playlist)
            } else {
                Toast.makeText(this, "Playlist name cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }
        
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.cancel()
        }
        
        builder.show()
    }
    
    private fun showPlaylist(playlist: Playlist) {
        currentViewMode = ViewMode.PLAYLIST
        currentPlaylistId = playlist.id
        isSearching = false
        
        // Get current playing song before changing filtered list
        val currentSongPath = player.currentMediaItem?.localConfiguration?.uri?.path
        
        filteredSongs.clear()
        filteredSongs.addAll(songs.filter { playlist.songIds.contains(it.id) })
        
        // Apply sorting to the playlist songs
        performSorting()
        adapter.notifyDataSetChanged()
        
        // Only update player if current song is not in the new playlist
        // This preserves playback when switching playlists
        val currentSongInPlaylist = currentSongPath != null && 
                                    filteredSongs.any { it.path == currentSongPath }
        if (!currentSongInPlaylist) {
            // Current song is not in this playlist, update player
            updatePlayerWithFilteredSongs()
        }
        
        supportActionBar?.title = playlist.name
        Toast.makeText(this, getString(R.string.showing_songs, filteredSongs.size), Toast.LENGTH_SHORT).show()
    }
    
    private fun showAddToPlaylistDialog(song: Song) {
        val playlists = playlistManager.getPlaylists()
        if (playlists.isEmpty()) {
            Toast.makeText(this, "No playlists. Create one first!", Toast.LENGTH_SHORT).show()
            return
        }
        
        val playlistNames = playlists.map { it.name }.toTypedArray()
        
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.add_to_playlist))
        
        builder.setItems(playlistNames) { _, which ->
            val playlist = playlists[which]
            playlistManager.addSongToPlaylist(playlist.id, song.id)
            Toast.makeText(this, "Added to ${playlist.name}", Toast.LENGTH_SHORT).show()
        }
        
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }
        
        builder.show()
    }
    
    // Sleep timer functionality
    private fun showSleepTimerDialog() {
        val options = arrayOf("15 minutes", "30 minutes", "45 minutes", "1 hour", "Cancel timer")
        val minutes = arrayOf(15, 30, 45, 60, 0)
        
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.sleep_timer))
        
        builder.setItems(options) { _, which ->
            val selectedMinutes = minutes[which]
            if (selectedMinutes > 0) {
                startSleepTimer(selectedMinutes)
            } else {
                cancelSleepTimer()
            }
        }
        
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }
        
        builder.show()
    }
    
    private fun startSleepTimer(minutes: Int) {
        sleepTimerManager.startTimer(
            durationMinutes = minutes,
            onTick = { millisUntilFinished ->
                // Could update UI with remaining time if needed
            },
            onFinish = {
                // Pause playback when timer finishes
                if (::player.isInitialized && player.isPlaying) {
                    player.pause()
                    Toast.makeText(this, "Sleep timer ended - playback paused", Toast.LENGTH_SHORT).show()
                }
            }
        )
        
        // Update sleep timer button to show it's active
        val colors = ThemeHelper.getThemeColors(currentTheme)
        sleepTimerButton?.setColorFilter(colors.primary)
        nowPlayingBinding.fullSleepTimer.setColorFilter(colors.primary)
        
        Toast.makeText(this, getString(R.string.sleep_timer_set, minutes), Toast.LENGTH_SHORT).show()
    }
    
    private fun cancelSleepTimer() {
        sleepTimerManager.cancelTimer()
        
        // Reset sleep timer button color
        val colors = ThemeHelper.getThemeColors(currentTheme)
        sleepTimerButton?.setColorFilter(colors.onSurface)
        nowPlayingBinding.fullSleepTimer.setColorFilter(colors.onSurface)
        
        Toast.makeText(this, getString(R.string.sleep_timer_cancel), Toast.LENGTH_SHORT).show()
    }
    
    private fun showSongOptionsDialog(song: Song) {
        val options = mutableListOf<String>()
        options.add(if (favoritesManager.isFavorite(song.id)) "Remove from Favorites" else "Add to Favorites")
        options.add("Add to Playlist")
        
        // If in playlist view, add option to remove from current playlist
        if (currentViewMode == ViewMode.PLAYLIST && currentPlaylistId != null) {
            options.add("Remove from Playlist")
        }
        
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle(song.title)
        
        builder.setItems(options.toTypedArray()) { _, which ->
            when (which) {
                0 -> {
                    // Toggle favorite
                    toggleFavorite(song)
                }
                1 -> {
                    // Add to playlist
                    showAddToPlaylistDialog(song)
                }
                2 -> {
                    // Remove from playlist (only if in playlist view)
                    if (currentViewMode == ViewMode.PLAYLIST && currentPlaylistId != null) {
                        playlistManager.removeSongFromPlaylist(currentPlaylistId!!, song.id)
                        
                        // Refresh the view
                        val playlist = playlistManager.getPlaylists().find { it.id == currentPlaylistId }
                        if (playlist != null) {
                            showPlaylist(playlist)
                        }
                    }
                }
            }
        }
        
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }
        
        builder.show()
    }
}
