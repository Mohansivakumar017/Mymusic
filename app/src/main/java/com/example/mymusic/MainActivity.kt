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
    private var currentTheme: ThemeType = ThemeType.SPOTIFY
    private var isSearching = false
    private var currentSortMode = SortMode.BY_NAME
    
    // Now Playing UI components
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    private var currentSong: Song? = null
    private var currentPlayingSong: Song? = null
    private val progressHandler = android.os.Handler(android.os.Looper.getMainLooper())
    private lateinit var progressUpdateRunnable: Runnable
    
    // Player Control View components
    private var nowPlayingTitle: TextView? = null
    private var nowPlayingArtist: TextView? = null
    private var nowPlayingAlbumArt: ImageView? = null

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                loadSongsAndStart()
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
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
            
            // Set player to PlayerControlView
            binding.playerControlView.player = player
            
            player.addListener(object : Player.Listener {
                override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                    super.onMediaItemTransition(mediaItem, reason)
                    
                    // Update current playing song when media item changes
                    val currentIndex = player.currentMediaItemIndex
                    if (currentIndex >= 0 && currentIndex < filteredSongs.size) {
                        currentPlayingSong = filteredSongs[currentIndex]
                        currentPlayingSong?.let { updateNowPlayingInfoImmediate(it) }
                    }
                    updateNowPlayingUI()
                }
                
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    super.onIsPlayingChanged(isPlaying)
                    updatePlayPauseButtons()
                    
                    // Keep player control visible when playing
                    if (isPlaying) {
                        binding.playerControlView.visibility = View.VISIBLE
                    }
                }
            })
            
            // Get references to now playing views (use post to ensure layout is inflated)
            binding.playerControlView.post {
                nowPlayingTitle = binding.playerControlView.findViewById(R.id.now_playing_title)
                nowPlayingArtist = binding.playerControlView.findViewById(R.id.now_playing_artist)
                nowPlayingAlbumArt = binding.playerControlView.findViewById(R.id.now_playing_album_art)
                // Update now playing info after views are initialized
                updateNowPlayingInfo()
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
    }

    private fun setupRecyclerView() {
        filteredSongs.addAll(songs)
        adapter = SongAdapter(filteredSongs, currentTheme) { position ->
            playSongAt(position)
        }
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
    
    private fun applySorting() {
        when (currentSortMode) {
            SortMode.BY_NAME -> {
                filteredSongs.sortBy { it.title.lowercase() }
                binding.btnSort.text = "Sort: Name ↑"
            }
            SortMode.BY_DATE -> {
                filteredSongs.sortByDescending { it.dateAdded }
                binding.btnSort.text = "Sort: Date ↓"
            }
            SortMode.BY_DURATION -> {
                filteredSongs.sortByDescending { it.duration }
                binding.btnSort.text = "Sort: Duration ↓"
            }
        }
        adapter.notifyDataSetChanged()
        updatePlayerWithFilteredSongs()
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
        val mediaItems = songs.map { MediaItem.fromUri(it.path) }
        player.setMediaItems(mediaItems)
        player.prepare()
    }

    private fun updatePlayerWithFilteredSongs() {
        if (filteredSongs.isEmpty()) return
        
        // Get current playing song and position if any
        val wasPlaying = player.isPlaying
        val currentPosition = player.currentPosition
        val currentSongPath = if (player.currentMediaItemIndex >= 0) {
            player.currentMediaItem?.localConfiguration?.uri?.path
        } else null
        
        // Update player with filtered songs
        val mediaItems = filteredSongs.map { MediaItem.fromUri(it.path) }
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
            }
        }
    }

    private fun playSongAt(index: Int) {
        if (index !in filteredSongs.indices) return
        
        // Store the song that's about to play
        currentPlayingSong = filteredSongs[index]
        
        try {
            player.seekTo(index, 0)
            player.play()
            
            // Immediately update the UI with correct song info
            currentPlayingSong?.let { updateNowPlayingInfoImmediate(it) }
            
            // Show the player control view
            binding.playerControlView.visibility = View.VISIBLE
            
        } catch (e: Exception) {
            Log.e("MainActivity", "Error playing song", e)
            Toast.makeText(this, "Error playing song", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        progressHandler.removeCallbacks(progressUpdateRunnable)
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
        // Restore player visibility if music was playing
        if (::player.isInitialized && player.currentMediaItemIndex >= 0) {
            binding.playerControlView.visibility = View.VISIBLE
            updateNowPlayingInfo()
        }
    }
    
    private fun setupNowPlayingUI() {
        // Setup bottom sheet behavior
        bottomSheetBehavior = BottomSheetBehavior.from(binding.nowPlayingContainer)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        bottomSheetBehavior.isHideable = true
        
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
                // Update current playing song
                val newIndex = player.currentMediaItemIndex
                if (newIndex >= 0 && newIndex < filteredSongs.size) {
                    currentPlayingSong = filteredSongs[newIndex]
                    currentPlayingSong?.let { updateNowPlayingInfoImmediate(it) }
                }
            }
        }
        
        nowPlayingBinding.fullNext.setOnClickListener {
            if (player.hasNextMediaItem()) {
                player.seekToNextMediaItem()
                // Update current playing song
                val newIndex = player.currentMediaItemIndex
                if (newIndex >= 0 && newIndex < filteredSongs.size) {
                    currentPlayingSong = filteredSongs[newIndex]
                    currentPlayingSong?.let { updateNowPlayingInfoImmediate(it) }
                }
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
        val currentIndex = player.currentMediaItemIndex
        if (currentIndex >= 0 && currentIndex < filteredSongs.size) {
            currentSong = filteredSongs[currentIndex]
            currentSong?.let { song ->
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
            }
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
    
    private fun updateRepeatButton() {
        val colors = ThemeHelper.getThemeColors(currentTheme)
        val tint = when (player.repeatMode) {
            Player.REPEAT_MODE_OFF -> colors.onSurface
            else -> colors.primary
        }
        nowPlayingBinding.fullRepeat.setColorFilter(tint)
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
        
        // Update song info in full player (bottom sheet)
        nowPlayingBinding.fullSongTitle.text = song.title
        nowPlayingBinding.fullSongArtist.text = song.artist
        nowPlayingBinding.fullAlbumArt.load(song.getAlbumArtUri()) {
            placeholder(R.drawable.ic_music_note)
            error(R.drawable.ic_music_note)
        }
    }
    
    private fun updateNowPlayingInfo() {
        // Use the stored current playing song instead of index
        val song = currentPlayingSong ?: run {
            // Fallback to index-based if currentPlayingSong is not set
            val currentIndex = player.currentMediaItemIndex
            if (currentIndex >= 0 && currentIndex < filteredSongs.size) {
                filteredSongs[currentIndex]
            } else {
                return
            }
        }
        
        updateNowPlayingInfoImmediate(song)
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
            adapter = SongAdapter(filteredSongs, currentTheme) { position ->
                playSongAt(position)
            }
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
}
