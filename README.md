# Vello - WhatsApp-inspired Chat App

Vello is a modern Android chat application inspired by WhatsApp, built using Jetpack Compose and Supabase.

## Downloads & Releases
You can find all versions of the Vello APK along with their specific release notes in the [GitHub Releases](https://github.com/mr-10/VELLO-testing/releases) section.

### Latest Version
- **v9.9.4**: [Download APK](https://github.com/mr-10/VELLO-testing/releases/download/v9.9.4/Vello_v9.9.4.apk)
- **v9.9.3.2**: [Download APK](https://github.com/mr-10/VELLO-testing/releases/download/v9.9.3.2/Vello.(9.9.3.2).apk)

## Features
- **Modern UI**: Built entirely with Jetpack Compose following Material 3 guidelines and WhatsApp's iconic design.
- **Supabase Integration**: Robust authentication and real-time database capabilities.
- **Email/OTP Authentication**: Secure login flow with probe-first logic for seamless sign-up and sign-in.
- **Profile Management**: Customizable user profiles with name, date of birth, and profile picture.
- **Fast Startup**: Optimized splash screen for quick access to the app.

## Tech Stack
- **UI**: Jetpack Compose, Material 3
- **Backend**: Supabase (Auth, Postgrest, Realtime, Storage, Functions)
- **Networking**: Ktor
- **Concurrency**: Kotlin Coroutines & Flow
- **Serialization**: Kotlinx Serialization
- **Image Loading**: Coil

## Latest Updates
- Redesigned Auth flow with improved logic for user existence checking.
- Optimized splash screen timeout to 500ms.
- Enhanced OTP verification UI with countdown timer and digit-only validation.
- Standardized repository pattern for authentication.

## Getting Started
To run this project, you will need to set up a Supabase project and provide the necessary credentials in `VelloApplication.kt`.

## License
MIT License
