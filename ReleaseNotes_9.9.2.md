# Release Notes - Vello v9.9.2 (Critical Crash Fix & Navigation Stability)

This update focuses on resolving the critical crash reported in the Communities section and improving overall application stability.

## 🛠️ Critical Fixes

### 🐛 Resolved Community Chat Crash
- **Data Model Alignment:** Fixed a serialization issue where chat messages were failing to decode from Supabase due to a naming mismatch between the app and the database (`chatId` vs `chat_id`).
- **Robust Error Handling:** Added `try-catch` blocks around message loading and navigation logic to ensure that even if a network or data error occurs, the app remains stable instead of crashing.
- **Navigation Sync:** Improved how the app transitions from the Communities screen to a Chat Detail screen, ensuring all parameters are passed safely.

## ✨ Improvements

### 🗺️ Navigation Stability
- **Cast Safety:** Refined the internal navigation key handling to prevent potential type-mismatch errors during screen transitions.
- **Dialog Management:** Improved the dismissal logic for user preview dialogs to prevent race conditions during navigation.

### 🌊 Other Enhancements
- **Theme Persistence:** Reinforced the local settings system to ensure your chosen theme and font size are applied even more reliably across different screens.
- **Combined Features:** Includes all the customization features from 9.9.0 and the sound/signaling features from 9.9.1.
