# 📐 Technical Design: Personality Engine & Local Memory (`06-personality-memory`)

> **Feature Slug:** `06-personality-memory` · **Requirements:** [`requirements.md`](./requirements.md)  
> **Visual Reference:** [`./design-reference/`](./design-reference/) (`stitch-prompt.md`)  
> **Blast Radius:** `🟢 ISOLATED` (Pure-Kotlin core domain addition + optional settings card)

---

## 1. Architecture & Data Flow

```mermaid
flowchart TD
    subgraph UI_Layer[Presentation & Feature Layer]
        CS["CompanionScreen (60/90 Hz)"] -->|"Touch Poke / Gaze"| CVM["CompanionViewModel"]
        SS["SettingsScreen"] -->|"View / Reset Memory"| SVM["SettingsViewModel"]
        CARD["CompanionMemoryCard"] -->|"Confirm Reset"| SVM
        REC["RobotEyesCanvas"] <---|Draw with MicroExpressions| CVM
    end

    subgraph Core_Layer[Pure-Kotlin Core Layer (:core)]
        PE["PersonalityEngine\n(Curiosity, Playfulness, Energy)"] -->|"Compute"| ME["MicroExpression\n(Arch, Tilt, Droop)"]
        PE -->|"Update"| MS["MoodState & BondTier"]
        CVM -->|"Delta Time + Face Status"| PE
        PORT["MemoryRepository (Interface)"] <---|Read/Write| CVM
        PORT <---|Read/Reset| SVM
    end

    subgraph Platform_Layer[Platform Implementation (:platform)]
        DS["DataStoreMemoryRepository\n(Preferences DataStore)"] ---|Implements| PORT
        DISK[("Device Local Storage\n(Zero Network / RAM-Only Frames)")] <---|4 Memory Keys| DS
    end
```

---

## 2. File Placement, Component Interfaces & Visual Mapping

| Action | Exact File Path | Layer | Responsibility & Visual Reference | Validates |
| :--- | :--- | :--- | :--- | :--- |
| `[NEW]` | `core/src/main/kotlin/com/eyekage/core/model/PersonalityModels.kt` | `[BE]` Core | Domain models: `MoodState`, `PersonalityVector`, `MicroExpression`, `CompanionMemory` | `Req 1.1, 2.1–2.4, 3.1` |
| `[NEW]` | `core/src/main/kotlin/com/eyekage/core/behavior/PersonalityEngine.kt` | `[BE]` Core | Deterministic mood and bond state machine (pure Kotlin) | `Req 1.1–1.5, 3.2` |
| `[NEW]` | `core/src/main/kotlin/com/eyekage/core/ports/MemoryRepository.kt` | `[BE]` Core | Port interface for reading, recording, and clearing companion memory | `Req 3.1, 4.3` |
| `[NEW]` | `core/src/test/kotlin/com/eyekage/core/behavior/PersonalityEngineTest.kt` | `[BE]` Test | Unit tests for mood decay, poke stimulation, curiosity boost, bond progression | `Req 1.1–1.5, 3.2` |
| `[NEW]` | `platform/src/main/kotlin/com/eyekage/platform/preferences/DataStoreMemoryRepository.kt` | `[BE]` Platform | Jetpack DataStore implementation of `MemoryRepository` | `Req 3.1, 4.3` |
| `[NEW]` | `platform/src/test/kotlin/com/eyekage/platform/preferences/DataStoreMemoryRepositoryTest.kt` | `[BE]` Test | Unit tests for DataStore memory persistence and atomic factory reset | `Req 3.1, 4.3` |
| `[MODIFY]` | `ui/src/main/kotlin/com/eyekage/ui/eyes/RobotEyesCanvas.kt` | `[FE]` UI | Adds parametric bottom eyelid arch and curious eye tilt support in DrawScope | `Req 2.1–2.4` |
| `[NEW]` | `feature/src/main/kotlin/com/eyekage/feature/settings/CompanionMemoryCard.kt` | `[FE]` Feature | UI Card displaying live mood chip, bond tier, stats grid, and Reset button | `Req 4.1–4.3` |
| `[MODIFY]` | `feature/src/main/kotlin/com/eyekage/feature/settings/SettingsScreen.kt` | `[FE]` Feature | Mounts `CompanionMemoryCard` inside settings list | `Req 4.1` |
| `[MODIFY]` | `feature/src/main/kotlin/com/eyekage/feature/settings/SettingsViewModel.kt` | `[FE]` Feature | Wires `MemoryRepository` state flow and `ResetMemory` event | `Req 4.1, 4.3` |
| `[MODIFY]` | `feature/src/main/kotlin/com/eyekage/feature/companion/CompanionViewModel.kt` | `[FE]` Feature | Wires `PersonalityEngine` updates to companion loop and memory recording | `Req 1.1–1.5, 3.3` |
| `[NEW]` | `feature/src/test/kotlin/com/eyekage/feature/settings/CompanionMemoryCardTest.kt` | `[FE]` Test | Unit tests for memory card state binding and confirmation dialog events | `Req 4.1–4.3` |

---

## 3. Data Models & Interface Contracts

### 3.1 Core Domain Models (`PersonalityModels.kt`)

```kotlin
enum class MoodState {
    CURIOUS,
    PLAYFUL,
    ALERT,
    DROWSY,
    RESTING
}

data class PersonalityVector(
    val curiosity: Float = 0.5f,
    val playfulness: Float = 0.5f,
    val energy: Float = 1.0f
) {
    init {
        require(curiosity in 0.0f..1.0f)
        require(playfulness in 0.0f..1.0f)
        require(energy in 0.0f..1.0f)
    }
}

data class MicroExpression(
    val eyelidOpennessMultiplier: Float = 1.0f,
    val bottomEyelidArch: Float = 0.0f,      // 0.0 = flat, 1.0 = happy crescent upward arch
    val eyeTiltDegrees: Float = 0.0f,        // Asymmetrical curious glance tilt
    val saccadeSpeedMultiplier: Float = 1.0f
)

data class CompanionMemory(
    val totalPokes: Int = 0,
    val totalGazeSeconds: Long = 0L,
    val bondTier: Int = 1,
    val lastInteractionTimestamp: Long = 0L
)
```

### 3.2 Port Contract (`MemoryRepository.kt`)

```kotlin
interface MemoryRepository {
    val memory: StateFlow<CompanionMemory>
    suspend fun recordPoke()
    suspend fun recordGazeDuration(seconds: Long)
    suspend fun clearMemory()
}
```

### 3.3 DataStore Key Schema (`DataStoreMemoryRepository.kt`)

| Key Name | Type | Default Value | Notes |
| :--- | :--- | :--- | :--- |
| `mem_total_pokes` | `intPreferencesKey` | `0` | Incremented on each screen poke |
| `mem_total_gaze_seconds` | `longPreferencesKey` | `0L` | Cumulative face gaze tracking seconds |
| `mem_bond_tier` | `intPreferencesKey` | `1` | Progression tier (1 to 5) |
| `mem_last_interaction_ts` | `longPreferencesKey` | `0L` | Epoch timestamp of last active interaction |

---

## 4. Error Handling Matrix

| Scenario | Possible Fault | Mitigation / Fallback |
| :--- | :--- | :--- |
| DataStore read failure | Corrupted preferences file | Fall back to default initial `CompanionMemory()`, continue running in memory. |
| Inactive backgrounding | System suspends companion | Personality decay pauses when app moves to background; resumes cleanly in `onStart`. |
| Reduced motion active | System "Remove Animations" | MicroExpression sets `eyeTiltDegrees = 0f` and clamps rapid bottom arch easing. |

---

## 5. Correctness Invariants

1. **Pure Kotlin Core Rule:** Zero `android.*` or `androidx.*` imports in `:core`.
2. **Zero Allocation in Canvas Render Loop:** All micro-expression variables are primitive floats; bottom eyelid arches use pre-allocated path objects in DrawScope.
3. **Atomic Memory Reset:** `clearMemory()` resets only memory keys, preserving user settings (`camera_enabled`, `default_mode_id`).
