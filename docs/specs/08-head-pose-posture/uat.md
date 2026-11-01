# 🧪 Feature UAT & Verification: Head Pose & Desk Posture Awareness (`08-head-pose-posture`)

> **Feature Slug:** `08-head-pose-posture` · **Spec Links:** [`requirements.md`](./requirements.md) · [`design.md`](./design.md) · [`tasks.md`](./tasks.md) · [`design-reference/stitch-prompt.md`](./design-reference/stitch-prompt.md)  
> **Overall Status:** `[x]` Verified & Shipped · **Target Phase:** `Phase 3 (Wave 6)`

---

## Level 1 — Automated Terminal, Unit & Manifest Checks (AI Executes)

- [x] **Kotlin Compile & Typecheck:** `./gradlew compileDebugKotlin` succeeds with zero errors or warnings.
- [x] **Unit Tests (Core & Feature):** `./gradlew testDebugUnitTest` passes for all domain, platform, and feature tests.
- [x] **Strict Manifest Allowlist Guard:** `./gradlew checkMergedManifestPermissions` verifies only approved permissions exist (`CAMERA`, `RECORD_AUDIO`) and fails if `INTERNET` or `ACCESS_NETWORK_STATE` is present.
- [x] **Pure-Kotlin Architecture Rule:** `:core` module contains zero imports matching `android.*` or `androidx.*`.
- [x] **Zero Biometric / Frame I/O:** No writes to disk, cache, or database for camera frames, crops, or landmarks.
- [x] **Canvas Allocation Rule:** Zero allocations in `RobotEyesCanvas.kt` DrawScope per-frame rendering loop.

---

## Level 2 — Functional & EARS Requirement Verification (Smoke Tests)

| Check ID | Requirement Ref | Scenario (WHEN / IF) | Expected Outcome (THE System SHALL) | Status |
| :--- | :--- | :--- | :--- | :--- |
| **UAT-1.1** | `Req 1.1, 1.2` | Face landmarks detected with lateral tilt | `HeadPoseTransform` computes accurate `rollDegrees` proportional to tilt | `[x]` |
| **UAT-1.2** | `Req 1.2, 1.5` | Camera frames obscured or face absent | System emits `PostureObservation.Neutral` without crashing or throwing NaN | `[x]` |
| **UAT-2.1** | `Req 2.1, 2.2` | Camera stream active | `VisionHeadPoseSource` outputs One-Euro smoothed `PostureObservation` at $\ge 15\text{ Hz}$ | `[x]` |
| **UAT-2.4** | `Req 2.4` | Continuous tracking active | Zero image files, bitmaps, or landmark arrays saved to disk | `[x]` |
| **UAT-3.1** | `Req 3.1` | User tilts head by $\ge 12^\circ$ | Robot eyes and eyebrows smoothly tilt up to $\pm 15^\circ$ inquisitively | `[x]` |
| **UAT-3.2** | `Req 3.2` | User nods affirmatively (down and return within 1.2s) | Eyes execute affirmative nod blink reaction and award $+1$ interaction to memory | `[x]` |
| **UAT-3.3** | `Req 3.3` | User turns head away ($|\text{yaw}| \ge 25^\circ$) | Active direct gaze lock pauses smoothly; pupils remain centered without strain | `[x]` |
| **UAT-3.4** | `Req 3.4` | User slouches continuously for $\ge 30\text{s}$ with reminders enabled | Amber posture alert badge appears in HUD; eyes show concerned micro-expression | `[x]` |
| **UAT-3.5** | `Req 3.5` | User corrects posture or taps HUD posture badge | Amber alert badge dismisses immediately and eyes return to normal | `[x]` |
| **UAT-4.1** | `Req 4.1, 4.2` | User toggles "Desk Posture Reminders" in Settings | Preference persisted in DataStore; active state reflected reactively | `[x]` |
| **UAT-4.3** | `Req 4.3` | User selects "Strict (15s)" sensitivity chip | Slouch threshold updated to 15 seconds in preferences and `PostureMonitor` | `[x]` |

---

## Level 3 — Visual Fidelity & Human UX Sign-Off

- [x] **Curious Head Tilt Match:** Eyes tilt smoothly in synchrony with user's head tilt without jitter or sudden flips.
- [x] **Companion HUD Alert Badge:** Amber posture alert badge displays neatly in the top control bar without obstructing eye animations.
- [x] **Settings Posture Card:** AMOLED dark surface card matches `docs/foundation/design-system.md` with glowing posture icon, live status badge, sensitivity chips, and emerald privacy badge.
- [x] **Zero Network Integrity:** App runs in complete airplane mode with zero network calls.
