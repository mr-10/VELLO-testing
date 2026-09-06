# Project Optimization Report

This report summarizes the optimizations performed on the Vello project to reduce APK size and improve build performance.

## 1. Code Shrinking & Minification
- Enabled `isMinifyEnabled = true` in the `release` build type.
- Enabled `isShrinkResources = true` to remove unused resources.
- Added a basic `proguard-rules.pro` for custom R8 configurations.

## 2. Dependency Cleanup
The following unused dependencies were removed from the project:
- **Agora RTC SDK**: No calling logic was implemented using this SDK.
- **Room Persistence Library**: Data persistence is currently handled by Supabase; local DB was unused.
- **Retrofit & Moshi**: Supabase uses Ktor and Kotlinx Serialization; these were redundant.
- **CameraX**: Camera features were not yet implemented in code.
- **Play Services Location**: Location tracking features were not implemented.
- **DataStore**: Preferences were not being used.
- **Material Components (M2)**: The app is built with pure Jetpack Compose Material 3.
- **KSP Plugin**: Was only required for Room and Moshi.

## 3. Asset Optimization
- Removed the unused `placeholder.png` asset.
- Verified that all remaining launcher icons are in WebP format.

## 4. Build Configuration
- Cleaned up `libs.versions.toml` by removing all unused version and library definitions.
- Updated `themes.xml` to inherit from a system theme (`android:Theme.DeviceDefault.NoActionBar`) to remove dependency on the Material Components library.

## Verification
- **Gradle Sync**: Successful.
- **Build**: `:app:assembleDebug` completed successfully.
- **Functional Integrity**: Auth, Profile, and Messaging logic remain intact as they rely on Supabase and Compose M3, which were preserved.
