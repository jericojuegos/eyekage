# 📋 Feature Requirements: Hand Gesture Recognition & Interactive Reactions (`09-hand-gestures`)

> **Feature Slug:** `09-hand-gestures` · **Phase / Wave:** `Phase 3 (Wave 7)`  
> **Depends On:** `08-head-pose-posture`, `06-personality-memory`  
> **Branch / Owner:** `feat/09-hand-gestures` · **Status:** `[ ]` Spec Ready  
> **Touches:** `:core:model`, `:core:behavior`, `:core:ports`, `:platform:vision`, `:platform:preferences`, `:ui:eyes`, `:feature:companion`, `:feature:settings`, `:app`

---

## 1. Introduction & Overview

EyeKage advances Phase 3 (Awareness+ Advanced Vision) by giving the companion real-time awareness of the user's natural hand gestures in front of the camera. In addition to following gaze and acknowledging head tilts/nods, EyeKage can now engage in touch-free, expressive physical dialogue across the room or desk surface—responding to greetings (Wave), playful banter (Peace / Victory sign), praise (Thumbs Up), intense resolve (Closed Fist), and command pauses (Stop / Flat Palm).

Adhering strictly to EyeKage's offline-first privacy architecture:
1. **Zero Cloud Inference & Zero Telemetry:** Hand gesture detection and geometric evaluation run 100% on-device using local inference from the front-facing camera feed. Manifest allowlists strictly bar network permissions (`INTERNET`, `ACCESS_NETWORK_STATE`), verified by CI guards.
2. **Zero Biometric or Frame Storage:** No camera frames, hand crops, skin-tone representations, or biometric identity embeddings are written to disk, SQLite, or preferences. The pipeline computes lightweight numerical classifications, bounding coordinates, and confidence scores in transient memory, discarding raw frames immediately.
3. **Thermal & Frame-Budget Efficiency:** Gesture inference is scheduled responsibly alongside facial tracking to maintain 60/90 Hz UI rendering fluidity on baseline hardware (MediaTek Helio G88 / realme C55) without thermal throttling.
4. **User-Controlled Opt-In:** Hand gesture recognition is user-configurable via an explicit toggle and sensitivity controls in the Settings screen.

### Assumptions (Flagged for Review)

- **A1 (Vision Pipeline Integration):** Uses MediaPipe Tasks Vision (`com.google.mediapipe:tasks-vision:0.10.14`) gesture recognition capabilities coupled with pure-Kotlin geometric debounce and state machine logic in `:core:behavior`. **Confirmed.**
- **A2 (Non-Verbal Companion Dialogue):** Supported gestures map to distinct, endearing robot eye reactions (happy crescent winks, celebratory affirmative bounces, focused pupil constrictions, and freeze pauses) rather than transactional UI shortcuts. **Confirmed.**
- **A3 (Hardware Permissions):** Operates under the existing, user-granted `android.permission.CAMERA`. Zero new Android permissions are required. **Confirmed.**
- **A4 (Independent Animation Clock):** Eye animations, blinks, and winks render at the display refresh rate (60/90 Hz) independent of vision inference frame rates, preserving zero-jitter fluidity even during momentary hand occlusion or frame drops. **Confirmed.**

### Domain Glossary

- **HandGesture:** Categorical enum in `:core:model` representing recognized gestures: `NONE`, `WAVE`, `PEACE_SIGN`, `THUMBS_UP`, `CLOSED_FIST`, `FLAT_PALM_STOP`.
- **HandObservation:** Real-time data model in `:core:model` encapsulating detected `gesture: HandGesture`, `confidence: Float`, normalized `center: Pair<Float, Float>?` in centered `[-1.0, 1.0]` coordinates, `boundingBoxArea: Float`, and `timestampNanos: Long`.
- **HandGestureReaction:** Domain sealed hierarchy in `:core:model` defining companion behavioral outputs: `WaveGreeting`, `PeaceWink`, `ThumbsUpApproval`, `FistResolve`, `HoldStop`, and `None`.
- **HandGestureSource:** Pure-Kotlin port interface in `:core:ports` streaming observable `HandObservation` updates and active tracking states to consumer features.
- **HandGestureReactionEngine:** Deterministic state machine in `:core:behavior` providing temporal debouncing (150–200 ms confirmation window), re-trigger cooldowns (1.5–2.0 s), and smooth reaction decay.
- **GestureSensitivity:** User-configurable classification threshold enum in `:core:model`: `LOW` (strict confidence $\ge 0.75$, 250 ms hold), `NORMAL` (standard confidence $\ge 0.60$, 150 ms hold), and `HIGH` (sensitive confidence $\ge 0.50$, 100 ms hold).

---

## 1.5 🖼️ Visual UI Inventory (Mandatory 1:1 Reference Audit)

> The UI components introduced in this feature are designed using EyeKage's OLED-black dark theme tokens (`#000000` background, `#0A0A12` cards, `#22D3EE` cyan accent, `#10B981` emerald status, `#F59E0B` amber alerts) matching `docs/foundation/design-system.md` and detailed in `design-reference/stitch-prompt.md`.

| Screen / Component | Section / Location | Visible Fields, Columns & Badges | All Buttons & Icon Actions | Mapped `Req` ID |
| :--- | :--- | :--- | :--- | :--- |
| `CompanionScreen.kt` | HUD Top Control Bar | Active Gesture Feedback Chip (Emerald pill with gesture icon 👋 / ✌️ / 👍 / ✊ / ✋ and label; auto-fades after 1.8s) | Tap to dismiss gesture cue, debug overlay toggle | `Req 3.4, 3.5` |
| `CompanionScreen.kt` | Robot Eyes Surface | Procedural eye animations: Happy arch double-blink (Wave), asymmetrical playful wink (Peace), proud pupil bounce (Thumbs Up), intense narrowed focus (Fist), surprised frozen saccades (Stop) | Touch / poke haptics (inherited) | `Req 3.1, 3.2, 3.3` |
| `SettingsScreen.kt` | Hand Gestures Card | Section title "Hand Gestures & Interactions", live status pill ("Watching...", "👋 Wave Detected", "Off"), live confidence indicator | Opt-in Switch ("Hand Gesture Recognition", default `true`), Sensitivity chips (`Low`, `Normal`, `High`) | `Req 4.1, 4.2, 4.3` |
| `SettingsScreen.kt` | Privacy Disclosures | Privacy shield badge ("100% On-Device Gesture Recognition. Zero Handprints or Photos Saved.") | None (Read-only verified disclosure) | `Req 4.4` |

---

## 2. Requirements & EARS Acceptance Criteria

### Requirement 1: Pure-Kotlin Hand Gesture Models & Recognizer Engine (`:core`)

**User Story:** As a developer, I want hand gesture data models and reaction state machines to be implemented in pure Kotlin without Android SDK dependencies, so that interaction logic is portable, deterministic, and easily tested.

#### Acceptance Criteria
1. **Req 1.1:** THE `HandGesture` enum in `:core:model` SHALL define `NONE`, `WAVE`, `PEACE_SIGN`, `THUMBS_UP`, `CLOSED_FIST`, and `FLAT_PALM_STOP`.
2. **Req 1.2:** THE `HandObservation` data class in `:core:model` SHALL encapsulate `gesture: HandGesture`, `confidence: Float`, `normalizedCenterX: Float?`, `normalizedCenterY: Float?`, `boundingBoxArea: Float`, and `timestampNanos: Long`.
3. **Req 1.3:** THE `HandGestureReaction` sealed interface in `:core:model` SHALL provide structured reaction events: `WaveGreeting(val timestampMs: Long)`, `PeaceWink(val isLeftEye: Boolean, val timestampMs: Long)`, `ThumbsUpApproval(val timestampMs: Long)`, `FistResolve(val timestampMs: Long)`, `HoldStop(val isFrozen: Boolean)`, and `None`.
4. **Req 1.4:** THE `HandGestureSource` interface in `:core:ports` SHALL expose `val handGestures: StateFlow<HandObservation>` and `val isTracking: StateFlow<Boolean>`.
5. **Req 1.5:** THE `HandGestureReactionEngine` in `:core:behavior` SHALL require a gesture to remain stable for a configurable temporal confirmation window ($100\text{--}250\text{ ms}$) before firing a reaction, preventing false triggers from transient hand transitions.
6. **Req 1.6:** THE `HandGestureReactionEngine` SHALL enforce a minimum cooldown duration ($1.5\text{ seconds}$) between successive firings of the same gesture, preventing repetitive event flooding.
7. **Req 1.7:** IF `HandObservation.confidence` is below the active `GestureSensitivity` threshold, THEN `HandGestureReactionEngine` SHALL discard the observation and remain in its current state without triggering reactions.

---

### Requirement 2: Platform Vision Pipeline & Gesture Source (`:platform:vision`)

**User Story:** As a user, I want my hand gestures to be detected reliably through the front camera without overheating my phone or draining excessive battery, so that EyeKage remains responsive during long desk sessions.

#### Acceptance Criteria
1. **Req 2.1:** THE `VisionHandGestureSource` in `:platform:vision` SHALL implement `HandGestureSource` using on-device vision processing tied to the front-facing CameraX frame stream.
2. **Req 2.2:** THE `VisionHandGestureSource` SHALL operate concurrently or interleaved with facial tracking, throttling gesture inference to a sustainable rate ($10\text{--}15\text{ Hz}$) to preserve thermal equilibrium on MediaTek Helio G88 baseline hardware.
3. **Req 2.3:** THE `VisionHandGestureSource` SHALL map raw detector outputs into normalized centered `[-1.0, 1.0]` coordinates with correct front-camera mirroring and display orientation handling.
4. **Req 2.4:** THE `VisionHandGestureSource` SHALL NEVER write, serialize, or cache any camera frames, hand crops, or raw landmark coordinates to disk, cache, or external storage.
5. **Req 2.5:** WHEN the camera pipeline is stopped (`ON_STOP` or camera toggled off), THE `VisionHandGestureSource` SHALL immediately release detector resources and emit `HandObservation.Empty`.

---

### Requirement 3: Expressive Robot Eye Reactions & HUD Feedback (`:ui:eyes`, `:feature:companion`)

**User Story:** As a user, I want EyeKage to react uniquely and delightfully when I wave, flash a peace sign, or give a thumbs-up, so that interacting feels alive, tactile, and rewarding.

#### Acceptance Criteria
1. **Req 3.1:** WHEN a `WAVE` gesture is confirmed, THE `CompanionViewModel` and `RobotEyesCanvas` SHALL execute a cheerful greeting reaction: eyes widen briefly, squint into happy curved crescents (`bottomEyelidArch` $\ge 0.6$), perform a friendly double-blink, and award $+1$ interaction to `MemoryRepository`.
2. **Req 3.2:** WHEN a `PEACE_SIGN` gesture is confirmed, THE `RobotEyesCanvas` SHALL execute an asymmetrical playful wink (one eye closes to $0.0$ openness with an eyebrow raise while the other remains attentive) for $600\text{ ms}$, then smoothly ease back to open.
3. **Req 3.3:** WHEN a `THUMBS_UP` gesture is confirmed, THE `CompanionViewModel` SHALL trigger an affirmative pupil bounce, gentle nod cycle, and award $+1$ interaction to `MemoryRepository`.
4. **Req 3.4:** WHEN a `CLOSED_FIST` gesture is confirmed, THE `RobotEyesCanvas` SHALL narrow eyelids slightly into a focused squint with contracted pupil scale ($0.75\times$) for $1.2\text{ seconds}$.
5. **Req 3.5:** WHILE a `FLAT_PALM_STOP` gesture is sustained, THE `CompanionViewModel` SHALL pause procedural saccades and freeze gaze coordinates in place with surprised wide eyelids (`openness` $1.0$) until the hand is lowered or $2.0\text{ seconds}$ elapse.
6. **Req 3.6:** WHEN any valid hand gesture is confirmed, THE `CompanionScreen` SHALL display an animated HUD feedback chip in the top control bar with the gesture's icon and label, smoothly fading out after $1.8\text{ seconds}$.

---

### Requirement 4: Hand Gesture Settings, Sensitivity & Privacy Disclosures (`:feature:settings`, `:platform:preferences`)

**User Story:** As a user, I want to control whether hand gestures are recognized, customize detection sensitivity, and verify how my camera data is protected, so that I have complete mastery over the companion's visual autonomy.

#### Acceptance Criteria
1. **Req 4.1:** THE `PreferencesRepository` and `DataStorePreferencesRepository` SHALL persist `gesture_recognition_enabled` (boolean, default `true`) and `gesture_sensitivity` (string, default `"NORMAL"`).
2. **Req 4.2:** THE `SettingsScreen` SHALL display a dedicated "Hand Gestures & Interactions" card (`HandGestureSettingsCard`) containing an opt-in toggle switch for gesture recognition.
3. **Req 4.3:** THE `HandGestureSettingsCard` SHALL provide selectable sensitivity chips: `Low` (strict), `Normal` (standard), and `High` (sensitive).
4. **Req 4.4:** WHILE gesture recognition is active, THE `HandGestureSettingsCard` SHALL display a live status pill indicating the current detection state (e.g., "Watching for gestures...", "👋 Wave Detected", or "Gesture Recognition Disabled").
5. **Req 4.5:** THE `HandGestureSettingsCard` SHALL display a verified privacy disclosure: "100% On-Device Gesture Recognition. Zero Handprints or Photos Saved."

---

### Requirement 5: Dependency Injection & System Composition Root (`:app`)

**User Story:** As a developer, I want all gesture components cleanly wired through the manual DI composition root (`AppContainer`), so that dependencies remain testable and decoupled without reflection.

#### Acceptance Criteria
1. **Req 5.1:** THE `AppContainer` in `:app` SHALL provide a singleton `HandGestureSource` instance lazily wired to `VisionHandGestureSource`.
2. **Req 5.2:** THE `AppContainer` SHALL inject `HandGestureSource` into `CompanionViewModel` and connect its tracking state with the camera lifecycle.

---

## 3. UI & Entrypoint Locations

- **Companion Screen HUD:** `feature/src/main/kotlin/com/eyekage/feature/companion/CompanionScreen.kt`
- **Companion View Model:** `feature/src/main/kotlin/com/eyekage/feature/companion/CompanionViewModel.kt`
- **Eyes Canvas Renderer:** `ui/src/main/kotlin/com/eyekage/ui/eyes/RobotEyesCanvas.kt`
- **Settings Screen:** `feature/src/main/kotlin/com/eyekage/feature/settings/SettingsScreen.kt`
- **Gesture Settings Card:** `feature/src/main/kotlin/com/eyekage/feature/settings/HandGestureSettingsCard.kt`
- **Design Reference:** `docs/specs/09-hand-gestures/design-reference/`

---

## 4. Non-Goals (Out of Scope for This Feature)

- **NG1 (No Sign Language or Fine Finger Tracking):** EyeKage recognizes macroscopic hand gestures (wave, peace, palm, fist, thumbs up), not full American Sign Language (ASL) or 21-joint skeleton finger physics.
- **NG2 (No Multi-Hand Complex Gestures):** Tracking focuses on the primary dominant hand in view; two-handed combo gestures are out of scope.
- **NG3 (No Camera Frame Recording):** Under no circumstance may hand images or landmark history be saved to disk.
- **NG4 (No Network Cloud ML):** Gesture classification runs 100% offline with zero external network connectivity.
