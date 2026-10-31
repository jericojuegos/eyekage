# 🧪 Feature UAT & Verification: Data-Driven Eye Modes (`02-eye-modes`)

> **Feature Slug:** `02-eye-modes` · **Spec Links:** [`requirements.md`](./requirements.md) · [`design.md`](./design.md) · [`tasks.md`](./tasks.md) · [`design-reference/`](./design-reference/)  
> **Overall Status:** `[x]` Passed · **Verified Date:** `2026-10-31`

---

## Level 1 — Automated Terminal, Unit & Architecture Checks (AI Executes)

- [x] **Kotlin Compile Check:** `./gradlew compileDebugKotlin` passes with 0 errors.
- [x] **Stack Hard-Failure Audit (Pure-Kotlin Core):** `:core` module contains zero imports matching `android.*` or `androidx.*` (verified via AST/grep audit).
- [x] **Neutral Mode Identifiers Audit:** Zero anime trademark terms (Sharingan, Byakugan, Rinnegan) found in Kotlin class or function names across `:core`.
- [x] **Unit Tests (Tier 1 — Mandatory):** `./gradlew testDebugUnitTest` passes for all core modes, ui mode rendering, and feature mode selection tests.
- [x] **Merged Manifest Permissions Allowlist:** `./gradlew checkMergedManifestPermissions` passes with strictly approved CAMERA permission only.

---

## Level 2 — Functional & EARS Requirement Verification (AI / Dev Smoke Test)

| Check ID | Requirement Ref | Scenario (WHEN / IF) | Expected Outcome (THE System SHALL) | Status |
| :--- | :--- | :--- | :--- | :--- |
| **UAT-1.1** | `Req 1.1` | Mode catalog initialization | Catalog exposes exactly 4 selectable modes and 1 reaction overlay using neutral IDs (`mode.normal`, `mode.crimson_tracker`, `mode.white_scan`, `mode.purple_orbit`, `reaction.awakening`). | `[x]` |
| **UAT-1.2** | `Req 1.2` | UI displays mode titles | Display labels are loaded from `res/values/mode_labels.xml` by key, keeping code decoupled from user-facing names. | `[x]` |
| **UAT-2.1** | `Req 2.1` | `mode.normal` is selected | Renderer draws rounded cyan eyes (`#22D3EE`) with soft radial glow. | `[x]` |
| **UAT-2.2** | `Req 2.2` | `mode.crimson_tracker` is selected | Renderer draws crimson iris (`#DC2626`) with sharp dark geometric tracking accents. | `[x]` |
| **UAT-2.3** | `Req 2.3` | `mode.white_scan` is selected | Renderer draws lavender-white pupil (`#E0E7FF`) with subtle horizontal scan-line pulse. | `[x]` |
| **UAT-2.4** | `Req 2.4` | `mode.purple_orbit` is selected | Renderer draws violet iris (`#A855F7`) with concentric orbital rings. | `[x]` |
| **UAT-3.1** | `Req 3.1` | User taps menu pill or opens mode drawer | UI displays bottom sheet modal with mode selection chips matching dark glassmorphic styling. | `[x]` |
| **UAT-3.2** | `Req 3.2` | User taps any mode selection chip | Active mode updates immediately and eyes animate a smooth 200 ms color morph transition. | `[x]` |

---

## Level 3 — Visual Fidelity (`design-reference/`) & Human UX Sign-Off

- [x] **Visual Reference Match:** Bottom sheet modal and mode chips match `design-reference/stitch-prompt.md` and `docs/foundation/design-system.md` specifications.
- [x] **OLED Black Faceplate:** Eyes rest seamlessly on `#000000` with no clipping or background seams.
- [x] **Smooth Transition Polish:** 200 ms color morph feels responsive and fluid without frame drops.
