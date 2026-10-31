# 📋 Feature Requirements: Gaze Tracking & Reactions (`04-gaze-tracking-reactions`)

> **Feature Slug:** `04-gaze-tracking-reactions` · **Phase / Wave:** `Phase 1 (Wave 2)`  
> **Depends On:** `01-eye-engine`, `03-camera-face-detection` · **Branch / Owner:** `feat/04-gaze-tracking-reactions` · **Status:** `[ ]` Spec Ready  
> **Touches:** `:core:behavior`, `:core:motion`, `:feature:companion`

---

## 1. Introduction & Overview

This feature connects the on-device vision pipeline (`03`) to the procedural eye engine (`01`). It maps normalized face coordinates into pupil gaze positions using One-Euro jitter smoothing, triggers behavioral reactions (the **Awakening** light burst on acquisition, searching saccades when lost), and supports playful touch reactions (blink, squint, poke) with permission-free haptics.

### Assumptions
- **A1 (Permission-Free Haptics):** Touch feedback uses `View.performHapticFeedback` or Compose `LocalHapticFeedback` with zero manifest permissions required (`VIBRATE` is avoided). **Confirmed.**
- **A2 (Awakening is a Transient Reaction):** The Awakening burst is an animated overlay that fires when a face is acquired and returns to the previously active mode within 800 ms. **Confirmed.**

---

## 2. Requirements & EARS Acceptance Criteria

### Requirement 1: Real-Time Gaze Mapping & Smoothing
- **Req 1.1:** THE MotionEngine SHALL map `FaceObservation` center coordinates into pupil `GazeTarget` positions with physical mirroring compensation.
- **Req 1.2:** THE OneEuroFilter SHALL smooth raw gaze coordinates with $f_{c\text{min}} = 1.0\text{ Hz}$ and speed coefficient $\beta = 0.007$, eliminating jitter while keeping tracking latency $\le 150\text{ ms}$.

### Requirement 2: Behavioral State Machine & Reactions
- **Req 2.1:** WHEN a face is detected from an idle state, THE BehaviorEngine SHALL trigger the `reaction.awakening` amber light burst overlay for 800 ms before settling on tracking.
- **Req 2.2:** WHEN a tracked face is lost, THE BehaviorEngine SHALL transition to a 2.0-second searching saccade state before falling back to ambient idle.

### Requirement 3: Touch & Poke Reactions (Camera-Independent)
- **Req 3.1:** WHEN the user taps or pokes the eye area on the screen, THE BehaviorEngine SHALL trigger a squint or startled blink reaction immediately.
- **Req 3.2:** WHEN a touch reaction triggers, THE System SHALL emit a subtle haptic feedback pulse via `performHapticFeedback`.
- **Req 3.3:** THE touch reaction SHALL operate identically whether the camera is enabled or disabled.

