# 🎨 Design System & Visual Identity — EyeKage

## 1. Visual Identity & Tone

- **Visual Tone:** **Dark & Premium.** Glowing, energetic robot eyes set against an absolute OLED-black face (`#000000`).
- **Personality:** Intelligent, alert, playful, and responsive. Inspired by physical desk robots (Vector, EMO) without derivative imitation.
- **Surface Philosophy:** Minimal on-screen chrome during active companion mode. Controls (drawer, mode switcher, camera indicator) are subtle and unobtrusive.
- **Theme Support:** Dark Theme ONLY in Phase 1 (required by robot-face aesthetic).

---

## 2. Color System & Design Tokens

### Core UI Palette (Compose Material 3 Tokens)

| Token | Light Value (Reserved) | Dark Value (Active) | Usage |
|---|---|---|---|
| `md_theme_dark_background` | `#F8FAFC` | `#000000` | True Black faceplate background |
| `md_theme_dark_surface` | `#FFFFFF` | `#0A0A12` | Settings panels, bottom sheets, cards |
| `md_theme_dark_surfaceVariant` | `#F1F5F9` | `#161622` | Mode picker chips, unselected items |
| `md_theme_dark_primary` | `#06B6D4` | `#22D3EE` | Primary accent, Normal mode glow, active states |
| `md_theme_dark_onPrimary` | `#FFFFFF` | `#00363D` | Text on primary buttons |
| `md_theme_dark_outline` | `#CBD5E1` | `#28283C` | Subtle dividers, chip borders |
| `md_theme_dark_error` | `#EF4444` | `#F87171` | Permission warning, critical errors |
| `camera_indicator_active` | — | `#10B981` | Emerald indicator: Camera is actively analyzing |
| `camera_indicator_inactive`| — | `#475569` | Muted slate indicator: Camera is off/idle |

---

## 3. Eye Mode Palettes & Procedural Geometry

All eye modes are data-driven. The renderer uses these tokens to draw pupils, outer iris glow, and interior highlights:

| Mode ID | Prototype Display Name | Primary Color | Secondary / Detail | Visual Behavior & Geometry |
|---|---|---|---|---|
| `mode.normal` | Normal | `#22D3EE` (Cyan) | `#0891B2` (Deep Cyan) | Rounded pill-shaped friendly eyes. Soft outer radial glow, subtle micro-saccades, natural blinks. |
| `mode.crimson_tracker` | Sharingan | `#DC2626` (Crimson) | `#18181B` (Black Accent) | Sharp, focused circular eyes with high-contrast crimson iris and geometric dark tracking markings. |
| `mode.white_scan` | Byakugan | `#E0E7FF` (Pale Lavender) | `#818CF8` (Soft Indigo) | Ethereal pale luminescent eyes with an active, subtle horizontal scan-line radar pulse. |
| `mode.purple_orbit` | Rinnegan | `#A855F7` (Violet) | `#581C87` (Deep Purple) | Concentric orbital rings that gently pulse and counter-rotate during gaze tracking. |
| `reaction.awakening` | Awakening | `#F59E0B` (Amber) | `#FEF3C7` (Bright Gold) | **Temporary Reaction Overlay.** Dramatic radial light burst when face is first acquired, then returns to active mode. |

---

## 4. Typography Scale (Material 3)

| Role | Font Family | Size | Weight | Line Height | Usage |
|---|---|---|---|---|---|
| `headlineMedium` | Inter / System | 24sp | Bold (700) | 32sp | Settings titles, mode names |
| `titleMedium` | Inter / System | 16sp | SemiBold (600) | 24sp | Section headers, card titles |
| `bodyMedium` | Inter / System | 14sp | Regular (400) | 20sp | Explanatory text, disclosures |
| `labelLarge` | Inter / System | 14sp | Medium (500) | 20sp | Buttons, mode chips, badges |
| `labelSmall` | JetBrains Mono / System | 11sp | Regular (400) | 16sp | Debug overlay (normalized coordinates) |

---

## 5. Motion, Saccades & Accessibility

### Gaze Tracking Smoothing (One-Euro Filter)
- **Minimum Cutoff Frequency ($f_{c\text{min}}$):** `1.0 Hz` — Eliminates jitter when the user holds their head still.
- **Speed Coefficient ($\beta$):** `0.007` — Dynamically increases cutoff frequency during fast head movements, eliminating lag.
- **Derivative Cutoff ($f_{c\text{der}}$):** `1.0 Hz`.

### Natural Idle Behaviors
- **Saccades:** Micro-adjustments ($1\text{–}3\text{ px}$) randomized every $1.5\text{–}4.0\text{ seconds}$ to make eyes feel organic.
- **Blink Cycle:** Spontaneous blinks every $3.0\text{–}7.0\text{ seconds}$ (duration $150\text{–}200\text{ ms}$). Quick double-blinks occur at $10\%$ probability.

### Reduced Motion Mode
- When Android's system "Remove Animations" setting is active (or overridden to `ON` in EyeKage settings):
  - Spontaneous idle micro-saccades and rotational orbital animations are disabled.
  - Blinks transition via instantaneous snap or soft $50\text{ ms}$ fade rather than full procedural lid sweeps.
  - The **Awakening** light burst is replaced with a gentle, non-strobing border highlight.
  - Gaze tracking remains responsive, ensuring visual feedback is preserved.

