# Security Fix: Credential Exposure and Git Exposure

This plan addresses the hardcoded credentials in `VelloApplication.kt` and the tracking of sensitive `.idea` files.

## User Review Required

> [!IMPORTANT]
> **Key Rotation**: You MUST rotate your Supabase keys in the Supabase Dashboard immediately after this fix is applied. The old keys are still present in your Git history.
> **Git History Purge**: This fix prevents *future* leaks. To remove existing leaks from your history, you should use a tool like `git filter-repo` or `BFG Repo-Cleaner`.

## Proposed Changes

### Build Configuration

#### [MODIFY] [libs.versions.toml](file:///E:/Work/Vello/gradle/libs.versions.toml)
- Add `secrets-gradle-plugin` to `[versions]` and `[plugins]`.

#### [MODIFY] [build.gradle.kts](file:///E:/Work/Vello/build.gradle.kts)
- Register the `secrets-gradle-plugin`.

#### [MODIFY] [build.gradle.kts (app)](file:///E:/Work/Vello/app/build.gradle.kts)
- Apply `secrets-gradle-plugin`.
- Enable `buildConfig = true`.

### Environment Configuration

#### [MODIFY] [.gitignore](file:///E:/Work/Vello/.gitignore)
- Improve `.idea` folder exclusion.
- Ensure `local.properties` is fully ignored.

#### [MODIFY] [local.properties](file:///E:/Work/Vello/local.properties)
- Add `SUPABASE_URL` and `SUPABASE_KEY`.

### Source Code

#### [MODIFY] [VelloApplication.kt](file:///E:/Work/Vello/app/src/main/java/com/mr10/vello/VelloApplication.kt)
- Replace hardcoded Supabase URL and Key with `BuildConfig` references.

## Verification Plan

### Automated Tests
- Run `./gradlew app:assembleDebug` to ensure the project builds correctly with the new plugin and generated `BuildConfig`.

### Manual Verification
- Verify that `BuildConfig.SUPABASE_URL` and `BuildConfig.SUPABASE_KEY` are correctly generated and used by the app.
- Check that `.idea/workspace.xml` and other sensitive files are not accidentally added back to the staging area.
