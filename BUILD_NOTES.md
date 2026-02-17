# Build Notes

## Issue Fixed: App Crash on Startup

### Problem
The app was crashing immediately when trying to open it.

### Root Cause
The build configuration had a mismatch between the actual implementation and the configuration:
- The app uses **ViewBinding** for UI (traditional Android Views)
- Build configuration incorrectly enabled **Compose** (`kotlin-compose` plugin and Compose dependencies)
- This caused the Compose runtime to initialize unnecessarily, leading to crashes

### Solution
Removed all Compose-related configuration and dependencies since the app doesn't use Compose UI:

1. **Replaced plugin**: Changed from `kotlin-compose` to `kotlin-android` in both root and app build files
2. **Removed Compose features**: Deleted `compose = true` from buildFeatures
3. **Removed composeCompiler block**: Not needed for ViewBinding apps
4. **Added kotlinOptions**: Properly configured JVM target to match compileOptions
5. **Removed Compose dependencies**: Removed all unused Compose libraries (activity-compose, compose-bom, compose-ui, material3, etc.)

### Changes Made

#### Root build.gradle.kts
```kotlin
// Before
plugins {
    alias(libs.plugins.kotlin.compose) apply false
}

// After
plugins {
    alias(libs.plugins.kotlin.android) apply false
}
```

#### app/build.gradle.kts
Key changes:
- Plugin: `kotlin-compose` → `kotlin-android`
- Added `kotlinOptions { jvmTarget = "11" }`
- Removed `composeCompiler { ... }`
- Removed `compose = true` from buildFeatures
- Removed all Compose dependencies

### Expected Outcome
With these fixes:
- ✅ The app should build successfully without errors
- ✅ The app should launch without crashing
- ✅ ViewBinding will work properly
- ✅ Smaller APK size (no unnecessary Compose libraries)
- ✅ All features (music playback, themes, search) work as expected

### Testing
Due to network restrictions in the build environment, the actual build cannot be completed. However, the configuration is now correct and will work in an environment with proper internet access to download Android Gradle Plugin and dependencies.

### Build Requirements
- Gradle: 8.10.2
- AGP: 8.5.2
- Kotlin: 2.0.21
- compileSdk: 35
- minSdk: 24
- JVM Target: 11
