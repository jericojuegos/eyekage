# 📋 Feature Requirements: Settings & Privacy Disclosures (`05-settings-privacy`)

> **Feature Slug:** `05-settings-privacy` · **Phase / Wave:** `Phase 1 (Wave 3)`  
> **Depends On:** `00-scaffold`, `02-eye-modes` · **Branch / Owner:** `feat/05-settings-privacy` · **Status:** `[ ]` Spec Ready  
> **Touches:** `:platform:data`, `:feature:settings`, `:core:ports`

---

## 1. Introduction & Overview

This feature provides the settings screen and local persistence layer for EyeKage. Using Jetpack Preferences DataStore, it manages exactly 5 approved user preferences (`default_mode_id`, `camera_enabled`, `reduced_motion_override`, `haptics_enabled`, `desk_mode_enabled`). It also provides a clear, plain-English "What EyeKage Stores" privacy card disclosing the offline, local-only nature of the app.

### Assumptions
- **A1 (DataStore Persistence):** Only the 5 approved keys documented in `tech-stack.md` may ever be written to disk. **Confirmed.**
- **A2 (Zero Telemetry Disclosure):** The privacy disclosure affirms that camera frames are never saved or uploaded, no accounts exist, and zero analytics are collected. **Confirmed.**

---

## 2. Requirements & EARS Acceptance Criteria

### Requirement 1: Local Preferences Storage (DataStore)
- **Req 1.1:** THE PreferencesRepository SHALL read and write the 5 approved preference keys asynchronously via Jetpack DataStore.
- **Req 1.2:** WHEN the application launches, THE System SHALL restore the saved `default_mode_id`, `reduced_motion_override`, `haptics_enabled`, and `desk_mode_enabled` settings.

### Requirement 2: Settings UI Controls
- **Req 2.1:** THE SettingsScreen SHALL provide toggles for: Camera Vision (`camera_enabled`), Haptic Feedback (`haptics_enabled`), and Desk Companion Mode (`desk_mode_enabled`).
- **Req 2.2:** THE SettingsScreen SHALL provide a segmented button or dropdown for Reduced Motion (`SYSTEM`, `ON`, `OFF`).
- **Req 2.3:** THE SettingsScreen SHALL provide a default eye mode selector dropdown.

### Requirement 3: Privacy & Data Transparency Disclosure
- **Req 3.1:** THE SettingsScreen SHALL display a dedicated "Data & Privacy Transparency" card.
- **Req 3.2:** THE Privacy Card SHALL explain in plain language:
  - No internet connection used or requested.
  - Camera frames processed live in RAM and never written to disk.
  - No facial recognition or biometric profiles created.
  - Exactly what 5 preference settings are stored locally.

