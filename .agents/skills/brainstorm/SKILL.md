---
name: brainstorm
description: >-
  Initialize and populate a project's Brain (AGENTS.md, docs/foundation/*,
  docs/planning/roadmap.md, and starter docs/specs/00-scaffold/ + 01+ feature specs)
  either for a brand-new project or inside an already-scaffolded repo (such as after
  `npm create tangible@latest` + `sync-brain.ps1`). Use when starting a project,
  populating an empty `docs/` folder, or when the user invokes /brainstorm.
---

# ðŸ§  Skill: Project Brainstorm & `docs/` Initializer (`/brainstorm`)

This skill populates the entire **`docs/` Project Brain** (`docs/foundation/`, `docs/planning/roadmap.md`, and `docs/specs/`) and `AGENTS.md`.

It works in **two ways**:
1. **Inside a Project Folder (e.g., after `npm create tangible@latest` or `create-next-app` + `sync-brain.ps1`):** Scans the existing scaffold (`package.json`, `tangible.config.js`, `composer.json`, `.agents/rules/stack-rules.md`) and populates `docs/` + `AGENTS.md` in place.
2. **Inside `master-brains`:** Runs [`brainstorm.md`](../../../brainstorm.md) to design a new project and run `sync-brain.ps1`.

---

## Step 1 â€” Detect Current Repo & Existing Scaffold

1. Check if `brainstorm.md` exists at the workspace root (`master-brains` repo):
   - If you are inside `master-brains` and the user wants to create a new project folder, read and follow `brainstorm.md` + `INVENTORY.md`.
2. If you are inside a **target project repo** (where `.agents/rules/stack-rules.md` already exists and `docs/foundation/` is empty or incomplete):
   - Inspect existing files at the root (`package.json`, `composer.json`, `tangible.config.js`, `*.php`, `src/`, `includes/`) so you don't ask questions the codebase already answers.
   - Read `.agents/rules/stack-rules.md` and `AGENTS.md` to see which stack pack is active (`tangible-wp`, `nextjs-supabase`, `wp-modular-oop`, `python-fastapi`, `web3-evm`).
   - **ðŸ–¼ï¸ FIGMA-FIRST VISUAL AUTO-DISCOVERY (CRITICAL):**
     - Check `docs/foundation/design-reference/` (or any screenshot path provided by the user) for `.png` / `.jpg` / `.webp` files.
     - **If `.png`/`.jpg` screenshots exist:** View/read **EVERY screenshot** using your file-reading/vision tool **BEFORE** asking the user any questions!
     - From the screenshots alone, automatically extract:
       1. What the product does and its navigation/tab structure
       2. Every feature module (grouping related screens + `[Modal]` screenshots into logical features like `01-connections`, `02-manual-syncs`, `03-automatic-syncs`, `04-post-edit-box`, `05-logs`)
       3. Every table column, form field, filter, toggle, modal, and button visible in the UI
       4. The visual theme (e.g., WP Admin native vs Tangible UI `--tui-*` tokens, primary colors, border radii)
     - **Do NOT ask the user to type out the product vision or feature list if the screenshots already show it!** Instead, present the features and data entities you discovered from the Figma screenshots and only ask the user to confirm or select technical dependencies (`tangible/fields`, `tangible/object`, `@tangible/ui`, etc.).

---

## Step 2 â€” Gather / Confirm Project Scope

- **Mode A â€” Figma Screenshots Exist in `docs/foundation/design-reference/` (No ClickUp Ticket Needed):**
  1. Present a concise summary of the **Discovered Screens, Features, Modals & Data Entities** extracted from the `.png` files.
  2. Ask only for any non-obvious backend details (e.g., which Tangible modules / third-party integrations to enable, and confirmation of the proposed Feature Wave Table).
- **Mode B â€” No Screenshots & No Pasted Brief:**
  Ask concisely:
  1. **Vision & Core Features:** What does this plugin/app do, who are the users/roles, and what are the core features for Phase 1?
  2. **Dependencies, Data & Integrations:**
     - **If `tangible-wp`:** Which Tangible modules & WP plugins do we need (`tangible/fields`, `tangible/object`, `@tangible/ui`, LearnDash, WooCommerce, ACF, BuddyBoss)?
     - **If `nextjs-supabase` / `python-fastapi` / `web3-evm`:** Main database tables/entities, auth methods, and external APIs?
  3. **UI & Design Direction:** Any Figma screenshots to drop into `docs/foundation/design-reference/`, or use default Tangible UI (`--tui-*`) / Stitch prompts?
  4. **Confirm Feature Wave Breakdown:** Propose the ordered **Feature Specs & Dependency Wave Table** (`00-scaffold`, `01-...`, `02-...`) and ask the user to confirm.

---

## Step 3 â€” Generate the Complete `docs/` Brain & Update `AGENTS.md`

Once confirmed, create and populate all of the following files:

### 1. Update Root `AGENTS.md`
- Fill in Section 1 (**What Is This Project?**), Section 2 (**Tangible Modules & External Integrations** or **Architecture Summary**), Section 5 (**Critical Constraints**), and Section 6 (**Anti-Hallucination List**).

### 2. Populate `docs/foundation/` (4 Files)
- **`docs/foundation/prd.md`** â€” Project vision, user roles/personas, Phase 1 feature scope, and explicit Non-Goals.
- **`docs/foundation/architecture.md`** â€” Folder structure map (matching `.agents/rules/stack-rules.md`), data flow Mermaid diagram, and module/entity boundaries (`includes/<feature-slug>/`, `includes/objects/`, or `src/features/<domain>/`).
- **`docs/foundation/tech-stack.md`** â€” Approved dependencies (`tangible.config.js` `install` modules, `composer.json` packages, `package.json` scripts, PHP/Node versions, test runners).
- **`docs/foundation/design-system.md`** â€” UI tokens (`--tui-theme-*` custom properties for `.tui-interface` or Tailwind/CSS tokens), typography, radii, and links to any screenshots in `docs/foundation/design-reference/`.

### 3. Populate `docs/planning/roadmap.md`
Create `docs/planning/roadmap.md` containing:
- **Phase Overview** (`Phase 0: 00-scaffold`, `Phase 1: Core MVP`, `Phase 2: Enhancements`)
- **Feature Specs Registry & Dependency Matrix** table:
  ```markdown
  | ID | Feature Slug | Wave | Depends On | Touches (Isolation Zone) | Spec Path | Status |
  |----|--------------|------|------------|--------------------------|-----------|--------|
  | `00` | `00-scaffold` | `Wave 0` | None | `tangible.config.js, composer.json, package.json, includes/index.php` | [`docs/specs/00-scaffold/`](../specs/00-scaffold/tasks.md) | `[ ]` |
  | `01` | `01-<feature-a>` | `Wave 1` | `00` | `includes/<feature-a>/, assets/src/<feature-a>/` | [`docs/specs/01-<feature-a>/`](../specs/01-<feature-a>/tasks.md) | `[ ]` |
  ```
- **Mermaid Dependency Wave Graph** (`flowchart LR`).

### 4. Populate Starter Specs in `docs/specs/`
- **`docs/specs/00-scaffold/tasks.md`:**
  - Concrete tasks to wire required dependencies (e.g., adding `tangible/fields` to `tangible.config.js`, `tangible/object` to `composer.json`, `@tangible/ui` to `package.json`, running `npx roll install && npm run build`, and verifying `php -l`).
- **For each Phase 1 feature (`docs/specs/<NN-feature-slug>/`):**
  - Generate `requirements.md` and `tasks.md` using the templates in [`../spec/resources/`](../spec/resources/).
  - **Map Visual Screenshots per Feature:** If `.png`/`.jpg` screenshots exist in `docs/foundation/design-reference/`, copy (or reference) the specific screen and `[Modal]` `.png` files that belong to `<NN-feature-slug>` into `docs/specs/<NN-feature-slug>/design-reference/` so `/spec`, `/build`, and `/verify` automatically match those exact screens 1:1!
