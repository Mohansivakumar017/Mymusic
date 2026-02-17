# Quick Feature Guide

## 🎵 My Music - New Features Overview

### ❤️ Favorites / Liked Songs

**How to Use:**
- Tap the ❤️ heart icon in the music player to add/remove favorites
- Access favorites from Menu → Favorites
- Heart icon fills when song is favorited

**Location:**
- Heart button in bottom player control bar
- Heart button in full-screen player (left of controls)
- Menu option: "Favorites"

---

### 📑 Playlists

**How to Use:**
1. Create playlist: Menu → Playlists → "+ Create New Playlist"
2. Add songs: Long-press any song → "Add to Playlist"
3. View playlist: Menu → Playlists → Select playlist
4. Remove from playlist: Long-press song while viewing playlist

**Location:**
- Menu option: "Playlists"
- Long-press context menu on any song

**Features:**
- Create unlimited playlists
- Custom playlist names
- Add/remove songs easily
- View song count per playlist

---

### 💤 Sleep Timer

**How to Use:**
1. Tap the 🕐 clock icon in the player
2. Choose duration: 15, 30, 45 min, or 1 hour
3. Music pauses automatically when timer expires
4. Cancel anytime via same menu

**Location:**
- Clock button in bottom player control bar
- Clock button in full-screen player (right of controls)

**Options:**
- ⏰ 15 minutes
- ⏰ 30 minutes
- ⏰ 45 minutes
- ⏰ 1 hour
- ❌ Cancel timer

---

### 🔔 Notification & Lock-screen Controls

**How to Use:**
- Automatically appears when playing music
- No setup required!

**Features:**
- Play/Pause button
- Previous/Next track buttons
- Song title and artist
- Album artwork
- Works on lock screen
- Persistent notification

**Controls Available:**
- ⏮️ Previous
- ⏯️ Play/Pause
- ⏭️ Next
- 🎵 Song info display

---

## UI Changes Summary

### Bottom Player Control Bar
```
[❤️] [⏮️] [⏯️] [⏭️] [🕐]
```
- **New:** ❤️ Favorite button (left)
- **New:** 🕐 Sleep timer button (right)
- **Existing:** Playback controls (center)

### Full Player Screen
```
      [❤️] [🔀] [⏮️] [⏯️] [⏭️] [🔁] [🕐]
```
- **New:** ❤️ Favorite button (far left)
- **New:** 🕐 Sleep timer button (far right)
- **Existing:** All playback controls (center)

### Menu Options
```
🔍 Search
❤️ Favorites        [NEW]
📑 Playlists         [NEW]
📄 All Songs         [NEW]
🎨 Themes
```

### Song Long-Press Menu
```
When long-pressing a song:
- Add/Remove from Favorites
- Add to Playlist
- Remove from Playlist (when in playlist view)
```

---

## Data Storage

All your preferences are saved automatically:

| Feature | Storage Method | Persistence |
|---------|---------------|-------------|
| Favorites | SharedPreferences | ✅ Permanent |
| Playlists | SharedPreferences (JSON) | ✅ Permanent |
| Themes | SharedPreferences | ✅ Permanent |
| Sleep Timer | In-memory | ❌ Session only |

**Data is stored locally on your device**

---

## Tips and Tricks

### Favorites
💡 Quick access to your most-played songs
💡 Heart icon changes color with your theme
💡 Favorites are sorted same as main library

### Playlists
💡 Create playlists for different moods/activities
💡 One song can be in multiple playlists
💡 Playlist order follows creation date
💡 Long-press is your friend!

### Sleep Timer
💡 Perfect for falling asleep to music
💡 Icon highlights when active
💡 Timer cancels if you close the app
💡 Choose your perfect duration

### Notifications
💡 Swipe to dismiss when not playing
💡 Works with Bluetooth headphones
💡 Album art shows if available
💡 Always accessible from lock screen

---

## Keyboard Shortcuts (if using emulator)

When app is in focus:
- **Space** - Play/Pause
- **Media Play/Pause** - Play/Pause
- **Media Next** - Next track
- **Media Previous** - Previous track

---

## Troubleshooting

### Favorites not saving?
✓ Check app permissions
✓ Ensure sufficient storage space
✓ Try restarting the app

### Notification not showing?
✓ Check notification permissions (Android 13+)
✓ Ensure "Do Not Disturb" is off
✓ Check battery optimization settings

### Sleep timer not working?
✓ Timer cancels if app is closed
✓ Timer only pauses, doesn't close app
✓ Check if timer is actually active (icon highlighted)

### Playlists empty?
✓ Make sure to add songs via long-press
✓ Songs must exist in library
✓ Try creating a new playlist

---

## What Didn't Change

✅ All existing features work the same:
- Music playback
- Shuffle and repeat modes
- Search functionality
- Sorting options
- Theme selection
- Now playing screen
- Album art display

**Nothing was removed or broken!**

---

## Quick Start Guide

1. **First time?** Grant permissions when prompted
2. **Organize:** Create a playlist for your workout
3. **Like:** Tap ❤️ on your favorite songs
4. **Sleep:** Set a 30-minute timer before bed
5. **Control:** Use lock screen controls during commute

Enjoy your enhanced music experience! 🎵
