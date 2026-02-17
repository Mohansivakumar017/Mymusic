# Theme System Documentation

## Overview
The My Music app features a comprehensive theme system that allows users to switch between different visual styles with themed color schemes.

## Architecture

### Core Components

1. **ThemeType.kt**
   - Enum defining available themes: SPOTIFY, APPLE_MUSIC, IOS_GLASS

2. **ThemeManager.kt**
   - Handles theme persistence using SharedPreferences
   - Provides methods to save and retrieve current theme
   
3. **ThemeHelper.kt**
   - Contains color definitions for each theme
   - Provides utility methods for theme names and descriptions
   - Returns ThemeColors objects with all necessary colors

4. **ThemeColors (Data Class)**
   - Stores color values for a theme
   - Includes flags for special effects (gradient, blur)
   - Contains primary, background, surface, and accent colors

### Theme Selection UI

**ThemeSelectionActivity.kt**
- Displays all available themes in a list
- Shows color previews for each theme
- Handles theme selection and persistence
- Returns result to MainActivity to trigger recreation

**ThemeAdapter.kt**
- RecyclerView adapter for theme list
- Shows theme name, description, and color preview
- Highlights currently selected theme

## Adding a New Theme

To add a new theme to the app:

1. **Add Theme Type**
```kotlin
enum class ThemeType {
    SPOTIFY,
    APPLE_MUSIC,
    IOS_GLASS,
    YOUR_NEW_THEME  // Add here
}
```

2. **Define Colors in ThemeHelper**
```kotlin
private fun getYourNewTheme(): ThemeColors {
    return ThemeColors(
        primary = Color.parseColor("#YOUR_COLOR"),
        primaryDark = Color.parseColor("#YOUR_DARK_COLOR"),
        background = Color.parseColor("#BG_COLOR"),
        surface = Color.parseColor("#SURFACE_COLOR"),
        onBackground = Color.parseColor("#TEXT_COLOR"),
        onSurface = Color.parseColor("#SECONDARY_TEXT"),
        accent = Color.parseColor("#ACCENT_COLOR"),
        cardBackground = Color.parseColor("#CARD_BG"),
        useGradient = false,  // Set to true for gradient
        useBlur = false       // Future feature
    )
}
```

3. **Update Helper Methods**
```kotlin
fun getThemeColors(theme: ThemeType): ThemeColors {
    return when (theme) {
        // ... existing themes
        ThemeType.YOUR_NEW_THEME -> getYourNewTheme()
    }
}

fun getThemeName(theme: ThemeType): String {
    return when (theme) {
        // ... existing themes
        ThemeType.YOUR_NEW_THEME -> "Your Theme Name"
    }
}
```

4. **Optional: Add Custom Drawables**
   - Create gradient backgrounds in `res/drawable/`
   - Reference them in the theme application code

## Theme Application

Themes are applied in MainActivity's `applyTheme()` method:

```kotlin
private fun applyTheme() {
    val colors = ThemeHelper.getThemeColors(currentTheme)
    
    // Apply colors to UI components
    binding.root.setBackgroundColor(colors.background)
    binding.toolbar.setBackgroundColor(colors.surface)
    // ... more components
    
    // Apply gradients if enabled
    if (colors.useGradient) {
        // Apply gradient drawable
    }
}
```

## Current Themes

### Dark Green Theme
- **Colors**: Dark background (#121212), Green accent (#1DB954)
- **Style**: Modern dark theme, high contrast
- **Best for**: Night listening, battery saving
- **Description**: Dark theme with green accent colors

### Light Red Theme  
- **Colors**: Light background (#FAFAFA), Red accent (#FC3C44)
- **Style**: Light and vibrant with gradients
- **Best for**: Daytime use, colorful interface
- **Description**: Light theme with red accent colors

### Minimalist Blue Theme
- **Colors**: Light gray (#F2F2F7), Blue accent (#007AFF)
- **Style**: Clean, minimalist design
- **Best for**: Clean aesthetic, modern look
- **Description**: Minimalist theme with blue accent colors

## Theme Persistence

Themes are saved using SharedPreferences:
- **Key**: `selected_theme`
- **Storage**: Private app preferences
- **Default**: SPOTIFY theme

## Best Practices

1. **Always provide fallback colors** for all theme properties
2. **Test themes on both light and dark displays**
3. **Ensure text readability** on all backgrounds
4. **Use Material Design color guidelines**
5. **Keep consistency** across all UI elements

## Future Enhancements

Potential improvements to the theme system:
- Dynamic color generation from album art
- User-customizable themes
- Dark/Light mode auto-switching
- More theme presets with different color schemes
- Theme scheduling (auto-switch based on time)
