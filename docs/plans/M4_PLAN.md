# M4 plan: YAMS behind IO, keep or drop

Branch: `arm/yams-io`. Assumes M3 is merged (scenarios, `frc`).

## Goal

Implement the Arm's IO a second time using YAMS, run the same scenarios
and replay comparison against it, and write down whether YAMS stays.

## Out of scope

YAMS command or trigger factories, YAMS swerve, YAGSL, changing `Arm.java`
behavior, any new mechanism.

## Constraints

- YAMS code lives only in `mechanisms/arm/io/ArmIOYams.java`.
- `Arm.java` and both scenarios must not change. If they have to, that is
  a finding for the decision note, not something to work around quietly.
- The vendor-import guard already allows `yams` inside `io/`.

## Verify before writing (refresh step, report first)

Read YAMS `2026.11.06` (the version in `vendordeps/yams_commands3.json`):

- The Commands v3 AdvantageKit example in the YAMS repository
  (`examples/commands3/advantage_kit`) and the docs at
  https://yams.yassrobotics.com/documentation/tutorials/advantagekit-integration
- `SmartMotorControllerConfig` in the Commands v3 build takes a
  `Mechanism`. Find out what it uses it for. Our IO does not own a
  mechanism, so report how the example handles this and propose the
  smallest way to satisfy it (for example, passing the `Arm` into the IO
  factory).
- How the YAMS arm is stepped in simulation (`simIterate` or equivalent),
  whether it uses the loop period or a wall clock, and what it publishes
  to NetworkTables on its own.
- Which extra vendor libraries YAMS needs at build and run time. If the
  build needs REVLib or ThriftyLib, stop; a human installs those.

## Phase 1: `ArmIOYams` (commit 1)

One class that serves the real robot and simulation, plus the existing
empty replay IO. Map YAMS readings to the existing `ArmIO` inputs; do not
add inputs. Add a `RunProfile` switch (environment variable
`ARM_IO=yams|plain`, default `plain`) so both stay runnable.

Done when: `./gradlew build` passes.
Commit: `Add YAMS-backed Arm IO behind the existing interface`

## Phase 2: evidence (no code changes expected)

Run, for `plain` and for `yams`:

- `frc sim ArmReachesGoal`, `frc sim ArmHoldsWithinLimits`
- `frc replay` then `frc log diff` on `Arm/GoalRad` and `Arm/AtGoal`
- the same scenario twice, comparing `frc log stats` on position

Record for each: pass or fail, time to goal, whether replay matched,
whether two runs were identical.

## Phase 3: decision note (commit 2)

Write `docs/decisions/0001-yams.md`:

- Lines of code: `ArmIOSim` + `ArmIOTalonFX` against `ArmIOYams`.
- The evidence table from Phase 2.
- What YAMS needed that the plain version did not (the `Mechanism`
  reference, extra libraries, NetworkTables side effects).
- Anything that broke or surprised.
- Recommendation: keep as the default for standard mechanisms, keep as an
  option, or remove. Cohen makes the call.

Then, according to the decision:

- Keep: make `yams` the default in `RunProfile`; note in `AGENTS.md` that
  standard mechanisms use `XIOYams` and unusual ones use hand-written IO.
- Drop: delete `ArmIOYams`, remove the switch, and list the YAMS vendor
  library under "Known broken or stale" for a human to uninstall.

Commit: `Record YAMS decision`
