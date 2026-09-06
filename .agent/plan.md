# Project Plan

Vello Phase 5: Auth Overhaul & Splash Screen. Implement email-only login with OTP, splash screen, and streamlined onboarding for new vs. returning users. Fix startup crashes.

## Project Brief

The Vello Phase 5 project brief has been generated. It focuses on the core MVP requirements: a new Splash Screen, Email OTP authentication, and a conditional onboarding flow for new vs. returning users. The tech stack is centered on Kotlin, Jetpack Compose, Navigation 3, and Material Adaptive, with Supabase handling the backend OTP logic. A fix for the v2.1 startup crash is prioritized for this release (v2.2).

## Implementation Steps
**Total Duration:** 4h 55m 18s

### Task_1_SetupDependencies: Add dependencies for Supabase (Auth), Navigation 3, and Material Adaptive. Initialize Supabase client in the Application class.
- **Status:** COMPLETED
- **Duration:** 26m 5s

### Task_2_AuthLogic: Implement AuthRepository and AuthViewModel for Supabase Sign-in, Sign-up, and Email Verification flows.
- **Status:** COMPLETED
- **Duration:** 3m 1s

### Task_3_AdaptiveUI: Create WhatsApp-style Sign-in and Sign-up screens using Jetpack Compose and Material Adaptive library.
- **Status:** COMPLETED
- **Duration:** 6m 13s

### Task_4_NavigationVersioning: Implement state-driven navigation with Navigation 3 and integrate the update management system/versioning.
- **Status:** COMPLETED
- **Duration:** 2m 33s

### Task_5_CoreLogic: Implement Profile Setup flow (Name, DOB, Profile Photo via Supabase Storage) and Real-time Messaging repository using Supabase Realtime.
- **Status:** COMPLETED
- **Duration:** 23m 3s

### Task_6_FeatureUIAndPerformance: Build the main UI (Chats, Conversation, Status, Calls, Settings) and implement performance optimizations like Baseline Profiles for mid-range devices.
- **Status:** COMPLETED
- **Duration:** 50m 42s

### Task_7_RunVerify_Fixed: Fix Auth flow (Phone OTP, navigation) and update version to 1.1.
- **Status:** COMPLETED

### Task_8_ConnectivityAndCalling: Implement contact syncing, user discovery, messaging animations, and Agora voice/video calling integration. Handle Contacts, Camera, and Microphone permissions.
- **Status:** COMPLETED
- **Acceptance Criteria:**
  - Agora API_KEY and Supabase integration configured
  - Contact sync and global user search functional
  - Messaging animations and typing indicators implemented
  - Voice/Video calling functional
  - Permissions handled
  - Build pass
- **Duration:** 1h 56m 35s

### Task_9_RunVerifyV2: Finalize Phase 3: Update version to 2.0 and perform final run and verify.
- **Status:** COMPLETED
- **Acceptance Criteria:**
  - App version updated to 2.0
  - Critic_agent verified application stability (no crashes)
  - Alignment with user requirements confirmed
  - All existing tests pass
  - App does not crash
- **Duration:** 8m 47s

### Task_10_Auth_and_Profile_Overhaul: Implement OTP Verification UI for Email/Phone, overhaul Profile Setup with M3 DatePicker, and refine auth state management for unauthenticated users.
- **Status:** COMPLETED
- **Updates:** Implemented Phase 4 (Version 2.1) refinements:
- **Acceptance Criteria:**
  - OTP Verification screen functional for both Email and Phone
  - Profile Setup UI updated with M3 DatePicker
  - Robust handling of 'User not logged in' state with redirects
  - UI matches modern messaging app aesthetics
- **Duration:** 11m 8s

### Task_11_Optimization_and_Final_Verification: Optimize APK size by cleaning up dependencies and assets. Perform final run and verify stability for version 2.1 release.
- **Status:** COMPLETED
- **Updates:** Finalized Phase 4 (Version 2.1):
- **Acceptance Criteria:**
  - APK size reduced through dependency and asset cleanup
  - App version updated to 2.1
  - Project builds successfully
  - Make sure all existing tests pass
  - App does not crash
  - Critic_agent verified application stability and alignment with user requirements
- **Duration:** 18m 27s

### Task_12_AuthOverhaulAndSplashScreen: Implement 1-second Splash Screen, fix startup crashes, overhaul auth flow to Email-only OTP, and implement conditional onboarding for new/returning users. Update app version to 2.2.
- **Status:** COMPLETED
- **Updates:** Phase 5 (Version 2.2) implemented:
- **Acceptance Criteria:**
  - Splash screen displays for 1 second
  - Startup crashes fixed
  - Email-only OTP authentication functional via Supabase
  - New users redirected to profile setup, returning users to Home
  - App version updated to 2.2 in build.gradle
  - Build pass
- **Duration:** 8m 15s

### Task_13_RunAndVerifyV2_2: Perform final run and verification for version 2.2. Instruct critic_agent to verify stability and UI fidelity.
- **Status:** COMPLETED
- **Updates:** Resolved the startup crash and refined the adaptive UI:
- Added `ktor-client-android` dependency to provide the required HTTP engine for Supabase, fixing the startup crash.
- Applied `widthIn(max = 600.dp)` constraints to the EmailLoginScreen and ProfileSetupScreen to ensure proper layout on tablets and large screens.
- Updated the app version to 2.2.1.
- Verified a successful build.
- The app is now stable and opens correctly with the 1-second splash screen.
- **Acceptance Criteria:**
  - Project builds successfully
  - Critic_agent verified application stability (no crashes)
  - Alignment with user requirements (Auth flow, Splash) confirmed
  - Make sure all existing tests pass
  - App does not crash
- **Duration:** 20m 29s

