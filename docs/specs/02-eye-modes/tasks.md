# ✅ Feature Tasks: Data-Driven Eye Modes (`02-eye-modes`)

> **Phase / Wave:** `Phase 1 (Wave 2)` · **Branch / Owner:** `feat/02-eye-modes` · **Status:** `[ ]` Spec Ready  
> **Depends On:** `01-eye-engine` · **Touches:** `:core:model`, `:core:modes`, `:ui`, `:feature:companion`

---

## 🔄 Implementation Tasks

### 1. Data Models & Catalog Registry (`⚡ BATCH`: Pure-Kotlin Catalog & Tests)
- [x] **1.1** `[BE]` Define `EyeModeId`, `PupilShape`, and `EyeModeSpec` data classes in `:core:model`.
  - _Requirements: 1.1_
- [x] **1.2** `[BE]` Create `DefaultModeCatalog` in `:core:modes` registering the 4 core modes and 1 reaction overlay with neutral IDs.
  - _Requirements: 1.1, 2.1–2.4_
- [x] **1.3** `[BE]` Write unit tests verifying catalog lookup, color validity, and absence of anime trademark strings in `:core`.
  - _Validates: Req 1.1, 1.2_
- [x] **1.4** `[REVIEW]` **Catalog Checkpoint:** `./gradlew :core:testDebugUnitTest` passes.

### 2. Mode Drawing Extensions & Shaders (`⚡ BATCH`: Mode Rendering)
- [x] **2.1** `[FE]` Add mode-specific iris geometry drawers in `:ui:eyes` (circular crimson, scan-line lavender, concentric violet orbits).
  - _Requirements: 2.1–2.4_
- [x] **2.2** `[FE]` Create `res/values/mode_labels.xml` with prototype string labels and configure neutral overlay variant.
  - _Requirements: 1.2_
- [x] **2.3** `[FE]` Add rendering and spec tests for all 4 eye modes in `:ui:eyes`.
  - _Validates: Req 2.1–2.4_
- [x] **2.4** `[REVIEW]` **Mode Rendering Checkpoint:** `./gradlew :ui:testDebugUnitTest` passes.

### 3. Mode Switcher Drawer UI (`⚡ BATCH`: Drawer & Selection)
- [x] **3.1** `[FE]` Implement `ModePickerBottomSheet` in `:feature:companion` with glowing chip buttons for each mode.
  - _Requirements: 3.1, 3.2_
- [x] **3.2** `[FE]` Wire mode selection event to `CompanionViewModel` with smooth color interpolation.
  - _Requirements: 3.2_
- [x] **3.3** `[REVIEW]` **Final Feature Checkpoint:** Run full `./gradlew testDebugUnitTest compileDebugKotlin`.

---

## 🔀 Task Dependency Graph (Intra-Feature Waves)

```json
{
  "waves": [
    { "id": 0, "tasks": ["1.1", "1.2", "1.3", "1.4"] },
    { "id": 1, "tasks": ["2.1", "2.2", "2.3", "2.4"] },
    { "id": 2, "tasks": ["3.1", "3.2", "3.3"] }
  ]
}
```

