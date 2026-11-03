# 🎨 UI / Google Stitch Prompt: Hand Gestures & Interactive Reactions (`09-hand-gestures`)

> **How to Use:**
> 1. **Google Stitch Export:** Copy the prompt block below into [Google Stitch](https://stitch.withgoogle.com/) (or Figma / v0). Once finalized, export the screen render as `01-hand-gestures.png` into this folder (`docs/specs/09-hand-gestures/design-reference/`).
> 2. **Client Design Sync:** If custom UI renders or Figma screenshots already exist, drop them here as `*.png` / `*.jpg` before running `/verify`.

---

## Screen 1 Prompt — `Hand Gestures & Interactive HUD` (Copy-Paste into Stitch)

```text
Design a native Android mobile screen for "EyeKage" — an offline-first intelligent virtual robot companion that lives on an OLED-black surface and reacts to user presence and hand gestures with expressive procedural robot eyes.

SCREEN PURPOSE:
Showcase the two primary UI surfaces for Feature 09-hand-gestures:
1. Companion Faceplate HUD with Active Gesture Recognition feedback chip.
2. Settings screen "Hand Gestures & Interactions" card with real-time detection preview and sensitivity options.

VISUAL DIRECTION & DESIGN TOKENS (from docs/foundation/design-system.md):
- Visual Tone: Premium Cybernetic Companion / Minimal OLED Black Dark Mode
- Mode: Pure OLED Dark
- Background: #000000 (Pure OLED Black)
- Card / Surface: #0A0A12 (Deep Navy-Black container)
- Card Border: #26263A (Subtle 1px border stroke)
- Text Primary: #FFFFFF (Crisp White)
- Text Muted: #94A3B8 (Cool Slate Gray)
- Primary Brand Accent: #22D3EE (Electric Cyan)
- Status / Active Accent: #10B981 (Vibrant Emerald Green)
- Alert Accent: #F59E0B (Warm Amber)
- Chip Background: #1E1B4B (Deep Indigo Slate)
- Border Radius: 16px rounded-2xl cards, 8px rounded-md chips, 20px pill badges
- Typography: Clean geometric sans-serif (Inter / Roboto) with monospace coordinate cues

LAYOUT & KEY COMPONENTS:

Surface A: Companion Faceplate (HUD Top Bar & Active Gesture Pill) [Req 3.4, 3.6]
- Central screen area: Two large, glowing robot eyes displayed symmetrically on #000000. Left eye is winking playfully (curved upward crescent with raised eyebrow), right eye is wide and glowing cyan (#22D3EE) with pupil focused.
- Top HUD Bar:
  - Top-left: Small camera tracking dot (glowing emerald #10B981).
  - Top-center: Floating Gesture Reaction Chip (Emerald background #10B981 at 15% opacity with 1px emerald border, containing a wave icon "👋 Wave Detected!" in emerald #10B981 bold text).
  - Top-right: Debug toggle and Settings gear icon (#94A3B8).
- Bottom: Sleek mode drawer pill ("● NORMAL MODE").

Surface B: Settings Screen — "Hand Gestures & Interactions" Card [Req 4.1–4.5]
- Card Container: 16px rounded card with #0A0A12 background and #26263A border.
- Header Row:
  - Left: Circular icon badge (36x36 dp, #22D3EE at 15% alpha) with an open hand icon (🖐️), next to title "Hand Gestures & Interactions" (bold 16sp white) and subtitle "Interact with EyeKage using hand gestures in front of the camera" (12sp slate).
  - Right: Cyan toggle switch (#22D3EE) in ON position.
- Real-Time Status Pill:
  - Full-width pill banner (#10B981 at 12% alpha, 20px radius) showing:
    - Glowing emerald pulse dot (8x8 dp)
    - Status text: "Active Gesture: ✌️ Peace Sign (Confidence: 89%)"
- Sensitivity Selector:
  - Section label: "GESTURE SENSITIVITY" (11sp uppercase slate #94A3B8).
  - Three horizontal selectable chips:
    - [ Low ] (Strict confidence ≥ 75%, 250ms hold)
    - [ Normal ★ ] (Selected, cyan border #22D3EE, #1E1B4B background, bold text)
    - [ High ] (Quick trigger ≥ 50%, 100ms hold)
- Privacy Shield Badge:
  - Horizontal shield badge (8px radius, #10B981 at 10% alpha):
    - Emerald shield checkmark icon (🛡️)
    - Text: "100% On-Device Gesture Recognition. Zero Handprints or Photos Saved."

CONSTRAINTS:
- No light mode, no white backgrounds.
- Zero network or cloud telemetry indicators.
- High contrast, legible typography, no clipped text.
```

---

## Export Checklist (Before Running `/build`)

- [ ] Saved approved screenshot as `docs/specs/09-hand-gestures/design-reference/01-hand-gestures.png`
- [ ] *(Optional)* Saved Stitch HTML export as `docs/specs/09-hand-gestures/design-reference/01-hand-gestures.html`
