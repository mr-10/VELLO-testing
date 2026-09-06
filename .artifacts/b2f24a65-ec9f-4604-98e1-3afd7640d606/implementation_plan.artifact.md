# Implementation Plan - v4.0.0: Major Update

This update addresses critical bugs in profile management, authentication flow, and launch routing.

## User Review Required

> [!IMPORTANT]
> This update involves changes to the database schema mapping. Ensure your Supabase `profiles` table has columns `full_name`, `date_of_birth`, and `avatar_url`.

## Proposed Changes

### Data Models & DTOs

#### [MODIFY] [UserProfile.kt](file:///E:/Work/Vello/app/src/main/java/com/mr10/vello/data/model/UserProfile.kt)
- Use `@SerialName` to map Kotlin fields to the actual Supabase column names:
  - `name` -> `full_name`
  - `dob` -> `date_of_birth`
  - `profilePictureUrl` -> `avatar_url`

### Authentication & Lifecycle

#### [MODIFY] [AuthRepository.kt](file:///E:/Work/Vello/app/src/main/java/com/mr10/vello/data/repository/AuthRepository.kt)
- Add `sessionStatus` flow to expose detailed auth state (Loading, Authenticated, etc.).

#### [MODIFY] [AuthRepositoryImpl.kt](file:///E:/Work/Vello/app/src/main/java/com/mr10/vello/data/repository/AuthRepositoryImpl.kt)
- Implement `sessionStatus` using Supabase's `auth.sessionStatus`.

#### [MODIFY] [AuthViewModel.kt](file:///E:/Work/Vello/app/src/main/java/com/mr10/vello/ui/auth/AuthViewModel.kt)
- Expose `sessionStatus` to the UI.
- Update `init` block to handle session status transitions.
- Ensure `isProfileChecked` accurately reflects whether we've attempted to fetch the profile.

### UI & Authentication Screens

#### [MODIFY] [EmailLoginScreen.kt](file:///E:/Work/Vello/app/src/main/java/com/mr10/vello/ui/auth/EmailLoginScreen.kt)
- Ensure background is strictly `Color.White`.
- Style the "Agree & Continue" button with `WhatsAppGreen`.
- Update secondary links (Privacy Policy, Terms) and auxiliary text to use a soft gray color as requested, while maintaining the "Phone Number Entry" layout for email input.

#### [MODIFY] [OtpVerificationScreen.kt](file:///E:/Work/Vello/app/src/main/java/com/mr10/vello/ui/auth/OtpVerificationScreen.kt)
- Align with the "SMS OTP" UX: individual boxes, resend timer, and auto-focus.
- Use the same white background and green/gray color scheme.

### Project Configuration

#### [MODIFY] [build.gradle.kts (app)](file:///E:/Work/Vello/app/build.gradle.kts)
- Bump `versionCode` to `4` and `versionName` to `"4.0.0"`.

## Verification Plan

### Automated Tests
- Build the project to ensure all `@SerialName` mappings and repository changes are correct.

### Manual Verification
- **New User:** Verify flow: Splash -> Email Entry -> OTP Verify -> Profile Setup -> Home.
- **Returning User:** Verify Splash -> Home (skips Auth and Profile Setup).
- **Logged Out:** Verify Splash -> Email Entry.
- **Profile Save:** Verify profile saves correctly with photo and no schema errors.
