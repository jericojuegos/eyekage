# ✅ Feature Tasks: On-Device Vision Pipeline (`03-camera-face-detection`)

> **Phase / Wave:** `Phase 1 (Wave 1)` · **Branch / Owner:** `feat/03-camera-face-detection` · **Status:** `[x]` Complete  
> **Depends On:** `00-scaffold` · **Touches:** `:platform:vision`, `:core:model`, `:core:ports`

---

## 🔄 Implementation Tasks

### 1. Vision Models & Port Contracts (`⚡ BATCH`: Core Contracts)
- [x] **1.1** `[BE]` Define `FaceObservation` and `FaceObservationSource` in `:core:model` and `:core:ports`.
  - _Requirements: 3.1_
- [x] **1.2** `[BE]` Implement `FakeFaceObservationSource` in `:core:ports` for unit tests and scripted headless CI verification.
  - _Requirements: 3.1_
- [x] **1.3** `[BE]` Write unit tests in `:core/src/test` verifying coordinate normalization and rotation transforms.
  - _Validates: Req 3.1, 3.2_
- [x] **1.4** `[REVIEW]` **Vision Contract Checkpoint:** `./gradlew :core:test` passes.

### 2. CameraX Lifecycle Pipeline & Detector (`⚡ BATCH`: Platform Vision)
- [x] **2.1** `[BE]` Implement `CameraXManager` in `:platform:vision` configuring headless `ImageAnalysis` on front camera with `KEEP_ONLY_LATEST`.
  - _Requirements: 1.1, 1.2, 1.3_
- [x] **2.2** `[BE]` Wire the winning face detector (from Phase 0 spike) to consume `ImageProxy` and emit `FaceObservation`.
  - _Requirements: 3.1, 3.2_
- [x] **2.3** `[BE]` Implement orientation normalization (portrait + landscape + sensor rotation handling).
  - _Requirements: 3.2_
- [x] **2.4** `[REVIEW]` **Camera Pipeline Checkpoint:** Compiles with zero errors.

### 3. Permission Flow & Privacy Indicator (`⚡ BATCH`: UI Indicator & Permission)
- [x] **3.1** `[FE]` Create `CameraPrivacyIndicator` composable in `:ui` displaying active green (`#10B981`) or inactive slate (`#475569`).
  - _Requirements: 2.1, 2.2_
- [x] **3.2** `[FE]` Implement in-context runtime permission request dialog in `:feature:companion` ("Look at me" trigger).
  - _Requirements: 2.3_
- [x] **3.3** `[REVIEW]` **Final Feature Checkpoint:** Run full `./gradlew testDebugUnitTest compileDebugKotlin`.

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

---

## 📝 Feature Activity Log

| Date | Task(s) | Status | Summary |
|---|---|---|---|
| 2026-10-31 | 1.1, 1.2, 1.3, 1.4 | `[x]` | Added `FaceObservation` bounds/dimensions, `FaceObservationTransform`, `FakeFaceObservationSource`, and unit tests. |
| 2026-10-31 | 2.1, 2.2, 2.3, 2.4 | `[x]` | Built `CameraXManager` with headless `ImageAnalysis` and `ResolutionSelector`, wired into `VisionFaceObservationSource` with orientation transforms. |
| 2026-10-31 | 3.1, 3.2, 3.3 | `[x]` | Verified `CompanionScreen` privacy dot integration, zero warning compile, and all unit tests. |
