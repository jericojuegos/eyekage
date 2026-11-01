# ✅ Phase 1 MVP Acceptance Checklist — EyeKage

This document is the **single source of truth** for verifying the Phase 1 MVP. Every checkbox must be validated on the physical baseline device (**RMX3710 / realme C55**) before Phase 1 is marked complete.

---

## 1. User Experience & Animation Fidelity

- [x] **Initial Launch:** The app launches directly into dark mode displaying animated robot eyes. Zero runtime permissions are requested at startup.
- [x] **Ambient Liveliness:** Spontaneous blinks, micro-saccades, and idle breathing animations run continuously, whether the camera is enabled or disabled.
- [x] **In-Context Permission:** Tapping "Look at me" prompts for camera permission with an in-app explanatory disclosure. If denied, the app remains fully functional with touch and idle animations.
- [x] **Awakening Reaction:** When a face is first detected, an amber light burst overlay activates dramatically, then smoothly transitions into the active eye mode.
- [x] **Smooth Gaze Tracking:** Pupils follow head movements left, right, up, and down across both **Portrait** and **Landscape** orientations, correctly handling front-camera mirroring.
- [x] **Face Lost & Recovery:** When the user leaves the frame or obscures the camera, the eyes enter a brief searching saccade, then return to idle without jarring visual pops.
- [x] **Mode Switching:** All 4 modes (`mode.normal`, `mode.crimson_tracker`, `mode.white_scan`, `mode.purple_orbit`) can be selected from the drawer and switch instantaneously.
- [x] **Touch & Poke Reactions:** Tapping or poking the eyes triggers playful squint/blink reactions accompanied by subtle permission-free haptics. Works with camera off.
- [x] **Optional Desk Companion Mode:** Enabling "Desk Mode" in settings keeps the screen awake while EyeKage is in the foreground. Exits cleanly when toggled off.
- [x] **Reduced Motion Support:** Respects system "Remove Animations" (or in-app setting), eliminating rapid strobe effects and micro-saccades while retaining smooth gaze tracking.

---

## 2. Real-Device Performance Targets (RMX3710 Baseline)

*Note: Targets are engineering benchmarks to be measured with Macrobenchmark / JankStats, not unverified promises.*

- [x] **Rendering Frame Rate:** Operates at $\ge 60\text{ fps}$ (or panel native $90\text{ Hz}$) with $< 2\%$ dropped frames during continuous gaze tracking (zero heap allocation in draw loops).
- [x] **Tracking Latency:** Motion-to-pupil response latency is measured at $\le 150\text{ ms}$ (p95) via One-Euro filter.
- [x] **30-Minute Thermal & Battery Stability:** App runs continuously for 30 minutes on the RMX3710 with camera active:
  - Thermal status does not enter severe throttling.
  - No out-of-memory (OOM) crashes or memory leaks.
  - Battery consumption remains within normal camera-use parameters.
- [x] **Decoupled Pipeline:** Artificially throttling face detection to $5\text{ Hz}$ does not drop eye rendering frame rate below $60\text{ fps}$.

---

## 3. Privacy, Security & Manifest Verification

- [x] **Manifest Allowlist:** Merged debug and release APK manifests contain ONLY `android.permission.CAMERA`.
- [x] **No Network Leaks:** Automated CI verification confirms `INTERNET` and `ACCESS_NETWORK_STATE` are absent. The app passes a fresh install in Airplane Mode.
- [x] **Zero Image Persistence:** Automated filesystem scan confirms zero camera frames, cropped faces, or biometric vector embeddings are written to disk.
- [x] **Visible Privacy Indicator:** A dedicated in-app camera indicator illuminates green only when the camera pipeline is actively capturing frames.
- [x] **Lifecycle Shutdown:** Camera pipeline releases hardware immediately when the app moves to background (`onStop`). Zero background execution.
- [x] **Settings Disclosure:** The settings screen provides a transparent, plain-English summary of what data is stored (only the 5 preference keys).

---

## 4. Engineering & Code Quality

- [x] **Pure Core Isolation:** Module `:core` has zero imports from `android.*`, `androidx.*`, Compose, CameraX, or ML Kit.
- [x] **Automated CI Green:** `./gradlew check testDebugUnitTest detekt lintDebug checkMergedManifestPermissions` passes with zero warnings-as-errors.
- [x] **Trademark Protection:** Anime trademarks (Sharingan, Byakugan, Rinnegan) are confined strictly to `mode_labels.xml`. All Kotlin code uses neutral IDs.
- [x] **Detector ADR Documented:** The Phase 0 comparison spike between MediaPipe and ML Kit is documented in `docs/foundation/decisions.md` with empirical benchmark data.

