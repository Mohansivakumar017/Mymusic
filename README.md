# My Music Player 🎵

A beautiful Android music player with multiple theme options inspired by popular music apps.

## Features ✨

### 🎨 Multiple Themes
The app now includes three beautiful themes to choose from:

1. **Dark Green Theme** (Default)
   - Dark theme with green accents
   - Perfect for night listening
   - Dark background with green accents

2. **Light Red Theme**
   - Light theme with red accents
   - Gradient backgrounds for a premium feel
   - Bright design with red accents

3. **Minimalist Blue Theme**
   - Minimalist theme with blue accents
   - Clean, minimalist design
   - Light gray aesthetic with blue accents

### ❤️ Favorites / Liked Songs
- Mark songs as favorites with the heart button in the player
- Access your favorite songs quickly from the menu
- Favorites are persisted across app sessions
- Easy toggle on/off with visual feedback

### 📑 Playlists
- **Create** custom playlists to organize your music
- **Edit** playlists by adding or removing songs
- **Delete** playlists you no longer need
- Long-press on any song to add it to a playlist
- Access playlists from the main menu
- Each playlist shows the number of songs it contains

### ⏯️ Lock-screen Controls
- Control playback from your device's lock screen
- See current song information without unlocking
- Use hardware buttons to control playback
- Powered by MediaSession API

### 🔔 Notification Player Controls
- Persistent notification with playback controls
- Album art displayed in notification
- Quick access to play/pause, next, and previous
- Notification updates with current song info

### 💤 Sleep Timer
- Set a timer to automatically pause playback
- Choose from preset durations: 15, 30, 45 minutes, or 1 hour
- Visual indicator when sleep timer is active
- Cancel timer anytime before it expires
- Perfect for falling asleep to music

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

### Managing Favorites
1. While a song is playing, tap the **heart icon** in the player
2. The heart will fill to indicate the song is favorited
3. Access all favorites from **Menu → Favorites**
4. Tap the heart again to remove from favorites

### Creating and Using Playlists
1. Tap **Menu → Playlists**
2. Select **"+ Create New Playlist"**
3. Enter a name and tap **"Create"**
4. To add songs to a playlist:
   - **Long-press** any song in your library
   - Select **"Add to Playlist"**
   - Choose the playlist
5. View a playlist by selecting it from the Playlists menu
6. Remove songs from playlists by long-pressing while viewing the playlist

### Using Lock-screen and Notification Controls
- When playing music, a notification appears automatically
- Use the notification controls to play/pause, skip tracks
- Controls also appear on your lock screen
- Album art and song info are displayed
- No setup required - works automatically!

### Setting a Sleep Timer
1. Tap the **clock icon** in the player
2. Choose a duration (15, 30, 45 minutes, or 1 hour)
3. The clock icon will highlight to show the timer is active
4. Music will automatically pause when the timer expires
5. Cancel anytime by tapping the clock and selecting **"Cancel timer"**

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
- **Media3 Session** - Notification and lock-screen controls
- **ViewBinding** - Type-safe view access
- **Material Design 3** - Modern UI components
- **Coil** - Image loading with album art
- **SharedPreferences** - Data persistence for favorites and playlists

### Architecture
- Clean separation of concerns
- Theme management system
- Modular adapter pattern
- Efficient filtering and search
- Persistent storage for user preferences

## Permissions Required 📋

- **READ_MEDIA_AUDIO** (Android 13+) or **READ_EXTERNAL_STORAGE** (older versions)
  - Required to access and play your music files
- **FOREGROUND_SERVICE**
  - Required for persistent music playback notification
- **POST_NOTIFICATIONS** (Android 13+)
  - Required to show playback controls in notifications

## Compatibility 📱

- **Minimum SDK**: Android 7.0 (API 24)
- **Target SDK**: Android 14 (API 35)
- **Recommended**: Android 10+ for best experience

## Testing 🧪

The project includes comprehensive test coverage:

### Test Suite
- **29+ Unit Tests** - Testing theme logic, data models, business logic, and new features
- **Instrumented Tests** - Testing theme persistence and Android components
- **Code Coverage** - JaCoCo integration for coverage reporting
- **CI/CD** - Automated testing via GitHub Actions

### New Feature Tests
- **PlaylistTest** - Tests for playlist data model
- **FavoritesManagerTest** - Tests for favorites persistence and management
- **ViewModeTest** - Tests for view mode enum

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
- Now Playing screen enhancements
- Equalizer
- Widget support
- Lyrics display
- Cross-fade between tracks
- Gapless playback

## Screenshots 📸

Coming soon! Build and run the app to see the beautiful themes in action.

## License

This project is open source and available for educational purposes.

## Credits

Developed with ❤️ using modern Android development practices.
