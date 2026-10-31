# ✅ Phase 0 Tasks: Foundation Scaffold & Detector Spike (`00-scaffold`)

> **Phase / Wave:** `Phase 0 (Wave 0)` · **Branch / Owner:** `feat/00-scaffold` · **Status:** `[ ]` Ready for `/build` (Gate E)  
> **Depends On:** None (Approved Brainstorm) · **Touches:** Root build files, `build-logic/`, `:core`, `:ui`, `:platform`, `:feature`, `:app`  
> **Key References:** [`docs/foundation/architecture.md`](../../foundation/architecture.md) · [`docs/foundation/tech-stack.md`](../../foundation/tech-stack.md) · [`docs/foundation/decisions.md`](../../foundation/decisions.md)

---

## 🔄 Implementation Tasks

### 1. Development Environment & Device Baseline (`⚡ BATCH`: Local Prerequisites)
- [x] **1.1** `[BE]` Verify Android Studio, Android SDK (`platforms;android-34`, `build-tools;34.0.0`), and JDK 17+ (JBR) on host machine.
- [x] **1.2** `[BE]` Configure `ANDROID_HOME` and verify `adb` recognizes connected **RMX3710** baseline device via `adb devices -l`.
- [x] **1.3** `[BE]` Capture baseline device telemetry via `adb shell`: Android OS release, SDK API level, SoC, display refresh rates, and front camera HAL hardware level.
- [x] **1.4** `[REVIEW]` **Environment Checkpoint:** Host toolchain verified, physical test device detected.

### 2. Gradle Build System & 5-Module Scaffold (`⚡ BATCH`: Modular Structure)
- [ ] **2.1** `[BE]` Initialize Gradle 8.9+ wrapper and `settings.gradle.kts` including the 5 modules: `:core`, `:ui`, `:platform`, `:feature`, `:app`.
- [ ] **2.2** `[BE]` Create version catalog `gradle/libs.versions.toml` with pinned dependencies (Kotlin 2.0+, Compose BOM, CameraX 1.4, DataStore 1.1, test libraries).
- [ ] **2.3** `[BE]` Configure `:core/build.gradle.kts` with `kotlin("jvm")` ONLY (zero Android SDK dependencies).
- [ ] **2.4** `[FE]` Configure `:ui/build.gradle.kts` with Compose Material 3 and theme tokens.
- [ ] **2.5** `[BE]` Configure `:platform/build.gradle.kts` with CameraX and DataStore.
- [ ] **2.6** `[FE]` Configure `:feature/build.gradle.kts` and `:app/build.gradle.kts` (AppContainer composition root).
- [ ] **2.7** `[REVIEW]` **Build Checkpoint:** Run `./gradlew compileDebugKotlin` — compiles across all 5 modules with zero warnings.

### 3. CI Privacy Guards & Architecture Rules (`⚡ BATCH`: Manifest & Code Quality)
- [ ] **3.1** `[BE]` Implement `checkMergedManifestPermissions` Gradle task enforcing allowlist (`android.permission.CAMERA` only) and denylist (`INTERNET`, `ACCESS_NETWORK_STATE`, `RECORD_AUDIO`).
- [ ] **3.2** `[BE]` Configure `detekt` with `ForbiddenImport` rule ensuring `:core` never imports `android.*` or `androidx.*`.
- [ ] **3.3** `[BE]` Configure GitHub Actions CI workflow (`.github/workflows/ci.yml`) running compile, unit tests, detekt, and manifest permission checks.
- [ ] **3.4** `[REVIEW]` **Quality Checkpoint:** Run `./gradlew detekt checkMergedManifestPermissions` — must pass cleanly.

### 4. Face Detector Comparison Spike (`⚡ BATCH`: MediaPipe vs. ML Kit Empirical Test)
- [ ] **4.1** `[BE]` Define `FaceObservationSource` interface in `:core:ports` emitting normalized `FaceObservation` streams.
- [ ] **4.2** `[BE]` Implement `detectorMediapipe` flavor in `:platform:vision` using MediaPipe Tasks Vision with bundled `.tflite` model asset.
- [ ] **4.3** `[BE]` Implement `detectorMlkit` flavor in `:platform:vision` using Bundled ML Kit.
- [ ] **4.4** `[BE]` Execute offline installation test on RMX3710 in Airplane Mode: verify inference works on first run without network.
- [ ] **4.5** `[BE]` Profile APK size, latency (p50/p95), CPU, and thermal behavior over a 15-minute test run on RMX3710.
- [ ] **4.6** `[REVIEW]` **Spike Decision Checkpoint:** Document findings in `docs/foundation/decisions.md` (ADR-004 update). Select winning detector; remove loser flavor.

---

## 🔀 Task Dependency Graph (Intra-Feature Waves)

```json
{
  "waves": [
    { "id": 0, "tasks": ["1.1", "1.2", "1.3"] },
    { "id": 1, "tasks": ["2.1", "2.2", "2.3", "2.4", "2.5", "2.6"] },
    { "id": 2, "tasks": ["3.1", "3.2", "3.3"] },
    { "id": 3, "tasks": ["4.1", "4.2", "4.3", "4.4", "4.5", "4.6"] }
  ]
}
```

