# 🧠 EyeKage — Agent Brain (`AGENTS.md`)

**Project:** EyeKage — Intelligent Virtual Robot Companion & Personal AI Assistant
**Context:** Personal (`contexts/personal.md`)
**Stack:** Native Android (Kotlin 2.x · Jetpack Compose · Material 3 · Coroutines / Flow · CameraX ImageAnalysis · Gradle Kotlin DSL)
**Target Platforms:** Android Phones & Tablets (Min SDK 26 · Target SDK 34/35 · Baseline Device: realme C55 / RMX3710)
**Architecture Pattern:** Layered Modular MVVM / UDF with Pure-Kotlin Core ("Portable Core, Platform Shell")
**Master Brain:** `android-kotlin-compose`
**Last synced:** `android-kotlin-compose-v2.1` — 2026-10-31

---

## 🚦 Project State

**Stage:** `active` (Phase 1 MVP Complete & Shipped)
**Stage last updated:** `2026-11-01`

| Stage | Meaning | Next Action / Skill |
|---|---|---|
| `initialized` | Brain & specs ready, Phase 0 not built yet | Await Gate E approval, then run **`/build`** on `docs/specs/00-scaffold/tasks.md` |
| `scaffolded` | Base 5-module project, CI guards & detector comparison verified | Run **`/spec`** or **`/build`** on Wave 1 features in `docs/planning/roadmap.md` |
| `active` | Feature wave development in progress | **`/spec`** → **`/build`** → **`/verify`** → **`/ship`** |

---

## ⚡ Deterministic Verification Commands

Run these commands after every task before marking `[x]`:
- **Kotlin Compile & Typecheck (Every task):** `./gradlew compileDebugKotlin`
- **Unit Tests (Core & Feature logic):** `./gradlew testDebugUnitTest`
- **Static Analysis & Lint:** `./gradlew detekt lintDebug`
- **Privacy & Manifest Allowlist Guard:** `./gradlew checkMergedManifestPermissions`
- **Full Verification (`/verify`):** `./gradlew check testDebugUnitTest`

---

## 🗺️ Context Map (`docs/INDEX.md`) & Skills (`.agents/skills/`)

> **Progressive Disclosure Rule:** Do not read all docs at once. Read [`docs/INDEX.md`](docs/INDEX.md) or use this quick table:

| Question / Action | File or Skill |
|---|---|
| What is the phased roadmap & feature dependency wave matrix? | [`docs/planning/roadmap.md`](docs/planning/roadmap.md) |
| Phase 1 MVP feature acceptance criteria checklist | [`docs/planning/phase-1-acceptance.md`](docs/planning/phase-1-acceptance.md) |
| Create or expand a feature spec (`requirements`, `design`, `tasks`, `uat`) | **`/spec`** ([`.agents/skills/spec/SKILL.md`](.agents/skills/spec/SKILL.md)) |
| Build Phase 0 scaffold or execute tasks/waves for a feature | **`/build`** ([`.agents/skills/build/SKILL.md`](.agents/skills/build/SKILL.md)) |
| Verify build, EARS acceptance criteria, and stack Hard Failures | **`/verify`** ([`.agents/skills/verify/SKILL.md`](.agents/skills/verify/SKILL.md)) |
| Ship completed feature, update `roadmap.md`, and generate PR summary | **`/ship`** ([`.agents/skills/ship/SKILL.md`](.agents/skills/ship/SKILL.md)) |
| What are we building overall & why? | [`docs/foundation/prd.md`](docs/foundation/prd.md) |
| Where do files live & what is the module layout? | [`docs/foundation/architecture.md`](docs/foundation/architecture.md) |
| What libraries, minSdk, constants & permissions exist? | [`docs/foundation/tech-stack.md`](docs/foundation/tech-stack.md) |
| What are the Compose tokens, theme & visual tone? | [`docs/foundation/design-system.md`](docs/foundation/design-system.md) |
| Why was X decided this way? | [`docs/foundation/decisions.md`](docs/foundation/decisions.md) |
| How do we execute tasks & waves? | [`.agents/rules/execution-protocol.md`](.agents/rules/execution-protocol.md) |
| What are the Android layer, Compose & privacy rules? | [`.agents/rules/stack-rules.md`](.agents/rules/stack-rules.md) |

---

## ⚠️ Critical Constraints — Never Violate

1. **Pure-Kotlin Core Rule:** The `:core` module must NEVER import Android SDK classes (`android.*`, `androidx.*`, CameraX, Compose, View, Context, ML Kit). It must remain 100% pure Kotlin / JVM.
2. **Strict Manifest Allowlist (CAMERA Only):** Merged debug and release manifests must NEVER contain `INTERNET`, `ACCESS_NETWORK_STATE`, or any unapproved permission. Only `android.permission.CAMERA` is allowed in Phase 1 (`VIBRATE` is avoided via `View.performHapticFeedback`).
3. **No Camera Frames or Biometric Persistence:** Never save raw camera frames, bitmaps, facial crops, or biometric identity embeddings to disk or databases. The vision pipeline only emits normalized numerical coordinates.
4. **Isolated Rendering & Camera:** The eye renderer in `:ui` and `:feature` must NEVER depend on CameraX or detector classes. It only reads normalized numerical target flows (`GazeTarget`, `Expression`). The animation runs at display frame rate (60/90 Hz) even if the camera is off or running slowly.
5. **Data-Driven Modes & Neutral IDs:** All mode identifiers in code must use neutral IDs (`mode.normal`, `mode.crimson_tracker`, `mode.white_scan`, `mode.purple_orbit`, `reaction.awakening`). Anime-inspired names are display-only labels confined to a single swappable resource file (`res/values/mode_labels.xml`).
6. **Stop & Ask on Scope or Permission Changes:** If an implementation or dependency suggests adding a new permission, background service, cloud dependency, or out-of-scope feature, STOP and obtain explicit user approval.
7. **Offline-First by Design:** The entire Phase 1 app must install and run with zero network connectivity from the first launch.

---

## 🚫 Anti-Hallucination List (Out of Scope for Phase 1)

Do NOT build or import in Phase 1 MVP:
- ❌ No `INTERNET` permission, analytics, crash reporting (Firebase/Crashlytics), or telemetry services.
- ❌ No audio permissions, microphone access (`RECORD_AUDIO`), or voice recognition.
- ❌ No cloud backend, Supabase, Firebase, or account/authentication system.
- ❌ No cross-platform abstractions (KMP, Flutter, React Native).
- ❌ No facial identification or biometric recognition (face detection/gaze tracking only).
- ❌ No emotion detection, sleepiness/fatigue estimation (deferred to Phase 3).
- ❌ No CCTV, RTSP, ONVIF, Frigate, or Home Assistant integration (deferred to Phase 5 Home Gateway).
- ❌ No in-app purchases, billing SDKs, Stripe, or monetization logic.
- ❌ No background camera processing or background services.
- ❌ No persistent memory, adaptive learning, or virtual-pet hunger/energy meters (deferred to Phase 2).
