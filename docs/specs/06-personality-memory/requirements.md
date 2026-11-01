# 📋 Feature Requirements: Personality Engine & Local Memory (`06-personality-memory`)

> **Feature Slug:** `06-personality-memory` · **Phase / Wave:** `Phase 2 (Wave 4)`  
> **Depends On:** `02-eye-modes`, `04-gaze-tracking-reactions`, `05-settings-privacy`  
> **Branch / Owner:** `feat/06-personality-memory` · **Status:** `[ ]` Spec Ready  
> **Touches:** `:core:behavior`, `:core:model`, `:core:ports`, `:platform:preferences`, `:ui:eyes`, `:feature:settings`, `:feature:companion`

---

## 1. Introduction & Overview

EyeKage transitions from a purely reactive eye tracker into an expressive robot companion with an on-device, rule-based personality engine and inspectable local memory. The personality engine models dynamic emotional states (Curiosity, Playfulness, and Energy) without stochastic black-box AI, driving procedural eye micro-expressions (curious tilts, playful happy arches, drowsy eyelids).

In adherence to EyeKage's offline-first privacy architecture, the companion's accumulated experience and bond level are persisted locally in Jetpack Preferences DataStore. Users can inspect their companion's memory, mood, and statistics at any time in Settings, and reset them with a single tap.

### Assumptions (Flagged for Review)

- **A1 (Deterministic Rule-Based Personality):** The companion's personality state machine is 100% deterministic, running in pure Kotlin in `:core:behavior` without network calls, LLMs, or cloud dependencies. **Confirmed.**
- **A2 (Transparent Local-Only Memory):** Memory records (interactions, gaze duration, bond tier) are stored in local DataStore preferences, completely inspectable and purgeable from Settings. **Confirmed.**
- **A3 (Micro-Expression Rendering):** Eyelid micro-expressions (happy arches, tilts) are drawn procedurally in `:ui:eyes` with zero per-frame heap allocations. **Confirmed.**

### Domain Glossary

- **Mood State:** The active internal emotional condition of the companion (`Curious`, `Playful`, `Alert`, `Drowsy`, `Resting`).
- **Bond Tier:** An integer progression level (1 to 5) reflecting cumulative user interaction time and touch responses.
- **Eyelid Micro-Expression:** Parametric deformations of the eye contour (asymmetrical tilt, bottom arch upward curve, top eyelid droop) representing emotional state.
- **Memory Snapshot:** An immutable summary of accumulated companion stats (total interactions, gaze seconds, current mood, bond tier).

---

## 2. Requirements & EARS Acceptance Criteria

### Requirement 1: Personality State Machine & Mood Dynamics

**User Story:** As a user, I want EyeKage to dynamically express curiosity, playfulness, and tiredness based on our interactions, so that the robot feels alive and responsive.

#### Acceptance Criteria
1. **Req 1.1:** THE PersonalityEngine SHALL maintain three continuous emotional dimensions in $[0.0, 1.0]$: `curiosity`, `playfulness`, and `energy`.
2. **Req 1.2:** WHEN a face is actively tracked, THE PersonalityEngine SHALL increase `curiosity` by $+0.05/\text{sec}$ up to $1.0$, boosting micro-saccade responsiveness.
3. **Req 1.3:** WHEN the user taps or pokes the eye area, THE PersonalityEngine SHALL increase `playfulness` by $+0.2$ up to $1.0$, triggering excited saccades and happy arch micro-expressions.
4. **Req 1.4:** WHILE no user interaction or face detection occurs for $> 120\text{ seconds}$, THE PersonalityEngine SHALL decay `energy` at $-0.01/\text{sec}$ down to a minimum of $0.2$, transitioning the companion into a `Drowsy` resting state.
5. **Req 1.5:** WHEN a poke or face acquisition occurs while in `Drowsy` state, THE PersonalityEngine SHALL restore `energy` to $\ge 0.8$ immediately and trigger a rapid awakening eyelid snap.

---

### Requirement 2: Procedural Eye Micro-Expressions

**User Story:** As a user, I want the eyes to subtly change shape when the companion is happy or curious, so that emotional states are visually intuitive.

#### Acceptance Criteria
1. **Req 2.1:** WHEN `playfulness` $\ge 0.6$, THE EyeRenderer SHALL draw inverted bottom eyelid arches creating an upward happy crescent curvature.
2. **Req 2.2:** WHEN `curiosity` $\ge 0.7$, THE EyeRenderer SHALL introduce an asymmetrical eye tilt of $4^\circ$ to $8^\circ$ between left and right eyes.
3. **Req 2.3:** WHEN `energy` $\le 0.4$, THE EyeRenderer SHALL lower upper eyelid baseline openness to $0.55$, slowing saccade velocity by $40\%$.
4. **Req 2.4:** WHERE the system or user setting has "Reduced Motion" enabled, THE EyeRenderer SHALL omit asymmetrical tilt and rapid eyelid snaps, maintaining calm steady eyes.

---

### Requirement 3: Local Memory Persistence & Progression

**User Story:** As a user, I want my companion to remember our time together and increase our bond level, so that long-term interaction feels rewarding.

#### Acceptance Criteria
1. **Req 3.1:** THE MemoryRepository SHALL persist companion metrics locally: `total_pokes`, `total_gaze_seconds`, `bond_tier`, and `last_interaction_timestamp`.
2. **Req 3.2:** THE PersonalityEngine SHALL advance `bond_tier` from 1 to 5 based on milestones ($1\text{ min} \to \text{Tier 1}$, $5\text{ mins} \to \text{Tier 2}$, $15\text{ mins} \to \text{Tier 3}$, $30\text{ mins} \to \text{Tier 4}$, $60+\text{ mins} \to \text{Tier 5}$).
3. **Req 3.3:** WHEN a face is acquired with `bond_tier` $\ge 3$, THE Companion SHALL emit a distinctive two-pulse cheerful greeting saccade.

---

### Requirement 4: Settings Memory Inspection & Factory Reset

**User Story:** As a privacy-conscious user, I want to inspect all stored companion memories and reset them to zero whenever I choose, so that I have complete control over my data.

#### Acceptance Criteria
1. **Req 4.1:** THE SettingsScreen SHALL display a dedicated "Companion Memory & Affinity" card showing the current `MoodState`, `BondTier`, total interactions, and total gaze tracking time.
2. **Req 4.2:** WHEN the user taps "Reset Companion Memory", THE UI SHALL present a confirmation dialog.
3. **Req 4.3:** WHEN the user confirms the reset, THE MemoryRepository SHALL atomically restore all memory keys to default initial state (`bond_tier = 1`, `0` pokes, `0` gaze seconds) and immediately reflect the reset in the UI.

---

## 3. UI & Entrypoint Locations

- **Companion Micro-Expressions:** `ui/src/main/kotlin/com/eyekage/ui/eyes/RobotEyesCanvas.kt`
- **Memory Card in Settings:** `feature/src/main/kotlin/com/eyekage/feature/settings/CompanionMemoryCard.kt`
- **Settings Screen Integration:** `feature/src/main/kotlin/com/eyekage/feature/settings/SettingsScreen.kt`
- **Core Personality & Mood State:** `core/src/main/kotlin/com/eyekage/core/behavior/PersonalityEngine.kt`
- **Memory Repository Port & Impl:** `core/src/main/kotlin/com/eyekage/core/ports/MemoryRepository.kt` & `platform/src/main/kotlin/com/eyekage/platform/preferences/DataStoreMemoryRepository.kt`

---

## 4. Non-Goals (Out of Scope for This Feature)

- **NG1:** No cloud synchronization, Supabase, or external account storage (deferred to Phase 4).
- **NG2:** No microphone access or audio voice recognition in this feature (reserved strictly for `07-voice-commands`).
- **NG3:** No intrusive notification spam or background battery-draining personality simulations while the app is closed.
