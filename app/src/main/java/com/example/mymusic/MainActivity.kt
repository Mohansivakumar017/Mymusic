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
import com.example.mymusic.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var player: ExoPlayer
    private val songs = mutableListOf<Song>()
    private val filteredSongs = mutableListOf<Song>()
    private lateinit var adapter: SongAdapter
    private lateinit var themeManager: ThemeManager
    private var currentTheme: ThemeType = ThemeType.SPOTIFY
    private var isSearching = false

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
        
        // Setup toolbar
        setSupportActionBar(binding.toolbar)
        
        // Apply theme
        applyTheme()

        try {
            player = ExoPlayer.Builder(this).build()
            binding.playerControlView.player = player
            
            player.addListener(object : Player.Listener {
                override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                    super.onMediaItemTransition(mediaItem, reason)
                    // You could update UI here to highlight the playing song in the list
                }
            })
        } catch (e: Exception) {
            Log.e("MainActivity", "Error initializing player", e)
        }

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
        binding.btnSortName.setOnClickListener {
            songs.sortBy { it.title.lowercase() }
            onDataChanged()
        }
        binding.btnSortDate.setOnClickListener {
            songs.sortByDescending { it.dateAdded }
            onDataChanged()
        }
        binding.btnSortDuration.setOnClickListener {
            songs.sortByDescending { it.duration }
            onDataChanged()
        }
        binding.btnShuffleMain.setOnClickListener {
            if (songs.isNotEmpty()) {
                player.shuffleModeEnabled = true
                val randomIndex = (0 until songs.size).random()
                playSongAt(randomIndex)
                Toast.makeText(this, "Shuffle Mode On", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun onDataChanged() {
        if (isSearching) {
            // Don't update playlist when searching, keep the filtered view
        } else {
            filteredSongs.clear()
            filteredSongs.addAll(songs)
        }
        adapter.notifyDataSetChanged()
        updatePlayerPlaylist()
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
            songs.sortBy { it.title.lowercase() } // Default sort
            filteredSongs.clear()
            filteredSongs.addAll(songs)
            adapter.notifyDataSetChanged()
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

    private fun playSongAt(index: Int) {
        if (index !in filteredSongs.indices) return
        
        // Find the song in the main songs list to get the correct player index
        val song = filteredSongs[index]
        val actualIndex = songs.indexOf(song)
        
        if (actualIndex == -1) return
        
        try {
            player.seekTo(actualIndex, 0)
            player.play()
        } catch (e: Exception) {
            Log.e("MainActivity", "Error playing song", e)
            Toast.makeText(this, "Error playing song", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::player.isInitialized) {
            player.release()
        }
    }
    
    private fun applyTheme() {
        val colors = ThemeHelper.getThemeColors(currentTheme)
        
        // Apply background colors
        binding.root.setBackgroundColor(colors.background)
        binding.toolbar.setBackgroundColor(colors.surface)
        binding.toolbar.setTitleTextColor(colors.onBackground)
        binding.sortBar.setBackgroundColor(colors.surface)
        binding.playerControlContainer.setBackgroundColor(colors.surface)
        
        // Apply status bar color
        window.statusBarColor = colors.primaryDark
        
        // Apply gradient if needed
        if (colors.useGradient && colors.gradientStart != null && colors.gradientEnd != null) {
            val gradientDrawable = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(colors.gradientStart, colors.gradientEnd)
            )
            binding.root.background = gradientDrawable
        }
        
        // Update button colors
        binding.btnSortName.setTextColor(colors.onBackground)
        binding.btnSortDate.setTextColor(colors.onBackground)
        binding.btnSortDuration.setTextColor(colors.onBackground)
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
        input.setPadding(50, 20, 50, 20)
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
        Toast.makeText(this, "Found ${filteredSongs.size} songs", Toast.LENGTH_SHORT).show()
    }
    
    private fun clearFilter() {
        isSearching = false
        filteredSongs.clear()
        filteredSongs.addAll(songs)
        adapter.notifyDataSetChanged()
        Toast.makeText(this, "Showing all songs", Toast.LENGTH_SHORT).show()
    }
}
