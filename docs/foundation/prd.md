# 📄 Product Requirements Document (PRD) — EyeKage

## 1. Product Summary & Vision

**EyeKage** is an intelligent virtual robot companion and personal AI assistant that lives on Android smartphones and tablets.

At its core, EyeKage expresses itself through two animated, emotionally responsive robot eyes displayed on an OLED-black surface. Using the device's front camera and on-device machine learning, EyeKage detects the user's presence, focuses its pupils, and tracks their head movements in real-time.

### Long-Term Vision
EyeKage is designed to evolve into a full-fledged intelligent companion combining:
1. **Virtual Robot Companion & Digital Pet:** Expressive procedural facial animations, personality, and playful physical/touch reactions.
2. **Vision-Aware AI Companion:** Real-time awareness of presence, approach, departure, and user engagement.
3. **Intelligent Personal Assistant (Phases 2 & 4):** Voice interaction, transparent local memory, and contextual suggestions.
4. **Home Awareness Assistant (Phase 5):** Structured event monitoring via a local Home AI Gateway connected to CCTV / Home Assistant.

The robot's eyes and visual personality remain the central, uncompromised identity of the product across all platforms.

---

## 2. Target Users & Problem Space

### Problem Statement
Existing solutions in the digital pet and AI assistant space suffer from key gaps:
- **Novelty robot-eye apps** are static, dumb toys with no visual awareness, memory, or intelligence.
- **Mainstream AI assistants** (Siri, Google Assistant, Gemini) are faceless, transactional utilities without personality or embodied presence.
- **Physical companion robots** (Vector, EMO, Eilik) require expensive dedicated hardware, risk hardware abandonment, and raise privacy concerns with cloud processing.

EyeKage bridges this by transforming accessible consumer hardware (smartphones and tablets) into an expressive, vision-aware virtual robot companion that is 100% offline-first and privacy-respecting.

### Target User Persona
- **Primary / Initial User:** Solo creator/developer (you) validating feasibility, thermal behavior, and companion engagement on mid-range hardware.
- **Secondary Audience:** Tech enthusiasts, fans of virtual pets, interactive AI companions, and smart-home automation who own Android devices.

---

## 3. Core Principles

1. **Offline & Privacy-First:** Real-time camera processing is strictly on-device. No camera frames, face crops, or biometric embeddings are stored or transmitted. Zero network access in Phase 1.
2. **Modular Architecture:** Core behavior, motion math, and data models are pure Kotlin, strictly decoupled from Android CameraX and UI rendering to enable future iOS/desktop expansion.
3. **User-Controlled Learning:** Memory and adaptation (starting in Phase 2) must be transparent, inspectable, and completely resettable by the user.
4. **Resilient Animation Engine:** Rendering operates on the display frame clock (60/90 Hz) independent of camera inference rates. Even if the camera is disabled or lagging, EyeKage remains fluid and alive.

---

## 4. Phase 1 MVP Scope & User Journey

### Phase 1 MVP Feature Groups
1. **Procedural Eye Engine:** High-performance Canvas rendering of robot eyes (saccades, blinks, gaze coordinates, expressions).
2. **Data-Driven Eye Modes:** Four distinct selectable modes (`mode.normal`, `mode.crimson_tracker`, `mode.white_scan`, `mode.purple_orbit`) plus temporary `reaction.awakening` overlay.
3. **On-Device Vision Pipeline:** Front-camera face detection via bundled model with zero network leakage and visible camera indicator.
4. **Gaze Tracking & Reactions:** One-Euro smoothed pupil tracking, face found/lost reactions, and touch interactions (tap, poke) that work with camera off.
5. **Settings & Privacy:** Camera toggles, default mode, reduced motion support, permission-free haptics, and local storage transparency.

### Core User Journey
1. User launches EyeKage → EyeKage displays animated idle robot eyes in dark mode. No permissions are requested.
2. User taps "Look at me" → Camera permission prompt appears in context.
3. User grants permission → In-app camera indicator illuminates; front camera streams frames to local detector.
4. Face detected → EyeKage triggers the **Awakening** reaction, then smoothly locks its gaze onto the user's face.
5. User moves head left, right, up, down → EyeKage's pupils follow fluidly.
6. User obscures face or steps away → EyeKage performs a brief search animation, then returns to gentle idle blinking.
7. User taps or pokes the screen → EyeKage reacts with a playful squint, blink, and subtle haptic pulse (even with camera disabled).
8. User switches modes in the drawer → Eyes morph smoothly into crimson, white scan, or purple orbit styles.

---

## 5. Phased Product Roadmap Overview

- **Phase 0:** Foundation Scaffold & Detector Verification Spike
- **Phase 1:** Native Android MVP (Eyes, Gaze Tracking, Modes, Settings)
- **Phase 2:** Voice Commands & Rule-Based Personality Engine (Local Memory)
- **Phase 3:** Awareness+ (Head Pose, Gestures, Multi-Person, Blink/Yawn Signals)
- **Phase 4:** Hybrid AI (Provider-Agnostic LLM, Voice Conversations, Tool Use)
- **Phase 5:** Home Intelligence (Local Gateway, CCTV/RTSP, Home Assistant)
- **Phase 6:** iOS Port (SwiftUI / AVFoundation / Apple Vision)
- **Future:** Physical Robot / Hardware Integration, Commercial Branding, Monetization

