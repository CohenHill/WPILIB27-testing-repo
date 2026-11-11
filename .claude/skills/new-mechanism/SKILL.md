---
name: new-mechanism
description: Scaffold a new mechanism (IO, sim IO, mechanism, tests, scenario, doc card) by copying the Arm. Use when the user types /new-mechanism <Name>.
argument-hint: "[MechanismName]"
disable-model-invocation: true
---
Create mechanism `$ARGUMENTS` by copying the Arm pattern. First run
`echo ${FRC_AI_LEVEL:-1}`: it must be 3 (see `docs/ai-levels.md`); if lower, stop.
Then read AGENTS.md,
`docs/robot/arm.md` and every file under
`src/main/java/frc/robot/mechanisms/arm/`. If `$ARGUMENTS` is empty or
`mechanisms/<name>/` exists, stop and ask. Use lowercase for the package and
folder, `X` below for the class name.

Create, matching the Arm's structure and style:

1. `mechanisms/<name>/XConstants.java`: limits, tolerance, gains, CAN id (all
   placeholders, marked as such).
2. `mechanisms/<name>/io/XIO.java`: interface plus an `@AutoLog` inputs class;
   every method has an empty default body (so `new XIO() {}` is the replay IO).
3. `mechanisms/<name>/io/XIOSim.java`: a working sim IO (rule 2). Step physics
   once per `updateInputs` using `LoggedOpModeRobot.DEFAULT_PERIOD`.
4. `mechanisms/<name>/io/XIOTalonFX.java`: Phoenix 6; compiles, marked
   untested on hardware. Vendor imports only here (rule 1).
5. `mechanisms/<name>/X.java`: a Commands v3 `Mechanism`, no vendor imports,
   with `updateInputs()` and `recordOutputs()`, pure static helpers, and
   commands with logic inside the command body.
6. `RunProfile`: one factory method for the new IO.
7. `Robot`: add the mechanism; call its `updateInputs()` before the scheduler
   and `recordOutputs()` after, in the order the Arm uses.
8. `src/test/java/.../XTest.java`: tests for the pure helpers (no natives).
9. A scenario in `frc/robot/sim/scenarios/` with real pass criteria on logged
   or public values, registered in `Scenarios.ALL`.
10. `docs/robot/<name>.md` card (owner line "unassigned") and an entry under
    "Interfaces that exist today" and "Known broken or stale" in AGENTS.md.

Use the `api-check` skill for every WPILib, Commands v3 or vendor call you
have not already seen in the Arm. Then run `./gradlew build` and
`./frc sim <new scenario>` and quote the output. Do not commit.
