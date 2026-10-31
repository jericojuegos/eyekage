# 📋 Feature Requirements: Procedural Eye Engine (`01-eye-engine`)

> **Feature Slug:** `01-eye-engine` · **Phase / Wave:** `Phase 1 (Wave 1)`  
> **Depends On:** `00-scaffold` · **Branch / Owner:** `feat/01-eye-engine` · **Status:** `[ ]` Spec Ready  
> **Touches:** `:core:motion`, `:ui:eyes`, `:feature:companion`

---

## 1. Introduction & Overview

The Procedural Eye Engine is the visual and emotional centerpiece of EyeKage. It renders two responsive, expressive robot eyes on an absolute OLED-black Canvas using Jetpack Compose drawing APIs. The engine drives continuous autonomous liveliness—including organic blinks, idle micro-saccades, and breathing dilations—fully decoupled from camera input. It also provides the optional Desk Companion display mode to keep the screen awake.

### Assumptions
- **A1 (Canvas vs. Asset Sprites):** The eyes are rendered purely procedurally via Compose `DrawScope` paths and radial gradients, ensuring crisp scaling on any screen density with zero image assets. **Confirmed.**
- **A2 (Display Refresh Rate):** The engine synchronizes with the Android Vsync frame clock (supporting 60 Hz and 90 Hz displays seamlessly via delta-time integration). **Confirmed.**

### Domain Glossary
- **Saccade:** Quick, simultaneous micro-movements of both eyes between fixation points (1–3 px randomized).
- **GazeTarget:** Normalized coordinate pair `(x, y)` in range `[-1.0, 1.0]` indicating where the pupils should look.
- **Eyelid Squint:** Procedural vertical clipping of eye height to convey mood or reaction.

---

## 2. Requirements & EARS Acceptance Criteria

### Requirement 1: Procedural Canvas Eye Rendering
**User Story:** As an EyeKage user, I want to see two smoothly rendered robot eyes that feel lively and responsive on my screen.
- **Req 1.1:** THE EyeRenderer SHALL draw left and right eyes symmetrically on an OLED-black background (`#000000`).
- **Req 1.2:** THE EyeRenderer SHALL support dynamic gaze offsets, pupil scaling, and eyelid opening factors.
- **Req 1.3:** THE EyeRenderer SHALL avoid per-frame allocations by caching Path and Paint objects across draws.

### Requirement 2: Autonomous Liveliness (Blinks & Saccades)
**User Story:** As a user, I want EyeKage to blink and glance around organically even when no camera tracking is active.
- **Req 2.1:** WHILE idle, THE MotionEngine SHALL trigger spontaneous blinks every 3.0 to 7.0 seconds with natural opening/closing easing curves (150–200 ms duration).
- **Req 2.2:** WHILE idle, THE MotionEngine SHALL execute subtle micro-saccades every 1.5 to 4.0 seconds.
- **Req 2.3:** WHERE reduced motion is enabled, THE MotionEngine SHALL disable micro-saccades and use soft-fade blinks.

### Requirement 3: Optional Desk Companion Display Mode
**User Story:** As a user, I want EyeKage to stay on my desk without the phone screen turning off automatically.
- **Req 3.1:** WHEN desk mode is enabled in settings, THE Activity SHALL set the `FLAG_KEEP_SCREEN_ON` window flag while EyeKage is in the foreground.
- **Req 3.2:** WHEN desk mode is disabled, THE Activity SHALL clear the `FLAG_KEEP_SCREEN_ON` flag.

