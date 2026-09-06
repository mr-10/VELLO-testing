# WhatsApp Clone — Full Screen & Design Specification

This document is written so it can be fed directly to an AI coding agent (Cursor, Copilot, a custom LLM agent, etc.) as a build spec.

---

## 1. Design System (Global)

### Color Palette

| Token | Light Mode | Dark Mode | Usage |
|---|---|---|---|
| Primary Green | `#25D366` | `#25D366` | FAB, send button, online dot |
| Primary Dark Teal | `#075E54` | `#075E54` | Header (legacy), brand accent |
| App Bar / Header | `#008069` | `#1F2C34` | Top nav bar |
| Chat Background | `#ECE5DD` (wallpaper pattern) | `#0B141A` | Chat screen backdrop |
| Bubble - Sent | `#D9FDD3` | `#005C4B` | Outgoing message bubble |
| Bubble - Received | `#FFFFFF` | `#202C33` | Incoming message bubble |
| Text Primary | `#111B21` | `#E9EDEF` | Main text |
| Text Secondary | `#667781` | `#8696A0` | Timestamps, last message preview |
| Divider | `#E9EDEF` | `#222D34` | List separators |
| Unread Badge | `#25D366` | `#25D366` | Unread count pill |
| Link/Mentions | `#027EB5` | `#53BDEB` | Links, @mentions |
| Error/Delete | `#EA0038` | `#EA0038` | Delete, block actions |
| Ticks (sent/delivered) | `#8696A0` | `#8696A0` | Single/double gray check |
| Ticks (read) | `#53BDEB` | `#53BDEB` | Blue double check |

### Typography

| Style | Font Size | Weight | Usage |
|---|---|---|---|
| Header Title | 20sp | 500 (Medium) | Screen titles |
| Chat Name (list) | 17sp | 500 | Chat list row name |
| Message Text | 16sp | 400 | Bubble content |
| Timestamp | 12sp | 400 | Message/list timestamps |
| Last Message Preview | 14sp | 400 | Chat list subtitle |
| Section Header | 14sp | 600, uppercase | "Contacts", "Recent" |

Font family: system default (SF Pro on iOS, Roboto on Android) or **Inter** / **Helvetica Neue** for cross-platform consistency.

### Core Animation Principles

| Interaction | Animation | Duration |
|---|---|---|
| Screen push (list → detail) | Slide-in from right, previous screen slides left 30% + dims | 250–300ms ease-out |
| Modal / bottom sheet | Slide up from bottom, backdrop fade 0→0.5 opacity | 300ms ease-out |
| Tab switch (Chats/Status/Calls) | Cross-fade + slight horizontal slide | 200ms ease-in-out |
| Message send | Bubble scales in from 0.8→1.0 + fades in, list auto-scrolls | 200ms spring |
| Message receive | Bubble slides up 8px + fades in | 200ms ease-out |
| Typing indicator | 3-dot bounce loop, staggered 150ms per dot | infinite loop |
| Pull to refresh | Circular spinner scales in, elastic overscroll | native physics |
| FAB (new chat) | Scale + rotate 0→180° morph into camera icon on scroll | 200ms |
| Long-press message | Bubble scales to 1.05, context menu fades/scales up from touch point | 150ms |
| Status ring (story) | Conic-gradient progress ring, segmented per status item | linear per item duration |
| Call connecting | Pulsing avatar (scale 1→1.1→1 loop) + ripple rings expanding outward | 1.5s loop |
| Swipe to reply | Bubble translates right on drag, reply icon fades in, snaps back on release | drag-linked |
| Voice note record | Mic icon scales up, waveform bars animate live, red pulsing REC dot | live/drag-linked |

---

## 2. Full Screen List (26 screens)

1. Splash Screen
2. Onboarding / Welcome
3. Phone Number Entry
4. OTP Verification
5. Profile Setup (name + photo)
6. Chats List (Home)
7. Individual Chat Screen
8. Camera Screen
9. Media/Gallery Picker
10. New Chat / Contact List
11. New Group Creation
12. Group Info
13. Contact/Chat Info
14. Status Tab (Stories list)
15. Status Viewer (full-screen story)
16. Calls Tab
17. Outgoing/Incoming Call Screen
18. Active Voice Call Screen
19. Active Video Call Screen
20. Search Screen
21. Media Full-Screen Viewer
22. Document Picker / Attachment Menu
23. Archived Chats
24. Starred Messages
25. Settings (root)
26. Chat Wallpaper Settings

---

## 3. Screen-by-Screen Breakdown

### 1. Splash Screen
- **Purpose:** Brand loading screen while app initializes/auth check runs.
- **Elements:** Centered logo (WhatsApp-style speech bubble icon), app name, subtle green background or white with logo only.
- **Colors:** Background `#075E54` or white; logo in brand green/white.
- **Animation:** Logo fades in + scales from 0.9→1.0 (400ms), auto-navigates after 1.5–2s or once auth check resolves.
- **Features:** Silent auth-token check → routes to Chats List (if logged in) or Onboarding (if not).

### 2. Onboarding / Welcome
- **Elements:** Illustration, headline ("Welcome to [App]"), short description, "Agree & Continue" button, ToS/Privacy links.
- **Colors:** White background, green CTA button, secondary gray links.
- **Animation:** Illustration fades/slides up on load; button has ripple/press-scale feedback.
- **Features:** ToS acceptance gate before phone entry.

### 3. Phone Number Entry
- **Elements:** Country code picker (flag + dial code), phone input field, "Next" button, small explanatory text about SMS verification.
- **Colors:** Input underline in green on focus; error state in red.
- **Animation:** Keyboard slides up pushing content; input field border color-transitions on focus (150ms).
- **Features:** Country auto-detect via locale/IP, input validation, carrier charge disclaimer.

### 4. OTP Verification
- **Elements:** 6-digit OTP boxes, auto-read via SMS (Android autofill), resend timer countdown, "Change number" link.
- **Colors:** Active OTP box border green, filled boxes dark text, disabled resend link gray.
- **Animation:** Each digit box pulses/scales briefly on input; shake animation (translateX ±8px, 3 cycles) on wrong OTP; countdown text ticks down.
- **Features:** Auto-fill from SMS, resend after 30–60s cooldown, rate-limit lockout messaging.

### 5. Profile Setup
- **Elements:** Circular avatar picker (camera icon overlay), name text field, optional "About" field, "Next"/"Done" button.
- **Colors:** Avatar placeholder gray circle with camera icon in green.
- **Animation:** Avatar picker bounces slightly on tap; image crop screen slides up modally.
- **Features:** Camera/gallery selection, name character limit, default avatar generation from initials.

### 6. Chats List (Home)
- **Elements:** Top app bar (app name, search icon, menu/overflow icon), tab bar (Chats / Status / Calls / Communities), scrollable list of chat rows (avatar, name, last message, timestamp, unread badge, mute/pin icons), FAB (new chat).
- **Colors:** Header teal/dark, unread badge green pill with white count text, pinned chat rows subtly tinted background.
- **Animation:** List rows fade+slide in on load (staggered 30ms each); swipe-left reveals archive/mute/delete actions with icon scale-in; FAB scales on scroll direction change; pull-to-refresh spinner.
- **Features:** Search, pin chat, mute chat, archive, mark unread, swipe actions, unread count, online status dot, typing indicator preview ("typing...") in last-message row.

### 7. Individual Chat Screen
- **Elements:** Header (avatar, contact name, last seen/typing status, video/voice call icons, overflow menu), scrollable message list (bubbles, date separators, timestamps, read receipts), bottom input bar (emoji icon, text field, attachment/camera/mic icon), scroll-to-bottom FAB when scrolled up.
- **Colors:** Sent bubbles light green, received bubbles white/dark-gray, background wallpaper pattern, tick marks gray→blue on read.
- **Animation:** New message bubble pop-in; typing indicator dots loop; long-press reveals reaction bar (emoji icons scale in sequentially) + context menu; swipe-right-on-bubble reveals reply icon; mic button morphs to waveform + timer while holding to record; message send button morphs mic↔send based on input text presence.
- **Features:** Text messaging, emoji/sticker/GIF picker, media attach (photo/video/doc/location/contact), voice notes (hold to record, swipe to cancel, slide to lock), reactions, reply/quote, forward, delete, star, copy, read receipts, typing indicator, disappearing messages toggle, encryption notice banner.

### 8. Camera Screen
- **Elements:** Full-screen live camera preview, capture button, flash/flip camera toggle, mode switcher (photo/video), gallery thumbnail shortcut (bottom-left), close (X) top-left.
- **Colors:** Black background/UI chrome, white icons, red REC indicator for video mode.
- **Animation:** Capture button scales down on press then bounces back; mode switch slides horizontally; shutter flash animation (white overlay fade 0→1→0, 150ms) on photo capture.
- **Features:** Photo/video capture, flash control, front/back toggle, zoom gesture, immediate send-preview after capture with caption field.

### 9. Media/Gallery Picker
- **Elements:** Grid of photos/videos from device gallery, multi-select checkmarks, selected-count header, "Send" button (bottom-right, badge with count).
- **Colors:** Selected items get green checkmark overlay + slight dim/border on thumbnail.
- **Animation:** Checkmark scales in on select (150ms bounce); grid items slightly shrink (scale 0.95) when selected.
- **Features:** Multi-select, preview on tap, caption entry before send, album/folder filter.

### 10. New Chat / Contact List
- **Elements:** Search bar, "New Group"/"New Contact"/"New Community" quick-action rows (icon + label), alphabetically-sectioned contact list with section index (A–Z scrubber on right), contact avatar + name + status.
- **Colors:** Section headers gray uppercase, quick-action icons in green circle backgrounds.
- **Animation:** List sections fade in; A–Z index scrubber highlights + triggers scroll-jump with haptic-style feedback.
- **Features:** Contact search/filter, invite non-app contacts via SMS link, quick access to group/community creation.

### 11. New Group Creation
- **Step 1 — Select Members:** Search + contact list with checkboxes, selected members shown as chips at top, "Next" arrow FAB.
- **Step 2 — Group Setup:** Group icon picker, group name field, selected member count.
- **Colors:** Selected chips green pill background with white X to remove.
- **Animation:** Chips slide in from bottom when member selected; step transition slides horizontally left.
- **Features:** Member search, max-member validation, group icon upload, group name required to proceed.

### 12. Group Info
- **Elements:** Group icon (large, tappable), group name/description (editable if admin), member list with admin badges, media/links/docs shared summary row, settings toggles (mute, disappearing messages), "Exit group"/"Report group" actions.
- **Colors:** Admin badge in green text/pill; destructive actions (exit/delete) in red.
- **Animation:** Sections expand/collapse (accordion, 200ms height transition) for "Media" and "Members" previews.
- **Features:** Add/remove members (admin), promote/demote admin, edit group info, mute notifications, view shared media grid, leave group.

### 13. Contact/Chat Info
- **Elements:** Large avatar, name, phone number, About text, action row (message/audio/video icons), shared media grid preview, options list (mute, disappearing messages, encryption, block, report).
- **Colors:** Standard list style; block/report in red text.
- **Animation:** Avatar tap opens full-screen viewer with shared-element zoom transition.
- **Features:** Block/unblock, report contact, view encryption details, media/links/docs tabs, starred messages shortcut, mute duration picker.

### 14. Status Tab (Stories List)
- **Elements:** "My Status" row (add-status "+" button on own avatar), "Recent updates" section (contacts with new status, ringed avatar), "Viewed updates" section (grayed ring).
- **Colors:** Unviewed status ring in green gradient/segments; viewed ring in gray.
- **Animation:** Ring segments animate in on load; tapping avatar triggers shared-element transition into full-screen viewer.
- **Features:** Post status (photo/video/text), view others' statuses, see viewers list (for own status), status privacy settings, auto-expire after 24h.

### 15. Status Viewer (Full-Screen Story)
- **Elements:** Full-screen media, segmented progress bar (top), sender name + timestamp, reply input (bottom), emoji quick-react row.
- **Colors:** Overlay gradient (black to transparent) top/bottom for text legibility over media.
- **Animation:** Segmented progress bars fill left-to-right per item (duration-linked, ~5s per image, video-length per video); tap-right/left advances/rewinds with instant segment jump; hold pauses progress; swipe-down dismisses with drag-following translateY.
- **Features:** Auto-advance through contact's statuses then next contact, reply via DM, emoji reactions, view viewer list, mute status updates from a contact.

### 16. Calls Tab
- **Elements:** Search bar, "Create call link" row, recent calls list (avatar, name, call type icon [incoming/outgoing/missed], timestamp), FAB (new call).
- **Colors:** Missed calls shown in red text/icon; outgoing/incoming in gray/green icon.
- **Animation:** List fade-in on load; swipe-left reveals delete action.
- **Features:** Call history, redial, video/voice call initiation, missed call indicator, call link sharing.

### 17. Outgoing/Incoming Call Screen (Pre-connect)
- **Elements:** Full-screen contact avatar/background blur, name, call status text ("Calling...", "Incoming call"), accept/decline buttons (incoming) or cancel button (outgoing), speaker/mute/video-toggle quick icons.
- **Colors:** Decline button red circle, accept button green circle, background dark gradient overlay on contact photo.
- **Animation:** Incoming: buttons pulse/breathe (scale 1↔1.05 loop); avatar has expanding ripple rings; swipe-up-to-accept / swipe-down-to-decline gesture with drag-linked motion (mobile lock-screen style). Outgoing: dots loading animation on "Calling..." text.
- **Features:** Accept/decline/mute-before-answer, quick-reply-with-text (decline + message), speaker toggle.

### 18. Active Voice Call Screen
- **Elements:** Avatar + name centered, call timer, control row (mute, speaker, add call, keypad, video toggle, end call).
- **Colors:** End-call button red, active toggles (mute/speaker) highlighted white-on-dark when enabled.
- **Animation:** Control icons toggle with background-fill transition (150ms); minimized state shrinks into floating pill draggable overlay.
- **Features:** Mute, speaker toggle, switch to video, add participant, minimize to floating bubble, end call, call duration timer.

### 19. Active Video Call Screen
- **Elements:** Full-screen remote video feed, self-view PiP (draggable, resizable), control bar (mute, camera flip, camera on/off, effects, end call), participant thumbnails (group calls).
- **Colors:** Dark UI chrome overlay on video, end-call red button.
- **Animation:** Self-PiP draggable with spring-snap to corners; controls auto-hide after 3s of inactivity (fade out), tap screen to reveal again; participant grid re-layouts with animated position transitions when someone joins/leaves.
- **Features:** Camera flip, mute/unmute, camera on/off, background blur/effects, add participant, grid/speaker view toggle, screen share (optional), end call.

### 20. Search Screen
- **Elements:** Search input (auto-focused), filter chips (Chats/Contacts/Messages/Media), results list grouped by category, recent searches when empty.
- **Colors:** Active filter chip green fill/white text; inactive chip outlined gray.
- **Animation:** Results fade/slide in as user types (debounced); matched text substring highlighted in bold/green.
- **Features:** Global search across chats, messages, contacts, and media; jump-to-message-in-context on result tap.

### 21. Media Full-Screen Viewer
- **Elements:** Full-screen image/video, top bar (back, sender info, forward/share/delete/star icons), bottom thumbnail filmstrip (if part of multi-media set), caption text if present.
- **Colors:** Black background, white icon overlay with subtle shadow for contrast.
- **Animation:** Shared-element zoom transition from thumbnail → full view; pinch-to-zoom + pan; swipe-down-to-dismiss (drag-linked scale-down + fade); horizontal swipe to next/prev media (slide transition).
- **Features:** Zoom/pan, swipe between media in same chat, share/forward/delete/star, video playback controls, save to device.

### 22. Document Picker / Attachment Menu
- **Elements:** Bottom-sheet grid of attachment types (Document, Camera, Gallery, Audio, Location, Contact, Poll, Event), each as icon-in-colored-circle + label.
- **Colors:** Each attachment type has a distinct accent color (e.g., Document=purple, Camera=pink, Gallery=blue, Location=green, Contact=orange) matching WhatsApp's convention.
- **Animation:** Bottom sheet slides up with backdrop fade; icons stagger-scale-in (20ms delay each) on open.
- **Features:** File picker, live location share, contact share, poll creation, calendar event share.

### 23. Archived Chats
- **Elements:** Header ("Archived"), list identical in style to main Chats List, empty-state illustration if none archived.
- **Colors:** Same as Chats List; muted section note about auto-archive settings.
- **Animation:** Standard list slide-in transition from Chats List (push navigation).
- **Features:** Unarchive (swipe or tap), same swipe actions as main list, settings link for "keep chats archived" toggle.

### 24. Starred Messages
- **Elements:** Chronological list of starred messages across all chats, each showing sender/chat name, message snippet, timestamp, jump-to-chat icon.
- **Colors:** Standard list; star icon in yellow/gold accent.
- **Animation:** Unstar action removes item with slide-out + height-collapse (200ms).
- **Features:** Cross-chat starred view, jump to original message in context, unstar.

### 25. Settings (Root)
- **Elements:** Profile summary row (avatar, name, status) at top, list sections: Account, Privacy, Chats, Notifications, Storage & Data, Help, Invite a Friend.
- **Colors:** Standard list style, icons in muted gray/green per category.
- **Animation:** Row press state (background flash + ripple); nested screens push in from right.
- **Features:** Profile edit shortcut, deep-links to each settings sub-screen, app version/about footer.

### 26. Chat Wallpaper Settings
- **Elements:** Grid of wallpaper presets (solid colors + patterned), "Choose from gallery" option, live chat-bubble preview overlay on selected wallpaper, per-chat vs. global apply toggle.
- **Colors:** Preset swatches cover full brand palette + neutrals; preview shows actual bubble colors on top.
- **Animation:** Selecting a wallpaper cross-fades the preview background (200ms); checkmark scales in on selected tile.
- **Features:** Per-chat custom wallpaper, brightness/dim toggle for dark mode, reset to default.

---

## 4. Suggested Build Priority (MVP → Full)

1. **MVP Core:** Splash → Phone/OTP → Profile Setup → Chats List → Individual Chat (text only) → Contact List/New Chat
2. **Media Layer:** Camera, Gallery Picker, Media Viewer, Attachment Menu, Voice Notes
3. **Social Layer:** Status Tab + Viewer, Group Creation + Group Info
4. **Calls Layer:** Calls Tab, Incoming/Outgoing screens, Active Voice/Video Call
5. **Polish Layer:** Search, Archived Chats, Starred Messages, Settings, Wallpaper, animations refinement, dark mode
