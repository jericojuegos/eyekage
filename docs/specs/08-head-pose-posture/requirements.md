# 📋 Feature Requirements: Head Pose & Desk Posture Awareness (`08-head-pose-posture`)

> **Feature Slug:** `08-head-pose-posture` · **Phase / Wave:** `Phase 3 (Wave 6)`  
> **Depends On:** `04-gaze-tracking-reactions`, `06-personality-memory`  
> **Branch / Owner:** `feat/08-head-pose-posture` · **Status:** `[ ]` Spec Ready  
> **Touches:** `:core:model`, `:core:behavior`, `:core:ports`, `:platform:vision`, `:platform:preferences`, `:ui:eyes`, `:feature:companion`, `:feature:settings`, `:app`

---

## 1. Introduction & Overview

EyeKage initiates Phase 3 (Awareness+ Advanced Vision) by upgrading its visual perception from 2D pupil tracking to 3D head pose estimation and desk posture awareness. As an interactive desk companion, EyeKage observes the user's natural head orientation—pitch (nodding / slouching), yaw (turning away / glancing), and roll (curious tilting)—and spatial proximity to create rich, organic physical rapport.

Adhering strictly to EyeKage's offline-first privacy architecture:
1. **Zero Cloud Inference & Zero Telemetry:** Head pose and posture calculations run 100% on-device using local facial geometry extracted from the already-approved MediaPipe front-camera stream. Network permissions (`INTERNET`, `ACCESS_NETWORK_STATE`) remain strictly barred by CI manifest guards.
2. **Zero Biometric or Frame Storage:** No camera frames, facial crops, landmark embeddings, or biometric identity records are written to disk, SQLite, or preferences. The pipeline computes lightweight numerical Euler angles and proximity ratios in memory, discarding raw data instantaneously.
3. **Non-Medical Companion Framing:** Posture and slouch detection are framed strictly as playful, ambient desk-buddy habits ("EyeKage notices you're leaning in close!"), never as medical or ergonomic diagnoses.
4. **User-Controlled Opt-In:** Desk posture reminders are disabled by default and require explicit user opt-in in Settings.

### Assumptions (Flagged for Review)

- **A1 (Facial Landmark Geometry Engine):** Uses the existing bundled MediaPipe Tasks Vision Face Detector (`face_detector.tflite`, 229 KB) keypoints (left eye, right eye, nose tip, mouth center, left ear tragion, right ear tragion) with pure-Kotlin geometric triangulation in `:core` to compute Euler angles without loading additional heavyweight 3D mesh models. **Confirmed.**
- **A2 (Non-Medical Posture Scope):** Slouching and proximity cues are companion nudges only. No health claims, medical warnings, or ergonomic certifications are made. **Confirmed.**
- **A3 (Hardware Permissions):** Uses only the existing, approved `android.permission.CAMERA`. Zero new Android permissions are introduced. **Confirmed.**
- **A4 (Independent Animation Clock):** Eye tilting and posture micro-expressions render at display refresh rate (60/90 Hz) via smoothed One-Euro filter targets, preserving visual fluidity even during camera frame drops. **Confirmed.**

### Domain Glossary

- **HeadPose:** Domain value class in `:core:model` containing Euler angles in degrees: `pitchDegrees` (vertical tilt / nod), `yawDegrees` (horizontal turn), and `rollDegrees` (lateral tilt / lean).
- **ProximityZone:** Spatial distance classification enum: `TOO_CLOSE` (< 30 cm), `NEAR` (30–45 cm), `NORMAL` (45–65 cm), `FAR` (> 65 cm).
- **PostureState:** Categorical posture classification: `BALANCED`, `SLOUCHING_DOWN` (prolonged downward pitch at near range), `LEANING_CLOSE` (sustained too-close distance), `TILTED_CURIOUS` (head tilted laterally), `TURNED_AWAY` (head turned away from screen).
- **PostureObservation:** Real-time data model combining `HeadPose`, `ProximityZone`, `estimatedDistanceRatio`, and `PostureState`.
- **PostureObservationSource:** Pure-Kotlin port interface in `:core:ports` streaming observable posture states to feature layers.
- **HeadPoseReactionEngine:** State machine in `:core:behavior` detecting dynamic gestures (affirmative nod cycles, curious tilts) and driving responsive eye shapes.
- **PostureMonitor:** Background temporal evaluator in `:core:behavior` that tracks slouch duration and triggers debounced posture alert cues.

---

## 1.5 🖼️ Visual UI Inventory (Mandatory 1:1 Reference Audit)

> The UI components introduced in this feature are designed using EyeKage's OLED-black dark theme tokens (`#000000` background, `#0A0A12` cards, `#22D3EE` cyan accent, `#10B981` emerald status, `#F59E0B` amber alerts) matching `docs/foundation/design-system.md` and detailed in `design-reference/stitch-prompt.md`.

| Screen / Component | Section / Location | Visible Fields, Columns & Badges | All Buttons & Icon Actions | Mapped `Req` ID |
| :--- | :--- | :--- | :--- | :--- |
| `CompanionScreen.kt` | HUD Top Control Bar | Subtle Posture Alert Badge (Amber posture icon 🧘 / ⚠️ when slouching detected; hidden when balanced) | Tap to dismiss alert, debug coordinate toggle | `Req 3.2, 3.4` |
| `CompanionScreen.kt` | Robot Eyes Surface | Organic eye tilt (mirrors user head roll $\pm 15^\circ$), affirmative nod bounce, inquisitive eyebrow raise | Touch / poke haptics (inherited) | `Req 3.1, 3.3` |
| `SettingsScreen.kt` | Posture & Awareness Card | Section title "Desk Posture & Awareness", active status badge ("Balanced", "Tilted", "Slouching"), live distance indicator | Opt-in Switch ("Desk Posture Reminders", default `false`), Slouch Sensitivity dropdown / chips (Gentle 45s, Standard 30s, Strict 15s) | `Req 4.1, 4.2, 4.3` |
| `SettingsScreen.kt` | Privacy Disclosures | Privacy shield badge ("100% On-Device Facial Geometry. Zero Photos Saved.") | None (Read-only verified disclosure) | `Req 4.4` |

---

## 2. Requirements & EARS Acceptance Criteria

### Requirement 1: Pure-Kotlin Head Pose Geometry & Models (`:core`)

**User Story:** As a developer, I want head pose Euler angles and posture states to be computed by pure-Kotlin domain engines without Android SDK dependencies, so that vision behavior is portable, deterministic, and easily tested.

#### Acceptance Criteria
1. **Req 1.1:** THE `HeadPose` data class in `:core:model` SHALL encapsulate `pitchDegrees: Float`, `yawDegrees: Float`, `rollDegrees: Float`, and `timestampNanos: Long`.
2. **Req 1.2:** THE `HeadPoseTransform` object in `:core:model` SHALL calculate normalized pitch, yaw, and roll angles from the 6 facial landmarks (left eye, right eye, nose tip, mouth center, left ear tragion, right ear tragion), correctly handling camera sensor rotation and front-camera mirroring.
3. **Req 1.3:** THE `PostureObservation` data class in `:core:model` SHALL combine `HeadPose`, `ProximityZone` (`TOO_CLOSE`, `NEAR`, `NORMAL`, `FAR`), `estimatedDistanceRatio: Float`, and `PostureState` (`BALANCED`, `SLOUCHING_DOWN`, `LEANING_CLOSE`, `TILTED_CURIOUS`, `TURNED_AWAY`).
4. **Req 1.4:** THE `PostureObservationSource` interface in `:core:ports` SHALL expose `val posture: StateFlow<PostureObservation>` and `val isTracking: StateFlow<Boolean>`.
5. **Req 1.5:** IF fewer than 2 facial landmarks are detected or detection confidence is below threshold ($< 0.5$), THEN `HeadPoseTransform` SHALL emit `PostureObservation.Neutral` without throwing exceptions.

---

## Requirement 2: Platform Landmark Extraction & Pose Pipeline (`:platform:vision`)

**User Story:** As a user, I want my head orientation and distance to be estimated smoothly from the camera without additional background battery drain or new permissions, so that the experience is responsive and efficient.

#### Acceptance Criteria
1. **Req 2.1:** THE `VisionHeadPoseSource` in `:platform:vision` SHALL implement `PostureObservationSource` using the existing CameraX and MediaPipe Face Detector landmark keypoints.
2. **Req 2.2:** THE `VisionHeadPoseSource` SHALL apply a `OneEuroFilter` to raw pitch, yaw, roll, and distance ratios, eliminating sensor jitter while tracking rapid head motions without perceptible latency ($< 30\text{ ms}$).
3. **Req 2.3:** WHILE the front camera is active and a face is present, `VisionHeadPoseSource` SHALL emit real-time `PostureObservation` updates at the camera analysis rate ($15\text{--}30\text{ Hz}$).
4. **Req 2.4:** THE `VisionHeadPoseSource` SHALL NEVER save, cache, or persist any camera frames, facial crops, or landmark coordinates to disk or database.
5. **Req 2.5:** WHEN the camera is disabled or stopped (`ON_STOP`), THE `VisionHeadPoseSource` SHALL immediately release frame analyzers and emit `PostureObservation.Neutral`.

---

## Requirement 3: 3D-Aware Companion Eye Reactions & Posture Cues (`:ui:eyes`, `:feature:companion`)

**User Story:** As a user, I want EyeKage to mirror my head tilts inquisitively, acknowledge my nods, and gently alert me if I am slouching at my desk, so that EyeKage feels truly alive and attentive.

#### Acceptance Criteria
1. **Req 3.1:** WHEN the user tilts their head laterally ($|\text{rollDegrees}| \ge 12^\circ$), THE `RobotEyesCanvas` and `CompanionViewModel` SHALL smoothly tilt the robot eyes and adjust eyebrows by up to $\pm 15^\circ$ with a curious micro-expression.
2. **Req 3.2:** WHEN the user performs an affirmative nod (downward pitch followed by return within $1.2\text{ seconds}$), THE `HeadPoseReactionEngine` SHALL trigger an affirmative nod blink reaction and award $+1$ interaction to `MemoryRepository`.
3. **Req 3.3:** WHEN the user turns their head away ($|\text{yawDegrees}| \ge 25^\circ$), THE `CompanionViewModel` SHALL smoothly ease pupils back toward neutral center and pause active direct gaze lock until the user faces the screen again.
4. **Req 3.4:** WHILE desk posture reminders are enabled, IF the user maintains a `SLOUCHING_DOWN` or `LEANING_CLOSE` posture continuously for longer than the debounce threshold (default $30\text{ seconds}$), THEN THE `CompanionScreen` SHALL display a subtle amber posture nudge badge in the HUD and trigger a concerned micro-expression.
5. **Req 3.5:** WHEN the user corrects their posture back to `BALANCED` (or taps the posture badge), THE `CompanionScreen` SHALL immediately clear the posture alert and return to normal companion state.

---

## Requirement 4: Posture Settings & Privacy Disclosures (`:feature:settings`, `:platform:preferences`)

**User Story:** As a user, I want to control whether posture reminders are enabled and inspect how posture data is handled, so that I have complete control over companion behavior.

#### Acceptance Criteria
1. **Req 4.1:** THE `PreferencesRepository` and `DataStorePreferencesRepository` SHALL persist `posture_reminder_enabled` (boolean, default `false`) and `posture_slouch_threshold_sec` (int, default `30`).
2. **Req 4.2:** THE `SettingsScreen` SHALL display a dedicated "Desk Posture & Awareness" card containing an opt-in toggle switch for posture reminders.
3. **Req 4.3:** THE "Desk Posture & Awareness" card SHALL include selectable threshold chips: Gentle ($45\text{s}$), Standard ($30\text{s}$), and Strict ($15\text{s}$).
4. **Req 4.4:** THE "Desk Posture & Awareness" card SHALL display a verified Privacy Disclosure stating: "Processed 100% on-device from facial geometry. Zero photos or biometric records are ever saved."

---

## Requirement 5: Manifest & CI Privacy Guarantee (`:app`, `build.gradle.kts`)

**User Story:** As a privacy auditor, I want proof that adding head pose and posture awareness introduces zero new Android permissions and zero network capability.

#### Acceptance Criteria
1. **Req 5.1:** THE `AndroidManifest.xml` SHALL NOT add any new hardware or system permissions for head pose tracking (using only `CAMERA` and `RECORD_AUDIO`).
2. **Req 5.2:** THE `checkMergedManifestPermissions` Gradle verification task SHALL continue to pass without error, verifying that `INTERNET` and `ACCESS_NETWORK_STATE` remain strictly absent.

---

## 3. UI & Entrypoint Locations

- **Companion HUD:** `feature/src/main/kotlin/com/eyekage/feature/companion/CompanionScreen.kt` top bar hosts the subtle `PostureIndicator` badge alongside the existing camera and mic indicators.
- **Eyes Procedural Canvas:** `ui/src/main/kotlin/com/eyekage/ui/eyes/RobotEyesCanvas.kt` consumes `rollAngleDegrees` to apply organic angular eye rotation.
- **Settings Screen:** `feature/src/main/kotlin/com/eyekage/feature/settings/SettingsScreen.kt` renders `PostureSettingsCard.kt` beneath `VoiceCommandsCard`.

---

## 4. Non-Goals (Out of Scope for This Feature)

- **NG1 (No Medical Diagnosis):** EyeKage does not diagnose spinal, cervical, musculoskeletal, or vision disorders. All cues are ambient companion habits.
- **NG2 (No Hand Gestures or Body Skeleton):** Hand wave/peace gestures and full-body skeletal tracking are deferred to subsequent Phase 3 waves (`09-hand-gestures`, `10-presence-approach`).
- **NG3 (No 3D Face Meshes / Heavy TFLite Models):** Does not import 468-point 3D FaceLandmarker mesh models (keeping APK light and baseline Helio G88 fast). Uses 6 keypoints and geometric triangulation.
- **NG4 (No Network / Cloud APIs):** Zero cloud calls or telemetry.
