# Walkthrough - Fixing "Bucket not found" Error

I have updated the profile picture upload logic to handle errors gracefully and provided instructions for creating the necessary bucket in Supabase.

## Changes Made

### Data Layer
- **[ProfileRepositoryImpl.kt](file:///E:/Work/Vello/app/src/main/java/com/mr10/vello/data/repository/ProfileRepositoryImpl.kt)**: Added `try-catch` blocks to `uploadProfilePicture` to catch `RestException` from Supabase and log specific error messages.

## User Actions Required

The "Bucket not found" error occurs because the `profile_pictures` bucket is missing in your Supabase project. Please follow these steps to resolve it:

1.  **Open Supabase Dashboard**: Go to [https://app.supabase.com/](https://app.supabase.com/).
2.  **Navigate to Storage**: Select your project and click **Storage** in the left menu.
3.  **Create Bucket**:
    - Click **New bucket**.
    - Name it exactly: `profile_pictures`
    - Make it a **Public bucket** (optional, but recommended for profile pictures).
    - Click **Create bucket**.
4.  **Set Policies**:
    - Go to **Policies** under the Storage section.
    - Create a new policy for the `profile_pictures` bucket.
    - Allow `INSERT` and `UPDATE` for authenticated users. A common policy is: `(auth.uid() = (storage.foldername(name))[1])` if you store files as `userId/profile.jpg`.
    - Allow `SELECT` for everyone if it's a public bucket, or for authenticated users.

Once the bucket is created, the app will be able to upload profile pictures successfully.
