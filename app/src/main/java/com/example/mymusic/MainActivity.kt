package com.example.mymusic

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
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
    private lateinit var adapter: SongAdapter

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                loadSongsAndStart()
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }

    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
        adapter = SongAdapter(songs) { position ->
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
            MediaStore.Audio.Media.ALBUM_ID
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

                while (it.moveToNext()) {
                    val id = it.getLong(idColumn)
                    val title = it.getString(titleColumn) ?: "Unknown"
                    val path = it.getString(dataColumn) ?: ""
                    val duration = it.getLong(durationColumn)
                    val dateAdded = it.getLong(dateAddedColumn)
                    val albumId = it.getLong(albumIdColumn)

                    if (path.isNotEmpty()) {
                        songs.add(Song(id, title, path, duration, dateAdded, albumId))
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
        if (index !in songs.indices) return
        
        try {
            player.seekTo(index, 0)
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
}
