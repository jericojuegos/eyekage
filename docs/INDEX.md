# 🗺️ EyeKage — Project Context Map (`docs/INDEX.md`)

> **Progressive Disclosure:** Read this map first. Never read all docs at once.
> Start with the file that answers your immediate question.

---

## 🧭 Fast Navigation

| I need to know... | Read this file |
|---|---|
| **What are we building overall, why, and for whom?** | [`docs/foundation/prd.md`](foundation/prd.md) |
| **What is the high-level architecture & module layout?** | [`docs/foundation/architecture.md`](foundation/architecture.md) |
| **What libraries, permissions, constants, and minSdk are used?** | [`docs/foundation/tech-stack.md`](foundation/tech-stack.md) |
| **Why was an architecture or technology decision made?** | [`docs/foundation/decisions.md`](foundation/decisions.md) |
| **What are the colors, eye palettes, themes, and UI tokens?** | [`docs/foundation/design-system.md`](foundation/design-system.md) |
| **What is the phased roadmap & feature dependency matrix?** | [`docs/planning/roadmap.md`](planning/roadmap.md) |
| **What are the exact Phase 1 MVP acceptance criteria?** | [`docs/planning/phase-1-acceptance.md`](planning/phase-1-acceptance.md) |
| **What features are deferred to Phases 2–6 or rejected?** | [`docs/planning/backlog.md`](planning/backlog.md) |
| **What bugs, blockers, or debt exist right now?** | [`docs/planning/issues.md`](planning/issues.md) |
| **What decisions and patterns were logged historically?** | [`docs/planning/history.md`](planning/history.md) |

---

## 📦 Feature Specs Registry (`docs/specs/`)

Every feature wave has an isolated spec bundle containing requirements, design, tasks, and stitch prompts:

| Spec Folder | Feature Group | Primary Output | Depends On | Touches (Isolation Zone) | Status |
|---|---|---|---|---|---|
| [`docs/specs/00-scaffold/`](specs/00-scaffold/tasks.md) | Foundation Scaffold | 5-module Gradle setup, CI privacy checks, test harness, face detector comparison spike | None | Root, build-logic, all modules | 📋 Ready for `/build` |
| [`docs/specs/01-eye-engine/`](specs/01-eye-engine/requirements.md) | Procedural Eye Engine | Compose Canvas eyes, blink, saccades, time-based animation, desk mode | `00-scaffold` | `:core:motion`, `:ui:eyes` | 📋 Spec ready |
| [`docs/specs/02-eye-modes/`](specs/02-eye-modes/requirements.md) | Data-Driven Eye Modes | Neutral IDs, mode catalog, swappable label resource, mode picker UI | `01-eye-engine` | `:core:model`, `:ui`, `:feature:companion` | 📋 Spec ready |
| [`docs/specs/03-camera-face-detection/`](specs/03-camera-face-detection/requirements.md) | On-Device Vision Pipeline | CameraX ImageAnalysis, bundled offline detector, indicator, no frame storage | `00-scaffold` | `:platform:vision`, `:core:model` | 📋 Spec ready |
| [`docs/specs/04-gaze-tracking-reactions/`](specs/04-gaze-tracking-reactions/requirements.md) | Gaze Tracking & Reactions | Face-to-gaze mapping, One-Euro smoothing, Awakening overlay, touch/poke reactions | `01-eye-engine`, `03-camera` | `:core:behavior`, `:feature:companion` | 📋 Spec ready |
| [`docs/specs/05-settings-privacy/`](specs/05-settings-privacy/requirements.md) | Settings & Privacy | DataStore persistence (5 keys), camera toggle, reduced motion, storage disclosure | `00-scaffold`, `02-modes` | `:platform:data`, `:feature:settings` | 📋 Spec ready |
