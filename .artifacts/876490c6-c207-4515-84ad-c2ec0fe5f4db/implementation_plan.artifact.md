# Implementation Plan - User Discovery, Contact Syncing, and Permission Handling

Implement User Discovery, Contact Syncing, Permission Handling, and Messaging Enhancements for Vello.

## User Review Required

> [!IMPORTANT]
> This update upgrades the app version to 2.0. It requires `READ_CONTACTS`, `CAMERA`, and `RECORD_AUDIO` permissions.
> Messaging UI is being overhauled to include animations and status checkmarks.

## Proposed Changes

### Build Configuration & Permissions

#### [MODIFY] [build.gradle.kts](file:///E:/Work/Vello/app/build.gradle.kts)
- Update `versionName` to "2.0".
- Add Agora RTC SDK dependency.

#### [MODIFY] [libs.versions.toml](file:///E:/Work/Vello/gradle/libs.versions.toml)
- Add Agora RTC SDK version and library definition.

#### [MODIFY] [AndroidManifest.xml](file:///E:/Work/Vello/app/src/main/AndroidManifest.xml)
- Add `READ_CONTACTS`, `CAMERA`, and `RECORD_AUDIO` permissions.

---

### Data Models & Repositories

#### [MODIFY] [Message.kt](file:///E:/Work/Vello/app/src/main/java/com/mr10/vello/data/model/Message.kt)
- Add `status` field (SENT, DELIVERED, READ).

#### [MODIFY] [ChatViewModel.kt](file:///E:/Work/Vello/app/src/main/java/com/mr10/vello/ui/chat/ChatViewModel.kt)
- Implement typing indicator logic using Supabase Realtime.
- Handle message status updates.

---

### UI Components & Screens

#### [NEW] [PermissionHandler.kt](file:///E:/Work/Vello/app/src/main/java/com/mr10/vello/ui/components/PermissionHandler.kt)
- A Compose component to request and handle permissions gracefully using Accompanist.

#### [NEW] [ContactViewModel.kt](file:///E:/Work/Vello/app/src/main/java/com/mr10/vello/ui/home/ContactViewModel.kt)
- Handle fetching device contacts, syncing with Supabase, and user search.

#### [NEW] [ContactListScreen.kt](file:///E:/Work/Vello/app/src/main/java/com/mr10/vello/ui/home/ContactListScreen.kt)
- Display synced contacts and provide a search bar for discovery.

#### [MODIFY] [ChatDetailScreen.kt](file:///E:/Work/Vello/app/src/main/java/com/mr10/vello/ui/chat/ChatDetailScreen.kt)
- Add fluid animations for message entry (Slide-in/Fade-in).
- Show "typing..." indicator.
- Display read/delivered checkmarks.

#### [MODIFY] [HomeScreen.kt](file:///E:/Work/Vello/app/src/main/java/com/mr10/vello/ui/home/HomeScreen.kt)
- Wire up the "New Chat" FAB to open `ContactListScreen`.

---

### Agora Skeleton

#### [NEW] [CallViewModel.kt](file:///E:/Work/Vello/app/src/main/java/com/mr10/vello/ui/calls/CallViewModel.kt)
- Skeleton for Agora calling logic.

## Verification Plan

### Automated Tests
- `ContactRepositoryTest`: Verify `getDeviceContacts` and `syncContacts` logic.
- `ChatViewModelTest`: Verify typing indicator state updates.

### Manual Verification
- Grant permissions and verify contacts are synced.
- Search for a user by phone number and start a new chat.
- Verify messaging animations and status checkmarks in `ChatDetailScreen`.
