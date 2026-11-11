---
name: verify
description: Verify the current change before handing it over. Use when the user types /verify or says a change is finished. Runs the verifier, and the safety reviewer only if the diff touches motion.
disable-model-invocation: true
---
1. List the changed files: `git diff --name-only main...HEAD` and
   `git status --short`.
2. Delegate to the `verifier` subagent: "Verify the current change against
   AGENTS.md. Changed files: <list>." Show its table unchanged.
3. If any changed file is under `src/main/java/frc/robot/mechanisms/`,
   `src/main/java/frc/robot/opmodes/`, or an `io/` folder, or the diff changes
   a command, limit or default command, also delegate to the
   `robot-safety-reviewer` subagent with the diff. Show its findings unchanged.
   Otherwise say "Safety review not needed: no motion code changed."
4. End with one line: `VERIFY: PASS` only if the verifier's OVERALL is PASS
   and the safety reviewer (if run) said PASS; `VERIFY: BLOCKED` if the only
   non-PASS rows are BLOCKED; otherwise `VERIFY: FAIL`.

Do not fix anything yourself. Report what failed.
