---
name: spec
description: >-
  Create or update a feature specification bundle in docs/specs/<NN-feature-slug>/
  (requirements.md, design.md, tasks.md, uat.md, and design-reference/stitch-prompt.md)
  using EARS acceptance criteria and visual design references, or assess mid-flight
  scope changes. Use when starting a new feature, expanding a roadmap feature before
  building, generating Google Stitch UI prompts, ingesting client Figma screenshots,
  or when the user invokes /spec.
---

# 📋 Skill: Feature Specification & UI Prompt Generator (`/spec`)

This skill manages the **Specify, Visual Reference & Design** stages of the BFD lifecycle using the templates in [`./resources/`](./resources/).

---

## Step 1 — Identify the Spec Mode

Check the user's request and `docs/planning/roadmap.md`:

- **Mode A (Expand an Existing Roadmap Feature):** `docs/specs/<NN-feature-slug>/requirements.md` and starter `tasks.md` already exist (pre-generated during project brainstorm), and need `design.md`, `uat.md`, and `design-reference/` prepared before coding.
- **Mode B (Create a Brand-New Feature Spec / Ticket):** The feature does not yet have a folder in `docs/specs/`.
- **Mode C (Mid-Flight Scope / Blast-Radius Change):** Modifying an existing feature's requirements, shared types (`shared/types/`), or database schema.
- **Mode D (Re-Open / Visual Gap Audit on an Already-Built `[x]` Feature):** A feature (`01-connections`) was already built while the user moved on to Feature 2 or 3, and the user noticed missing UI elements from `design-reference/*.png` or broken interactions.
  - **How Mode D works:**
    1. View every `.png` screenshot in `docs/specs/<NN-feature-slug>/design-reference/` and compare it against the actual built files in `includes/<feature>/` (or `src/features/<domain>/`) and `requirements.md`.
    2. Identify every missing button, icon action (e.g., Duplicate `⧉`, Delete `🗑️`, `+ Add Connection`), filter dropdown, accordion table, or broken handler.
    3. Add the missing EARS criteria to `requirements.md` (Section 1.5 & Section 2) and append a **`### 3. Delta Fixes & Missing UI Controls`** section (`- [ ] 3.1`, `- [ ] 3.2`) to `docs/specs/<NN-feature-slug>/tasks.md` (leaving completed `1.x` and `2.x` tasks `[x]`).
    4. Mark the feature status `[>]` in `docs/planning/roadmap.md` so `/build <NN-feature-slug>` immediately executes only the new unchecked `3.x` delta tasks!

---

## Step 2 — Read Foundation Context & Inspect Visual References (`design-reference/`)

1. Read `AGENTS.md` (Check **Anti-Hallucination List** and **Critical Constraints**).
2. Read `docs/foundation/architecture.md`, `docs/foundation/tech-stack.md`, and `docs/foundation/design-system.md`.
3. Check for existing visual references in:
   - `docs/foundation/design-reference/` (Global app shell / client brand guide screenshots)
   - `docs/specs/<NN-feature-slug>/design-reference/` (Feature-specific Figma screenshots `*.png`/`*.jpg` or Stitch exports `*.html`)
   - **If screenshots/HTML already exist in `design-reference/`:** Read the image/HTML files (`read_file` views `.png`/`.jpg` directly!) and extract every visible input field, table column, button, modal, and state so `requirements.md` and `design.md` match the client/approved design 1:1.
4. Read `docs/planning/roadmap.md` (**Feature Specs Registry & Dependency Matrix**) to determine the feature's `Wave`, `Depends On` prerequisites, and `Touches (Isolation Zone)` paths.
5. **If Mode C (Mid-Flight Change):** Search the codebase for all usages of any modified shared interface, database column, or API contract, classify the blast radius (`🟢 ISOLATED`, `🟡 CONTAINED`, `🔴 CASCADING`, or `⛔ BREAKING`), and confirm with the user if `🔴` or `⛔`.

---

## Step 3 — Generate or Update the Spec Bundle (`docs/specs/<NN-feature-slug>/`)

Use the templates in [`./resources/`](./resources/) to create or complete the files inside `docs/specs/<NN-feature-slug>/`:

### 1. `requirements.md` (Template: [`./resources/requirements.md.template`](./resources/requirements.md.template))
- Flag any open assumptions (`A1`, `A2` — `Needs confirmation`) at the top.
- Define domain glossary terms.
- Write user stories with numbered **EARS acceptance criteria** (`1.1 WHEN ..., THE System SHALL ...`, `1.2 IF ..., THEN THE System SHALL ...`).

### 2. `design-reference/stitch-prompt.md` (Template: [`./resources/stitch-prompt.md.template`](./resources/stitch-prompt.md.template))
- **If the feature has UI (`[FE]`) and `design-reference/` does NOT yet contain approved `.png`/`.jpg` screenshots:**
  - Generate `docs/specs/<NN-feature-slug>/design-reference/stitch-prompt.md` populated with exact hex color tokens from `docs/foundation/design-system.md` and screen sections from `requirements.md`.
  - Tell the user they can copy-paste `stitch-prompt.md` into **Google Stitch** (`stitch.withgoogle.com`) and drop the exported `.png` (and optional `.html`) into `docs/specs/<NN-feature-slug>/design-reference/` before running `/build`!

### 3. `design.md` (Template: [`./resources/design.md.template`](./resources/design.md.template))
- Draw a Mermaid `flowchart TD` diagram showing UI → Server/API → Domain/DB data flow.
- List every `[NEW]`, `[MODIFY]`, and `[DELETE]` file path with its layer (`[BE]`, `[FE]`), visual reference (`design-reference/<screen>.png`), and validated `Req X.Y` IDs.
- Define the Data Models, API Request/Response contract, **Error Handling Matrix**, and **Correctness Properties**.

### 4. `tasks.md` (Template: [`./resources/tasks.md.template`](./resources/tasks.md.template))
- Break the design into numbered tasks (`1.1`, `1.2`, `1.3`, `2.1`) with layer tags (`[BE]`, `[FE]`, `[REVIEW]`), exact backticked file paths, and `_Requirements: X.Y_` traceability.
- **Multi-View & Sub-Tab Decomposition:** If a feature has multiple sub-tabs, distinct screens, or complex modals (e.g., Destinations + Sources, or List + Edit Modal), decompose the UI into separate numbered `⚡ BATCH` sections (`### 2. Frontend Sub-View A...`, `### 3. Frontend Sub-View B...`), each ending with a `[REVIEW]` checkpoint task so `/build` can test and commit each sub-view atomically.
- **Smart 2-Tier Testing Policy (Unit + E2E):**
  - **Tier 1 (Mandatory Unit Tests `- [ ]` Task `1.3`):** Always include a mandatory unit/invariant test task in `src/features/<domain>/__tests__/<feature>.test.ts` (or `tests/Unit/<Domain>/<Feature>Test.php`, `tests/test_<feature>.py`, or `contracts/test/<Feature>.t.sol`) covering `Property 1` (happy path) and `Property 2` (invalid/unauthorized rejection).
  - **Tier 2 (Smart Playwright E2E Task in `e2e/<feature>.spec.ts`):**
    - Mark the E2E task **mandatory (`- [ ]`)** if the feature is a **Critical User Journey** (Auth/Onboarding, Core Product Value Loop, or Checkout/Billing).
    - Mark the E2E task **optional (`- [ ]*`)** for secondary/settings UI features (which rely on Tier 1 unit tests + Level 3 AI Live Browser UAT).
- Group compile-coupled tasks under `⚡ BATCH` headings (max 3–4 files per batch) and populate the `## 🔀 Task Dependency Graph` JSON block.

### 5. `uat.md` (Template: [`./resources/uat.md.template`](./resources/uat.md.template))
- Generate the 3-level verification checklist (**Level 1 Automated**, **Level 2 EARS Functional**, **Level 3 Visual Fidelity & Human UX**) mapped directly to `Req 1.1..N` and `design-reference/`.

---

## Step 4 — Sync `docs/planning/roadmap.md`

If this is a new feature (Mode B), add a row to the **Feature Specs Registry & Dependency Matrix** and update the Mermaid Dependency Wave Graph in `docs/planning/roadmap.md`.
