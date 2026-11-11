# M5 plan: agent system v1

Branch: `ai/agent-system-v1`. Assumes M3 is merged. Can run before or
after M4.

## Goal

Give Claude Code and GitHub Copilot the same planning, verification and
guard rails, built on the `frc` tool, and set up the per-student AI levels.

## Out of scope

Writer agents (implementer, test-writer), a sim-only operator agent,
agent-teams mode, model routing between vendors, Claude calling Copilot.
These were considered and rejected for this team.

## Verify before writing (refresh step, report first)

Read the current docs; formats change:

- Claude Code: subagents, skills, hooks guide and reference, settings and
  permissions, memory (AGENTS.md handling), output styles.
  Index: https://code.claude.com/docs/llms.txt
- Copilot: custom instructions support matrix, agent skills, custom
  agents, cloud agent setup steps. Start at https://docs.github.com/en/copilot
- Report: exact frontmatter fields you will use for agents and skills;
  hook event names and input fields; where Copilot reads skills and agent
  files from; anything in this plan the docs contradict.

## Phase 1: agents (commit 1)

`.claude/agents/`, each with one job, the narrowest tools, a fixed output
format and a refusal list. Each starts with "Read AGENTS.md first."

| Agent | Model tier | Tools | Output |
| --- | --- | --- | --- |
| `planner` | strongest | read, search, fetch | Checklist: files, rules that apply, tests and scenarios to add, consumers, questions. First line `STOP: ask <owner>` if the task touches an open question or a mechanism someone else owns |
| `verifier` | strongest | read, search, shell | Table `item, PASS/FAIL/BLOCKED, evidence` walking "Before finishing any change". Real-robot items are BLOCKED |
| `robot-safety-reviewer` | strongest | read, search | Only for diffs that touch motion. Checks current limits, soft limits, behavior when disabled, nothing moves without a command. Findings by severity, then PASS or FAIL. Correctness gaps only |
| `docs-sync` | smallest | read, edit (docs only) | Updates mechanism cards and AGENTS.md tables from code. Never invents a value |
| `log-analyst` | smallest | read, shell (`frc log` only) | A short finding with the numbers behind it |
| `tutor` | mid | read, search | Explains, asks the student to predict, quizzes. Never edits |

Done when: each agent, invoked on a trivial prompt, follows its format.
Commit: `Add planner, verifier, safety, docs, log and tutor agents`

## Phase 2: skills (commit 2)

`.claude/skills/<name>/SKILL.md`. Shared skills use only portable
frontmatter fields so Copilot can load them. Under 500 lines each;
description leads with when to use it.

| Skill | Kind | Does |
| --- | --- | --- |
| `plan` | user-run, forks to `planner` | Prints a plan; edits nothing |
| `verify` | user-run | Runs `verifier`; runs the safety reviewer only if the diff touches motion |
| `sim` | model or user | Picks scenarios for the change, runs `frc sim`, quotes `summary.json` |
| `replay` | model or user | `frc replay` then `frc log diff`; reports first divergence |
| `log-triage` | model or user | Bounded `frc log` queries for a named symptom |
| `new-mechanism` | user-run | Scaffolds IO, sim IO, mechanism, test, scenario, doc card, copying the Arm |
| `explain` | user-run, forks to `tutor` | Walks a student through a file |
| `feature` | user-run, side effects off by default | plan, wait for yes, build, verify, docs. Prints a commit message; never commits. For level 3 users |

Done when: `/plan` and `/verify` work end to end on a small change to the
Arm (for example, a new preset angle).
Commit: `Add plan, verify, sim, replay and scaffolding skills`

## Phase 3: hooks (commit 3)

Hooks are `frc hook <name>` subcommands in the tools source set, reading
the hook JSON on standard input, so they behave the same on Windows and
Mac. To keep them fast, add a Gradle task that builds a runnable tools jar
and have the `frc` wrappers call `java -jar` for `hook` commands, locating
the WPILib JDK if `java` is not on the path.

| Event | Subcommand | Behavior |
| --- | --- | --- |
| Session start | `session-start` | Prints WPILib version, branch, the student's level, last test result |
| Before shell command | `guard-shell` | Blocks deploy, push, recursive delete, and test filters that deselect tests |
| Before edit | `guard-edit` | Blocks removing or disabling a test, loosening a scenario check, removing a CI step, edits to protected paths |
| After Java edit | `post-edit` | Runs Spotless and a compile on the file; reports, never blocks |
| Stop | `on-stop` | If `src/main` changed, runs tests and attaches the result. Reports once; respects the stop-hook-active flag |
| Before compaction | `pre-compact` | Asks to keep the plan, file lists and open FAIL rows |

Unit-test every guard with sample hook inputs.

Done when: an attempt to delete `RunModeTest` is blocked with a clear
message, and an attempt to run `./gradlew deploy` is blocked.
Commit: `Add guard and reporting hooks via frc hook`

## Phase 4: Copilot parity and levels (commit 4)

- `tools/sync-ai` (an `frc sync-ai` subcommand): generates
  `.github/instructions/*.instructions.md` from `.claude/rules/*.md` and
  `.github/agents/*.agent.md` from `.claude/agents/*.md`. `--check` mode
  exits non-zero on drift; add it to CI.
- `.github/workflows/copilot-setup-steps.yml`: JDK, Gradle cache, build
  the tools jar.
- `.claude/rules/`: `io.md`, `mechanisms.md`, `opmodes.md`, each scoped by
  path.
- `docs/ai-levels.md`: levels 0 to 3, what each allows, how to set yours
  in the git-ignored `.claude/settings.local.json`.
- `AGENTS.md`: three-line learning contract with a pointer.

Done when: `frc sync-ai --check` passes in CI.
Commit: `Sync Copilot instructions and document AI levels`

## Phase 5: human check (Cohen)

- [ ] In Copilot Chat in VS Code, ask "how do I run a sim scenario?" and
      get the `frc` answer.
- [ ] Run `/plan` on a task that touches a mechanism with a named owner;
      the plan starts with `STOP`.
