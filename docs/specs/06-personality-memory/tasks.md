# ✅ Feature Tasks: Personality Engine & Local Memory (`06-personality-memory`)

> **Phase / Wave:** `Phase 2 (Wave 4)` · **Branch / Owner:** `feat/06-personality-memory` · **Status:** `[>]` In Progress  
> **Depends On:** `02-eye-modes`, `04-gaze-tracking-reactions`, `05-settings-privacy` · **Touches:** `:core:behavior`, `:core:model`, `:core:ports`, `:platform:preferences`, `:ui:eyes`, `:feature:settings`, `:feature:companion`  
> **Spec Links:** [`requirements.md`](./requirements.md) · [`design.md`](./design.md) · [`uat.md`](./uat.md) · [`design-reference/stitch-prompt.md`](./design-reference/stitch-prompt.md)

---

## 🔄 Implementation Tasks

### 1. Domain Models & Personality Engine (`⚡ BATCH`: Pure Kotlin Core)
- [x] **1.1** `[BE]` Create domain models (`MoodState`, `PersonalityVector`, `MicroExpression`, `CompanionMemory`) in `core/src/main/kotlin/com/eyekage/core/model/PersonalityModels.kt`.
  - _Requirements: 1.1, 2.1–2.4, 3.1_
- [x] **1.2** `[BE]` Define `MemoryRepository` interface in `core/src/main/kotlin/com/eyekage/core/ports/MemoryRepository.kt`.
  - _Requirements: 3.1, 4.3_
- [x] **1.3** `[BE]` Implement pure-Kotlin `PersonalityEngine` in `core/src/main/kotlin/com/eyekage/core/behavior/PersonalityEngine.kt` with curiosity/playfulness boosts, energy decay, and bond tier progression.
  - _Requirements: 1.1–1.5, 3.2_
- [x] **1.4** `[BE]` Write unit tests in `core/src/test/kotlin/com/eyekage/core/behavior/PersonalityEngineTest.kt` verifying state transitions, timeouts, and micro-expression outputs.
  - _Validates: Req 1.1–1.5, 3.2_
- [x] **1.5** `[REVIEW]` **Core Personality Checkpoint:** Run `./gradlew :core:test` — all unit tests pass with zero Android imports.

---

### 2. Platform DataStore & Canvas Micro-Expressions (`⚡ BATCH`: Storage & Visuals)
- [x] **2.1** `[BE]` Implement `DataStoreMemoryRepository` in `platform/src/main/kotlin/com/eyekage/platform/preferences/DataStoreMemoryRepository.kt` managing the 4 memory keys and atomic reset.
  - _Requirements: 3.1, 4.3_
- [x] **2.2** `[BE]` Write unit tests in `platform/src/test/kotlin/com/eyekage/platform/preferences/DataStoreMemoryRepositoryTest.kt` validating memory recording and factory reset.
  - _Validates: Req 3.1, 4.3_
- [x] **2.3** `[FE]` Enhance `RobotEyesCanvas` in `ui/src/main/kotlin/com/eyekage/ui/eyes/RobotEyesCanvas.kt` to draw procedural bottom eyelid happy arches and curious glance tilts without per-frame heap allocations.
  - _Requirements: 2.1–2.4_
- [x] **2.4** `[REVIEW]` **Visual & Storage Checkpoint:** Run `./gradlew :ui:compileDebugKotlin :platform:testDebugUnitTest`.

---

### 3. UI Settings Card & Screen Integration (`⚡ BATCH`: UI & End-to-End)
- [ ] **3.1** `[FE]` Create `CompanionMemoryCard` in `feature/src/main/kotlin/com/eyekage/feature/settings/CompanionMemoryCard.kt` displaying mood chip, bond tier, stats grid, and Reset confirmation dialog.
  - _Requirements: 4.1–4.3_
- [ ] **3.2** `[FE]` Wire `MemoryRepository` to `SettingsViewModel` and mount `CompanionMemoryCard` inside `SettingsScreen`.
  - _Requirements: 4.1, 4.3_
- [ ] **3.3** `[FE]` Connect `PersonalityEngine` and `MemoryRepository` to `CompanionViewModel` to drive live companion loop and persistent bond tracking.
  - _Requirements: 1.1–1.5, 3.3_
- [ ] **3.4** `[FE]` Write unit tests in `feature/src/test/kotlin/com/eyekage/feature/settings/CompanionMemoryCardTest.kt`.
  - _Validates: Req 4.1–4.3_
- [ ] **3.5** `[REVIEW]` **Final Feature Checkpoint:** Run full `./gradlew compileDebugKotlin testDebugUnitTest checkMergedManifestPermissions`.

---

## 🔀 Task Dependency Graph (Intra-Feature Waves)

```json
{
  "waves": [
    { "id": 0, "tasks": ["1.1", "1.2", "1.3", "1.4", "1.5"] },
    { "id": 1, "tasks": ["2.1", "2.2", "2.3", "2.4"] },
    { "id": 2, "tasks": ["3.1", "3.2", "3.3", "3.4", "3.5"] }
  ]
}
```

---

## 📝 Feature Activity Log

| Timestamp | Task(s) | Status | Details |
|---|---|---|---|
| 2026-11-01 | Spec Created | `[x]` | Generated complete specification bundle (`requirements.md`, `design.md`, `tasks.md`, `uat.md`, and `stitch-prompt.md`). |
| 2026-11-01 | Tasks 1.1–1.5 | `[x]` | Implemented pure Kotlin personality models, MemoryRepository port, PersonalityEngine, and full unit test suite. |
| 2026-11-01 | Tasks 2.1–2.2 | `[x]` | Implemented DataStoreMemoryRepository with 4 keys and atomic reset, fully verified via DataStore unit tests. |
| 2026-11-01 | Tasks 2.3–2.4 | `[x]` | Enhanced RobotEyesCanvas with zero-allocation procedural happy arches and asymmetrical eye tilts; verified :ui and :platform checkpoints. |
