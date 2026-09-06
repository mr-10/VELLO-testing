---
name: Vello Design System
colors:
  surface: '#f7f9fc'
  surface-dim: '#d8dadd'
  surface-bright: '#f7f9fc'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#f2f4f7'
  surface-container: '#eceef1'
  surface-container-high: '#e6e8eb'
  surface-container-highest: '#e0e3e6'
  on-surface: '#191c1e'
  on-surface-variant: '#3f4946'
  inverse-surface: '#2d3133'
  inverse-on-surface: '#eff1f4'
  outline: '#6f7976'
  outline-variant: '#bec9c5'
  surface-tint: '#1c695f'
  primary: '#00453d'
  on-primary: '#ffffff'
  primary-container: '#075e54'
  on-primary-container: '#8dd5c8'
  inverse-primary: '#8cd4c7'
  secondary: '#006b5f'
  on-secondary: '#ffffff'
  secondary-container: '#8cf1e1'
  on-secondary-container: '#006f64'
  tertiary: '#00471c'
  on-tertiary: '#ffffff'
  tertiary-container: '#006129'
  on-tertiary-container: '#3fe374'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#a8f0e3'
  primary-fixed-dim: '#8cd4c7'
  on-primary-fixed: '#00201c'
  on-primary-fixed-variant: '#005047'
  secondary-fixed: '#8ff4e3'
  secondary-fixed-dim: '#72d8c8'
  on-secondary-fixed: '#00201c'
  on-secondary-fixed-variant: '#005047'
  tertiary-fixed: '#66ff8e'
  tertiary-fixed-dim: '#3de273'
  on-tertiary-fixed: '#002109'
  on-tertiary-fixed-variant: '#005322'
  background: '#f7f9fc'
  on-background: '#191c1e'
  surface-variant: '#e0e3e6'
typography:
  display-lg:
    fontFamily: Inter
    fontSize: 40px
    fontWeight: '700'
    lineHeight: 48px
    letterSpacing: -0.02em
  display-md:
    fontFamily: Inter
    fontSize: 32px
    fontWeight: '600'
    lineHeight: 40px
    letterSpacing: -0.015em
  headline-lg:
    fontFamily: Inter
    fontSize: 28px
    fontWeight: '600'
    lineHeight: 36px
    letterSpacing: -0.01em
  headline-lg-mobile:
    fontFamily: Inter
    fontSize: 22px
    fontWeight: '600'
    lineHeight: 28px
    letterSpacing: -0.01em
  headline-md:
    fontFamily: Inter
    fontSize: 20px
    fontWeight: '600'
    lineHeight: 26px
    letterSpacing: -0.005em
  headline-sm:
    fontFamily: Inter
    fontSize: 18px
    fontWeight: '600'
    lineHeight: 24px
    letterSpacing: 0em
  title-lg:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '600'
    lineHeight: 22px
    letterSpacing: 0em
  title-md:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '600'
    lineHeight: 20px
    letterSpacing: 0.01em
  body-lg:
    fontFamily: Roboto Flex
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
    letterSpacing: 0.01em
  body-md:
    fontFamily: Roboto Flex
    fontSize: 14.5px
    fontWeight: '400'
    lineHeight: 20px
    letterSpacing: 0.015em
  body-sm:
    fontFamily: Roboto Flex
    fontSize: 13px
    fontWeight: '400'
    lineHeight: 18px
    letterSpacing: 0.02em
  label-lg:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '500'
    lineHeight: 20px
    letterSpacing: 0.02em
  label-md:
    fontFamily: Inter
    fontSize: 12px
    fontWeight: '500'
    lineHeight: 16px
    letterSpacing: 0.03em
  label-sm:
    fontFamily: Inter
    fontSize: 11px
    fontWeight: '600'
    lineHeight: 14px
    letterSpacing: 0.04em
rounded:
  sm: 0.5rem
  DEFAULT: 1rem
  md: 1.5rem
  lg: 2rem
  xl: 3rem
  full: 9999px
spacing:
  space-2xs: 0.125rem
  space-xs: 0.25rem
  space-sm: 0.5rem
  space-md: 0.75rem
  space-base: 1rem
  space-lg: 1.25rem
  space-xl: 1.5rem
  space-2xl: 2rem
  space-3xl: 3rem
  chat-bubble-p: 0.625rem 0.875rem
  gutter-mobile: 1rem
  gutter-tablet: 1.5rem
  gutter-desktop: 2rem
  max-chat-width: 720px
  sidebar-width: 380px
---

## Brand & Style

This design system delivers an evolved, refined interpretation of modern messaging by merging signature messaging tones with the structural discipline of Material Design 3. The aesthetic is utilitarian yet warm, prioritizing immediate legibility, high-density glanceability, and conversational intimacy.

The style anchors on **Modern Material**: systematic tonal depth, fluid state transitions, expansive touch targets, and tactile rounded surfaces. Ambient elevation supersedes heavy drop shadows, relying on subtle surface-tier changes to convey spatial relationships. The emotional response is centered on trust, effortless speed, and familiar digital connectivity.

## Colors

The color palette translates recognizable messaging greens into functional Material Design 3 role assignments, anchoring communication states and interactive chrome.

### Role Mappings & Tokens
- **Primary (`#075E54`)**: Dark Forest Teal. Used for primary app bars, top navigation surfaces, and high-emphasis brand elements.
- **Secondary (`#128C7E`)**: Light Teal. Primary interactive accents, active tabs, floating action buttons (FABs), and selection states.
- **Tertiary (`#25D366`)**: Vivid Emerald Accent. Reserved for unread status indicators, active call alerts, notification badges, and high-energy affirmative actions.
- **Surface & Background (`#F0F2F5`)**: Canvas neutral for conversation list backings and chat viewport neutral grounding.
- **Message Bubble Sent (`#E2FFC7`)**: High-legibility light emerald tint for outgoing message bubbles with 90%+ contrast against `#111B21`.
- **Message Bubble Received (`#FFFFFF`)**: Pure surface-container-lowest, providing a crisp base for incoming text and inline media.
- **On-Surface (`#111B21`)**: Deep charcoal-slate for optimal text contrast across light bubbles and containers.
- **On-Surface Variant (`#667781`)**: Mid-tone slate for timestamps, read receipt ticks, and message meta details.

## Typography

Typography balances structured UI navigation with natural, fluid conversational rhythm. 

- **Headers & Labels (Inter)**: Delivers clear, architectural geometry suited for top navigation, tab labels, contact names, and modal banners.
- **Message Content & Body (Roboto Flex)**: Provides variable weight tracking and open counters, optimized for rapid, fatigue-free reading across varied message lengths and embedded previews.
- **Conversation Timestamps & Status Markers**: Standardized on `label-sm` with tabular numerals to ensure uniform alignment within variable-length chat bubbles.

## Layout & Spacing

The layout is built on an adaptive multi-pane system rooted in an 8-point base grid, scaled down to 4-point micro-increments for compact conversational density.

### Breakpoints & Adaptations
- **Mobile (< 600px)**: Single-pane view transitioning full-screen between conversations list, active thread, and contact information. Margins are fixed to `gutter-mobile` (16px).
- **Tablet (600px - 1023px)**: Dual-pane master-detail arrangement. Left navigation panel docks at 340px fixed width; active chat fluidly fills the remaining viewport.
- **Desktop (1024px+)**: Three-tier workspace. Left panel docks at `sidebar-width` (380px), main conversation view centers with a maximum line length of `max-chat-width` (720px) to maintain readability, and optional contextual media/profile inspector slides in from the right edge at 360px.

## Elevation & Depth

Visual hierarchy implements Material Design 3 tonal elevation, where surface elevation is articulated primarily via tinted background tokens and delicate ambient shadows rather than stark drop borders.

- **Level 0 (Flat Canvas)**: Hex `#F0F2F5` chat wallpaper canvas and base lists. No shadow.
- **Level 1 (Card & Bubbles)**: Pure white `#FFFFFF` received bubbles and list tiles on hover. Shadow: `0px 1px 2px rgba(11, 20, 26, 0.06), 0px 1px 3px rgba(11, 20, 26, 0.1)`.
- **Level 2 (Top Bars & Search Containers)**: Dark forest header bar (`#075E54`) and floating search pills. Shadow: `0px 2px 4px rgba(0, 0, 0, 0.08), 0px 4px 8px rgba(0, 0, 0, 0.06)`.
- **Level 3 (FAB & Action Modals)**: Floating Action Buttons (`#128C7E` or `#25D366`) and contextual dropdown menus. Shadow: `0px 4px 8px rgba(18, 140, 126, 0.24), 0px 8px 16px rgba(0, 0, 0, 0.08)`.
- **Level 4 (Dialogs & Media Viewers)**: Centered interactive overlays, lightboxes, and call interfaces. Shadow: `0px 12px 24px rgba(11, 20, 26, 0.18)`.
- **Separators & Ghost Borders**: Subsurface dividers use 1px solid `rgba(134, 150, 160, 0.15)` to avoid optical harshness.

## Shapes

This design system uses a pill-shaped curvature architecture (`roundedness: 3`) to yield friendly, tactile touchpoints aligned with contemporary Material 3 specifications.

- **Pills (`rounded-full` / 9999px)**: Action chips, search input capsules, audio record buttons, Floating Action Buttons, and unread count badges.
- **Cards & Sheets (`rounded-3xl` / 1.5rem to 2rem)**: Profile details cards, bottom sheets, context menus, and inline attachments previews.
- **Conversation Bubbles**: Unique directional curvature. Symmetrical `1.125rem` corners except for the anchor tail corner (bottom-right on sent bubbles; bottom-left on received bubbles) which tapers to `0.25rem` for conversational orientation.

## Components

### Buttons
- **Primary / FAB**: Pill-shaped capsule with `#128C7E` background and `#FFFFFF` icon/text. On hover, apply an 8% brightness overlay; on active press, scale to 0.98. The tertiary emerald `#25D366` is applied specifically to new chat and call-action triggers.
- **Segmented & Pill Buttons**: Outlined with `rgba(18, 140, 126, 0.3)` or solid filled with `#E2FFC7` for active filters (e.g., "Unread", "Groups", "Favorites").

### Chat Bubbles
- **Sent Message**: Background `#E2FFC7`, On-surface `#111B21`. Right-aligned. Trailing metadata holds timestamp in `label-sm` (`#667781`) alongside double-check icons tinting to `#34B7F1` upon read state.
- **Received Message**: Background `#FFFFFF`, On-surface `#111B21`. Left-aligned. Integrated elevation Level 1.
- **Spacing**: Tightly grouped consecutive messages from the same sender share a 2px vertical margin, expanding to 8px when alternating senders.

### Chips & Badges
- **Status Badges**: Solid `#25D366` circular badge for unread count, displaying `label-sm` bold white text. Online status is a 10px `#25D366` circle with a 2px white border knockout.
- **Filter Chips**: 32px height, fully pill-shaped. Inactive: `#FFFFFF` with faint slate border; Active: `#075E54` text on light mint `#D9FDD3` background.

### Input Fields
- **Chat Bar**: 48px minimum height rounded pill (`rounded-full`) resting inside an elevated Level 1 bottom app bar. Background `#FFFFFF`, placeholder text in `#667781`. Includes embedded leading emoji/attachment icons and trailing mic or send action.
- **Search Bar**: Centered pill shape with `#F0F2F5` fill, zero outline, and leading search icon in `#667781`.

### Lists & Row Tiles
- **Conversation Row**: 72px fixed height. Leading 48px circular avatar with micro-presence indicator. Title in `title-lg` with single-line truncation. Trailing column anchors timestamp (`label-sm`) and unread badge. Active state uses 4% Primary Forest Teal wash.
- **Dividers**: Inset 76px from the left to clear avatars, 1px high, colored with `rgba(134, 150, 160, 0.15)`.

### Cards & Media Previews
- **Shared Media**: Rounded-3xl containers with clipped boundaries. Audio notes feature a dynamic scrubbing waveform colored with Secondary Teal (`#128C7E`) and playhead thumb.
- **Document Previews**: Level 1 elevated white card with dedicated filetype icon badge, file size caption, and instant download arrow.