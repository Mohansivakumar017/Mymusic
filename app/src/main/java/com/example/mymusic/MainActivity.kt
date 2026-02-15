package com.example.mymusic

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
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
    private var shuffleQueue = mutableListOf<Int>()
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

        player = ExoPlayer.Builder(this).build()
        binding.playerControlView.player = player

        setupRecyclerView()
        setupSortButtons()
        checkPermissionAndLoad()
    }

    private fun setupRecyclerView() {
        adapter = SongAdapter(songs) { position ->
            playSongAt(position)
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }

    private fun setupSortButtons() {
        binding.btnSortName.setOnClickListener {
            songs.sortBy { it.title.lowercase() }
            onDataSorted()
        }
        binding.btnSortDate.setOnClickListener {
            songs.sortByDescending { it.dateAdded }
            onDataSorted()
        }
        binding.btnSortDuration.setOnClickListener {
            songs.sortByDescending { it.duration }
            onDataSorted()
        }
    }

    private fun onDataSorted() {
        adapter.notifyDataSetChanged()
        initShuffle()
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
            initShuffle()
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
            MediaStore.Audio.Media.DATE_ADDED
        )
        val cursor = contentResolver.query(uri, projection, null, null, null)

        cursor?.use {
            while (it.moveToNext()) {
                val id = it.getLong(it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID))
                val title = it.getString(it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE))
                val path = it.getString(it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA))
                val duration = it.getLong(it.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION))
                val dateAdded = it.getLong(it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED))

                songs.add(Song(id, title, path, duration, dateAdded))
            }
        }
    }

    private fun initShuffle() {
        shuffleQueue = songs.indices.shuffled().toMutableList()
    }

    private fun playNext() {
        if (songs.isEmpty()) return

        if (shuffleQueue.isEmpty()) {
            shuffleQueue = songs.indices.shuffled().toMutableList()
        }

        val index = shuffleQueue.removeAt(0)
        playSongAt(index)
    }

    private fun playSongAt(index: Int) {
        val song = songs[index]
        val mediaItem = MediaItem.fromUri(song.path)
        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()

        player.removeListener(playbackListener)
        player.addListener(playbackListener)
    }

    private val playbackListener = object : Player.Listener {
        override fun onPlaybackStateChanged(state: Int) {
            if (state == Player.STATE_ENDED) playNext()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        player.release()
    }
}
