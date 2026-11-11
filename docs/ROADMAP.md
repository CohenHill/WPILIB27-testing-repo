# Development roadmap

Repo state read at commit `aaa287e` (bare-bones setup merged).
Each milestone ends with something that runs and a check that proves it.
Nothing here has dates; the one fixed point is kickoff in early January.

## Where the repo is now

Working:

- `Robot` extends `LoggedOpModeRobot`, runs `Scheduler.getDefault().run()` each loop.
- Two OpModes (`DriveTeleop`, `DoNothingAuto`) implementing `org.wpilib.opmode.OpMode`.
- Sim writes a log to `logs/`; replay turns on when `AKIT_LOG_PATH` is set.
- `AGENTS.md`, `CLAUDE.md`, Copilot instructions, Claude permissions, `api-check` skill.
- Vendor libraries present: AdvantageKit, Commands v3, Phoenix 6, PathPlannerLib, YAMS (Commands v3 build).

Gaps found while reading it:

| Gap | Why it matters | Fixed in |
| --- | --- | --- |
| No `src/test`, and CI only builds | Nothing fails when behavior breaks | M1 (done) |
| Base package is `first.robot` | Renaming gets harder with every file added | M1 (done) |
| No mechanism, no IO interface, no inputs logged | Replay has nothing to replay yet | M2 |
| Sim GUI is off by default and nothing can enable an OpMode headless | Agents and CI cannot exercise the robot | M3 |
| PhotonLib is not installed | Needed for vision | M6 |
| `AGENTS.md` has no "Interfaces that exist today" or "Known broken or stale" sections | Agents need them once there are mechanisms | M2 |
| No contributing guide, PR template, code owners or branch protection | Team goals on GitHub rules and commit messages | M1 |

## Milestones at a glance

| # | Milestone | Proves | Depends on | Best done by |
| --- | --- | --- | --- | --- |
| M1 | Repo hygiene and first test | CI catches a broken test | none | Mentor + agent |
| M2 | Reference mechanism through the IO wall | Record in sim, replay, outputs match | M1 | Mentor or lead |
| M3 | `frc` CLI v0 and headless scenarios | An agent can run and judge a sim alone | M2 | Lead + agent |
| M4 | YAMS behind IO: keep or drop | Same mechanism, less code, still replays | M3 | Lead |
| M5 | Agent system v1 | Plan, verify and guard rails work on a real change | M3 | Mentor |
| M6 | Swerve behind `DriveIO` | Drives in sim; replays | M3 | Lead |
| M7 | Vision and localization | Pose error is measured, not guessed | M6 | Lead + student |
| M8 | Autos and auto-align | Two scoring autos pass scenarios in CI | M6, M7 | Students |
| M9 | Context controls, driver metrics, dashboard | Driver report from a log | M6 | Students |
| M10 | Student onboarding | Every student ships a toy mechanism | M4, M5 | Everyone |
| M11 | Kickoff readiness | Beta upgrade done; platform frozen | all | Mentor |

A hardware track runs alongside from M2 onward (see below).

Order rationale: M3 comes before swerve on purpose. Once a headless scenario
can pass or fail, every later milestone can be checked by an agent or by CI
instead of by someone watching a screen.

---

## M1: Repo hygiene and first test

Deliverables:

- Real team number set.
- Base package renamed from `first.robot` to the team's choice (update
  `ROBOT_MAIN_CLASS` and the `gversion` `classPackage` in `build.gradle`).
- `src/test/java/...`: one JUnit test of real logic. `Constants` mode
  selection is the only logic so far; make it testable (a small pure
  function that takes "is real" and "log path" and returns the mode).
- CI: the existing workflow already runs `./gradlew build`, which runs
  tests. Add a status badge and make the check required.
- `CONTRIBUTING.md`: branch naming, commit convention (imperative subject,
  mechanism name, why in the body), PR steps.
- `.github/pull_request_template.md` with the explain-back questions: what
  changed, why, what could break, what evidence.
- `.github/CODEOWNERS` (mentor for now; mechanism owners added later).
- Branch protection on `main`: PR required, CI required, one review.

Done when: a PR that breaks the test is blocked by CI.

## M2: Reference mechanism through the IO wall

Build one deliberately simple mechanism by hand, with no YAMS, so the
pattern is visible. A single-motor roller or arm is enough.

Deliverables:

- `mechanisms/<name>/io/`: `XIO` interface with an `@AutoLog` inputs class,
  `XIOSim` (WPILib motor sim), `XIOTalonFX` (real), and an empty replay IO.
- `mechanisms/<name>/X.java`: a Commands v3 `Mechanism` with two or three
  command methods. No vendor imports.
- `Robot.robotPeriodic()` order: update and process all inputs, then run
  the scheduler, then record outputs. Commands v3 mechanisms have no
  `periodic()`, so this must be explicit.
- `RunProfile.java`: one place that picks the IO per mechanism (`SIM`,
  `REPLAY`, `COMP`). Selected by environment or Gradle property.
- Unit tests for the mechanism logic against a fake IO.
- `AGENTS.md`: add the six architecture rules, "Interfaces that exist
  today" and "Known broken or stale".
- `docs/robot/<name>.md`: the mechanism card (what it is, IO signals, owner).

Done when: run sim, command the mechanism from `DriveTeleop`, stop, replay
the log, and the replayed outputs equal the recorded outputs. This is
checked by hand in AdvantageScope for now; M3 automates it.

## M3: `frc` CLI v0 and headless scenarios

Deliverables:

- `SimDriver` (sim only): selects an OpMode, enables for N seconds, feeds
  scripted inputs, then exits the program. Needed because the sim GUI is
  off by default and the Driver Station sim extension does nothing yet.
- `src/sim/scenarios/`: two scenarios for the M2 mechanism, each with pass
  criteria on logged values.
- `tools/frc-cli` (Java, Gradle subproject) with wrappers `frc` and
  `frc.cmd`: `test`, `sim <scenario>`, `replay <log>`, `log keys`,
  `log stats`, `log diff`. Compact JSON output, capped length.
- CI runs both scenarios and one replay comparison on every PR.
- `AGENTS.md` commands section switches to `frc`.

Spikes folded in: can the CLI read a 2027 AdvantageKit log with WPILib's
reader classes, including struct signals; does a fixed-tick sim run faster
than real time and give the same result twice.

Done when: `frc sim <scenario>` exits 0 or 1 with a JSON summary, and a
deliberately broken command makes CI fail.

## M4: YAMS behind IO, keep or drop

Deliverables:

- `XIOYams` for the M2 mechanism, selected through `RunProfile`.
- The M3 scenarios and the replay comparison run unchanged against it.
- A short decision note in `docs/decisions/`: lines of code saved, sim
  quality, anything that broke.

Done when: the decision is written down. If YAMS stays, it is the default
IO for standard mechanisms. If not, remove the vendor library.

## M5: Agent system v1

Deliverables:

- Agents: `planner`, `verifier`, `robot-safety-reviewer`, `docs-sync`,
  `log-analyst`, `tutor`.
- Skills: `plan`, `verify`, `sim`, `replay`, `log-triage`, `new-mechanism`
  (scaffolds everything M2 built by hand), `explain`.
- Hooks as `frc hook ...` subcommands: session start summary, shell guard
  (deploy, push), edit guard (tests, scenario criteria, vendor imports
  outside `io/`), format and compile after edits.
- `tools/sync-ai`: generates Copilot path instructions and agent files
  from the Claude ones; CI fails on drift.
- Learning levels 0 to 3 documented; per-student setting in a git-ignored
  file.

Done when: `/plan` then `/verify` on a small real change produce a correct
plan and a PASS/FAIL/BLOCKED table, and the edit guard blocks an attempt to
delete a test.

## M6: Swerve behind `DriveIO`

Deliverables:

- Tuner X generated swerve for MK5i, R2 (6.03:1), Kraken X60 drive,
  CANcoder, Pigeon 2, CANivore. Constants in one file; ratio as an enum.
- `DriveIO` wrapping the generated drivetrain; `DriveIOSim`; replay IO.
- Field-relative teleop drive in `DriveTeleop` with a gamepad.
- Scenarios: drive a square; rotate in place; both assert final pose.

Spike folded in: does the wrapped drivetrain log and replay cleanly. If
not, fall back to per-module `ModuleIO` on raw Phoenix 6 devices.

Done when: scenarios pass in CI and a replayed drive log matches.

## M7: Vision and localization

Deliverables:

- PhotonLib added (human installs the vendor library).
- `VisionIO`, `VisionIOPhoton`, `VisionIOPhotonSim`, replay IO. Empty slot
  documented for QuestNav.
- Pose estimator fusing drive odometry with a list of vision sources.
- Logged per observation: residual, tags used, accepted or rejected.
- Scenario: drive past tags; assert estimated pose stays within a set
  error of the simulator's true pose.

Done when: `frc log` can report localization error for a sim run.

## M8: Autos and auto-align

Deliverables:

- PathPlanner behind an `AutoPaths` adapter (spike: following a path from
  a Commands v3 command).
- Two `@Autonomous` OpModes on a placeholder field, each with a scenario.
- Drive-to-pose command with a scenario asserting position, heading and
  time to align.

Done when: both autos and the align scenario pass in CI.

## M9: Context controls, driver metrics, dashboard

Deliverables:

- `RobotContext`: zone, held piece, alliance, exposed as triggers. Zones
  are data in one file.
- `Controls.java` bindings scoped by context.
- `frc log driver-report`: cycle time, time to align, idle time, time per
  zone.
- `frc log events` and a `match-debrief` skill: alerts, brownouts, CAN
  health, loop overruns.
- Elastic layout file in `dashboard/`.
- `logs/golden/` replayed in CI.

Done when: a driver report is produced from one sim "match" log.

## M10: Student onboarding

Deliverables:

- Setup guide for Windows and Mac (WPILib install, including the hidden
  `~/.wpilib` folder on Mac).
- A one-session walkthrough of the reference mechanism.
- Each student adds a toy mechanism at their level using spec, plan,
  build, scenario, PR with explain-back.
- First weekly AI-off code walk.

Done when: every student has one merged PR they can explain.

## M11: Kickoff readiness

Deliverables:

- Upgrade to WPILib Beta 1 and matching vendor libraries (expected to
  break vendor libraries). Use the M3 scenarios as the safety net.
- `docs/game/` template ready to fill.
- Platform layer frozen: changes to `lib/`, `Robot`, `RunProfile` and
  `tools/` need mentor review from here on.

Done when: all scenarios pass on the beta and the template repo is tagged.

---

## Hardware track (alongside, from M2)

| Step | Check |
| --- | --- |
| Deploy the current bare-bones code to the Systemcore | OpModes selectable from the 2027 Driver Station |
| Log to USB (MBR-formatted, not a USB 3 drive) | A log file appears and opens in AdvantageScope |
| Pull that log and replay it on a laptop | Replay runs to the end |
| Update device firmware to match Phoenix 6 `26.70` | Devices visible in Tuner X Preview |
| Bench profile for the M2 mechanism | Real motor on the bench, everything else simulated |
| Swerve on blocks, then on carpet | After M6 passes in sim |

Deploying is always a human action.

## Standing risks

- Beta 1 will break vendor libraries. Do not upgrade mid-milestone.
- `LoggedOpModeRobot` and Commands v3 are new; behavior may shift between
  alphas. The replay comparison in CI is the early warning.
- PathPlanner with Commands v3 is unproven here. If M8's spike fails, use
  WPILib's trajectory API.
- The CLI's struct decoding is the hardest part of M3. Ship numeric and
  boolean signals first.

## Next action

Start M1. It is small, needs no design decisions beyond the package name,
and gives every later milestone a CI gate.
