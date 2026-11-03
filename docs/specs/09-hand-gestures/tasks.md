# ✅ Feature Tasks: Hand Gesture Recognition & Interactive Reactions (`09-hand-gestures`)

> **Phase / Wave:** `Phase 3 (Wave 7)` · **Branch / Owner:** `feat/09-hand-gestures` · **Status:** `[ ]` Planned  
> **Depends On:** `08-head-pose-posture`, `06-personality-memory` · **Touches:** `:core:model`, `:core:behavior`, `:core:ports`, `:platform:vision`, `:platform:preferences`, `:ui:eyes`, `:feature:companion`, `:feature:settings`, `:app`  
> **Spec Links:** [`requirements.md`](./requirements.md) · [`design.md`](./design.md) · [`uat.md`](./uat.md) · [`design-reference/`](./design-reference/)

---

## 🔄 Implementation Tasks

### 1. Pure-Kotlin Core Models, Ports & Gesture Reaction Engine (`⚡ BATCH`: Core Domain & Logic)
- [x] **1.1** `[BE]` Create domain models in `core/src/main/kotlin/com/eyekage/core/model/`:
  - `HandGesture.kt`: Enum of supported macroscopic gestures (`NONE`, `WAVE`, `PEACE_SIGN`, `THUMBS_UP`, `CLOSED_FIST`, `FLAT_PALM_STOP`) and `GestureSensitivity` (`LOW`, `NORMAL`, `HIGH`)
  - `HandObservation.kt`: Data class with `gesture`, `confidence`, `normalizedCenterX`, `normalizedCenterY`, `boundingBoxArea`, and `timestampNanos`
  - `HandGestureReaction.kt`: Sealed hierarchy (`WaveGreeting`, `PeaceWink`, `ThumbsUpApproval`, `FistResolve`, `HoldStop`, `None`)
  - _Requirements: 1.1, 1.2, 1.3_
- [x] **1.2** `[BE]` Create port interfaces in `core/src/main/kotlin/com/eyekage/core/ports/`:
  - `HandGestureSource.kt`: Interface exposing `val handGestures: StateFlow<HandObservation>` and `val isTracking: StateFlow<Boolean>`
  - `FakeHandGestureSource.kt`: Test double emitting deterministic hand observations and tracking states
  - _Requirements: 1.4_
- [x] **1.3** `[BE]` Implement `HandGestureReactionEngine.kt` in `core/src/main/kotlin/com/eyekage/core/behavior/`:
  - State machine with configurable confirmation window ($100\text{--}250\text{ ms}$) and re-trigger cooldown timer ($1.5\text{ seconds}$)
  - Sensitivity filtering by minimum confidence threshold
  - _Requirements: 1.5, 1.6, 1.7_
- [x] **1.4** `[BE]` Write unit tests in `core/src/test/kotlin/com/eyekage/core/behavior/HandGestureReactionEngineTest.kt`:
  - Test confirmation debounce, cooldown suppression, sensitivity clamping, and observation decay
  - _Requirements: 1.5, 1.6, 1.7_
- [x] **1.5** `[REVIEW]` **Core Checkpoint:** Run `./gradlew :core:test` — all unit tests pass with zero errors.

---

### 2. Platform Vision Source, Preferences & DI (`⚡ BATCH`: Platform Vision & Persistence)
- [x] **2.1** `[BE]` Update `PreferencesRepository.kt` and `DataStorePreferencesRepository.kt`:
  - Persists `gesture_recognition_enabled` (boolean, default `true`) and `gesture_sensitivity` (string, default `"NORMAL"`) in Jetpack Preferences DataStore
  - _Requirements: 4.1_
- [x] **2.2** `[BE]` Implement `VisionHandGestureSource.kt` in `platform/src/main/kotlin/com/eyekage/platform/vision/`:
  - Connects to front camera frame analysis loop, maps normalized bounding coordinates to centered `[-1.0, 1.0]` space, and emits `HandObservation`
  - Enforces zero camera frame or handprint persistence and lifecycle-aware cleanup
  - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5_
- [x] **2.3** `[BE]` Provide `HandGestureSource` singleton in `app/src/main/kotlin/com/eyekage/di/AppContainer.kt`:
  - Wires `VisionHandGestureSource` into the application composition root
  - _Requirements: 5.1, 5.2_
- [x] **2.4** `[BE]` Write automated tests in `platform/src/test/kotlin/com/eyekage/platform/vision/VisionHandGestureSourceTest.kt`:
  - Test coordinate normalization, lifecycle release, and empty frame fallbacks
  - _Requirements: 2.1–2.5_
- [x] **2.5** `[REVIEW]` **Platform Checkpoint:** Run `./gradlew :platform:test` — must pass with zero errors.

---

### 3. Eyes Canvas Reactions & Companion Screen HUD (`⚡ BATCH`: Companion Expressions & Feedback)
- [ ] **3.1** `[FE]` Update `RobotEyesCanvas.kt` in `ui/src/main/kotlin/com/eyekage/ui/eyes/`:
  - Adds asymmetrical left/right eye openness modifiers (`leftEyeOpennessMultiplier`, `rightEyeOpennessMultiplier`) to support natural winks
  - _Requirements: 3.2_
- [ ] **3.2** `[FE]` Update `CompanionViewModel.kt` in `feature/src/main/kotlin/com/eyekage/feature/companion/`:
  - Injects `HandGestureSource` and `HandGestureReactionEngine`
  - Dispatches procedural reactions: Wave (happy crescent + double blink + memory increment), Peace (playful wink), Thumbs Up (affirmative bounce + memory increment), Fist (focused pupil scale), Hold (frozen saccades)
  - Manages active gesture feedback chip state with 1.8s auto-dismiss
  - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5, 3.6_
- [ ] **3.3** `[FE]` Update `CompanionScreen.kt` in `feature/src/main/kotlin/com/eyekage/feature/companion/`:
  - Mounts animated emerald Active Gesture Feedback chip in top HUD bar when a gesture is recognized
  - _Requirements: 3.4, 3.6_
- [ ] **3.4** `[FE]` Write unit tests in `feature/src/test/kotlin/com/eyekage/feature/companion/CompanionViewModelGestureTest.kt`:
  - Verify gesture reaction dispatch, memory increment on wave/thumbs-up, wink trigger, and HUD chip emission
  - _Requirements: 3.1–3.6_
- [ ] **3.5** `[REVIEW]` **Companion Checkpoint:** Run `./gradlew compileDebugKotlin` — must pass with zero warnings or errors.

---

### 4. Settings Screen, Card & Final Verification (`⚡ BATCH`: Settings & Verification)
- [ ] **4.1** `[FE]` Implement `HandGestureSettingsCard.kt` in `feature/src/main/kotlin/com/eyekage/feature/settings/`:
  - AMOLED dark card matching design system tokens (`#0A0A12`, `#26263A`, `#22D3EE`, `#10B981`)
  - Features opt-in switch, real-time live detection pill, sensitivity chips (`Low`, `Normal`, `High`), and privacy shield disclosure
  - _Requirements: 4.2, 4.3, 4.4, 4.5_
- [ ] **4.2** `[FE]` Update `SettingsScreen.kt` and `SettingsViewModel.kt`:
  - Mounts `HandGestureSettingsCard` directly beneath `PostureSettingsCard`
  - Wires preference toggles and sensitivity chip selections to `PreferencesRepository`
  - _Requirements: 4.1, 4.2, 4.3_
- [ ] **4.3** `[FE]` Write unit tests in `feature/src/test/kotlin/com/eyekage/feature/settings/HandGestureSettingsCardTest.kt`:
  - Verify card UI interactions, switch toggles, sensitivity persistence, and live status pill
  - _Requirements: 4.1–4.5_
- [ ] **4.4** `[REVIEW]` **Final Feature Checkpoint:** Run `./gradlew checkMergedManifestPermissions testDebugUnitTest` — all checks must pass with zero errors (triggers automatic `/ship` & roadmap sync!).

---

## 🔀 Task Dependency Graph (Intra-Feature Waves)

```json
{
  "waves": [
    { "id": 0, "tasks": ["1.1", "1.2", "1.3"] },
    { "id": 1, "tasks": ["1.4", "1.5"] },
    { "id": 2, "tasks": ["2.1", "2.2", "2.3"] },
    { "id": 3, "tasks": ["2.4", "2.5"] },
    { "id": 4, "tasks": ["3.1", "3.2", "3.3"] },
    { "id": 5, "tasks": ["3.4", "3.5"] },
    { "id": 6, "tasks": ["4.1", "4.2"] },
    { "id": 7, "tasks": ["4.3", "4.4"] }
  ]
}
```

---

## 🐛 Ad-Hoc / Small Enhancements
*(None yet)*

---

## 🧪 Feature Verification & UAT Gate
- [ ] **Level 1 (Automated Build & Unit Tests):** Typecheck, unit tests (`testDebugUnitTest`), and privacy guard (`checkMergedManifestPermissions`) pass with zero errors
- [ ] **Level 2 & 3 (UAT & Visual Match):** `/verify` completed in [`uat.md`](./uat.md)

---

## 🛑 Blocked / Waiting
*(None)*

---

## 📝 Feature Activity Log

| Date | Task | Action & Files Touched |
| :--- | :--- | :--- |
| 2026-11-01 | Init | Feature spec bundle created for `09-hand-gestures`. |
| 2026-11-03 | Batch 1 (1.1-1.5) | Implemented pure-Kotlin domain models, ports, `HandGestureReactionEngine`, and unit tests. All passed. |
| 2026-11-03 | Batch 2 (2.1-2.5) | Implemented `VisionHandGestureSource`, DataStore preferences, `AppContainer` DI, and platform tests. Passed. |
