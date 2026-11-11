# AI levels

AI tools are optional. Everyone has a level from 0 to 3. The level says what
the AI may do for you. It never changes the rule that you must be able to
explain every line you submit.

| Level | Name | AI may | You do |
| --- | --- | --- | --- |
| 0 | AI off | Nothing | Everything, with mentors and docs |
| 1 | Learn | Explain code (`/explain`), quiz you | Write all code |
| 2 | Plan and check | Plan (`/plan`), verify (`/verify`), run scenarios (`sim`), replay and read logs (`replay`, `log-triage`, `match-debrief`) | Write all code |
| 3 | Build | Everything in 2, plus build from an approved plan (`/feature`, `/new-mechanism`) | Approve the plan, review the result, explain it back |

Level 1 is the default when no level is set.

## What stays human at every level

Merging, pushing, deploying to a robot, and changing `vendordeps/`, `.wpilib/`
or `gradle/`. Hooks block the AI from deploying, pushing, recursive deletes, and
removing or weakening tests, scenarios and CI steps.

## Set your level

Put this in `.claude/settings.local.json` (it is git-ignored, so it is yours):

```json
{
  "env": {
    "FRC_AI_LEVEL": "2"
  }
}
```

The session-start hook prints your level at the start of each session, and
`/feature` and `/new-mechanism` refuse to run below level 3. The level is a
team agreement, not a lock: a hook cannot stop you editing this file. A mentor
can ask to see it. GitHub Copilot does not read this setting; follow your level
by hand.

## Weekly AI-off code walk

Once a week each student explains one of their merged changes with no AI help.
The pull request template's "I can explain every line" box is that promise.

## What works where

| Feature | Claude Code | GitHub Copilot |
| --- | --- | --- |
| Project rules (`AGENTS.md`) | Yes | Yes |
| Path-scoped rules | `.claude/rules/` | `.github/instructions/` (generated) |
| Agents | `.claude/agents/` | `.github/agents/` (generated; models and hook guards are not carried over) |
| Skills `sim`, `replay`, `log-triage`, `api-check` | Yes | Yes (reads `.claude/skills/`) |
| Skills `/plan`, `/verify`, `/explain`, `/feature`, `/new-mechanism` | Yes | Not portable: they use Claude Code fields |
| Hook guards (deploy, push, test removal) | Yes | No |

`./frc sync-ai` regenerates the Copilot files from the Claude ones, and
`./frc sync-ai --check` (run in CI) fails if they have drifted. Never edit the
generated files by hand.
