# 🛠️ Technology Stack & Dependencies — EyeKage

## 1. Core Technical Specifications

| Layer | Technology | Specification / Version | Rationale |
|---|---|---|---|
| **Language** | Kotlin | 2.0+ (K2 Compiler enabled) | Modern, expressive, strict null-safety, standard for Android. |
| **Concurrency** | Kotlinx Coroutines & Flow | 1.8+ | Structured concurrency for lifecycle-bound camera streams and UI state. |
| **UI Framework** | Jetpack Compose | Compose BOM 2024.09+ | Declarative UI, programmatic Canvas drawing, fine-grained state reactivity. |
| **Design System** | Material 3 | `androidx.compose.material3` | Dark theme foundations, typography, accessible contrast. |
| **Camera Access** | CameraX | `1.4.0` (ImageAnalysis only) | Robust device compatibility layer across Android OEM quirks. |
| **Face Detection** | MediaPipe Tasks Vision vs. ML Kit Bundled | Phase 0 Comparison Spike | On-device ML. Evaluating offline privacy, latency, and telemetry. |
| **Smoothing Engine** | One-Euro Filter | Pure Kotlin in `:core:motion` | Minimizes jitter at low speeds while maintaining zero-lag during fast gaze shifts. |
| **Dependency Injection** | Manual DI (`AppContainer`) | Pure Kotlin composition root | Zero reflection, zero annotation processing (KSP/KAPT), ultra-fast builds. |
| **Preferences Storage**| Jetpack Preferences DataStore | 1.1+ | Safe, asynchronous, typed key-value storage for 5 approved keys. |
| **Build System** | Gradle Kotlin DSL | Gradle 8.9+ / AGP 8.6+ | Type-safe build scripts with `libs.versions.toml` version catalog. |

---

## 2. Platform SDK Compatibility

| Setting | Value | Notes / Justification |
|---|---|---|
| **`minSdk`** | **26 (Android 8.0 Oreo)** | Provisionally locked. Covers >95% of active devices while providing modern camera/graphics HAL support and safe library floors. |
| **`targetSdk`** | **34 / 35 (Android 14 / 15)** | Follows latest Google Play SDK standards and predictive back support. |
| **`compileSdk`** | **34 / 35** | Enables AGSL `RuntimeShader` APIs and Android 14+ system capabilities. |
| **Baseline Device** | **RMX3710 (realme C55)** | MediaTek Helio G88 (`mt6768`, 12nm) · Mali-G52 MC2 · `arm64-v8a` · 6.72″ **90 Hz / 60 Hz IPS LCD** (1080x2400, 480 dpi) · 8 MP front camera · **Android 14 (API 34)** verified via ADB. |

---

## 3. Approved Permissions (Manifest Allowlist)

The project enforces an automated CI allowlist check on merged debug and release manifests:

| Permission | Usage | Status |
|---|---|---|
| `android.permission.CAMERA` | Real-time front-camera face tracking | ✅ **Allowed** (Only requested on-demand) |
| `android.permission.INTERNET` | Network access | 🚫 **STRICTLY PROHIBITED** (Fails build) |
| `android.permission.ACCESS_NETWORK_STATE` | Network state check | 🚫 **STRICTLY PROHIBITED** (Fails build) |
| `android.permission.RECORD_AUDIO` | Microphone recording | 🚫 **PROHIBITED in Phase 1** (Revisit P2) |
| `android.permission.VIBRATE` | Device vibration | ⚠️ **Avoided** (Using `View.performHapticFeedback` without permission) |
| `android.permission.WAKE_LOCK` | Keeping screen awake | ⚠️ **Avoided** (Using Compose `keepScreenOn` window flag for desk mode) |

---

## 4. Persisted Data Keys (DataStore)

Only five keys may ever be saved to disk in Phase 1:

| Key | Type | Default | Description |
|---|---|---|---|
| `default_mode_id` | String | `"mode.normal"` | Selected default eye mode on app launch. |
| `camera_enabled` | Boolean | `false` | Whether camera tracking is active by default. |
| `reduced_motion_override` | String | `"SYSTEM"` | `"SYSTEM"` \| `"ON"` \| `"OFF"` motion settings. |
| `haptics_enabled` | Boolean | `true` | Whether subtle tap/poke haptics are enabled. |
| `desk_mode_enabled` | Boolean | `false` | Whether desk companion mode keeps screen awake. |

---

## 5. Approved Development & Test Tooling

These libraries are permitted in `testImplementation` / `androidTestImplementation` scopes only:
- `junit:junit:4.13.2` & `org.junit.jupiter:junit-jupiter` (Unit testing)
- `org.jetbrains.kotlinx:kotlinx-coroutines-test` & `app.cash.turbine:turbine` (Flow testing)
- `androidx.compose.ui:ui-test-junit4` (Compose UI testing)
- `io.github.takahirom.roborazzi:roborazzi` (Screenshot verification)
- `androidx.benchmark:benchmark-macro-junit4` & `androidx.metrics:metrics-performance` (Macrobenchmark & JankStats)
- `io.gitlab.arturbosch.detekt:detekt-gradle-plugin` (Architecture & style rules)
- `org.jlleitschuh.gradle:ktlint-gradle` (Formatting)
- `com.dropbox.dependency-guard` (Dependency drift protection)

