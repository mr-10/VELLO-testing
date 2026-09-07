# Release Notes - Vello v9.8.1 (Animation & Management Update)

This release focuses on enhancing the user experience through fluid animations, improved navigation tools, and robust profile/account management features.

## 🚀 New Features

### 🔍 Settings Search
- **Search Anything:** A new search icon in the Settings section allows you to quickly find specific setting categories.
- **Real-time Filtering:** Results update instantly as you type with smooth appearance animations.
- **Direct Navigation:** Tap any search result to jump directly to that setting screen.

### 🖼️ Profile Picture Preview (WhatsApp Style)
- **Full-Screen View:** Tap your profile picture in the Edit Profile screen to view it in a full-screen, immersive preview.
- **Dedicated Controls:** A new camera icon clarifies how to change your photo, while the image itself is used for viewing.

### ⏳ Scheduled Account Deletion
- **Safety Window:** Initiating account deletion now schedules a 24-hour waiting period before permanent removal.
- **Instant Cancellation:** If you change your mind, simply log back into your account within 24 hours to automatically stop the deletion process.

## 🎨 UI & UX Improvements

### 🌊 Global Slide Animations
- **Fluid Navigation:** All screen transitions now feature a smooth horizontal slide animation (right-to-left for forward navigation, left-to-right for backward).
- **Consistent Experience:** This animation is applied globally across all main screens and settings sub-screens.

### 🛠️ Refined Account Menu
- **Simplified Interface:** The Account settings section has been decluttered to focus on three essential options:
    - **Email address:** View your email and toggle public visibility.
    - **Delete account:** Access the new scheduled deletion workflow.
    - **Sign out:** Quickly logout from your current session.
- **Email Visibility:** Added a new popup to easily switch between "Show to public" and "Hide email address."

## ⚙️ Technical Enhancements
- **Smart Storage Management:** Replacing your profile picture now automatically triggers a cleanup process to remove old, unused images.
- **Cache-Busting Pictures:** Improved the way profile pictures are handled to ensure updates appear instantly without caching delays.
- **Proactive Deletion Detection:** The app now checks for pending deletions during login to offer immediate restoration of your account.
