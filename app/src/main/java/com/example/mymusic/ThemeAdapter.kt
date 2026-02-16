package com.example.mymusic

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.mymusic.databinding.ItemThemeBinding

class ThemeAdapter(
    private val themes: List<ThemeType>,
    private var selectedTheme: ThemeType,
    private val onThemeClick: (ThemeType) -> Unit
) : RecyclerView.Adapter<ThemeAdapter.ThemeViewHolder>() {
    
    class ThemeViewHolder(val binding: ItemThemeBinding) : RecyclerView.ViewHolder(binding.root)
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ThemeViewHolder {
        val binding = ItemThemeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ThemeViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: ThemeViewHolder, position: Int) {
        val theme = themes[position]
        val colors = ThemeHelper.getThemeColors(theme)
        
        holder.binding.textThemeName.text = ThemeHelper.getThemeName(theme)
        holder.binding.textThemeDescription.text = ThemeHelper.getThemeDescription(theme)
        
        // Set preview colors
        holder.binding.previewPrimary.setBackgroundColor(colors.primary)
        holder.binding.previewBackground.setBackgroundColor(colors.background)
        holder.binding.previewSurface.setBackgroundColor(colors.surface)
        
        // Show selection indicator
        if (theme == selectedTheme) {
            holder.binding.iconSelected.visibility = android.view.View.VISIBLE
            holder.binding.root.strokeWidth = 4
            holder.binding.root.strokeColor = colors.primary
        } else {
            holder.binding.iconSelected.visibility = android.view.View.GONE
            holder.binding.root.strokeWidth = 1
            holder.binding.root.strokeColor = ContextCompat.getColor(holder.itemView.context, android.R.color.darker_gray)
        }
        
        holder.itemView.setOnClickListener {
            val oldSelected = selectedTheme
            selectedTheme = theme
            onThemeClick(theme)
            
            // Update the UI for both items
            notifyItemChanged(themes.indexOf(oldSelected))
            notifyItemChanged(themes.indexOf(theme))
        }
    }
    
    override fun getItemCount() = themes.size
}