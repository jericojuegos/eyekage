# 🎨 UI / Google Stitch Prompt: Voice Commands & Audio Card (`07-voice-commands`)

> **How to Use:**
> 1. Copy the prompt block below into [Google Stitch](https://stitch.withgoogle.com/) (or v0 / Figma AI). Once satisfied, export the screenshot (`01-voice-commands-card.png`) and optional HTML export into this folder (`docs/specs/07-voice-commands/design-reference/`).
> 2. Or, if building directly from design tokens, `/build` will use the specifications in `docs/foundation/design-system.md`.

---

## Screen 1 Prompt — Voice Commands & Audio Settings Card (Copy-Paste into Stitch)

```text
Design a dark-mode Android settings card component for "EyeKage" — an Intelligent Virtual Robot Companion & Personal AI Assistant.

SCREEN PURPOSE:
Provide user controls to enable or disable on-device voice recognition, display an interactive audio status indicator, showcase supported spoken commands, and reassure the user with local privacy disclosures.

VISUAL DIRECTION & DESIGN TOKENS (from docs/foundation/design-system.md):
- Visual Tone: Futuristic, tactile, sleek cyberpunk dark mode.
- Mode: Dark Mode AMOLED (#000000 background).
- Surface / Card Background: #0A0A12 (Deep Navy/Obsidian) with subtle #1E1B4B / #312E81 border.
- Text Primary: #F8FAFC (Slate 50) | Text Muted: #94A3B8 (Slate 400).
- Primary Brand Accent: #22D3EE (Cyan 400) | Secondary Accent: #F59E0B (Amber 500) | Alert/Security Accent: #10B981 (Emerald 500).
- Border Radius: 16px (rounded-2xl) card with 16px internal padding.

LAYOUT & KEY COMPONENTS:
1. Card Header:
   - Icon: Glowing microphone wave icon in #F59E0B (Amber 500).
   - Title: "Voice Commands & Audio".
   - Subtitle: "Control eyes & reactions hands-free with on-device speech".
2. Master Toggle Switch:
   - "Enable Voice Commands" with an Android Switch toggle (Amber when checked).
3. Active State / Microphone Status Pill:
   - A pulsing badge: "Microphone Active (On-Device)" with amber indicator dot when enabled, or "Microphone Inactive / Off" in muted gray when disabled.
4. Supported Voice Commands Chip Sheet:
   - Section label: "WHAT CAN YOU SAY?".
   - Chips in #1E1B4B with #94A3B8 text and #22D3EE accents:
     - `"EyeKage, awaken!"`
     - `"Normal eyes"`
     - `"Crimson tracker"`
     - `"White scan"`
     - `"Purple orbit"`
     - `"Sleep"`
     - `"Blink"`
5. Local Privacy Security Badge:
   - Emerald Shield icon (#10B981).
   - Text: "100% On-Device · No Audio Stored · Zero Network Access".

CONSTRAINTS:
- 100% offline; do not mention cloud recognition or remote servers.
- High contrast, legible typography, adhering to Material 3 dark surface tokens.
```

---

## Export Checklist (Before Running `/build`)

- [ ] Saved approved screenshot(s) as `docs/specs/07-voice-commands/design-reference/01-voice-commands-card.png`
- [ ] *(Optional)* Saved Stitch HTML/Tailwind export as `docs/specs/07-voice-commands/design-reference/01-voice-commands-card.html`
