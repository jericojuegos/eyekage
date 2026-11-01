# 🧪 Feature UAT & Verification: On-Device Voice Commands (`07-voice-commands`)

> **Feature Slug:** `07-voice-commands` · **Spec Links:** [`requirements.md`](./requirements.md) · [`design.md`](./design.md) · [`tasks.md`](./tasks.md) · [`design-reference/stitch-prompt.md`](./design-reference/stitch-prompt.md)  
> **Overall Status:** `[ ]` Pending · **Verified Date:** `YYYY-MM-DD`

---

## Level 1 — Automated Terminal, Unit & Manifest Checks (AI Executes)

- [ ] **Kotlin Compile & Typecheck:** `./gradlew compileDebugKotlin` succeeds with zero errors.
- [ ] **Unit Tests (Core & Feature):** `./gradlew testDebugUnitTest` passes for all domain, platform, and feature tests.
- [ ] **Strict Manifest Allowlist Guard:** `./gradlew checkMergedManifestPermissions` verifies `android.permission.RECORD_AUDIO` and `CAMERA` are approved, and fails if `INTERNET` is present.
- [ ] **Pure-Kotlin Architecture Rule:** `:core` module contains zero imports matching `android.*` or `androidx.*`.
- [ ] **Zero Audio File I/O:** No writes to disk/cache for audio data.

---

## Level 2 — Functional & EARS Requirement Verification (Smoke Tests)

| Check ID | Requirement Ref | Scenario (WHEN / IF) | Expected Outcome (THE System SHALL) | Status |
| :--- | :--- | :--- | :--- | :--- |
| **UAT-1.1** | `Req 1.1, 1.2` | User speaks "Crimson tracker" or "Sharingan" | `VoiceCommandParser` emits `SwitchMode(EyeMode.CrimsonTracker)` | `[ ]` |
| **UAT-1.2** | `Req 1.2, 1.4` | Background noise or unregistered phrase spoken | `VoiceCommandParser` returns `null`, no exceptions, eyes unaffected | `[ ]` |
| **UAT-2.1** | `Req 2.1, 2.5` | Microphone actively records speech | Offline engine processes speech without cloud calls; no audio persisted | `[ ]` |
| **UAT-2.4** | `Req 2.4` | `RECORD_AUDIO` permission denied | System emits `Error(isPermissionError = true)` and stops gracefully | `[ ]` |
| **UAT-3.1** | `Req 3.1` | Voice commands enabled and listening | Amber microphone indicator pill illuminates in companion top bar | `[ ]` |
| **UAT-3.2** | `Req 3.2, 3.3` | User says "Awaken" | Eyes snap open in Awakening reaction, memory interactions increment $+1$ | `[ ]` |
| **UAT-3.4** | `Req 3.4` | User leaves Companion Screen (`ON_STOP`) | Speech recognition stops and microphone hardware is released immediately | `[ ]` |
| **UAT-4.1** | `Req 4.1, 4.2` | User toggles "Enable Voice Commands" in Settings | Preference persisted in DataStore; state updates reactively | `[ ]` |

---

## Level 3 — Visual Fidelity & Human UX Sign-Off

- [ ] **Amber Privacy Indicator:** Glowing amber mic pill is clearly visible in `CompanionScreen` when listening, and disappears when idle or disabled.
- [ ] **Settings Voice Card:** AMOLED dark surface card matches `docs/foundation/design-system.md` with glowing amber mic icon, toggle, command chips, and emerald privacy badge.
- [ ] **Zero Network Integrity:** App runs without network, internet permissions are absent from manifest.
