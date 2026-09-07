# Vello Release Notes - v9.9.3

## Summary
This update focuses on stabilizing core messaging functionality, improving database integration with Supabase, and introducing deep UI customization options.

## 🚀 What's New
- **Advanced Customization:**
    - **Global Themes:** Added 8 distinct color palettes (accessible via Settings > Chats).
    - **Custom Wallpapers:** Set any image as chat background with adjustable opacity (10% to 80%).
    - **Font Scaling:** Three font size options (Small, Medium, Large) with real-time preview.
- **Improved UI:**
    - Refined "Enter is send" keyboard logic.
    - Removed redundant Avatar Persona feature to streamline settings.

## 🛠️ Fixes & Improvements
- **Messaging Stability:**
    - Fixed an issue where messages would remain stuck in a "PENDING" (clock) state.
    - Improved `ChatViewModel` logic to handle message state transitions more reliably.
- **Supabase Integration:**
    - Added comprehensive error logging for database operations in Logcat (filter by `ChatRepository`).
    - Provided `setup_messaging.sql` for automated database schema synchronization.
- **Performance:**
    - Optimized local settings persistence using `SharedPreferences`.

## 📝 Important Note
Users must run the `setup_messaging.sql` script in their Supabase dashboard to enable Realtime messaging and apply the necessary RLS policies for this version to function correctly.
