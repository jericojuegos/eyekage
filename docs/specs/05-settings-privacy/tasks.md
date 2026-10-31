# ✅ Feature Tasks: Settings & Privacy Disclosures (`05-settings-privacy`)

> **Phase / Wave:** `Phase 1 (Wave 3)` · **Branch / Owner:** `feat/05-settings-privacy` · **Status:** `[ ]` Spec Ready  
> **Depends On:** `00-scaffold`, `02-eye-modes` · **Touches:** `:platform:data`, `:feature:settings`, `:core:ports`

---

## 🔄 Implementation Tasks

### 1. DataStore Repository & Ports (`⚡ BATCH`: Local Data Layer)
- [ ] **1.1** `[BE]` Define `PreferencesRepository` interface in `:core:ports`.
  - _Requirements: 1.1_
- [ ] **1.2** `[BE]` Implement `DataStorePreferencesRepository` in `:platform:data` strictly managing the 5 approved keys.
  - _Requirements: 1.1, 1.2_
- [ ] **1.3** `[BE]` Write unit tests in `:platform/src/test` verifying default fallbacks and asynchronous roundtrip persistence.
  - _Validates: Req 1.1, 1.2_
- [ ] **1.4** `[REVIEW]` **DataStore Checkpoint:** `./gradlew :platform:testDebugUnitTest` passes.

### 2. Settings Screen & Privacy Card (`⚡ BATCH`: UI Settings)
- [ ] **2.1** `[FE]` Build `SettingsScreen` in `:feature:settings` with Material 3 Switches, Segmented Buttons, and Dropdowns.
  - _Requirements: 2.1, 2.2, 2.3_
- [ ] **2.2** `[FE]` Build `DataPrivacyDisclosureCard` displaying plain-English storage disclosures.
  - _Requirements: 3.1, 3.2_
- [ ] **2.3** `[FE]` Wire `SettingsViewModel` to `PreferencesRepository` with unidirectional data flow.
  - _Requirements: 2.1, 2.2_
- [ ] **2.4** `[REVIEW]` **Settings UI Checkpoint:** Compiles with zero errors.

### 3. Screen Integration & Full UAT Hand-Off (`⚡ BATCH`: End-to-End Verification)
- [ ] **3.1** `[FE]` Wire navigation between `CompanionScreen` and `SettingsScreen`.
  - _Requirements: 2.1_
- [ ] **3.2** `[BE]` Add instrumented test verifying no unauthorized files or images are written to `filesDir` or `cacheDir`.
  - _Validates: Req 3.2_
- [ ] **3.3** `[REVIEW]` **Final Feature Checkpoint:** Run full `./gradlew check testDebugUnitTest` and verify `docs/planning/phase-1-acceptance.md`!

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

