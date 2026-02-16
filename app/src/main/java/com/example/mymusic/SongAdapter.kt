package com.example.mymusic

import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.RoundedCornersTransformation
import com.example.mymusic.databinding.ItemSongBinding

class SongAdapter(
    private val songs: List<Song>,
    private val onSongClick: (Int) -> Unit
) : RecyclerView.Adapter<SongAdapter.SongViewHolder>() {

    class SongViewHolder(val binding: ItemSongBinding) : RecyclerView.ViewHolder(binding.root) {
        var clickCallback: Runnable? = null
    }
    
    private var currentPlayingPosition = -1
    
    companion object {
        private const val CLICK_ANIMATION_DURATION = 150L
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val binding = ItemSongBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SongViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = songs[position]
        holder.binding.textTitle.text = song.title
        holder.binding.textDuration.text = formatDuration(song.duration)
        
        // Cancel any pending click callbacks from recycled view
        holder.clickCallback?.let { holder.itemView.removeCallbacks(it) }
        holder.clickCallback = null
        
        holder.binding.imgAlbumArt.load(song.getAlbumArtUri()) {
            crossfade(true)
            placeholder(R.drawable.ic_music_note)
            error(R.drawable.ic_music_note)
            transformations(RoundedCornersTransformation(12f))
        }

        // Show playing indicator with pulse animation if this is the current song
        if (position == currentPlayingPosition) {
            holder.binding.imgPlayingIndicator.visibility = android.view.View.VISIBLE
            if (holder.binding.imgPlayingIndicator.animation == null) {
                val pulseAnimation = AnimationUtils.loadAnimation(holder.itemView.context, R.anim.pulse)
                holder.binding.imgPlayingIndicator.startAnimation(pulseAnimation)
            }
        } else {
            holder.binding.imgPlayingIndicator.visibility = android.view.View.GONE
            holder.binding.imgPlayingIndicator.clearAnimation()
        }
        
        // Add click animation with scale effect
        holder.itemView.setOnClickListener {
            val pressAnimation = AnimationUtils.loadAnimation(holder.itemView.context, R.anim.item_press)
            holder.itemView.startAnimation(pressAnimation)
            
            // Store callback so it can be canceled if view is recycled
            val clickPosition = holder.bindingAdapterPosition
            holder.clickCallback = Runnable {
                if (clickPosition != RecyclerView.NO_POSITION && clickPosition == holder.bindingAdapterPosition) {
                    val releaseAnimation = AnimationUtils.loadAnimation(holder.itemView.context, R.anim.item_release)
                    holder.itemView.startAnimation(releaseAnimation)
                    onSongClick(clickPosition)
                }
                holder.clickCallback = null
            }
            holder.itemView.postDelayed(holder.clickCallback!!, CLICK_ANIMATION_DURATION)
        }
    }
    
    override fun onViewRecycled(holder: SongViewHolder) {
        super.onViewRecycled(holder)
        // Clean up pending callbacks when view is recycled
        holder.clickCallback?.let { holder.itemView.removeCallbacks(it) }
        holder.clickCallback = null
    }

    override fun getItemCount() = songs.size

    private fun formatDuration(durationMs: Long): String {
        val seconds = (durationMs / 1000) % 60
        val minutes = (durationMs / (1000 * 60)) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }
    
    fun setCurrentPlayingPosition(position: Int) {
        val oldPosition = currentPlayingPosition
        currentPlayingPosition = position
        if (oldPosition != -1) {
            notifyItemChanged(oldPosition)
        }
        if (position != -1) {
            notifyItemChanged(position)
        }
    }
}
