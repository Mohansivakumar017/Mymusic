package com.example.mymusic

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mymusic.databinding.ActivityThemeSelectionBinding

class ThemeSelectionActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityThemeSelectionBinding
    private lateinit var themeManager: ThemeManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityThemeSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Select Theme"
        
        themeManager = ThemeManager(this)
        
        setupRecyclerView()
        applyCurrentTheme()
    }
    
    private fun setupRecyclerView() {
        val themes = listOf(
            ThemeType.SPOTIFY,
            ThemeType.APPLE_MUSIC,
            ThemeType.IOS_GLASS
        )
        
        val adapter = ThemeAdapter(themes, themeManager.getTheme()) { theme ->
            themeManager.saveTheme(theme)
            // Recreate both this activity and the main activity
            setResult(RESULT_OK)
            recreate()
        }
        
        binding.recyclerViewThemes.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewThemes.adapter = adapter
    }
    
    private fun applyCurrentTheme() {
        val theme = themeManager.getTheme()
        val colors = ThemeHelper.getThemeColors(theme)
        
        window.statusBarColor = colors.primaryDark
        binding.root.setBackgroundColor(colors.background)
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}