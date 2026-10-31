# 📋 Feature Requirements: On-Device Vision Pipeline (`03-camera-face-detection`)

> **Feature Slug:** `03-camera-face-detection` · **Phase / Wave:** `Phase 1 (Wave 1)`  
> **Depends On:** `00-scaffold` · **Branch / Owner:** `feat/03-camera-face-detection` · **Status:** `[ ]` Spec Ready  
> **Touches:** `:platform:vision`, `:core:model`, `:core:ports`

---

## 1. Introduction & Overview

This feature delivers the headless, on-device camera analysis pipeline for EyeKage. Using Android CameraX `ImageAnalysis`, it streams low-resolution frames from the front-facing camera directly to the offline face detector selected in Phase 0. The camera feed is strictly headless (never displayed to the user or written to disk), runs only while the app is actively in the foreground, and illuminates a mandatory in-app privacy indicator.

### Assumptions
- **A1 (Zero Persistence):** No image buffers, bitmaps, or crops are ever persisted to disk. **Confirmed.**
- **A2 (Headless ImageAnalysis):** CameraX does not bind a `Preview` use case; frames are consumed in-memory by the detector and immediately closed. **Confirmed.**
- **A3 (In-Context Permission):** Camera permission is requested on-demand only when the user taps the camera activation control ("Look at me"). **Confirmed.**

---

## 2. Requirements & EARS Acceptance Criteria

### Requirement 1: CameraX Headless Pipeline & Lifecycle Binding
- **Req 1.1:** THE CameraManager SHALL bind an `ImageAnalysis` use case targeting the front-facing camera with target resolution $\approx 480\text{p}$ and `STRATEGY_KEEP_ONLY_LATEST`.
- **Req 1.2:** THE CameraManager SHALL bind strictly to the Activity's `LifecycleOwner` and automatically unbind/release the camera on `ON_STOP` or `ON_PAUSE`.
- **Req 1.3:** THE CameraManager SHALL close every `ImageProxy` immediately after detector inference completes to prevent buffer starvation.

### Requirement 2: Privacy Controls & Visual Indicator
- **Req 2.1:** THE UI SHALL display a green privacy dot (`#10B981`) in the top bar whenever camera analysis is actively receiving frames.
- **Req 2.2:** IF the camera is disabled or unpermitted, THE UI SHALL render the indicator as inactive slate (`#475569`).
- **Req 2.3:** IF camera permission is denied, THEN THE System SHALL gracefully keep all touch and idle eye animations active with zero crashes.

### Requirement 3: Normalized Face Observation Emission
- **Req 3.1:** WHEN a face is detected, THE Detector SHALL emit a `FaceObservation` containing normalized center coordinates $(x, y) \in [-1.0, 1.0]$, bounding box size, and confidence score.
- **Req 3.2:** THE Detector SHALL automatically handle front-camera sensor orientation and mirroring so that normalized coordinates match the user's physical perspective.

