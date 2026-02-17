# Music Controller Auto-Hide Fix

## Problem Statement
The music controller and timeline were hiding unexpectedly after a few seconds of inactivity. Specifically:
1. The entire music controller would disappear
2. The timeline seekbar would hide (though the time display remained visible)

## Root Cause
The `PlayerControlView` component from the AndroidX Media3 library has a built-in auto-hide feature with a default timeout of approximately 5 seconds. When this timeout expires, the controller (including buttons and the timeline seekbar) automatically hides to provide a cleaner viewing experience during playback.

## Solution
Disabled the auto-hide timeout by configuring the `PlayerControlView` to never automatically hide.

### Code Changes
**File:** `app/src/main/java/com/example/mymusic/MainActivity.kt`

**Location:** Lines 132-134 (in the `onCreate` method)

**Change:**
```kotlin
// Set player to PlayerControlView
binding.playerControlView.player = player
// Disable auto-hide timeout to keep controller always visible
binding.playerControlView.setShowTimeoutMs(0)
binding.playerControlView.showController()
```

### Explanation
- `setShowTimeoutMs(0)`: Sets the timeout to 0 milliseconds, which disables the auto-hide feature entirely
- `showController()`: Explicitly shows the controller immediately when the player is initialized

## Result
The music controller and timeline seekbar now remain visible at all times, regardless of user inactivity. Users can always:
- See the current playback position
- Adjust the timeline by dragging the seekbar
- Access all playback controls (play/pause, skip, shuffle, repeat, etc.)

## Technical Details
- **Component:** `androidx.media3.ui.PlayerControlView`
- **Default Behavior:** Auto-hide after ~5 seconds of inactivity
- **Modified Behavior:** Always visible (timeout disabled)
- **Layout:** The seekbar is defined in `custom_player_control.xml` as `androidx.media3.ui.DefaultTimeBar` with ID `exo_progress`

## Testing
To verify this fix works:
1. Start playing music
2. Wait for more than 5 seconds without touching the screen
3. Verify the music controller remains visible
4. Verify the timeline seekbar remains visible and draggable
5. Verify time labels (current/duration) remain visible

## Impact
This is a minimal, surgical change that only affects the auto-hide behavior. All other functionality remains unchanged:
- Playback controls still work as expected
- Timeline seeking still works
- UI appearance is unchanged
- No changes to other components or features
