# ✅ Feature Tasks: Gaze Tracking & Reactions (`04-gaze-tracking-reactions`)

> **Phase / Wave:** `Phase 1 (Wave 2)` · **Branch / Owner:** `feat/04-gaze-tracking-reactions` · **Status:** `[ ]` Spec Ready  
> **Depends On:** `01-eye-engine`, `03-camera-face-detection` · **Touches:** `:core:behavior`, `:core:motion`, `:feature:companion`

---

## 🔄 Implementation Tasks

### 1. Behavior State Machine & Smoothing (`⚡ BATCH`: Core Behavior Logic)
- [ ] **1.1** `[BE]` Implement `CompanionBehaviorStateMachine` in `:core:behavior` with states: `Idle`, `Awakening`, `Tracking`, `Searching`, `Poked`.
  - _Requirements: 2.1, 2.2, 3.1_
- [ ] **1.2** `[BE]` Wire `OneEuroFilter` to dynamic face observations in `:core:motion`.
  - _Requirements: 1.1, 1.2_
- [ ] **1.3** `[BE]` Write pure-Kotlin unit tests verifying state transitions, timeout fallbacks, and smoothing behavior using `FakeFaceObservationSource`.
  - _Validates: Req 1.2, 2.1, 2.2, 3.1_
- [ ] **1.4** `[REVIEW]` **Behavior Checkpoint:** `./gradlew :core:testDebugUnitTest` passes.

### 2. Awakening Reaction & Touch Feedback (`⚡ BATCH`: UI Reactions)
- [ ] **2.1** `[FE]` Create `AwakeningOverlay` composable in `:ui:eyes` with radial amber light burst (`#F59E0B`).
  - _Requirements: 2.1_
- [ ] **2.2** `[FE]` Implement touch gesture detector on `RobotEyesCanvas` detecting tap/poke events.
  - _Requirements: 3.1, 3.3_
- [ ] **2.3** `[FE]` Wire permission-free haptic feedback (`View.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)`).
  - _Requirements: 3.2_
- [ ] **2.4** `[REVIEW]` **Reactions Checkpoint:** Compiles with zero errors.

### 3. Screen Integration & End-to-End Tracking (`⚡ BATCH`: Screen Integration)
- [ ] **3.1** `[FE]` Connect `CompanionViewModel` to `FaceObservationSource` and `CompanionBehaviorStateMachine`.
  - _Requirements: 1.1, 2.1, 2.2_
- [ ] **3.2** `[FE]` Add debug overlay in debug builds displaying normalized bounding box coordinates as shapes (never raw pixels).
  - _Requirements: 1.1_
- [ ] **3.3** `[REVIEW]` **Final Feature Checkpoint:** Run full `./gradlew testDebugUnitTest compileDebugKotlin`.

---

## 🔀 Task Dependency Graph (Intra-Feature Waves)

```json
{
  "waves": [
    { "id": 0, "tasks": ["1.1", "1.2", "1.3", "1.4"] },
    { "id": 1, "tasks": ["2.1", "2.2", "2.3", "2.4"] },
    { "id": 2, "tasks": ["3.1", "3.2", "3.3"] }
  ]
}
```

