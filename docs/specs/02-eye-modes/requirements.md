# 📋 Feature Requirements: Data-Driven Eye Modes (`02-eye-modes`)

> **Feature Slug:** `02-eye-modes` · **Phase / Wave:** `Phase 1 (Wave 2)`  
> **Depends On:** `01-eye-engine` · **Branch / Owner:** `feat/02-eye-modes` · **Status:** `[ ]` Spec Ready  
> **Touches:** `:core:model`, `:core:modes`, `:ui`, `:feature:companion`

---

## 1. Introduction & Overview

EyeKage features four distinct selectable eye modes and one transient reaction overlay. To ensure intellectual property protection and future commercial rebranding safety, mode identifiers in code are strictly neutral (`mode.normal`, `mode.crimson_tracker`, `mode.white_scan`, `mode.purple_orbit`, `reaction.awakening`). Display names are loaded from localized string resources, allowing prototype anime names to be swapped for original commercial branding via a single resource configuration.

### Assumptions
- **A1 (Data-Driven Specifications):** Eye modes are represented as pure Kotlin data structures in `:core:modes` defining geometry, primary/secondary ARGB colors, and pupil details. **Confirmed.**
- **A2 (Rebrand Safety):** No trademarked anime names (Sharingan, Byakugan, Rinnegan) may appear in Kotlin code, class names, or tests. **Confirmed.**

---

## 2. Requirements & EARS Acceptance Criteria

### Requirement 1: Mode Catalog & Neutral Identifiers
- **Req 1.1:** THE System SHALL define exactly 4 selectable modes and 1 reaction overlay using neutral IDs (`mode.normal`, `mode.crimson_tracker`, `mode.white_scan`, `mode.purple_orbit`, `reaction.awakening`).
- **Req 1.2:** THE UI SHALL resolve display labels from `res/values/mode_labels.xml` by key, decoupling code from user-visible names.

### Requirement 2: Visual Style Rendering per Mode
- **Req 2.1:** WHEN `mode.normal` is active, THE EyeRenderer SHALL draw rounded cyan eyes (`#22D3EE`).
- **Req 2.2:** WHEN `mode.crimson_tracker` is active, THE EyeRenderer SHALL draw crimson iris elements (`#DC2626`) with sharp dark geometric tracking accents.
- **Req 2.3:** WHEN `mode.white_scan` is active, THE EyeRenderer SHALL draw lavender-white pupils (`#E0E7FF`) with a sweeping scan-line pulse.
- **Req 2.4:** WHEN `mode.purple_orbit` is active, THE EyeRenderer SHALL draw violet concentric orbital rings (`#A855F7`).

### Requirement 3: Mode Switcher Drawer UI
- **Req 3.1:** WHEN the user swipes or taps the subtle menu pill, THE UI SHALL present a bottom sheet or side drawer containing mode selection chips.
- **Req 3.2:** WHEN a mode chip is tapped, THE UI SHALL update the active mode instantly and animate a smooth 200 ms color morph.

