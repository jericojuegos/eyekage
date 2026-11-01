# 🎨 UI / Google Stitch Prompt: Companion Memory & Affinity Card (`06-personality-memory`)

> **How to Use:**
> 1. Copy the prompt block below into [Google Stitch](https://stitch.withgoogle.com/) (or v0 / Figma AI). Once satisfied, export the screenshot (`01-memory-card.png`) and optional HTML export into this folder (`docs/specs/06-personality-memory/design-reference/`).
> 2. Or, if building directly from design tokens, `/build` will use the specifications in `docs/foundation/design-system.md`.

---

## Screen 1 Prompt — Companion Memory & Affinity Card (Copy-Paste into Stitch)

```text
Design a dark-mode Android settings card component for "EyeKage" — an Intelligent Virtual Robot Companion & Personal AI Assistant.

SCREEN PURPOSE:
Display the companion's current mood, personality metrics, bond progression tier, and lifetime interaction statistics to the user in an inspectable, transparent format with a factory-reset action.

VISUAL DIRECTION & DESIGN TOKENS (from docs/foundation/design-system.md):
- Visual Tone: Futuristic, tactile, sleek cyberpunk dark mode.
- Mode: Dark Mode AMOLED (#000000 background).
- Surface / Card Background: #0A0A12 (Deep Navy/Obsidian) with subtle #1E1B4B / #312E81 border.
- Text Primary: #F8FAFC (Slate 50) | Text Muted: #94A3B8 (Slate 400).
- Primary Brand Accent: #22D3EE (Cyan 400) | Secondary Accent: #F59E0B (Amber 500) | Alert/Destructive Accent: #EF4444 (Red 500).
- Border Radius: 16px (rounded-2xl) card with 16px internal padding.

LAYOUT & KEY COMPONENTS:
1. Card Header:
   - Icon: Heart / Brain holographic icon in #22D3EE.
   - Title: "Companion Memory & Affinity".
   - Subtitle: "Local on-device personality and bonding progression".
2. Current Mood Badge:
   - A glowing pill chip displaying the live mood (e.g., "Curious & Alert" in Cyan, or "Playful" in Amber, or "Drowsy" in Indigo).
3. Bond Progression Tier:
   - Tier label: "Bond Tier 3: Attentive Companion".
   - Segmented progress bar or milestone dots indicating progression to Tier 4.
4. Statistics Grid (2x2 Clean Counters):
   - "Total Pokes / Interactions": e.g., "42".
   - "Time Gazed Together": e.g., "14m 30s".
   - "Curiosity Level": "85%".
   - "Playfulness": "60%".
5. Destructive Action:
   - A subtle outline button: "Reset Companion Memory" with red accent text (#EF4444) and a refresh/trash icon.
   - Confirmation dialog state: "Reset Memory? This will clear all bond progression and interaction stats back to zero. Preferences will be preserved." with "Cancel" and "Reset" buttons.

CONSTRAINTS:
- 100% offline; do not mention cloud sync or user accounts.
- Keep typography crisp, monospace stats labels, and high contrast.
```

---

## Export Checklist (Before Running `/build`)

- [ ] Saved approved screenshot(s) as `docs/specs/06-personality-memory/design-reference/01-memory-card.png`
- [ ] *(Optional)* Saved Stitch HTML/Tailwind export as `docs/specs/06-personality-memory/design-reference/01-memory-card.html`
