---
name: ship
description: >-
  Finalize a verified feature or phase: mark the feature [x] in docs/planning/roadmap.md,
  sync docs/foundation/ if architecture or schema changed, archive completed phases to
  docs/planning/history.md, and generate a Requirement-traceable commit or PR summary.
  Use when a feature passes /verify or when the user invokes /ship.
---

# 🚀 Skill: Ship Feature & Sync Brain (`/ship`)

This skill closes the loop on a verified feature (`docs/specs/<NN-feature-slug>/`) or a completed phase.

---

## Step 1 — Pre-Ship Gate Check

1. Check `docs/specs/<NN-feature-slug>/tasks.md` and `uat.md`:
   - Confirm all required implementation tasks are `[x]`.
   - Confirm `uat.md` has passed **`/verify`**. (If not yet verified, run `../verify/SKILL.md` first.)
2. Run `git status` and `git diff --stat` to confirm no `.env*` secrets, debug logs, or untracked junk files are staged.

---

## Step 2 — Sync Macro Roadmap & Foundation Docs

1. **Update `docs/planning/roadmap.md`:**
   - In the **Feature Specs Registry & Dependency Matrix**, change the feature's `Status` from `[>]` to `[x]`.
   - Check off the corresponding feature items in the Phase checklist.
   - Identify which next-wave features in the Dependency Matrix are now **unblocked**!
2. **Check `docs/foundation/` Drift:**
   - Did this feature add or modify a database table, environment variable, or architectural decision?
   - If YES → Update `docs/foundation/architecture.md`, `docs/foundation/tech-stack.md`, or append an ADR to `docs/foundation/decisions.md`.
3. **If Entire Phase Is Complete:**
   - Summarize the completed phase into `docs/planning/history.md` (with completion date, features shipped, and key lessons).
   - Update `AGENTS.md` `## 🚦 Project State` to the next Phase (or `complete`).

---

## Step 3 — Path-Scoped Commit Per Feature & PR Summary

1. **One Scoped Commit Per Verified Feature (Never `git add .` Across Unrelated Features):**
   - Stage **only** the verified feature's `Touches (Isolation Zone)` paths, its `docs/specs/<NN-feature-slug>/` bundle, and `docs/planning/roadmap.md` (plus any updated `docs/foundation/` file):
     ```bash
     git add <Touches-paths> docs/specs/<NN-feature-slug>/ docs/planning/roadmap.md
     git commit -m "feat(<NN-feature-slug>): <summary> (Req 1.1-X.Y)"
     ```
   - *(If shipping multiple features from a parallel wave, create **1 separate commit per feature** so each feature can be reviewed or reverted independently in `git log`!)*
2. **Local Review First vs. GitHub PR:**
   - **Solo / Personal Projects:** Review locally first via the IDE/App (`Files Changed` pane + `localhost:3000` / local environment) before pushing to remote.
   - **Team / Client Projects (2+ Devs):** After local verification, push the feature branch (`git push -u origin feat/<NN-feature-slug>`) and open a GitHub PR using the summary below.
3. **Generate PR / Completion Summary:**

```markdown
## 🚀 Shipped: [Feature Name] (`<NN-feature-slug>`)

### Summary
- **Spec Bundle:** `docs/specs/<NN-feature-slug>/`
- **Requirements Implemented:** `Req 1.1, 1.2, ...`
- **Verification:** [`docs/specs/<NN-feature-slug>/uat.md`](docs/specs/<NN-feature-slug>/uat.md) (✅ Passed)

### Key Files Changed
- `[path/to/file]` — [1-line summary]

### Next Unblocked Wave in `docs/planning/roadmap.md`
- Ready to build next: `[next-feature-slug(s)]`
```
