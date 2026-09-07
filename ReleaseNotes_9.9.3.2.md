# Vello Release Notes - v9.9.3.2

## Summary
This minor update addresses a critical bug in the messaging system that prevented new messages from being saved to the database.

## 🛠️ Fixes & Improvements
- **Supabase Messaging Fix:**
    - Resolved a critical issue where messages were failing to insert into the Supabase `messages` table due to a missing `created_at` timestamp.
    - Updated `ChatRepositoryImpl` to automatically generate an ISO-8601 timestamp for every new message.
    - Refactored `Message` data model to ensure field names match the backend schema exactly (`created_at`).
- **Build & Distribution:**
    - Updated versioning to `9.9.3.2` (Build 24).
    - Generated a fresh APK for testing and production deployment.

## 🚀 Deployment
- The new APK `Vello (9.9.3.2).apk` is now available in the project root.
- All changes have been pushed to the GitHub repository.

## 📝 Note
This version is fully compatible with the existing Supabase schema. No database migrations are required if you have already applied the `9.9.3` updates.
