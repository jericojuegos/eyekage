# 🧪 Feature UAT & Verification: Personality Engine & Local Memory (`06-personality-memory`)

> **Feature Slug:** `06-personality-memory` · **Spec Links:** [`requirements.md`](./requirements.md) · [`design.md`](./design.md) · [`tasks.md`](./tasks.md) · [`design-reference/stitch-prompt.md`](./design-reference/stitch-prompt.md)  
> **Overall Status:** `[ ]` Pending Verification · **Target Platform:** realme C55 (RMX3710)

---

## Level 1 — Automated Terminal, Unit & Stack Checks (AI Executes)

- [ ] **Build & Compile Check:** `./gradlew compileDebugKotlin` passes with 0 errors across all modules.
- [ ] **Pure-Kotlin Core Rule:** AST/grep audit confirms 0 `android.*` or `androidx.*` imports in `:core`.
- [ ] **Privacy Allowlist Check:** `./gradlew checkMergedManifestPermissions` strictly permits `android.permission.CAMERA` only (zero network/storage/mic permissions added).
- [ ] **Automated Unit Tests:** `./gradlew testDebugUnitTest` passes for all domain, platform, and feature tests.
- [ ] **Static Analysis:** `./gradlew detekt` passes cleanly with baseline.

---

## Level 2 — Functional & EARS Requirement Verification (Smoke Test)

| Check ID | Requirement Ref | Scenario (WHEN / IF) | Expected Outcome (THE System SHALL) | Status |
| :--- | :--- | :--- | :--- | :--- |
| **UAT-1.1** | `Req 1.1, 1.2` | Face is actively tracked for $> 5\text{s}$ | `curiosity` reaches $\ge 0.8$, saccade frequency increases smoothly | `[ ]` |
| **UAT-1.2** | `Req 1.3, 2.1` | User taps/pokes the eyes repeatedly | `playfulness` increases, eyes render happy upward bottom eyelid arch | `[ ]` |
| **UAT-1.3** | `Req 1.4, 2.3` | Inactive for $> 120\text{s}$ | `energy` drops below $0.4$, eyes transition to `Drowsy` with lowered eyelids | `[ ]` |
| **UAT-1.4** | `Req 1.5` | Screen poked while companion is `Drowsy` | `energy` snaps back to $\ge 0.8$ with an immediate wake reaction | `[ ]` |
| **UAT-2.1** | `Req 2.4` | Reduced motion is active in settings | Asymmetrical eye tilts are clamped to $0^\circ$, smooth motion preserved | `[ ]` |
| **UAT-3.1** | `Req 3.1, 3.2` | Gaze tracking and pokes accumulate | `bond_tier` advances, stats persist across app restart | `[ ]` |
| **UAT-4.1** | `Req 4.1` | Settings opened | "Companion Memory & Affinity" card displays live mood and stats | `[ ]` |
| **UAT-4.2** | `Req 4.2, 4.3` | User taps "Reset Companion Memory" & confirms | Stats return to 0, bond tier returns to 1, preferences are preserved | `[ ]` |

---

## Level 3 — Visual Fidelity (`design-reference/`) & Human UX Sign-Off

- [ ] **Card Styling:** `CompanionMemoryCard` matches `docs/foundation/design-system.md` dark cyberpunk tokens (`#0A0A12` background, cyan/amber glowing chips, crisp monospace counters).
- [ ] **Micro-Expression Aesthetics:** Bottom eyelid happy crescent and curious tilt look natural and robot-like without visual clipping or jitter.
- [ ] **Zero Performance Degradation:** Display frame rate remains rock-solid at $60/90\text{ Hz}$ on RMX3710 with micro-expressions active.
- [ ] **Sign-Off:** Approved to mark `[x]` in `docs/planning/roadmap.md` via `/ship`.
