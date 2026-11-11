---
name: feature
description: Build a feature end to end - plan, wait for approval, build, verify, update docs, print a commit message. Use when the user types /feature <description>. For level 3 users only.
argument-hint: "[feature description]"
disable-model-invocation: true
---
Feature: $ARGUMENTS

Confirm the user's AI level is 3 (see `docs/ai-levels.md`). If it is lower,
stop and say which skill to use instead (`/plan` or `/explain`).

1. Plan: delegate to the `planner` subagent. Show its plan. If it starts with
   `STOP:`, stop there.
2. Wait. Do not write any code until the user answers "yes". If they change
   the plan, plan again.
3. Build exactly the plan, following AGENTS.md. Use `api-check` before any
   WPILib or vendor call. Add the tests and scenarios the plan lists.
4. Verify: run the `verify` skill. Fix FAIL rows and run it again; stop and
   report if it still fails after two rounds.
5. Docs: delegate to the `docs-sync` subagent.
6. Print a suggested commit message (imperative subject, mechanism name, why in
   the body, co-author trailer if AI helped) and a summary table of what was
   verified. Do not commit, push or deploy; the person does that.
