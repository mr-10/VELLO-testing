# Implementation Plan - Size and Performance Optimization for Vello 2.1

This plan aims to reduce the APK size and improve performance by removing unused dependencies, enabling R8 minification, and optimizing assets.

## User Review Required

> [!IMPORTANT]
> I have identified a significant number of unused dependencies in the project (Agora, Room, Retrofit, CameraX, etc.). I will be removing these to reduce the app size. If any of these were intended for very near-future use, please let me know.

## Proposed Changes

### Build Configuration

#### [MODIFY] [app/build.gradle.kts](file:///E:/Work/Vello/app/build.gradle.kts)
- Enable `isMinifyEnabled` and `isShrinkResources` for the `release` build type.
- Remove unused dependencies:
    - `agora-rtc-sdk`
    - `androidx.camera.*`
    - `androidx.datastore.preferences`
    - `androidx.room.*`
    - `retrofit` & `converter-moshi`
    - `moshi-kotlin`
    - `play-services-location`
    - `ktor-client-android` (Unless required by Supabase BOM, but Supabase usually handles its own engine)
    - `com.google.android.material:material` (Compose app doesn't need the XML Material library)
- Remove KSP plugins and configurations for Room and Moshi.

### Asset Optimization

#### [DELETE] [placeholder.png](file:///E:/Work/Vello/app/src/main/res/drawable/placeholder.png)
#### [NEW] [placeholder.webp](file:///E:/Work/Vello/app/src/main/res/drawable/placeholder.webp)
- Convert the placeholder image to WebP format.

### Code Cleanup

#### [MODIFY] [VelloApplication.kt](file:///E:/Work/Vello/app/src/main/java/com/mr10/vello/VelloApplication.kt)
- Remove `AGORA_APP_ID` constant as it is unused.

## Verification Plan

### Automated Tests
- Run `./gradlew :app:assembleRelease` to verify that the build succeeds with minification enabled.
- Run `./gradlew :app:test` to ensure no regressions in logic.

### Manual Verification
- Verify the app still functions correctly (Auth, Chat list, etc.) after optimization.
- Compare APK size before and after changes (if possible to estimate).
