# 📐 Technical Design: Procedural Eye Engine (`01-eye-engine`)

> **Feature Slug:** `01-eye-engine` · **Requirements:** [`requirements.md`](./requirements.md)  
> **Visual Reference:** [`./design-reference/stitch-prompt.md`](./design-reference/stitch-prompt.md) (`docs/foundation/design-system.md`)  
> **Blast Radius:** `🟢 ISOLATED` (Pure-Kotlin core motion models + Compose UI Canvas renderer)

---

## 1. Architecture & Data Flow

The Procedural Eye Engine consists of three layers:
1. **Core Domain (`:core:motion`):** Pure Kotlin math and state logic. Computes low-latency, jitter-smoothed gaze coordinates using the `OneEuroFilter`, schedules autonomous natural blinks via `BlinkScheduler`, and generates subtle micro-saccades with `SaccadeGenerator`.
2. **Presentation Drawing (`:ui:eyes`):** Zero-allocation Compose Canvas renderer `RobotEyesCanvas`. Draws symmetrical robot eyes on an absolute OLED-black background (`#000000`) with diffuse outer radial glow, vibrant iris fill, and bright core highlight, modulated by eyelid clipping and pupil scale.
3. **Screen Integration (`:feature:companion` & `:app`):** `CompanionScreen` embeds `RobotEyesCanvas` and drives continuous 60/90 Hz frame animation via `withFrameNanos` / Compose animation clocks. Desk Companion Mode sets/clears `WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON` on the host window without requiring wake locks.

```mermaid
flowchart TD
    Obs["FaceObservation (Vision / Camera)"] -->|Normalized x, y| MotionEngine["EyeMotionEngine (:core:motion)"]
    Clock["Vsync Frame Clock (60/90 Hz)"] -->|delta-time / timestamp| MotionEngine
    
    subgraph CoreMotion [":core:motion (Pure Kotlin)"]
        Filter["OneEuroFilter (Jitter reduction)"]
        Blink["BlinkScheduler (Eyelid factor)"]
        Saccade["SaccadeGenerator (Micro-glances)"]
        Filter --> MotionState["EyeMotionState (gazeX, gazeY, eyelid, scale)"]
        Blink --> MotionState
        Saccade --> MotionState
    end
    
    MotionEngine --> CoreMotion
    MotionState -->|Immutable State| Canvas["RobotEyesCanvas (:ui:eyes)"]
    Canvas -->|DrawScope (No per-frame allocations)| OLED["OLED Canvas Surface (#000000)"]
```

---

## 2. File Placement, Component Interfaces & Visual Mapping

| Action | Exact File Path | Layer | Responsibility & Visual Reference | Validates |
| :--- | :--- | :--- | :--- | :--- |
| `[NEW]` | `core/src/main/kotlin/com/eyekage/core/motion/OneEuroFilter.kt` | `[BE]` Core | 1D & 2D One-Euro smoothing filter for low-latency, jitter-free gaze coordinates | `Req 1.2` |
| `[NEW]` | `core/src/main/kotlin/com/eyekage/core/motion/BlinkScheduler.kt` | `[BE]` Core | Spontaneous blink scheduler (3–7s interval, 150–200ms duration, easing, double-blinks) | `Req 2.1, 2.3` |
| `[NEW]` | `core/src/main/kotlin/com/eyekage/core/motion/SaccadeGenerator.kt` | `[BE]` Core | Micro-saccade offset generator (1.5–4.0s interval, randomized tiny offsets) | `Req 2.2, 2.3` |
| `[NEW]` | `core/src/main/kotlin/com/eyekage/core/motion/EyeMotionEngine.kt` | `[BE]` Core | Motion coordinator emitting `EyeMotionState` from target gaze, time delta, and settings | `Req 1.2, 2.1, 2.2, 2.3` |
| `[NEW]` | `core/src/test/kotlin/com/eyekage/core/motion/OneEuroFilterTest.kt` | `[BE]` Test | Frequency response, jitter elimination, and sudden movement latency tests | `Req 1.2` |
| `[NEW]` | `core/src/test/kotlin/com/eyekage/core/motion/BlinkSchedulerTest.kt` | `[BE]` Test | Interval bounds, blink curve evaluation, double-blink behavior, reduced motion | `Req 2.1, 2.3` |
| `[NEW]` | `core/src/test/kotlin/com/eyekage/core/motion/SaccadeGeneratorTest.kt` | `[BE]` Test | Saccade cadence, offset boundaries, and reduced motion suppression | `Req 2.2, 2.3` |
| `[NEW]` | `ui/src/main/kotlin/com/eyekage/ui/eyes/EyeSpec.kt` | `[FE]` UI | Data class defining eye dimensions, colors, glow radius, pupil ratio | `Req 1.1, 1.2` |
| `[NEW]` | `ui/src/main/kotlin/com/eyekage/ui/eyes/RobotEyesCanvas.kt` | `[FE]` UI | Symmetrical Compose Canvas eye renderer with radial glow, pupil offset, eyelid clipping | `Req 1.1, 1.2, 1.3` |
| `[NEW]` | `ui/src/test/kotlin/com/eyekage/ui/eyes/EyeSpecTest.kt` | `[FE]` Test | Geometry calculation tests and state boundary validations | `Req 1.1, 1.2` |
| `[MODIFY]` | `feature/src/main/kotlin/com/eyekage/feature/companion/CompanionScreen.kt` | `[FE]` UI | Mounts `RobotEyesCanvas` connected to `EyeMotionEngine` and `uiState` | `Req 1.1, 2.1` |

---

## 3. Data Models & Motion State Contracts

### Domain Models (`:core:motion`)
```kotlin
data class GazeTarget(
    val x: Float, // Normalized range [-1.0f, 1.0f]
    val y: Float  // Normalized range [-1.0f, 1.0f]
) {
    companion object {
        val Center = GazeTarget(0f, 0f)
    }
}

data class EyeMotionState(
    val gazeX: Float = 0f,           // Smoothed and saccade-adjusted X [-1f, 1f]
    val gazeY: Float = 0f,           // Smoothed and saccade-adjusted Y [-1f, 1f]
    val eyelidOpenness: Float = 1.0f, // 1.0 = fully open, 0.0 = fully closed
    val pupilScale: Float = 1.0f     // Dilation factor [0.8f, 1.2f]
)
```

### Motion Constants & Tuning
- **One-Euro Filter:**
  - $f_{c\text{min}} = 1.0\text{ Hz}$ (stationary jitter cutoff)
  - $\beta = 0.007$ (speed sensitivity coefficient)
  - $f_{c\text{der}} = 1.0\text{ Hz}$ (derivative smoothing cutoff)
- **Blink Cadence:**
  - Base Interval: Uniform random $[3.0, 7.0]\text{ s}$
  - Blink Duration: $180\text{ ms}$ (closing $80\text{ ms}$, reopening $100\text{ ms}$)
  - Double Blink Chance: $10\%$ (triggers secondary $120\text{ ms}$ blink after $100\text{ ms}$)
  - Reduced Motion Blink: Soft-fade opacity or immediate snap, no eyelid sweep.
- **Saccade Cadence:**
  - Interval: Uniform random $[1.5, 4.0]\text{ s}$
  - Offset Magnitude: Small random glance $[ -0.06\text{f}, 0.06\text{f} ]$ in normalized space
  - Reduced Motion Saccade: Completely disabled ($0.0\text{f}$).

---

## 4. Compose Rendering & Zero-Allocation Strategy

1. **DrawScope Path and Paint Caching:**
   - Pre-allocate reusable `Path` objects in `remember` blocks.
   - Use `drawRoundRect` and `drawCircle` with explicit `DrawScope` primitives where possible to leverage native hardware acceleration without allocating geometry objects per frame.
   - Brushes (radial glow, iris gradient, core gradient) are keyed on canvas size or cached across draw passes.
2. **Eyelid Clipping:**
   - Procedural vertical clipping is achieved either via `clipRect` / `clipPath` or by modulating the round rect's vertical bounds (`top` and `bottom` symmetrically approaching eye center) based on `eyelidOpenness`.
   - Modulating height via `drawRoundRect` is completely allocation-free and avoids canvas clip layers on mobile GPUs.

---

## 5. Correctness Properties & Invariants

- **Property 1 (Coordinate Invariant):** All emitted gaze coordinates $(x, y)$ from `OneEuroFilter` and `EyeMotionEngine` remain strictly bounded within $[-1.0\text{f}, 1.0\text{f}]$.
- **Property 2 (Eyelid Invariant):** `eyelidOpenness` is strictly bounded within $[0.0\text{f}, 1.0\text{f}]$, where $0.0\text{f}$ represents a fully closed eye and $1.0\text{f}$ is fully open.
- **Property 3 (Zero Allocations in Draw Loop):** The Compose Canvas draw pass performs zero object allocations per frame (`new Path`, `new Paint`, `new Matrix` are avoided).
- **Property 4 (Reduced Motion Compliance):** When `reducedMotion = true`, `SaccadeGenerator` produces zero offset ($0.0\text{f}, 0.0\text{f}$), and eyelid motion does not perform multi-frame sweeping.
- **Property 5 (Pure Kotlin Core):** No class in `:core` imports any `android.*` or `androidx.*` packages.

---

## 6. Verification Commands

- Kotlin Compile: `./gradlew compileDebugKotlin`
- Unit Tests: `./gradlew :core:testDebugUnitTest :ui:testDebugUnitTest :feature:testDebugUnitTest`
- Architecture Check: Confirm `:core` has 0 Android imports.
