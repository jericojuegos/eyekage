# 📜 Project History & Pattern Evolution — EyeKage

## Phase 0: Initialization & Brainstorming (2026-10-31)

### Milestones Completed
- Completed 7-round BFD brainstorm in Personal context (`Mode 1: Own Idea`).
- Defined core product scope: intelligent virtual robot companion with expressive animated eyes, on-device face tracking, and data-driven eye modes.
- Established the "Portable Core, Platform Shell" architecture separating pure-Kotlin domain logic from Android CameraX/Compose.
- Gated face detector selection to an empirical Phase 0 comparison between MediaPipe Tasks Vision and Bundled ML Kit.
- Upgraded `master-brains` with the new reusable `android-kotlin-compose` stack pack (`v2.1`).
- Initialized EyeKage project brain files and synchronized `.agents/{rules,skills}` engine.

---

## Phase 1: Android Companion MVP (2026-11-01)

### Milestones Completed
- **`00-scaffold`:** 5-module modular architecture (`:app`, `:core`, `:ui`, `:platform`, `:feature`), custom Gradle manifest permission guard, MediaPipe vs. ML Kit comparison spike.
- **`01-eye-engine`:** Procedural Compose Canvas robot eyes with smooth saccades, micro-jitters, natural blinks, and desk companion mode.
- **`03-camera-face-detection`:** Decoupled CameraX `ImageAnalysis` front-camera face detector with green LED privacy dot.
- **`02-eye-modes`:** Data-driven eye palette registry with 4 neutral modes, awakening overlay, and interactive bottom drawer.
- **`04-gaze-tracking-reactions`:** One-Euro filter smoothed gaze target tracking, touch poke reactions, awakening state machine.
- **`05-settings-privacy`:** Jetpack DataStore preferences, AMOLED dark settings screen, zero-network security disclosures.

---

## Phase 2: Voice & Personality Engine (2026-11-01)

### Milestones Completed
- **`06-personality-memory`:** Deterministic pure-Kotlin personality engine (`curiosity`, `playfulness`, `energy`), micro-expressions (eyelid arch, saccade velocity, pupil dilation), persistent local memory DataStore with 5 bond tiers, and inspectable/resettable memory card.
- **`07-voice-commands`:** On-device speech recognition via Android platform `SpeechRecognizer` (`EXTRA_PREFER_OFFLINE`), pure-Kotlin `VoiceCommandParser` synonym matcher, glowing amber microphone privacy indicator in companion top bar, AMOLED voice commands settings card with active status pill and supported phrase chips.
- Strict manifest auditing enforcing zero-network policy (`RECORD_AUDIO` allowed for local speech recognition; `INTERNET` and `ACCESS_NETWORK_STATE` strictly prohibited; zero audio persistence to disk).

---

## New Patterns Identified (Pending Upstream Promotion)

The following architectural patterns were originated in EyeKage and logged for future promotion in `INVENTORY.md`:

1. **`android-merged-manifest-guard`:**
   - *Description:* Custom Gradle task that parses merged debug and release manifests and fails CI if unapproved permissions, services, or receivers are introduced by dependencies.
   - *Target:* `stacks/android-kotlin-compose/skills/build/examples/ManifestPermissionGuardTask.gradle.kts.template` (promoted in Phase 0).
2. **`android-on-device-vision-pipeline`:**
   - *Description:* Decoupled CameraX `ImageAnalysis` (headless, low-res) feeding a pure-Kotlin `FaceObservationSource` port with normalized coordinates.
   - *Target:* Reusable stack pack pattern after Phase 1 validation.
3. **`data-driven-mode-skin-registry`:**
   - *Description:* Decoupling dynamic visual skins/modes using neutral internal IDs and swappable localized resource overlays to insulate code from trademark and branding pivots.
   - *Target:* Project-specific (`EyeKage/docs/foundation/`).
4. **`procedural-compose-canvas-eyes`:**
   - *Description:* High-performance Compose Canvas rendering with time-based frame clock and One-Euro jitter smoothing.
   - *Target:* Project-specific (`EyeKage/docs/foundation/`).

