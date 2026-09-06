# Implementation Plan - Fix Supabase Storage "Bucket not found" Error

The user is encountering a "Bucket not found" error when uploading profile pictures. This is because the `profile_pictures` bucket does not exist in their Supabase project.

## Proposed Changes

### Data Layer

#### [MODIFY] [ProfileRepositoryImpl.kt](file:///E:/Work/Vello/app/src/main/java/com/mr10/vello/data/repository/ProfileRepositoryImpl.kt)
- Wrap the upload logic in a `try-catch` block.
- Log the error for better debugging.
- Rethrow a more descriptive exception if the bucket is not found, or let the original exception propagate if it's already clear, but ensure it's handled.

## Verification Plan

### Manual Verification
- I will provide instructions to the user to create the bucket in their Supabase dashboard.
- The user should verify that after creating the bucket, the upload works.

## User Instructions for Supabase Dashboard

To fix the "Bucket not found" error, you need to create the storage bucket in your Supabase dashboard:

1.  Go to your [Supabase Dashboard](https://app.supabase.com/).
2.  Select your project.
3.  Click on **Storage** in the left sidebar.
4.  Click **New bucket**.
5.  Enter `profile_pictures` as the name.
6.  (Recommended) Toggle **Public bucket** to ON if you want the profile pictures to be publicly accessible via URL.
7.  Click **Create bucket**.
8.  Ensure you have set up the appropriate **Storage Policies** (RLS) to allow users to upload their own files. For example, a policy for `INSERT` and `UPDATE` where `(auth.uid() = (storage.foldername(name))[1])`.
