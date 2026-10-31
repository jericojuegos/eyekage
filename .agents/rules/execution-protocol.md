---
trigger: always_on
description: Universal BFD Execution Protocol â€” Feature-scoped specs (requirements/design/tasks/uat), model-tier modes, atomic task loop, and scope guardrails
---

# âš›ï¸ Rule: BFD Execution Protocol

**Purpose:** Deterministic, conflict-free execution across **Solo or Multi-Developer teams (2+ devs / parallel branches)** and across both **Frontier Models** (Gemini Pro/Argon, Claude Opus/Sonnet, Codex/o3, DeepSeek R1) and **Fast/Low-Tier Models** (Flash/Lite, Haiku, Grok Mini, local models).

---

## 1. Three-Folder `docs/` Brain + Two-Folder `.agents/` Engine

To prevent Git merge conflicts when 2+ developers or parallel AI agents work on the same project simultaneously, BFD separates **System Truths**, **Macro Roadmap**, **Micro Feature Specs**, and the **`.agents/` Engine**:

1. **System Foundation (`docs/foundation/`):** Project-wide permanent truths (`prd.md`, `architecture.md`, `tech-stack.md`, `design-system.md`, `decisions.md`).
2. **Macro Planning (`docs/planning/`):** Tracks project-wide phases in `roadmap.md` (including the **Feature Specs Registry & Dependency Matrix**), `backlog.md`, `history.md`, and `issues.md`.
3. **Micro Feature Specs (`docs/specs/<NN-feature-slug>/`):** Every feature group or ticket has its own isolated directory following the **4-Skill Lifecycle (`/spec` â†’ `/build` â†’ `/verify` â†’ `/ship`)**:
   - `requirements.md` â€” **1. Specify (`/spec`):** Assumptions (`A1`), Glossary, User Stories, and numbered EARS Acceptance Criteria (`Req 1.1`, `Req 1.2`)
   - `design.md` â€” **2. Design (`/spec`):** Mermaid architecture flow, exact `[NEW]` / `[MODIFY]` files, Data Models, Error Handling Matrix, and Correctness Properties
   - `tasks.md` â€” **3. Build (`/build`):** Numbered tasks (`1.1`, `1.2`) with layer tags (`[BE]`, `[FE]`), `_Requirements: X.Y_` traceability, `- [ ]*` optional test tasks, `âš¡ BATCH` tags, intra-feature `waves` JSON graph, and Activity Log
   - `uat.md` â€” **4. Verify & Ship (`/verify` â†’ `/ship`):** 3-Level verification & Hard-Failure review checklist
4. **AI Engine (`.agents/`):**
   - `.agents/rules/` â€” Always-on guardrails (`execution-protocol.md` + `stack-rules.md`)
   - `.agents/skills/` â€” 4 self-contained lifecycle skills:
     - [`spec/SKILL.md`](../skills/spec/SKILL.md) (`/spec`) + `spec/resources/*.template`
     - [`build/SKILL.md`](../skills/build/SKILL.md) (`/build`) + `build/examples/*.template`
     - [`verify/SKILL.md`](../skills/verify/SKILL.md) (`/verify`)
     - [`ship/SKILL.md`](../skills/ship/SKILL.md) (`/ship`)

> **Multi-Dev & Parallel Agent Rules:**
> 1. **How to Know Which Feature to Build First (or in Parallel):** Check the **Feature Specs Registry & Dependency Matrix** in `docs/planning/roadmap.md`. Every feature has a **`Wave`**, a **`Depends On`** column, and a **`Touches (Isolation Zone)`** column:
>    - A feature is **Ready to Build** only when **all** specs listed in its `Depends On` column are marked `[x]`.
>    - Two developers or parallel AI agents can work **simultaneously** on any two features that are both Ready to Build, have **non-overlapping `Touches` paths**, and have each claimed their row (`Status: [>]`, `Branch / Owner: feat/<slug> (@dev-or-agent)`).
> 2. **Intra-Feature Parallelism:** Inside a single feature's `tasks.md`, subagents can also execute sub-tasks in parallel according to the `## ðŸ”€ Task Dependency Graph (Intra-Feature Waves)` JSON block at the bottom of `tasks.md`.
> 3. **File Isolation During Coding:** During daily coding on a feature branch (`feat/<feature-slug>`), **all** task marker updates (`[>]` â†’ `[x]`) and Activity Log entries happen inside `docs/specs/<NN-feature-slug>/tasks.md`. Only touch `docs/planning/roadmap.md` when claiming (`[>]`) or shipping (`[x]`) a feature spec!

---

## 2. Model-Tier Execution Modes

Operate in the mode that matches your model capability (or as instructed by the user):

### A. Strict Atomic Mode *(Default â€” Mandatory for Low/Fast Models)*
- **Scope:** Execute **1 sub-task (`1.1`, `1.2`)** at a time from `docs/specs/<NN-feature-slug>/tasks.md`.
- **Context Cap:** Never load more than **4 files** at once (`AGENTS.md`, `docs/specs/<NN-feature-slug>/design.md`, `docs/specs/<NN-feature-slug>/tasks.md`, and the target code/example file).
- **Design & Example First:** Follow `design.md` and copy from `.agents/skills/build/examples/` if a matching template exists before writing custom logic.
- **Verify Every Step:** Run the stack's verification command after **every** task before marking `[x]`.

### B. Coupled Batch Mode *(Frontier Models â€” or `âš¡ BATCH` tasks)*
- **Scope:** When tasks in `docs/specs/<NN-feature-slug>/tasks.md` are marked `âš¡ BATCH` (compile-time or runtime coupled, such as DB migration + Route Handler + UI component), declare:
  > *"Executing coupled batch in `docs/specs/<NN-feature-slug>/tasks.md`: [list task IDs]."*
- **Execution:** Implement the coupled tasks together, run full verification (`build` + `typecheck` + `lint`) across the batch, then mark all completed batch tasks `[x]`.

---

## 3. The Standard Task Loop (`/build`)

For every task (or declared `âš¡ BATCH` group) in the active `docs/specs/<NN-feature-slug>/tasks.md`:

1. **ORIENT** â€” Read `docs/specs/<NN-feature-slug>/tasks.md` and `design.md` to identify the active `[>]` task (or pick the next `[ ]` task and mark it `[>]`).
2. **CHECK BOUNDARIES** â€” Verify the target file path against `docs/foundation/architecture.md` and `.agents/rules/stack-rules.md` (Layer Boundaries) and confirm the work is **not** on the `AGENTS.md` Anti-Hallucination List.
3. **IMPLEMENT** â€” Write the smallest scoped code that satisfies the task and its traced `_Requirements: X.Y_` using `docs/specs/<NN-feature-slug>/design.md` and `.agents/skills/build/examples/`.
4. **VERIFY (Mandatory)** â€” Run deterministic verification in the terminal:
   - **Next.js / TypeScript:** `pnpm tsc --noEmit` (and `pnpm build` at Checkpoints / feature completion)
   - **WordPress / PHP:** `php -l <file.php>` on all touched PHP files
   - If verification fails, **do not mark `[x]`**. Fix or follow the 3-Strike Rule below.
5. **SYNC FEATURE TRACKER** â€” Immediately update `docs/specs/<NN-feature-slug>/tasks.md` (`[>]` â†’ `[x]`) and append to its Activity Log. When all tasks in `tasks.md` are `[x]`, run **`/verify`** (`.agents/skills/verify/SKILL.md`) and **`/ship`** (`.agents/skills/ship/SKILL.md`).
6. **COMMIT (1 Scoped Commit Per Verified Feature)** â€” Once a feature passes `/verify`, commit **only** that feature's `Touches` paths and `docs/specs/<NN-feature-slug>/` (`feat(<NN-feature-slug>): <summary> (Req 1.1-X.Y)`). Parallel subagents must **never** run `git add .` or `git commit` mid-wave; only the Main Agent commits each feature after root verification passes.

---

## 4. Budget Guard & Loop Prevention

### Pause & Ask Thresholds
Pause and ask for confirmation (`"âš ï¸ BUDGET GUARD TRIGGERED: [reason]. Awaiting approval..."`) before:
1. **Unplanned Multi-File Edits:** Touching **more than 3 files** in a single turn *unless* executing a declared `âš¡ BATCH` group defined in `docs/specs/<NN-feature-slug>/tasks.md`.
2. **Destructive Actions:** Deleting files, dropping database tables/columns, or overwriting environment/config files.
3. **High-Blast-Radius Changes:** Modifying shared types (`shared/types/`), auth middleware, or database schemas without running `/spec` (Mode C: Blast-Radius Check) first.

### The 3-Strike Rule (Loop Prevention)
If the same error persists after **3 fix attempts**:
1. **STOP** immediately â€” do not attempt a 4th blind retry.
2. **REPORT** the exact error, the 3 approaches tried, and why each failed.
3. **ASK** the user for guidance.

---

## 5. Task Marker Syntax (`docs/specs/<NN-feature-slug>/tasks.md`)

| Marker | Meaning | Rule |
| :--- | :--- | :--- |
| `- [ ]` | Pending | Ready to pick up in sequence |
| `- [ ]*` | Optional Test Task | Unit / property-based test sub-task (recommended for compliance; can be skipped (`[-]`) for fast MVP) |
| `- [>]` | Active | **Only ONE `[>]` task** (or `âš¡ BATCH` group) per `docs/specs/<NN-feature-slug>/tasks.md` |
| `- [x]` | Complete | Implementation exists AND verification command passed |
| `- [!]` | Blocked | External blocker or failed verify â€” always append reason on the same line |
| `- [-]` | Deferred | Explicitly skipped â€” append reason |
| `âš¡ BATCH` | Coupled | Indicates task group is compile/runtime coupled |

---

## 6. Right-Sized Routing: New Feature vs. Bug Fix vs. Small Update

Match the skill to the size of the work:

| Scenario | Criteria | Skill to Use | Where It's Tracked |
| :--- | :--- | :--- | :--- |
| **A1. Single Roadmap Feature** | Next unblocked feature in `docs/planning/roadmap.md` | **`/spec`** (if `design.md` needed) â†’ **`/build`** (Mode A) â†’ **`/verify`** â†’ **`/ship`** | `docs/specs/<NN-feature-slug>/` |
| **A2. Multi-Feature / Full Phase (`2â€“15+ features`)** | Automating an entire Wave or Phase (`Phase 1` / `all`) | **`/goal /build Phase 1`** (Mode B Wave Orchestrator: spawns 1 fresh `self` subagent per feature wave-by-wave to prevent context bloat!) | `docs/planning/roadmap.md` + `docs/specs/<NN-feature-slug>/` |
| **B. New Unplanned Feature / Ticket** | New scope not yet in `roadmap.md` | **`/spec`** (creates spec folder + adds row to `roadmap.md`) â†’ **`/build`** | `docs/specs/<NN-feature-slug>/` + `docs/planning/roadmap.md` |
| **C. Small Update / Polish (`<= 2 files`, `< 30 min`)** | Copy tweak, UI styling adjustment, or minor validation | **Fast-Track:** Skip creating a new spec folder! Append a `- [ ]` task under `## ðŸ› Ad-Hoc / Small Enhancements` in the **existing** `docs/specs/<NN-feature-slug>/tasks.md` â†’ **`/build`** | Existing `docs/specs/<NN-feature-slug>/tasks.md` |
| **D. Bug Fix** | Broken build, runtime error, or regression | Fix + run terminal verification â†’ log under `## ðŸ“ Feature Activity Log` in the related `docs/specs/<NN-feature-slug>/tasks.md` (or run **`/spec`** for `fix-<bug-slug>` if 4+ files) | Related `docs/specs/<NN-feature-slug>/tasks.md` (plus `docs/planning/issues.md` if caused by a brain gap) |
