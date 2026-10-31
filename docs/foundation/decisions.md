# 📜 Architecture Decision Records (ADRs) — EyeKage

## ADR-001: Native Android (Kotlin + Jetpack Compose) First

- **Status:** Accepted (2026-10-31)
- **Context:** EyeKage eventually needs to support iOS, tablets, and potential physical robotics. We evaluated Kotlin Multiplatform (KMP), Flutter, React Native, and Native Android.
- **Decision:** Build Phase 1 strictly as a Native Android application using Kotlin 2.x and Jetpack Compose.
- **Consequences:**
  - *Positive:* Direct, unhindered access to CameraX low-latency frames, zero bridge overhead, direct hardware haptics, and native frame-clock synchronization.
  - *Positive:* Eliminates speculative multiplatform setup overhead during initial prototyping.
  - *Negative:* iOS requires a separate native implementation or future KMP migration (planned for Phase 6).
  - *Mitigation:* Architecture ADR-002 enforces a pure-Kotlin domain core to maximize future logic sharing.

---

## ADR-002: "Portable Core, Platform Shell" (Pure-Kotlin Core)

- **Status:** Accepted (2026-10-31)
- **Context:** To ensure core logic (math, behavior, state machines) does not become coupled to Android-specific APIs.
- **Decision:** Enforce a strict 5-module boundary where `:core` is a pure Kotlin/JVM module with zero `android.*` or `androidx.*` dependencies.
- **Consequences:**
  - *Positive:* All motion algorithms (One-Euro filter, saccades), state machines, and data models can be shared via KMP or ported to Swift without rewrites.
  - *Positive:* Unit tests in `:core` run instantaneously on the host JVM without Robolectric or emulator requirements.
  - *Negative:* Requires explicit interface/port definitions and mapping between Android CameraX objects and domain models.

---

## ADR-003: Neutral Mode IDs & Rebrand Safety Separation

- **Status:** Accepted (2026-10-31)
- **Context:** Initial prototype modes are inspired by anime visual styles (Sharingan, Byakugan, Rinnegan). Using these names in code creates intellectual property risks and makes rebranding difficult.
- **Decision:** All codebase identifiers, classes, enums, and tests must use neutral semantic IDs (`mode.normal`, `mode.crimson_tracker`, `mode.white_scan`, `mode.purple_orbit`, `reaction.awakening`). Display names are confined strictly to an isolated string resource file (`res/values/mode_labels.xml`).
- **Consequences:**
  - *Positive:* Swapping to original commercial branding requires altering a single XML resource file, with zero code or test refactoring.
  - *Positive:* Protects codebase from IP leakage during external developer or investor reviews.

---

## ADR-004: Face Detector Evaluation Gate (MediaPipe vs. Bundled ML Kit)

- **Status:** Accepted (2026-10-31)
- **Context:** ML Kit bundled face detection is known to pull in Google Data Transport telemetry and `INTERNET` permissions via manifest merger. MediaPipe Tasks Vision provides fully offline inference with local `.tflite` assets, but its merged manifest must be empirically verified.
- **Decision:** Conduct a Phase 0 spike comparing MediaPipe Tasks Vision Face Detector vs. ML Kit Bundled behind a common `FaceObservationSource` port. MediaPipe is preferred if it passes all offline and privacy checks. If neither passes without fragile workarounds, development must stop and present findings.
- **Consequences:**
  - *Positive:* Prevents silent telemetry or unapproved permissions from entering the application.
  - *Positive:* Clean port abstraction allows swapping detector engines without modifying eye rendering.

---

## ADR-005: Zero Internet & Enforceable Manifest Allowlist in CI

- **Status:** Accepted (2026-10-31)
- **Context:** EyeKage is an offline-first companion that processes live camera feeds. User trust requires absolute technical proof that camera data never leaves the device.
- **Decision:** Declare zero network permissions in the application manifest. Implement an automated Gradle task (`checkMergedManifestPermissions`) that inspects the final merged manifest in CI and fails the build if `INTERNET`, `ACCESS_NETWORK_STATE`, or unapproved services appear.
- **Consequences:**
  - *Positive:* Ironclad privacy guarantee that can be independently audited.
  - *Positive:* Prevents third-party library updates from silently injecting analytics or network loggers.
  - *Negative:* Cloud AI features (Phase 4) will require explicit architectural review and user permission before introduction.

---

## ADR-006: Procedural Compose Canvas Eye Renderer with Optional AGSL

- **Status:** Accepted (2026-10-31)
- **Context:** Evaluated Rive, Lottie, OpenGL ES, and Jetpack Compose Canvas for rendering the animated robot eyes.
- **Decision:** Use procedural Compose `Canvas` drawing with cached `Path` and `Paint` objects on the frame clock. Support AGSL `RuntimeShader` on API 33+ as an optional visual enhancement, with gradient fallbacks for older APIs.
- **Consequences:**
  - *Positive:* Zero third-party runtime dependencies (e.g. no Rive C++ runtime).
  - *Positive:* Full programmatic control over dynamic gaze coordinates, iris distortions, and eyelid squinting.
  - *Positive:* Respects Android reduced-motion settings automatically.

---

## ADR-007: Manual Dependency Injection via AppContainer

- **Status:** Accepted (2026-10-31)
- **Context:** Evaluated Hilt, Koin, and Manual DI for dependency management across the 5 modules.
- **Decision:** Use a lightweight, pure-Kotlin `AppContainer` pattern rooted in `MainApplication`.
- **Consequences:**
  - *Positive:* Zero annotation processing (KSP/KAPT), significantly faster build and compilation times.
  - *Positive:* 100% transparent object graph with zero runtime reflection.
  - *Positive:* Keeps `:core` completely free of DI framework annotations.
  - *Negative:* Requires manual wiring of dependencies when adding new services (acceptable for Phase 1 scope).

---

## ADR-008: Deferred Monetization & Lean Persistence

- **Status:** Accepted (2026-10-31)
- **Context:** Personal BFD context defaults suggest Stripe payments and Supabase authentication.
- **Decision:** Formally defer all monetization, billing SDKs, user accounts, and remote databases. Persist only 5 approved local user preferences via Jetpack Preferences DataStore.
- **Consequences:**
  - *Positive:* Zero recurring infrastructure costs, zero billing compliance overhead for Phase 1.
  - *Positive:* Maximum user privacy and frictionless app installation.

