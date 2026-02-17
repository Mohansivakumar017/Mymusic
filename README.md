# My Music Player 🎵

A beautiful Android music player with multiple theme options inspired by popular music apps.

## Features ✨

### 🎨 Multiple Themes
The app now includes three beautiful themes to choose from:

1. **Dark Green Theme** (Default)
   - Dark theme with green accents
   - Perfect for night listening
   - Dark background with vibrant green highlights

2. **Light Red Theme**
   - Light theme with red accents
   - Gradient backgrounds for a premium feel
   - Bright and energetic color scheme

3. **Minimalist Blue Theme**
   - Minimalist theme with blue accents
   - Clean, minimalist design
   - Light gray aesthetic with blue highlights

### 🔍 Search Functionality
- Search songs by title, artist, or album name
- Real-time filtering of your music library
- Easy-to-use search dialog

### 📱 Enhanced UI
- **Larger album art** (64dp) for better visibility
- **Three-line song display**: Title, Artist, and Duration
- **Material Design cards** with elevation and rounded corners
- **Responsive toolbar** with quick access to themes and search
- **Theme-aware components** that adapt to your selected theme

### 🎵 Music Features
- Play all your local music files
- **Sort options**: By name, date added, or duration
- **Shuffle play** for random playback
- **Album art display** with fallback icon
- Previous/Next track controls
- Play/Pause functionality

## How to Use 🚀

### Selecting a Theme
1. Tap the **menu icon** (three dots) in the top right
2. Select **"Themes"**
3. Choose from Dark Green, Light Red, or Minimalist Blue themes
4. The app will automatically restart with your new theme!

### Searching for Music
1. Tap the **search icon** in the toolbar
2. Enter song name, artist, or album
3. Tap **"Search"** to filter results
4. Tap **"Clear"** to show all songs again

### Playing Music
- Tap any song to start playing
- Use the player controls at the bottom:
  - Previous/Play/Pause/Next buttons
  - Progress bar to seek through the song
  - Shuffle toggle for random playback

### Sorting Your Library
- Use the sort buttons at the top:
  - **Name**: Alphabetical order
  - **Recent**: Most recently added first
  - **Duration**: Longest songs first
- Tap the **shuffle button** for instant shuffle play

## Theme Customization 🎨

Each theme provides a complete visual overhaul:

### Color Schemes
- **Dark Green**: Dark background (#121212) with green accents (#1DB954)
- **Light Red**: Light background (#FAFAFA) with red accents (#FC3C44)
- **Minimalist Blue**: Light gray background (#F2F2F7) with blue accents (#007AFF)

### Visual Elements
- Custom background colors
- Themed song cards
- Colored action buttons
- Matching status bar

## Technical Details 🔧

### Built With
- **Kotlin** - Modern Android development
- **ExoPlayer** - Professional audio playback
- **ViewBinding** - Type-safe view access
- **Material Design 3** - Modern UI components
- **Coil** - Image loading with album art
- **SharedPreferences** - Theme persistence

### Architecture
- Clean separation of concerns
- Theme management system
- Modular adapter pattern
- Efficient filtering and search

## Permissions Required 📋

- **READ_MEDIA_AUDIO** (Android 13+) or **READ_EXTERNAL_STORAGE** (older versions)
  - Required to access and play your music files

## Compatibility 📱

- **Minimum SDK**: Android 7.0 (API 24)
- **Target SDK**: Android 14 (API 35)
- **Recommended**: Android 10+ for best experience

## Testing 🧪

The project includes comprehensive test coverage:

### Test Suite
- **26 Unit Tests** - Testing theme logic, data models, and business logic
- **Instrumented Tests** - Testing theme persistence and Android components
- **Code Coverage** - JaCoCo integration for coverage reporting
- **CI/CD** - Automated testing via GitHub Actions

### Running Tests
```bash
# Run all unit tests
./run-tests.sh test

# Generate coverage report
./run-tests.sh coverage

# Run all checks (build, test, lint)
./run-tests.sh check
```

See [TESTING.md](TESTING.md) for detailed testing documentation.

## Continuous Integration 🔄

This project uses GitHub Actions for automated testing and builds:
- ✅ Automated builds on every push
- ✅ Unit tests execution
- ✅ Lint checks
- ✅ Test reports and artifacts

## Future Enhancements 🚧

Potential features for future releases:
- Favorites/Playlists
- Now Playing screen with larger album art
- Equalizer
- Sleep timer
- Widget support
- Lyrics display

## Screenshots 📸

Coming soon! Build and run the app to see the beautiful themes in action.

## License

This project is open source and available for educational purposes.

## Credits

Developed with ❤️ using modern Android development practices.
