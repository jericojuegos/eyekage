# 📋 Backlog & Anti-Hallucination Scope — EyeKage

## 1. Deferred Feature Backlog (Phases 2–6)

These capabilities are officially planned in the product vision, but are strictly out of scope for Phase 1 MVP:

| Feature / System | Target Phase | Description & Prerequisites |
|---|---|---|
| **On-Device Voice Commands** | **Phase 2** | Wake words & commands ("EyeKage, Sharingan!", "Sleep", "Blink"). Requires on-device ASR / keyword spotter (`Vosk` / `Sherpa-ONNX`) and `RECORD_AUDIO` permission. |
| **Rule-Based Personality Engine** | **Phase 2** | Mood states, spontaneous expressions, idle personality habits. Plugs into `:core:behavior`. |
| **Inspectable Local Memory** | **Phase 2** | Persistent interaction history, favorite mode frequency, transparent rules. Must include user "Inspect / Reset / Disable Memory" UI. |
| **Head Pose & Gesture Recognition** | **Phase 3** | Pitch/yaw/roll head estimation, hand gestures (wave, peace, palm). MediaPipe Tasks Vision Gesture/Pose. |
| **Presence & Departure Detection** | **Phase 3** | Person counting, room entry/departure awareness without facial identification. |
| **Fatigue & Engagement Signals** | **Phase 3** | Prolonged eye closure, blink frequency, yawning. Must be framed as non-medical, uncertain estimates. |
| **Hybrid AI / LLM Integration** | **Phase 4** | Cloud LLM provider interface (`:core:ai`) for natural voice conversation. Requires user approval for `INTERNET` permission. |
| **Home Intelligence Gateway Client** | **Phase 5** | Consumes structured CCTV/smart home events from local gateway (Frigate, RTSP, ONVIF, Home Assistant via MQTT/WebSockets). |
| **Native iOS Application** | **Phase 6** | Swift, SwiftUI, AVFoundation, Apple Vision. Evaluates KMP logic reuse from `:core`. |
| **Physical Companion Hardware** | **Future** | Embedded Linux / RTOS robot chassis with servo-driven neck and stereo cameras. |

---

## 2. Explicitly Rejected & Forbidden Items (Anti-Hallucination List)

Any pull request, subagent task, or patch introducing the following items in Phase 1 is a **hard failure**:

| Rejected Feature / Pattern | Reason for Exclusion |
|---|---|
| 🚫 **Network Access (`INTERNET`)** | Phase 1 is 100% offline. Zero network calls, telemetry, or remote dependencies. |
| 🚫 **Microphone & Audio (`RECORD_AUDIO`)** | Zero audio capabilities in Phase 1. Haptics only. |
| 🚫 **Facial Identification / Recognition** | EyeKage detects *where* a face is, never *who* it is. No face embedding databases. |
| 🚫 **Saving Camera Frames or Face Crops** | Zero image frames may be stored on disk. Vision outputs normalized bounds only. |
| 🚫 **Cloud Backends (Supabase, Firebase, AWS)** | No cloud databases, remote storage, or user authentication in Phase 1. |
| 🚫 **Cross-Platform Frameworks (Flutter, RN, KMP)** | Android-first native execution in Phase 1. Do not introduce KMP or multiplatform plugins. |
| 🚫 **CCTV / RTSP Stream Decoding on Phone** | Phones must not process multi-camera RTSP video feeds. Handled by future Phase 5 gateway. |
| 🚫 **Background Camera Processing** | Camera must stop immediately at `ON_STOP`. No foreground camera services. |
| 🚫 **Payments, Ads, In-App Billing (Stripe)** | No monetization or commercial billing infrastructure in Phase 1 prototype. |
| 🚫 **Anime Trademarks in Code Identifiers** | Mode names (Sharingan, Byakugan) must never appear in package, class, or variable names. |
| 🚫 **Gamified Pet Stats (Hunger, Energy, Streaks)** | EyeKage is an ambient companion, not a high-pressure digital pet demanding daily chores. |

