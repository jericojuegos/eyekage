# ✅ Feature Tasks: Head Pose & Desk Posture Awareness (`08-head-pose-posture`)

> **Phase / Wave:** `Phase 3 (Wave 6)` · **Branch / Owner:** `feat/08-head-pose-posture` · **Status:** `[ ]` Spec Ready  
> **Depends On:** `04-gaze-tracking-reactions`, `06-personality-memory` · **Touches:** `:core:model`, `:core:behavior`, `:core:ports`, `:platform:vision`, `:platform:preferences`, `:ui:eyes`, `:feature:companion`, `:feature:settings`, `:app`  
> **Spec Links:** [`requirements.md`](./requirements.md) · [`design.md`](./design.md) · [`uat.md`](./uat.md) · [`design-reference/stitch-prompt.md`](./design-reference/stitch-prompt.md)

---

## 🔄 Implementation Tasks

### 1. Pure-Kotlin Core Models, Geometry & Behavior (`⚡ BATCH`: Domain & Behavioral Engines)
- [x] **1.1** `[BE]` Create `HeadPose.kt` and `PostureObservation.kt` in `core/src/main/kotlin/com/eyekage/core/model/`
  - Defines `HeadPose` value class (`pitchDegrees`, `yawDegrees`, `rollDegrees`), `ProximityZone` enum, `PostureState` enum, and `PostureObservation` data model
  - _Requirements: 1.1, 1.3_
- [x] **1.2** `[BE]` Implement `HeadPoseTransform.kt` in `core/src/main/kotlin/com/eyekage/core/model/`
  - Pure-Kotlin geometric estimation of Euler angles and distance ratios from 6 facial landmarks with rotation and mirroring transforms
  - _Requirements: 1.2, 1.5_
- [x] **1.3** `[BE]` Create `PostureObservationSource.kt` and `FakePostureObservationSource.kt` in `core/src/main/kotlin/com/eyekage/core/ports/`
  - Defines pure-Kotlin port interface streaming `StateFlow<PostureObservation>` with zero Android SDK dependencies and test double
  - _Requirements: 1.4_
- [x] **1.4** `[BE]` Implement `HeadPoseReactionEngine.kt` in `core/src/main/kotlin/com/eyekage/core/behavior/`
  - Pure-Kotlin gesture recognizer for curious head tilt trigger, affirmative nod cycle detection, and turned-away pause
  - _Requirements: 3.1, 3.2, 3.3_
- [x] **1.5** `[BE]` Implement `PostureMonitor.kt` in `core/src/main/kotlin/com/eyekage/core/behavior/`
  - Pure-Kotlin temporal tracker for continuous slouch duration with configurable debounced alert emission and instant clear
  - _Requirements: 3.4, 3.5_
- [x] **1.6** `[BE]` Write automated unit tests in `core/src/test/kotlin/com/eyekage/core/model/HeadPoseTransformTest.kt`, `core/src/test/kotlin/com/eyekage/core/behavior/HeadPoseReactionEngineTest.kt`, and `core/src/test/kotlin/com/eyekage/core/behavior/PostureMonitorTest.kt`
  - Tests mathematical accuracy, angle limits $[-90^\circ, 90^\circ]$, nod recognition, tilt hysteresis, and slouch debounce timing
  - _Requirements: 1.1, 1.2, 1.5, 3.1, 3.2, 3.4_
- [x] **1.7** `[REVIEW]` **Core & Unit Test Checkpoint:** Run `./gradlew :core:test` — must pass with zero errors (triggers auto-checkpoint commit in `/build`)

---

### 2. Platform Vision Source, Preferences & DI (`⚡ BATCH`: Platform Vision & Persistence)
- [x] **2.1** `[BE]` Update `PreferencesRepository.kt` and `DataStorePreferencesRepository.kt`
  - Persists `posture_reminder_enabled` (boolean, default `false`) and `posture_slouch_threshold_sec` (int, default `30`) in Jetpack Preferences DataStore
  - _Requirements: 4.1_
- [x] **2.2** `[BE]` Implement `VisionHeadPoseSource.kt` in `platform/src/main/kotlin/com/eyekage/platform/vision/`
  - Extracts 6 MediaPipe face keypoints from `VisionFaceObservationSource` / CameraX loop, applies 4-channel `OneEuroFilter`, and emits `PostureObservation`
  - Guarantees zero disk persistence and lifecycle-aware shutdown
  - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5_
- [x] **2.3** `[BE]` Provide `PostureObservationSource` singleton in `app/src/main/kotlin/com/eyekage/di/AppContainer.kt`
  - Wires `VisionHeadPoseSource` into the composition root
  - _Requirements: 2.1_
- [x] **2.4** `[BE]` Write automated tests in `platform/src/test/kotlin/com/eyekage/platform/vision/VisionHeadPoseSourceTest.kt`
  - Tests filter smoothing, lifecycle start/stop, and empty observation handling
  - _Requirements: 2.1, 2.2, 2.5_
- [x] **2.5** `[REVIEW]` **Platform & Manifest Checkpoint:** Run `./gradlew checkMergedManifestPermissions compileDebugKotlin` — must pass with zero errors (triggers auto-checkpoint commit in `/build`)

---

### 3. Companion UI 3D Eye Tilt, Reactions & HUD Posture Alert (`⚡ BATCH`: Companion Screen & Eye Canvas)
- [x] **3.1** `[FE]` Update `RobotEyesCanvas.kt` in `ui/src/main/kotlin/com/eyekage/ui/eyes/`
  - Adds `eyeTiltDegrees: Float` parameter to canvas rendering, applying smooth angular rotation and organic eyebrow squint to mirror user head roll
  - Preserves zero per-frame allocation rule in `DrawScope`
  - _Requirements: 3.1_
- [x] **3.2** `[FE]` Wire `PostureObservationSource`, `HeadPoseReactionEngine`, and `PostureMonitor` into `CompanionViewModel.kt`
  - Listens to posture observations, updates eye tilt angle, acknowledges affirmative nods with $+1$ interaction to `MemoryRepository`, and evaluates slouch alert state
  - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5_
- [x] **3.3** `[FE]` Update `CompanionScreen.kt` with subtle Posture Alert Badge
  - Renders amber posture icon (🧘 / ⚠️) in the top HUD when slouching is detected; tap clears the alert
  - _Requirements: 3.4, 3.5_
- [x] **3.4** `[FE]` Write automated unit tests in `feature/src/test/kotlin/com/eyekage/feature/companion/CompanionViewModelPostureTest.kt`
  - Tests head tilt propagation, affirmative nod reaction, turned-away gaze pause, and slouch alert emission
  - _Requirements: 3.1, 3.2, 3.3, 3.4_
- [x] **3.5** `[REVIEW]` **Companion Screen Checkpoint:** Run `./gradlew :feature:testDebugUnitTest :ui:testDebugUnitTest` — must pass with zero errors (triggers auto-checkpoint commit in `/build`)

---

### 4. Settings Posture Card & Full Verification (`⚡ BATCH`: Settings & Verification)
- [ ] **4.1** `[FE]` Build `PostureSettingsCard.kt` in `feature/src/main/kotlin/com/eyekage/feature/settings/`
  - AMOLED dark card matching design reference: opt-in toggle switch, live posture badge ("● Balanced" / "▲ Slouching"), reminder sensitivity chips (15s, 30s, 45s), and privacy shield
  - _Requirements: 4.2, 4.3, 4.4_
- [ ] **4.2** `[FE]` Integrate `PostureSettingsCard` into `SettingsScreen.kt` and `SettingsViewModel.kt`
  - Mounts directly beneath `VoiceCommandsCard` with reactive state flows
  - _Requirements: 4.2_
- [ ] **4.3** `[FE]` Write automated unit tests in `feature/src/test/kotlin/com/eyekage/feature/settings/PostureSettingsCardTest.kt`
  - Tests toggle interaction, sensitivity chip selection, and DataStore state reflection
  - _Requirements: 4.1, 4.2, 4.3_
- [ ] **4.4** `[REVIEW]` **Final Feature Checkpoint:** Run full verification (`./gradlew testDebugUnitTest checkMergedManifestPermissions compileDebugKotlin`) -> triggers `/ship` & roadmap sync!
  - _Requirements: All_

---

## 🔀 Task Dependency Graph (Intra-Feature Waves)

```json
{
  "waves": [
    { "id": 1, "tasks": ["1.1", "1.2", "1.3", "1.4", "1.5", "1.6", "1.7"] },
    { "id": 2, "tasks": ["2.1", "2.2", "2.3", "2.4", "2.5"] },
    { "id": 3, "tasks": ["3.1", "3.2", "3.3", "3.4", "3.5"] },
    { "id": 4, "tasks": ["4.1", "4.2", "4.3", "4.4"] }
  ]
}
```

---

## 📝 Feature Activity Log

| Timestamp | Task | Action / Summary | Result |
|---|---|---|---|
| 2026-11-01 19:40 | Spec Init | Created specification bundle for `08-head-pose-posture` | Ready |
| 2026-11-01 20:07 | Batch 1 (1.1-1.7) | Implemented pure-Kotlin head pose models, transform, ports, gesture engines & unit tests | Passed |
| 2026-11-01 20:14 | Batch 2 (2.1-2.5) | Implemented VisionHeadPoseSource, DataStore posture preferences, AppContainer DI, and verified manifest allowlist | Passed |
| 2026-11-01 20:26 | Batch 3 (3.1-3.5) | Wired CompanionViewModel posture events, eye tilt canvas rotation, and HUD posture badge | Passed |
