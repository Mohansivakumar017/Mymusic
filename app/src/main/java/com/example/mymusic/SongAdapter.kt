package com.example.mymusic

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.RoundedCornersTransformation
import com.example.mymusic.databinding.ItemSongBinding

class SongAdapter(
    private val songs: List<Song>,
    private val theme: ThemeType = ThemeType.SPOTIFY,
    private val onSongClick: (Int) -> Unit
) : RecyclerView.Adapter<SongAdapter.SongViewHolder>() {

    class SongViewHolder(val binding: ItemSongBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val binding = ItemSongBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SongViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = songs[position]
        val colors = ThemeHelper.getThemeColors(theme)
        
        holder.binding.textTitle.text = song.title
        holder.binding.textArtist.text = song.artist
        holder.binding.textDuration.text = formatDuration(song.duration)
        
        // Apply theme colors
        holder.binding.textTitle.setTextColor(colors.onBackground)
        holder.binding.textArtist.setTextColor(colors.onSurface)
        holder.binding.textDuration.setTextColor(colors.onSurface)
        holder.binding.root.setCardBackgroundColor(colors.cardBackground)
        
        holder.binding.imgAlbumArt.load(song.getAlbumArtUri()) {
            crossfade(true)
            placeholder(R.drawable.ic_music_note)
            error(R.drawable.ic_music_note)
            transformations(RoundedCornersTransformation(12f))
        }

        holder.itemView.setOnClickListener { onSongClick(position) }
    }

    override fun getItemCount() = songs.size

    private fun formatDuration(durationMs: Long): String {
        val seconds = (durationMs / 1000) % 60
        val minutes = (durationMs / (1000 * 60)) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }
}
