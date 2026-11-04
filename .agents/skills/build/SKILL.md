---
name: build
description: >-
  Execute tasks or coupled waves for a feature in docs/specs/<NN-feature-slug>/tasks.md
  (including Phase 0 00-scaffold setup) OR orchestrate an entire multi-feature Wave/Phase
  automatically using fresh per-feature subagents. Use when implementing a feature,
  building a Wave or Phase (e.g., /build Phase 1, /build all), scaffolding a project,
  or when the user invokes /build.
---

# ⚡ Skill: Feature & Wave Builder (`/build`)

This skill supports **two execution modes**:
- **Mode A — Single-Feature Build (`/build` or `/build <NN-feature-slug>`):** Builds `00-scaffold` or a single feature spec inside the current session.
- **Mode B — Multi-Feature Wave Orchestrator (`/build Phase 1`, `/build Wave 1`, or `/build all`):** Orchestrates **2 to 15+ features** hands-off without context bloat by spawning a **fresh `self` subagent per feature** wave-by-wave!

---

## 🚀 Mode B — Multi-Feature Wave Orchestrator (When Building 2+ Features / A Whole Wave or Phase)

> **Why this mode exists:** Building 5+ features inside a single chat session bloats the context window and degrades code quality on later features. In Mode B, the **Main Agent acts strictly as the Wave Manager** (staying under 10% context usage) and delegates each feature to a **fresh subagent** (`invoke_subagent` in Antigravity, or the `task` / subagent tool in OpenCode, Claude Code, and Codex).
>
> **🔁 BUILT-IN AUTONOMOUS GOAL LOOP (Works in OpenCode, Claude Code, Codex & Antigravity):**
> When Mode B is triggered (e.g., `/build Phase 1` or `/build all`), **DO NOT STOP between tasks or between waves to ask `"Would you like me to continue to the next wave?"`**. Keep executing Wave 0 → Wave 1 → Wave 2 → Wave N continuously until every requested feature in `docs/planning/roadmap.md` is marked `[x]` and root verification passes!

1. **Wave 0 Gate (`00-scaffold`):**
   - Check `docs/planning/roadmap.md`. If `00-scaffold` is not yet `[x]`, execute **Step 2 (`00-scaffold`)** first (or via a single subagent) and verify `pnpm tsc --noEmit && pnpm build` before launching Wave 1.
2. **Group Unfinished Features by Wave:**
   - Read the **Feature Specs Registry & Dependency Matrix** in `docs/planning/roadmap.md`.
   - Identify all features in the current unblocked `Wave` (`Wave 1`, then `Wave 2`, etc.) whose `Depends On` prerequisites are all `[x]`.
3. **Dispatch Fresh Subagents Per Feature (`invoke_subagent` / `task` tool):**
   - Mark the current Wave's features `[>]` in `docs/planning/roadmap.md`.
   - For each feature in the current Wave, spawn a fresh subagent:
     - **In Antigravity:** Call `invoke_subagent` (`TypeName: "self"`, `Model: "inherit"`, `Role: "<Feature Name> Builder"`).
     - **In OpenCode / Claude Code / Codex:** Call the `task` subagent tool (emit multiple `task` tool calls in a single response to run same-wave features in parallel!).
   - Pass each subagent this exact scoped prompt:
     ```text
     Execute feature `<NN-feature-slug>` end-to-end without stopping:
     1. Read `AGENTS.md`, `docs/foundation/`, and `docs/specs/<NN-feature-slug>/`.
     2. If `design.md` or `uat.md` is missing in `docs/specs/<NN-feature-slug>/`, run `.agents/skills/spec/SKILL.md` first.
     3. Run `.agents/skills/build/SKILL.md` (Step 3) to implement all tasks and unit tests in `docs/specs/<NN-feature-slug>/tasks.md`. (If `design-reference/` has no screenshots, build UI directly from `docs/foundation/design-system.md` without pausing.)
     4. Run `.agents/skills/verify/SKILL.md` and update `docs/specs/<NN-feature-slug>/uat.md`.
     5. STRICT ISOLATION & GIT RULE: Only create/modify files inside `docs/specs/<NN-feature-slug>/` and your assigned `Touches` paths (`<Touches>`). Do NOT edit `docs/planning/roadmap.md`, and do NOT run `git add` or `git commit` inside a subagent (the Main Agent commits each feature cleanly after wave verification).
     ```
   - *(In Antigravity: end your turn after launching the Wave's subagents and wait for the automatic wakeup notification. In OpenCode / Claude Code: the `task` tool returns directly when the subagents finish, so proceed immediately to Step 4 in the next step!)*
4. **Inter-Wave Root Checkpoint (Unit + Playwright E2E), Path-Scoped Per-Feature Commits & Automatic Next-Wave Continuation:**
   - When the Wave's subagents finish, run root verification once:
     - `pnpm tsc --noEmit && pnpm test --run && pnpm build` (or `php -l` + `vendor/bin/phpunit` / `pytest` / `forge test`).
     - If an `e2e/` directory with Playwright specs (`e2e/*.spec.ts`) exists, run `pnpm test:e2e` (`npx playwright test`) once at the Wave boundary (so parallel subagents never collide on `localhost:3000`).
   - Run **`/ship`** (`../ship/SKILL.md`) to mark the completed Wave features `[x]` in `docs/planning/roadmap.md` and create **1 separate, path-scoped commit per verified feature** (never lump multiple features into one giant commit, and never run `git add .` across unverified files):
     ```bash
     git add <Touches-for-feature-A> docs/specs/<NN-feature-A>/
     git commit -m "feat(<NN-feature-A>): <summary> (Req 1.1-X.Y)"
     ```
   - **Immediately and automatically launch the next Wave** in `docs/planning/roadmap.md` (repeating Step 3) without pausing for user confirmation, stopping only when all requested features are `[x]` or if a 3-Strike blocker occurs.

---

## 🎯 Mode A — Single-Feature Build (Step-by-Step)

### Step 1 — Check Dependency Wave & Claim Feature

1. Read `docs/planning/roadmap.md` (**Feature Specs Registry & Dependency Matrix**):
   - **If `00-scaffold` is still `[ ]`:** Target `docs/specs/00-scaffold/tasks.md` first.
   - **Otherwise:** Select the requested (or next unblocked) feature whose `Depends On` prerequisites are all `[x]`.
   - **Multi-Dev / Parallel Subagent Check:** Ensure no other active (`[>]`) feature has overlapping `Touches (Isolation Zone)` paths. Mark the feature row `[>]` in `docs/planning/roadmap.md` with your branch/owner *(skip editing `roadmap.md` if running as an isolated subagent in Mode B)*.
2. Check `docs/specs/<NN-feature-slug>/`:
   - If `design.md` does not exist yet (for features `01+`), run the **`/spec`** skill (`../spec/SKILL.md`) first to generate `design.md`, `design-reference/stitch-prompt.md`, `tasks.md`, and `uat.md`.

---

### Step 2 — Phase 0 (`00-scaffold`) Execution (Only When `00-scaffold` Is Active)

When executing `docs/specs/00-scaffold/tasks.md`:
1. Read `docs/foundation/architecture.md`, `docs/foundation/tech-stack.md`, and `docs/foundation/design-system.md` (plus `docs/foundation/design-reference/` if global app shell screenshots exist).
2. Scaffold the base application and copy canonical starter patterns from [`./examples/`](./examples/) (e.g., Supabase clients, RLS policies, AI client, design tokens, or WP plugin main/loader).
3. Install only the approved packages listed in `docs/foundation/tech-stack.md` (including `vitest` so `pnpm test --run` is ready for feature unit tests).
4. Run full verification (`pnpm tsc --noEmit && pnpm test --run && pnpm build` or `php -l`), mark all tasks `[x]` in `docs/specs/00-scaffold/tasks.md` and `docs/planning/roadmap.md`, and update `AGENTS.md` Project State to `scaffolded` / `active`.

---

### Step 3 — Feature Task & Wave Execution (`01+` Specs)

Read `docs/specs/<NN-feature-slug>/design.md`, `docs/specs/<NN-feature-slug>/tasks.md`, and `.agents/rules/stack-rules.md`.

#### 🎨 Visual Design Gate (Before Executing `[FE]` Frontend UI Tasks)
Before implementing any `[FE]` UI task in `tasks.md`, inspect `docs/specs/<NN-feature-slug>/design-reference/`:
- **If approved screenshots (`*.png` / `*.jpg`) or Stitch HTML exports (`*.html`) exist in `design-reference/`:**
  1. Read the image file(s) (`read_file` views `.png`/`.jpg` directly) and any `.html` reference file(s) in `docs/specs/<NN-feature-slug>/design-reference/`.
  2. Replicate the visual layout, component hierarchy, spacing, and design tokens 1:1 while refactoring any raw Stitch HTML into clean, modular components inside the feature's isolation zone (following `.agents/rules/stack-rules.md`).
- **If NO `.png`, `.jpg`, or `.html` visual reference exists in `design-reference/` yet:**
  - **In interactive single-feature runs (without `/goal`):** Pause before executing `[FE]` tasks and ask the user:
    > *"⚠️ VISUAL DESIGN GATE: No approved UI screenshot/HTML found in `docs/specs/<NN-feature-slug>/design-reference/`. Would you like to paste `design-reference/stitch-prompt.md` into Google Stitch (or drop client Figma screenshots into `design-reference/`) first, or reply **'proceed'** to build the UI directly from `docs/foundation/design-system.md`?"*
  - **In autonomous `/goal` or Mode B subagent runs:** Automatically proceed using the tokens, colors, and component rules in `docs/foundation/design-system.md`.

#### ⚙️ Task & Automatic Test Execution Loop
Execute pending tasks according to the intra-feature wave order (`## 🔀 Task Dependency Graph`) and your model mode:
1. **Mark Active (`[>]`):** Mark the current sub-task (`1.1`) or coupled `⚡ BATCH` group (`1.1`, `1.2`, `1.3`) as `[>]` in `docs/specs/<NN-feature-slug>/tasks.md`.
2. **Use Stack Examples (`./examples/`):** Inspect [`./examples/`](./examples/) and adapt the matching stack template (including `feature.test.ts.template` or `unit-test.php.template` for unit tests).
3. **Implement Code + Automated Unit Tests Within Isolation Zone:**
   - Create or modify only the exact files listed in `design.md` (`## 2. File Placement`), enforcing every traced EARS requirement (`_Requirements: X.Y_`).
   - Write the feature's unit/invariant tests in `src/features/<domain>/__tests__/<feature>.test.ts` (or `tests/Unit/<Domain>/<Feature>Test.php`) covering `Property 1` (valid inputs) and `Property 2` (invalid/unauthorized rejection).
4. **Verify Every Task / Batch (Types + Automated Tests):**
   - **Next.js / TypeScript:** Run `pnpm tsc --noEmit && pnpm test --run` (and `pnpm build` at Checkpoints).
   - **WordPress / PHP:** Run `php -l <file.php>` on all touched PHP files (and `vendor/bin/phpunit` when configured).
   - If an error or failing test occurs, fix it immediately (respecting the **3-Strike Rule** in `.agents/rules/execution-protocol.md`).
5. **Mark Complete (`[x]`) & Auto-Checkpoint Commits:**
   - Once verification and unit tests pass, mark the task(s) `[x]` in `docs/specs/<NN-feature-slug>/tasks.md` and append a row to `## 📝 Feature Activity Log`.
   - **Atomic Checkpoint Commits (Mode A — Single-Feature Build):**
     Whenever a `[REVIEW]` checkpoint task (e.g. `1.4`, `2.3`, `3.3`) passes verification:
     - Automatically create a clean, path-scoped Git checkpoint commit for that completed batch:
       ```bash
       git add <touched-files-for-this-batch> docs/specs/<NN-feature-slug>/tasks.md
       git commit -m "feat(<NN-feature-slug>): <batch summary> (Tasks X.Y-X.Z)"
       ```
     *(This ensures working backend logic and intermediate UI sub-tabs are safely committed to Git without waiting for the entire feature to finish!)*

---

### Step 4 — Hand Off to `/verify` & Auto-Ship

When all implementation tasks in `docs/specs/<NN-feature-slug>/tasks.md` are marked `[x]`:
1. Run the **`/verify`** skill (`../verify/SKILL.md`) to execute Level 1, Level 2, and Level 3 verification and update `docs/specs/<NN-feature-slug>/uat.md`.
2. **Automatic Ship:** If `/verify` passes (`✅ PASS`), **immediately run `/ship`** (`../ship/SKILL.md`) automatically without pausing to ask the user to type `/ship`:
   - Marks the feature `[x]` in `docs/planning/roadmap.md`.
   - Updates `AGENTS.md` project state.
   - Creates the final feature completion commit for any remaining spec/uat updates:
     ```bash
     git add docs/specs/<NN-feature-slug>/ docs/planning/roadmap.md AGENTS.md <Touches>
     git commit -m "feat(<NN-feature-slug>): complete <Feature Name> (Req 1.1-X.Y)"
     ```
   - Prints the final completion report!
