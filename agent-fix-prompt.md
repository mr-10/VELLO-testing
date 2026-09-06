# Prompt for AI Coding Agent — Fix Profile Save, Add Email Auth, Fix Launch Routing

Paste everything below into your AI coding agent (Cursor / Copilot / etc.) as one instruction.

---

## Context

This is a WhatsApp-clone Android app (package `com.mr10.vello`) using Supabase as the backend (Auth + Postgres + Storage). There are three separate bugs to fix.

---

## Bug 1 — Column name mismatch on Save Profile

The Save Profile screen currently fails with:
```
Could not find the 'dob' column of 'profiles' in the schema cache
```

The Supabase `profiles` table has these actual columns:
- `id` (uuid)
- `full_name` (text)
- `date_of_birth` (date)
- `avatar_url` (text)
- `updated_at` (timestamptz)

**Fix:** Find the upsert/insert call for saving the profile (likely in a `ProfileRepository`, `ProfileViewModel`, or similar file) and update the field/key names sent to Supabase so they match the table exactly:
- `name` → `full_name`
- `dob` → `date_of_birth`
- `profilePictureUrl` → `avatar_url`

Also update any Kotlin data class / DTO used to serialize this request so its `@SerialName` or JSON key annotations match the same column names. Search the whole project for any other place these three field names are used (e.g. when *reading* the profile back) and correct those too, so reads and writes stay consistent.

---

## Bug 2 — App always opens Profile Setup instead of checking session/auth state

Right now, every time the app is opened, it lands directly on the **Profile Setup** screen with blank fields — even if the user already has an account and a saved profile, and even if they're not logged in at all.

**Required behavior (fix the app's startup/navigation logic):**
1. On launch, check Supabase Auth for an existing valid session.
   - **No session / not logged in** → show the authentication flow first (see Bug 3 below), not Profile Setup.
   - **Session exists** → check whether a row already exists in `profiles` for that user id.
     - **Profile exists** → skip Profile Setup entirely and go straight to the main Chats List screen.
     - **No profile row yet** (first-time user, just verified) → show Profile Setup, and this time pre-fill the Full Name, Date of Birth, and avatar fields from any existing data instead of leaving them blank if partial data exists.
2. This routing decision should happen once at a splash/loading step before any UI is shown, not after the user is already looking at a blank form.

---

## Bug 3 — Add full authentication flow with email verification (WhatsApp-style, but email-based)

Currently there appears to be no real gate before Profile Setup. Add a complete auth flow using **Supabase Auth with email + OTP verification**, structured the same way WhatsApp structures its phone-based flow, but using email instead of phone number:

1. **Email Entry screen** — user enters their email address, taps Continue.
2. **Send OTP** — call Supabase Auth's email OTP sign-in (`signInWithOtp` with email, no password) to send a 6-digit verification code to that address.
3. **OTP Verification screen** — 6-digit code input (same UX pattern as an SMS OTP screen: individual boxes, resend timer/cooldown, auto-focus next digit), verifies the code against Supabase Auth.
4. On successful verification, Supabase creates/returns the authenticated session — persist it (Supabase's Kotlin client handles session persistence automatically if configured with a session storage; make sure that's enabled so the user isn't asked to log in again on next app open).
5. After verification, run the profile-existence check from Bug 2 to decide whether to show Profile Setup or go straight to Chats List.

**Do not use password-based auth** — keep it OTP/email-code only, matching the passwordless feel of WhatsApp's phone flow.

---

## Acceptance checklist for the agent to self-verify

- [ ] Saving a profile with name, date of birth, and photo succeeds with no schema errors.
- [ ] Closing and reopening the app after a profile is already saved goes straight to the Chats List — never back to a blank Profile Setup.
- [ ] Closing and reopening the app while logged out shows the Email Entry screen, not Profile Setup or Chats List.
- [ ] A brand-new user goes: Email Entry → OTP Verify → Profile Setup (blank, first time) → Chats List.
- [ ] A returning verified user with a saved profile goes: (splash/session check) → Chats List directly.
