# M3 plan: headless scenarios and the `frc` CLI

Branch: `tools/frc-cli-v0`. Assumes M2 is merged (Arm, `RunProfile`).

## Goal

Let an agent or CI run the robot in simulation with no GUI and no person,
and get a pass or fail. Then wrap the common commands and log queries in
one tool, `frc`.

## Out of scope

Agents, hooks, game-piece physics, gamepads, graphs, driver reports,
`frc log pull`. No Python and no outside tooling repositories.

## Design

### Scenarios (in-process, sim only)

```
frc/robot/sim/
  SimDriver.java        runs one scenario, then exits the program
  Scenario.java         name, OpMode to select, duration, checks
  Check.java            a named condition over a logged or live value
  scenarios/ArmReachesGoal.java
  scenarios/ArmHoldsWithinLimits.java
```

- Active only when `RunMode` is `SIM` and the environment variable
  `SIM_SCENARIO=<name>` is set. Otherwise `SimDriver` does nothing.
- `SimDriver` selects the scenario's OpMode, enables the robot, counts
  loops (not wall time) until the duration is reached, disables,
  evaluates checks, writes `build/sim/<name>/summary.json`, and exits
  with code 0 on pass or 1 on fail.
- `summary.json`: scenario, pass, per-check name / pass / observed value /
  expected, loop count, loop overruns, path of the log written.
- Checks read values through the mechanism's public getters or recorded
  outputs. They never reach into IO.

### CLI

A separate Gradle source set named `tools`, so the CLI is not packed into
the robot jar, plus Gradle `JavaExec` tasks (the existing `replayWatch`
task in `build.gradle` is the pattern). Wrappers `frc` (sh) and `frc.cmd`
in the repo root call Gradle. If a separate source set fights GradleRIO,
fall back to a package `frc.tools` in the main source set and say so.

| Command | Does |
| --- | --- |
| `frc test` | `./gradlew test` |
| `frc sim <scenario>` | Runs with `SIM_SCENARIO` set; prints `summary.json` |
| `frc sim --list` | Lists scenario names |
| `frc replay <log>` | Runs with `AKIT_LOG_PATH` set; prints the output log path |
| `frc log keys <log> [prefix]` | Signal names and types |
| `frc log stats <log> <key> [--from s --to s]` | Count, min, max, mean, last |
| `frc log diff <log> <key>` | Compares `RealOutputs/<key>` with `ReplayOutputs/<key>`; first divergence |

Output rules: compact JSON on standard output, at most 50 lines unless
`--limit` is given, never a raw series. Errors go to standard error with a
non-zero exit code.

## Verify before writing (refresh step, report first)

- How simulation selects an OpMode and enables the robot in WPILib 2027
  (`DriverStationSim` or its replacement; OpMode selection API). The
  changelog says the Driver Station sim extension is a no-op for now, so
  this must work through the simulation classes directly. If it cannot be
  done, stop: the whole milestone depends on it.
- Whether AdvantageKit's `setUseTiming(false)` can be used in `SIM` so a
  scenario runs faster than real time, and whether two runs then produce
  identical outputs.
- WPILib's Java log reader class for `.wpilog` files in 2027 (package and
  API), and how AdvantageKit names keys in the file.
- How to add a source set and a `JavaExec` task under GradleRIO alpha 8.

## Phase 1: spike, enabling headless (no commit unless it works)

Smallest possible proof: with `SIM_SCENARIO=probe`, enable `DriveTeleop`
for 100 loops and exit 0. Report how it was done and how long it took in
wall time, with and without real-time pacing.

Stop here and report. Continue only after Cohen confirms.

## Phase 2: scenarios (commit 1)

Build `SimDriver`, `Scenario`, `Check` and the two Arm scenarios:

- `ArmReachesGoal`: within N loops of the first goal, `Arm/AtGoal` is true
  and position is within tolerance.
- `ArmHoldsWithinLimits`: position never leaves the soft limits; no loop
  overruns.

Done when: each scenario exits 0; breaking the arm's gain on purpose makes
`ArmReachesGoal` exit 1 with a useful `summary.json` (quote it, then
revert).

Commit: `Add headless sim scenarios for the Arm`

## Phase 3: CLI wrappers (commit 2)

`frc test`, `frc sim`, `frc sim --list`, `frc replay`. Both wrapper
scripts. Unit tests for argument parsing.

Done when: `./frc sim ArmReachesGoal` prints the summary and returns the
right exit code on Mac or Linux. Windows is BLOCKED unless run there.

Commit: `Add frc CLI with test, sim and replay`

## Phase 4: log queries (commit 3)

`frc log keys`, `stats`, `diff`. Start with number and boolean signals.
Struct signals (poses) are a stretch: list them in `keys` with their type
and report "unsupported type" for `stats` and `diff`.

Add a small fixture log under `src/tools/test/resources` (or the
equivalent), produced by a scenario run, and unit tests against it.

Done when: after `frc sim ArmReachesGoal` then `frc replay <log>`,
`frc log diff <replayed log> Arm/GoalRad` reports no divergence.

Commit: `Add frc log keys, stats and diff`

## Phase 5: CI and docs (commit 4)

- `.github/workflows/build.yml`: after the build, run both scenarios and
  one replay comparison. This plan authorizes that edit. If simulation
  cannot run in the CI container, report why and leave CI unchanged.
- `AGENTS.md`: switch "Commands" to `frc`; add to "Before finishing any
  change": relevant scenarios pass, quoted from `summary.json`.
- `.claude/settings.json`: allow `Bash(./frc *)`.
- `ROADMAP.md`: mark M3 done; record the spike results.

Commit: `Run scenarios in CI and document frc`

## Phase 6: human check (Cohen)

- [ ] On Windows: `.\frc.cmd sim ArmReachesGoal` works.
- [ ] Open a PR that breaks the arm; CI fails on the scenario step.
