# Fix Supabase Implementation and Add Community Support

This plan addresses common Supabase integration issues in the Vello app and introduces foundational support for community features as suggested.

## User Review Required

> [!IMPORTANT]
> - I will be adding `.execute()` to several Supabase database calls. This is necessary for the requests to actually be sent to the server in the current SDK version (3.7.0).
> - I am adding `community_id` to the `Message` model. This will require a corresponding update to the Supabase database schema (adding a nullable `community_id` column to the `messages` table).

## Proposed Changes

### Data Models

#### [MODIFY] [Message.kt](file:///E:/Work/Vello/app/src/main/java/com/mr10/vello/data/model/Message.kt)
- Add nullable `communityId` field.

#### [NEW] [Community.kt](file:///E:/Work/Vello/app/src/main/java/com/mr10/vello/data/model/Community.kt)
- Create a new data model for Communities.

---

### Repositories

#### [MODIFY] [ChatRepositoryImpl.kt](file:///E:/Work/Vello/app/src/main/java/com/mr10/vello/data/repository/ChatRepositoryImpl.kt)
- Add `.execute()` to `sendMessage` and `createOrUpdateChatMembers`.
- Update Realtime subscription to use `PostgresAction.ALL` and ensure it's properly handled if necessary, following the suggested pattern.

#### [MODIFY] [ProfileRepositoryImpl.kt](file:///E:/Work/Vello/app/src/main/java/com/mr10/vello/data/repository/ProfileRepositoryImpl.kt)
- Add `.execute()` to `updateProfile`, `updateEmailVisibility`, and `scheduleAccountDeletion`.

#### [NEW] [CommunityRepository.kt](file:///E:/Work/Vello/app/src/main/java/com/mr10/vello/data/repository/CommunityRepository.kt)
- Define interface for fetching and joining communities.

#### [NEW] [CommunityRepositoryImpl.kt](file:///E:/Work/Vello/app/src/main/java/com/mr10/vello/data/repository/CommunityRepositoryImpl.kt)
- Implement community fetching and joining logic with proper filters and `.execute()` calls.

---

### UI / ViewModels

#### [MODIFY] [CommunitiesViewModel.kt](file:///E:/Work/Vello/app/src/main/java/com/mr10/vello/ui/communities/CommunitiesViewModel.kt)
- Update to use `CommunityRepository` to fetch actual community data instead of just user profiles.

## Verification Plan

### Automated Tests
- I will verify the code compiles and the new models are correctly serializable.

### Manual Verification
- The user should verify that message sending still works (it should be more reliable now with `.execute()`).
- The user should verify the "Communities" screen now reflects community data if available in the database.
