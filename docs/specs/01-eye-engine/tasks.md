# ✅ Feature Tasks: Procedural Eye Engine (`01-eye-engine`)

> **Phase / Wave:** `Phase 1 (Wave 1)` · **Branch / Owner:** `feat/01-eye-engine` · **Status:** `[ ]` Spec Ready  
> **Depends On:** `00-scaffold` · **Touches:** `:core:motion`, `:ui:eyes`, `:feature:companion`

---

## 🔄 Implementation Tasks

### 1. Motion Math & Blink Engine (`⚡ BATCH`: Pure-Kotlin Math & Tests)
- [ ] **1.1** `[BE]` Implement `OneEuroFilter` in `:core:motion` for jitter reduction and low-latency gaze following.
  - _Requirements: 1.2_
- [ ] **1.2** `[BE]` Implement `BlinkScheduler` and `SaccadeGenerator` in `:core:motion` driving procedural eyelid/gaze timing.
  - _Requirements: 2.1, 2.2, 2.3_
- [ ] **1.3** `[BE]` Write pure-Kotlin unit tests in `:core/src/test` validating One-Euro frequency response and blink timing distributions.
  - _Validates: Req 1.2, 2.1, 2.2_
- [ ] **1.4** `[REVIEW]` **Core Motion Checkpoint:** `./gradlew :core:testDebugUnitTest` passes with zero errors.

### 2. Compose Canvas Eyes Renderer (`⚡ BATCH`: UI Canvas Drawing)
- [ ] **2.1** `[FE]` Create `RobotEyesCanvas` composable in `:ui:eyes` drawing pill/circle eye shapes using cached `DrawScope` paths.
  - _Requirements: 1.1, 1.3_
- [ ] **2.2** `[FE]` Implement eyelid clipping and outer radial glow shaders (with AGSL on API 33+ and radial gradient fallback).
  - _Requirements: 1.1, 1.2_
- [ ] **2.3** `[FE]` Add Roborazzi screenshot test verifying default eye geometry and blink frames.
  - _Validates: Req 1.1_
- [ ] **2.4** `[REVIEW]` **Canvas Checkpoint:** `./gradlew :ui:compileDebugKotlin` passes.

### 3. Companion Screen Host & Desk Mode (`⚡ BATCH`: Screen Integration)
- [ ] **3.1** `[FE]` Implement `CompanionScreen` in `:feature:companion` binding `RobotEyesCanvas` to `CompanionViewModel`.
  - _Requirements: 1.1, 2.1_
- [ ] **3.2** `[FE]` Implement desk companion mode window flag management (`FLAG_KEEP_SCREEN_ON`).
  - _Requirements: 3.1, 3.2_
- [ ] **3.3** `[REVIEW]` **Final Feature Checkpoint:** Run `./gradlew testDebugUnitTest compileDebugKotlin` cleanly.

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

