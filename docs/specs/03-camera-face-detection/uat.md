# 🧪 Feature UAT & Verification: On-Device Vision Pipeline (`03-camera-face-detection`)

> **Feature Slug:** `03-camera-face-detection` · **Spec Links:** [`requirements.md`](./requirements.md) · [`design.md`](./design.md) · [`tasks.md`](./tasks.md) · [`design-reference/`](./design-reference/)  
> **Overall Status:** `[x]` Passed · **Verified Date:** `2026-10-31`

---

## Level 1 — Automated Terminal, Unit & CI Checks (AI Executes)

- [x] **Kotlin Compile & Typecheck:** `./gradlew compileDebugKotlin` passes with zero errors and zero warnings.
- [x] **Core Unit Tests:** `./gradlew :core:test` passes all coordinate normalization, transformation, and `FaceObservation` tests.
- [x] **Platform Unit Tests:** `./gradlew :platform:testDebugUnitTest` passes.
- [x] **Privacy & Manifest Allowlist Guard:** `./gradlew checkMergedManifestPermissions` passes with only `CAMERA` allowed (zero network/storage permissions).
- [x] **Stack Hard-Failure Audit:**
  - [x] `:core` module contains ZERO imports matching `android.*` or `androidx.*`.
  - [x] No image frames, bitmaps, or face embeddings persisted to disk or SQLite.
  - [x] No allocations in Canvas render loops.

---

## Level 2 — Functional & EARS Requirement Verification (AI / Dev Smoke Test)

| Check ID | Requirement Ref | Scenario (WHEN / IF) | Expected Outcome (THE System SHALL) | Status |
| :--- | :--- | :--- | :--- | :--- |
| **UAT-1.1** | `Req 1.1` | Camera starts with `start()` | Binds headless `ImageAnalysis` targeting front-facing camera with `STRATEGY_KEEP_ONLY_LATEST` and ~480p resolution | `[x]` |
| **UAT-1.2** | `Req 1.2` | Activity/Lifecycle reaches `ON_STOP` or `ON_PAUSE` | Camera unbinds and releases hardware camera sensor immediately via `DefaultLifecycleObserver` | `[x]` |
| **UAT-1.3** | `Req 1.3` | `ImageAnalysis.Analyzer` receives frame | Closes `ImageProxy` immediately in `finally` block to prevent camera pipeline starvation | `[x]` |
| **UAT-2.1** | `Req 2.1` | Camera analysis pipeline is running (`isRunning == true`) | UI renders green privacy indicator (`#10B981`) in top bar | `[x]` |
| **UAT-2.2** | `Req 2.2` | Camera analysis pipeline is stopped (`isRunning == false`) | UI renders slate privacy indicator (`#475569`) | `[x]` |
| **UAT-2.3** | `Req 2.3` | Camera permission is denied or camera unavailable | System logs warning and falls back gracefully to idle/touch eye animations without crashing | `[x]` |
| **UAT-3.1** | `Req 3.1` | Face is detected in front of camera | Emits `FaceObservation` with $(x, y) \in [-1.0, 1.0]$ where $(0.0, 0.0)$ is center, and confidence $\in [0.0, 1.0]$ | `[x]` |
| **UAT-3.2** | `Req 3.2` | Face moves across camera field of view | Normalized coordinates are mirrored for front camera so user moving left yields $x < 0$ | `[x]` |

---

## Level 3 — Visual Fidelity (`design-reference/`) & Human UX Sign-Off

- [x] **Privacy Indicator Visual Match:** Built privacy dot in header matches `#10B981` (active) and `#475569` (inactive) specified in `docs/foundation/design-system.md` and `stitch-prompt.md`.
- [x] **Zero Camera Preview Displayed:** App screen is pure OLED black (`#000000`) with companion eye/faceplate; raw camera video feed is strictly invisible and headless.
- [x] **Sign-Off:** Approved to mark `[x]` in `docs/planning/roadmap.md` via `/ship`.
