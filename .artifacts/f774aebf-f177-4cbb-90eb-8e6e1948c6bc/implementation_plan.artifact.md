# Implementation Plan - Fix Startup Crash and Refine Adaptive UI (v2.2.1)

This plan addresses a startup crash caused by a missing Ktor engine for Supabase and refines the UI of `ProfileSetupScreen` and `EmailLoginScreen` to be more adaptive on larger screens (tablets).

## User Review Required

> [!NOTE]
> The Ktor version is being set to `3.0.3` to match the requirements of Supabase 3.7.0.

## Proposed Changes

### Build Configuration

#### [MODIFY] [libs.versions.toml](file:///E:/Work/Vello/gradle/libs.versions.toml)
- Add `ktor` version `3.0.3`.
- Add `ktor-client-android` library definition.

#### [MODIFY] [build.gradle.kts](file:///E:/Work/Vello/app/build.gradle.kts)
- Update `versionName` to `"2.2.1"`.
- Add `implementation(libs.ktor.client.android)` to dependencies.

---

### UI Refinement

#### [MODIFY] [ProfileSetupScreen.kt](file:///E:/Work/Vello/app/src/main/java/com/mr10/vello/ui/auth/ProfileSetupScreen.kt)
- Wrap the main `Column` in a `Box` with `Alignment.TopCenter` to center the content horizontally.
- Add `widthIn(max = 600.dp)` to the `Column` to prevent it from stretching on tablets.

#### [MODIFY] [EmailLoginScreen.kt](file:///E:/Work/Vello/app/src/main/java/com/mr10/vello/ui/auth/EmailLoginScreen.kt)
- Add `widthIn(max = 600.dp)` to the `Column` inside the centering `Box`.

## Verification Plan

### Automated Tests
- Run `./gradlew :app:assembleDebug` to verify the build passes.
- Perform Gradle Sync.

### Manual Verification
- Verify the app launches without `IllegalStateException: Failed to find HTTP client engine implementation`.
- Check `ProfileSetupScreen` and `EmailLoginScreen` on a tablet emulator/device to ensure they are centered and not stretched.
- Verify the version name in the build output or app settings.
