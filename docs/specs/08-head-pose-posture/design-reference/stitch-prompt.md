# 🎨 UI / Google Stitch Prompt: Head Pose & Desk Posture Awareness (`08-head-pose-posture`)

> **How to Use:**
> 1. **Personal / New UI (Google Stitch):** Copy the prompt block below into [Google Stitch](https://stitch.withgoogle.com/) (or v0 / Figma AI). Once happy with the design, export the **screenshot (`01-posture-settings.png`)** and optional **HTML/Tailwind code (`01-posture-settings.html`)** into this folder (`docs/specs/08-head-pose-posture/design-reference/`).
> 2. **Client Already Has Designs:** Drop wireframe/Figma screenshots (`*.png` / `*.jpg`) into this folder and run `/spec` or `/build`.

---

## Screen 1 Prompt — Desk Posture & Awareness Card (Settings Screen)

```text
Design an Android mobile settings card component for "EyeKage" — an intelligent virtual robot companion that lives on OLED-black screens.

SCREEN PURPOSE:
Configure desk posture reminders, view live posture orientation status, adjust slouch detection sensitivity, and verify on-device privacy disclosures.

VISUAL DIRECTION & DESIGN TOKENS (from docs/foundation/design-system.md):
- Visual Tone: Dark & Premium, Futuristic Cybernetic Companion, Clean & Minimal
- Mode: Dark Theme Only
- Background: #000000 (True AMOLED Black)
- Card Surface: #0A0A12 (Deep Midnight Obsidian)
- Card Border: #28283C (Subtle High-Contrast Outline, 1px)
- Text Primary: #FFFFFF (Crisp White)
- Text Muted: #94A3B8 (Slate Gray)
- Primary Accent Glow: #22D3EE (Cyan Accent)
- Active Posture Indicator: #10B981 (Emerald Green for "Balanced")
- Slouch / Warning Indicator: #F59E0B (Warm Amber for "Slouching / Too Close")
- Typography: Inter / System Sans-Serif, 16sp Card Title, 14sp Body, 12sp Status Badges
- Border Radius: 16px rounded-2xl cards, 8px rounded-lg chips

LAYOUT & KEY COMPONENTS (Mapped to EARS Requirements):
1. Card Header ([Req 4.2]):
   - Left: Glowing Cyan icon inside dark circular badge (Person sitting upright / desk posture silhouette 🧘 or posture alignment icon).
   - Title: "Desk Posture & Awareness" (16sp SemiBold).
   - Subtitle: "Ambient reminders when slouching at your desk" (13sp Slate).
   - Right: Smooth Android Material 3 Switch toggle (Off by default, glowing Cyan when On).

2. Live Posture State Badge ([Req 4.3]):
   - Horizontal pill bar showing current estimated posture:
     - "● Balanced" with emerald pulse dot, or "▲ Slouching (32°)" with warm amber highlight when active.
     - Small secondary text: "Distance: ~50cm (Normal)" or "Too Close (<30cm)".

3. Slouch Sensitivity Selector ([Req 4.3]):
   - Label: "REMINDER TIMING" (11sp uppercase, bold slate).
   - 3 selectable pill chips horizontally:
     - Chip 1: "Strict (15s)"
     - Chip 2: "Standard (30s)" [Selected with Cyan outline and glowing text]
     - Chip 3: "Gentle (45s)"

4. Interactive Posture Preview on Companion HUD ([Req 3.1, 3.4]):
   - Mock miniature companion face showing robot eyes subtly tilting by 12 degrees inquisitively.
   - Small HUD pill: glowing Amber posture icon badge (🧘) next to the green camera indicator.

5. On-Device Privacy Shield ([Req 4.4]):
   - Subtle dark badge with emerald shield checkmark icon:
     "100% On-Device Facial Geometry. Zero photos or biometric records are ever saved."

CONSTRAINTS:
- No medical claims or doctor/ergonomic certifications.
- No cloud, analytics, or sync references.
- High contrast, dark AMOLED palette with zero bright backgrounds.
```

---

## Export Checklist (Before Running `/build`)

- [ ] Saved approved screenshot(s) as `docs/specs/08-head-pose-posture/design-reference/01-posture-settings.png`
- [ ] *(Optional)* Saved Stitch HTML export as `docs/specs/08-head-pose-posture/design-reference/01-posture-settings.html`
