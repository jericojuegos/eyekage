# 🧪 Feature UAT & Verification: Procedural Eye Engine (`01-eye-engine`)

> **Feature Slug:** `01-eye-engine` · **Spec Links:** [`requirements.md`](./requirements.md) · [`design.md`](./design.md) · [`tasks.md`](./tasks.md) · [`design-reference/`](./design-reference/)  
> **Overall Status:** `[x]` Passed · **Verified Date:** `2026-10-31`

---

## Level 1 — Automated Terminal, Unit & Architecture Checks (AI Executes)

- [x] **Kotlin Compile Check:** `./gradlew compileDebugKotlin` passes with 0 errors.
- [x] **Stack Hard-Failure Audit (Pure-Kotlin Core):** `:core` module contains zero imports matching `android.*` or `androidx.*` (verified via AST/grep audit).
- [x] **Unit Tests (Tier 1 — Mandatory):** `./gradlew testDebugUnitTest` passes for all core motion, ui, and feature tests (18 core tests + 3 ui tests + 3 feature tests).
- [x] **Merged Manifest Permissions Allowlist:** `./gradlew checkMergedManifestPermissions` passes with strictly approved CAMERA permission only.

---

## Level 2 — Functional & EARS Requirement Verification (AI / Dev Smoke Test)

| Check ID | Requirement Ref | Scenario (WHEN / IF) | Expected Outcome (THE System SHALL) | Status |
| :--- | :--- | :--- | :--- | :--- |
| **UAT-1.1** | `Req 1.1` | App displays companion faceplate screen | Left and right eyes render symmetrically on true OLED-black background (`#000000`). | `[x]` |
| **UAT-1.2** | `Req 1.2` | Gaze target coordinates are provided or updated | Gaze offsets shift pupil rendering smoothly within normalized bounds `[-1.0, 1.0]`. | `[x]` |
| **UAT-1.3** | `Req 1.3` | Display frame rendering cycle (60/90 Hz) | Canvas draw pass completes without per-frame heap allocations or frame jitter. | `[x]` |
| **UAT-2.1** | `Req 2.1` | Idle state without external gaze input | Spontaneous natural blinks trigger every 3.0 to 7.0 seconds with natural easing (150–200 ms). | `[x]` |
| **UAT-2.2** | `Req 2.2` | Idle state without external gaze input | Subtle micro-saccades shift eye gaze slightly every 1.5 to 4.0 seconds. | `[x]` |
| **UAT-2.3** | `Req 2.3` | Reduced motion is active in settings / system | Micro-saccades are suppressed (zero offset), and eyelid transitions use instantaneous or soft fade. | `[x]` |
| **UAT-3.1** | `Req 3.1` | Desk companion mode enabled in settings | Activity sets `FLAG_KEEP_SCREEN_ON` window flag while EyeKage is active in foreground. | `[x]` |
| **UAT-3.2** | `Req 3.2` | Desk companion mode disabled in settings | Activity clears `FLAG_KEEP_SCREEN_ON` window flag. | `[x]` |

---

## Level 3 — Visual Fidelity (`design-reference/`) & Human UX Sign-Off

- [x] **Visual Reference Match:** Built UI matches cyan glowing pill eyes from `design-reference/stitch-prompt.md` and `docs/foundation/design-system.md` tokens.
- [x] **OLED Blackness:** Background is `#000000` with zero stray background borders or light leakage.
- [x] **Liveliness & Polish:** Eyes feel alive, organically responsive, and friendly on screen.
