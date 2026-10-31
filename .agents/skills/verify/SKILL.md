---
name: verify
description: >-
  Run Level 1 (Automated build/types/tests), Level 2 (EARS functional smoke checks),
  and Level 3 (Visual fidelity against design-reference/*.png & Stack Hard-Failure review)
  verification against docs/specs/<NN-feature-slug>/uat.md. Use after completing feature
  tasks or when the user invokes /verify.
---

# 🧪 Skill: Feature Verification & Hard-Failure Review (`/verify`)

This skill verifies a completed feature against its EARS acceptance criteria (`requirements.md`), technical design (`design.md`), visual mockups (`design-reference/`), UAT checklist (`uat.md`), and stack rules (`.agents/rules/stack-rules.md`).

---

## Step 1 — Load the Feature Spec & UAT Checklist

1. Read `docs/specs/<NN-feature-slug>/requirements.md`, `design.md`, `tasks.md`, and `uat.md`.
2. If `uat.md` does not exist yet, create it from [`../spec/resources/uat.md.template`](../spec/resources/uat.md.template).

---

## Step 2 — Level 1: Automated Build, Unit Tests, Playwright E2E & Stack Hard-Failure Audit

1. **Run Terminal Verification, Unit Tests & Golden-Path E2E:**
   - **Next.js / TypeScript:** `pnpm tsc --noEmit && pnpm test --run && pnpm build` (verifying all unit tests in `src/features/<domain>/__tests__/` pass).
   - **WordPress / PHP:** `php -l` across all plugin/theme PHP files (plus `vendor/bin/phpunit` for `tests/Unit/`).
   - **Python / FastAPI:** `uv run ruff check . && uv run pytest`.
   - **Web3 / Foundry:** `forge build && forge test -vvv` + `pnpm tsc --noEmit && pnpm test --run`.
   - **Playwright E2E Smoke Suite (If `e2e/*.spec.ts` exists & not inside a parallel subagent wave):** Run `pnpm test:e2e` (`npx playwright test`) to verify Golden-Path user journeys in a real headless browser. *(If running inside a parallel Mode B subagent, skip starting the E2E server inside the subagent — the Main Agent runs `pnpm test:e2e` once at the Inter-Wave Checkpoint.)*
2. **Audit Against `.agents/rules/stack-rules.md` (Pre-Commit / Hard-Failure Checklist) & `AGENTS.md`:**
   - Check every file touched by this feature (`design.md` Section 2) for:
     - Layer boundary violations (e.g., cross-feature imports `features/[a]/` → `features/[b]/`, business logic in `page.tsx` or `templates/`)
     - Security violations (e.g., missing `import 'server-only'`, missing Zod `safeParse`, missing RLS, or in WordPress: missing `ABSPATH` guard, unprepared `$wpdb` SQL, missing nonce/capability check, unescaped template output)
     - Anti-Hallucination violations (anything forbidden in `AGENTS.md` or `requirements.md` Non-Goals).

---

## Step 3 — Level 2: EARS Functional & Error-Matrix Verification

Verify each numbered requirement (`Req 1.1`, `Req 1.2`, etc.) and each row of the **Error Handling Matrix** in `design.md`:
- Confirm happy-path behavior (`WHEN ... THE System SHALL ...`).
- Confirm error-path behavior (`IF ... THEN THE System SHALL ...`) returns the exact expected status code, writes zero partial DB records on failure, and preserves user input in the UI.
- Update the status of each row in `docs/specs/<NN-feature-slug>/uat.md` (`[x]` Pass or `[!]` Fail with file:line details).

---

## Step 4 — Level 3: Visual Fidelity (`design-reference/`) & UX Check

1. Check `docs/specs/<NN-feature-slug>/design-reference/`:
   - If reference screenshots (`*.png` / `*.jpg`) exist, read them with `read_file` and compare against the implemented UI components (or live browser render if using `/browser`) to confirm layout, sections, fields, and visual hierarchy match.
2. Confirm UI components use design tokens from `docs/foundation/design-system.md` (no arbitrary hardcoded hex colors).
3. Confirm loading states, empty states, and double-submit prevention are in place.
4. Mark `## 🧪 Feature Verification & UAT Gate` complete (`[x]`) in `docs/specs/<NN-feature-slug>/tasks.md`.

---

## Step 5 — Output Review Summary

Output a clear verification report:

```markdown
## 🧪 Verification Report: `<NN-feature-slug>`
**Result:** ✅ PASS / ❌ FAIL

- **Level 1 (Build & Types):** [PASS/FAIL]
- **Stack Hard-Failure Audit:** [PASS/FAIL]
- **Level 2 (EARS Criteria Covered):** [X/Y Verified]
- **Level 3 (Visual Match vs `design-reference/` & Tokens):** [PASS/FAIL]
```

If **✅ PASS**, prompt the user to run **`/ship`** (`../ship/SKILL.md`) to update `roadmap.md` and prepare the commit/PR.
