# 📋 Feature Requirements: On-Device Voice Commands (`07-voice-commands`)

> **Feature Slug:** `07-voice-commands` · **Phase / Wave:** `Phase 2 (Wave 5)`  
> **Depends On:** `06-personality-memory`  
> **Branch / Owner:** `feat/07-voice-commands` · **Status:** `[ ]` Spec Ready  
> **Touches:** `:core:model`, `:core:ports`, `:core:behavior`, `:platform:audio`, `:platform:preferences`, `:feature:companion`, `:feature:settings`, `:app`, `build.gradle.kts`

---

## 1. Introduction & Overview

EyeKage expands its companion personality with hands-free, on-device voice command recognition in Phase 2 Wave 5. Users can wake, sleep, trigger eyelid blinks, and smoothly switch eye modes using natural voice phrases ("EyeKage", "Awaken", "Normal eyes", "Crimson tracker", "White scan", "Purple orbit", "Sleep", "Blink").

Adhering strictly to EyeKage's offline-first privacy architecture:
1. **Zero Cloud Transmission:** Voice recognition operates 100% on-device. Network permissions (`INTERNET`, `ACCESS_NETWORK_STATE`) remain strictly prohibited and barred by CI manifest guards.
2. **Zero Audio Persistence:** Raw audio buffers, PCM streams, and spectrograms are processed in-memory only and discarded immediately. No audio files are ever written to disk or database.
3. **Transparent Privacy Indicator:** An amber/orange glowing microphone indicator is visibly rendered whenever the microphone is actively listening.
4. **User-Controlled Opt-In:** Voice commands are disabled by default and require explicit user activation in Settings alongside the Android runtime `RECORD_AUDIO` permission.

### Assumptions (Flagged for Review)

- **A1 (Hardware Permission Approval):** Adding voice recognition requires `android.permission.RECORD_AUDIO`. Merged manifest and CI allowlist checks (`checkMergedManifestPermissions`) will be updated to approve `RECORD_AUDIO` while keeping `INTERNET` barred. **Needs Confirmation / Approval.**
- **A2 (Platform SpeechRecognizer Engine):** Uses Android's built-in `SpeechRecognizer` configured with `EXTRA_PREFER_OFFLINE = true`, decoupled from core logic via a pure-Kotlin `VoiceCommandSource` port. **Confirmed.**
- **A3 (Neutral Command Identifiers):** Code identifiers use strictly neutral IDs (`command.mode.normal`, `command.mode.crimson`, `command.mode.white_scan`, `command.mode.purple_orbit`, `command.awaken`, `command.sleep`, `command.blink`). Anime-inspired spoken aliases are handled via neutral parser synonyms. **Confirmed.**
- **A4 (Hardware Lifecycle Binding):** The microphone is only held while the app is active in foreground with voice commands enabled. It is immediately released on `ON_STOP` or when toggled off. **Confirmed.**

### Domain Glossary

- **VoiceCommand:** A sealed domain intent emitted by the voice pipeline (`SwitchMode`, `TriggerAwakening`, `Sleep`, `Blink`, `CenterGaze`).
- **VoiceCommandSource:** Pure-Kotlin port interface in `:core:ports` streaming `VoiceCommandEvent` to feature layers.
- **VoiceCommandEvent:** Observable state stream (`ListeningStarted`, `CommandRecognized`, `ListeningStopped`, `Error`).
- **VoiceCommandParser:** Pure-Kotlin parser in `:core:behavior` that maps raw recognized speech text tokens to canonical `VoiceCommand` instances.
- **Audio Privacy Indicator:** An amber visual pill/dot in the UI indicating live microphone hardware capture.

---

## 2. Requirements & EARS Acceptance Criteria

### Requirement 1: Pure-Kotlin Voice Command Domain & Parsing (`:core`)

**User Story:** As a developer, I want speech intents to be parsed by a pure-Kotlin domain engine without Android dependencies, so that command recognition logic remains testable and portable.

#### Acceptance Criteria
1. **Req 1.1:** THE `VoiceCommand` sealed class in `:core:model` SHALL define discrete intent variants: `SwitchMode(EyeMode)`, `TriggerAwakening`, `Sleep`, `Blink`, and `CenterGaze`.
2. **Req 1.2:** THE `VoiceCommandParser` in `:core:behavior` SHALL parse recognized spoken strings into corresponding `VoiceCommand` variants case-insensitively, supporting phrase synonyms (e.g., "crimson", "crimson tracker", "sharingan", "red eyes" $\to$ `SwitchMode(EyeMode.CrimsonTracker)`).
3. **Req 1.3:** THE `VoiceCommandSource` interface in `:core:ports` SHALL expose `val events: Flow<VoiceCommandEvent>` and lifecycle controls `fun startListening()` and `fun stopListening()`.
4. **Req 1.4:** IF a recognized phrase does not match any registered command synonym, THEN `VoiceCommandParser` SHALL return `null` without throwing exceptions or mutating state.

---

### Requirement 2: Platform Audio Listener & Speech Engine (`:platform:audio`)

**User Story:** As a user, I want voice commands to be captured efficiently on-device without network leakage, so that my speech remains private.

#### Acceptance Criteria
1. **Req 2.1:** THE `SpeechRecognizerVoiceSource` in `:platform:audio` SHALL implement `VoiceCommandSource` using Android's platform `SpeechRecognizer` with `RecognizerIntent.EXTRA_PREFER_OFFLINE = true`.
2. **Req 2.2:** WHILE `SpeechRecognizerVoiceSource` is actively listening, it SHALL emit `VoiceCommandEvent.ListeningStarted` and maintain the active listening status.
3. **Req 2.3:** WHEN a recognized speech result matches a valid command via `VoiceCommandParser`, THE `SpeechRecognizerVoiceSource` SHALL emit `VoiceCommandEvent.CommandRecognized(command)`.
4. **Req 2.4:** IF `android.permission.RECORD_AUDIO` is not granted or speech recognition fails with an error, THEN `SpeechRecognizerVoiceSource` SHALL emit `VoiceCommandEvent.Error` and transition to `ListeningStopped`.
5. **Req 2.5:** THE `SpeechRecognizerVoiceSource` SHALL NEVER save, buffer to flash, or persist any raw audio PCM bytes or recordings.

---

### Requirement 3: Companion UI Audio Privacy Indicator & Action Dispatch (`:feature:companion`)

**User Story:** As a user, I want clear visual feedback when EyeKage is listening to me and when it executes a voice command, so that I always know the microphone status.

#### Acceptance Criteria
1. **Req 3.1:** WHEN voice listening is active, THE `CompanionScreen` SHALL display an amber microphone indicator pill/dot alongside the camera status indicator.
2. **Req 3.2:** WHEN a `SwitchMode(mode)` command is recognized, THE `CompanionViewModel` SHALL smoothly transition the active eye mode to the target mode.
3. **Req 3.3:** WHEN an `Awaken` or `Blink` command is recognized, THE `CompanionViewModel` SHALL trigger the respective eyelid animation reaction and award $+1$ interaction to companion memory.
4. **Req 3.4:** WHEN the `CompanionScreen` lifecycle transitions to `ON_STOP` or background, THE `CompanionViewModel` SHALL stop voice listening and release the microphone hardware immediately.

---

### Requirement 4: Settings Preferences & Audio Privacy Disclosures (`:feature:settings`, `:platform:preferences`)

**User Story:** As a privacy-conscious user, I want an explicit opt-in toggle and command guide in Settings, so that I have full control over microphone usage.

#### Acceptance Criteria
1. **Req 4.1:** THE `PreferencesRepository` SHALL persist a boolean preference `voice_commands_enabled` in Jetpack Preferences DataStore with a default value of `false`.
2. **Req 4.2:** THE `SettingsScreen` SHALL display a dedicated "Voice Commands & Audio" card containing an opt-in toggle for voice control.
3. **Req 4.3:** THE "Voice Commands & Audio" card SHALL include an expandable/scrollable chip list of supported phrases ("Awaken", "Sleep", "Blink", "Normal eyes", "Crimson", "White scan", "Purple orbit").
4. **Req 4.4:** THE "Voice Commands & Audio" card SHALL display a verified Privacy Disclosure stating: "Processed 100% on-device. Audio is never stored or transmitted."

---

### Requirement 5: Manifest & CI Permission Allowlist (`:app`, `build.gradle.kts`)

**User Story:** As a developer, I want the permission allowlist to strictly guard against network access while permitting microphone access for Phase 2.

#### Acceptance Criteria
1. **Req 5.1:** THE `app/src/main/AndroidManifest.xml` SHALL declare `<uses-permission android:name="android.permission.RECORD_AUDIO" />` without `tools:node="remove"`.
2. **Req 5.2:** THE `checkMergedManifestPermissions` task in `build.gradle.kts` SHALL include `android.permission.RECORD_AUDIO` in `approvedHardwarePermissions` and remove it from `prohibitedPermissions`.
3. **Req 5.3:** THE `checkMergedManifestPermissions` task SHALL CONTINUE TO REJECT `android.permission.INTERNET` and `android.permission.ACCESS_NETWORK_STATE` with a build-failing `GradleException`.

---

## 3. UI & Entrypoint Locations

- **Companion Screen HUD:** Top control bar in `CompanionScreen.kt` renders amber `MicIndicator` next to `CameraIndicator`.
- **Settings Screen:** `SettingsScreen.kt` renders `VoiceCommandsCard` directly beneath `CompanionMemoryCard`.
