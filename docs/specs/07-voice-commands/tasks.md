# ✅ Feature Tasks: On-Device Voice Commands (`07-voice-commands`)

> **Phase / Wave:** `Phase 2 (Wave 5)` · **Branch / Owner:** `feat/07-voice-commands` · **Status:** `[ ]` Spec Ready  
> **Depends On:** `06-personality-memory` · **Touches:** `:core:model`, `:core:ports`, `:core:behavior`, `:platform:audio`, `:platform:preferences`, `:feature:companion`, `:feature:settings`, `:app`, `build.gradle.kts`  
> **Spec Links:** [`requirements.md`](./requirements.md) · [`design.md`](./design.md) · [`uat.md`](./uat.md) · [`design-reference/stitch-prompt.md`](./design-reference/stitch-prompt.md)

---

## 🔄 Implementation Tasks

### 1. Pure-Kotlin Core Domain & Unit Tests (`⚡ BATCH`: Ports, Models, Parser & Unit Tests)
- [ ] **1.1** `[BE]` Create `VoiceCommand.kt` and `VoiceCommandEvent.kt` in `core/src/main/kotlin/com/eyekage/core/model/`
  - Defines `VoiceCommand` sealed interface (`SwitchMode`, `TriggerAwakening`, `Sleep`, `Blink`, `CenterGaze`) and `VoiceCommandEvent` sealed hierarchy
  - _Requirements: 1.1, 1.3_
- [ ] **1.2** `[BE]` Create `VoiceCommandSource.kt` interface and `FakeVoiceCommandSource.kt` test double in `core/src/main/kotlin/com/eyekage/core/ports/`
  - Zero Android imports; exposes `val events: Flow<VoiceCommandEvent>`, `val isListening: StateFlow<Boolean>`, `startListening()`, and `stopListening()`
  - _Requirements: 1.3_
- [ ] **1.3** `[BE]` Implement `VoiceCommandParser.kt` in `core/src/main/kotlin/com/eyekage/core/behavior/`
  - Case-insensitive token parser resolving spoken synonyms to canonical `VoiceCommand` variants, returning `null` on unknown phrases
  - _Requirements: 1.2, 1.4_
- [ ] **1.4** `[BE]` Write automated unit tests in `core/src/test/kotlin/com/eyekage/core/behavior/VoiceCommandParserTest.kt`
  - Tests valid commands, all mode synonyms, awakening phrases, sleep/blink commands, and unknown noise phrases
  - _Requirements: 1.2, 1.4_
- [ ] **1.5** `[REVIEW]` **Core & Unit Test Checkpoint:** Run `./gradlew :core:test` — must pass with zero errors (triggers auto-checkpoint commit in `/build`)

---

### 2. Platform Audio Engine, Preferences & Manifest Allowlist (`⚡ BATCH`: Platform Speech & Allowlist)
- [ ] **2.1** `[BE]` Update `PreferencesRepository.kt` and `DataStorePreferencesRepository.kt` with `voiceCommandsEnabled: Flow<Boolean>` and `setVoiceCommandsEnabled(enabled: Boolean)`
  - Uses key `voice_commands_enabled` (default `false`)
  - _Requirements: 4.1_
- [ ] **2.2** `[BE]` Implement `SpeechRecognizerVoiceSource.kt` in `platform/src/main/kotlin/com/eyekage/platform/audio/`
  - Implements `VoiceCommandSource` using `android.speech.SpeechRecognizer` with `EXTRA_PREFER_OFFLINE = true`, lifecycle checks, and zero audio saving
  - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5_
- [ ] **2.3** `[BE]` Update `app/src/main/AndroidManifest.xml` and `build.gradle.kts`
  - Declares `<uses-permission android:name="android.permission.RECORD_AUDIO" />` (removing `tools:node="remove"`)
  - Updates `checkMergedManifestPermissions` to approve `android.permission.RECORD_AUDIO` in `approvedHardwarePermissions` and remove from `prohibitedPermissions`
  - _Requirements: 5.1, 5.2, 5.3_
- [ ] **2.4** `[BE]` Provide `VoiceCommandSource` singleton in `app/src/main/kotlin/com/eyekage/di/AppContainer.kt`
  - Pure-Kotlin manual dependency injection composition root wiring
  - _Requirements: 2.1_
- [ ] **2.5** `[REVIEW]` **Platform & Manifest Checkpoint:** Run `./gradlew checkMergedManifestPermissions compileDebugKotlin` — must pass with zero errors (triggers auto-checkpoint commit in `/build`)

---

### 3. Companion UI Audio Privacy Indicator & Voice Command Dispatch (`⚡ BATCH`: Companion Screen)
- [ ] **3.1** `[FE]` Wire `VoiceCommandSource` into `CompanionViewModel.kt`
  - Collects `voiceCommandSource.events` when voice is enabled, dispatches eye modes, triggers awakening/reactions, and awards $+1$ poke/interaction to `MemoryRepository`
  - Lifecycle-aware: starts listening when screen resumes (if enabled) and stops on `onStop`
  - _Requirements: 3.2, 3.3, 3.4_
- [ ] **3.2** `[FE]` Update `CompanionScreen.kt` with Amber Microphone Privacy Indicator
  - Renders glowing amber mic badge next to camera indicator when `isVoiceListening == true`
  - Handles runtime `RECORD_AUDIO` permission request flow
  - _Requirements: 3.1_
- [ ] **3.3** `[FE]` Write automated unit tests in `feature/src/test/kotlin/com/eyekage/feature/companion/CompanionViewModelVoiceTest.kt`
  - Tests voice-triggered eye mode switching, awakening reaction, and memory progression
  - _Requirements: 3.2, 3.3_
- [ ] **3.4** `[REVIEW]` **Companion Screen Checkpoint:** Run `./gradlew :feature:testDebugUnitTest` — must pass with zero errors (triggers auto-checkpoint commit in `/build`)

---

### 4. Settings Voice Commands Card & Full Verification (`⚡ BATCH`: Settings & Verification)
- [ ] **4.1** `[FE]` Build `VoiceCommandsCard.kt` in `feature/src/main/kotlin/com/eyekage/feature/settings/`
  - AMOLED dark surface card with amber mic header icon, opt-in switch, active status indicator, supported command chips, and 100% on-device privacy shield
  - _Requirements: 4.2, 4.3, 4.4_
- [ ] **4.2** `[FE]` Integrate `VoiceCommandsCard` into `SettingsScreen.kt` and expose toggle in `SettingsViewModel.kt`
  - Mounts directly under `CompanionMemoryCard` with reactive state binding
  - _Requirements: 4.2_
- [ ] **4.3** `[FE]` Write automated unit test in `feature/src/test/kotlin/com/eyekage/feature/settings/VoiceCommandsCardTest.kt`
  - Tests toggle interaction and state binding
  - _Requirements: 4.1, 4.2_
- [ ] **4.4** `[REVIEW]` **Final Feature Checkpoint:** Run full verification (`./gradlew testDebugUnitTest checkMergedManifestPermissions compileDebugKotlin`) -> triggers `/ship` & roadmap sync!
  - _Requirements: All_

---

## 🔀 Task Dependency Graph (Intra-Feature Waves)

```json
{
  "waves": [
    { "id": 1, "tasks": ["1.1", "1.2", "1.3", "1.4", "1.5"] },
    { "id": 2, "tasks": ["2.1", "2.2", "2.3", "2.4", "2.5"] },
    { "id": 3, "tasks": ["3.1", "3.2", "3.3", "3.4"] },
    { "id": 4, "tasks": ["4.1", "4.2", "4.3", "4.4"] }
  ]
}
```

---

## 📝 Feature Activity Log

| Timestamp | Task | Action / Summary | Result |
|---|---|---|---|
| 2026-11-01 12:35 | Spec Init | Created specification bundle for `07-voice-commands` | Ready |
