# M2 plan: reference mechanism through the IO wall

Branch: `arm/reference-mechanism`. Assumes M1 is merged (`frc.robot`,
`RunMode`, first test).

## Goal

Build one simple mechanism, a single-jointed arm, by hand and without
YAMS, so the IO pattern is visible to every student and agent that copies
it later. Prove it by recording a sim run and replaying it.

## Out of scope

YAMS, the `frc` CLI, headless enabling, swerve, gamepads, tuning UI,
bench profiles. The arm has no real hardware behind it yet.

## Design

```
frc/robot/
  RunProfile.java                 picks an IO implementation per mechanism
  mechanisms/arm/
    Arm.java                      Commands v3 Mechanism. No vendor imports
    ArmConstants.java             limits, tolerances, gains, CAN id
    io/
      ArmIO.java                  interface + @AutoLog inputs class
      ArmIOSim.java               WPILib arm physics + PID
      ArmIOTalonFX.java           Phoenix 6. Compiles; untested on hardware
```

`ArmIO`:

- Inputs (all logged): `connected`, `positionRad`, `velocityRadPerSec`,
  `appliedVolts`, `supplyCurrentAmps`.
- Methods, each with an empty default body so `new ArmIO() {}` is the
  replay implementation: `updateInputs(inputs)`, `setTargetPosition(rad)`,
  `setVoltage(volts)`, `stop()`.

`Arm`:

- Holds the IO and the inputs object. Nothing else touches the IO.
- `updateInputs()`: `io.updateInputs(inputs)` then
  `Logger.processInputs("Arm", inputs)`.
- Pure helpers (static, unit-testable): `clampGoal(rad)` to soft limits,
  `atGoal(positionRad, goalRad)` within tolerance.
- Command methods: `moveTo(rad)` (finishes when at goal), `hold()` (holds
  the current goal until interrupted), `stop()`. A safe default command.
- Records outputs each loop: `Arm/GoalRad`, `Arm/AtGoal`.

`RunProfile`: for now derived from `RunMode` only. `REAL` gives
`ArmIOTalonFX`, `SIM` gives `ArmIOSim`, `REPLAY` gives the empty IO. Write
it as a small class with one factory method per mechanism, so bench
profiles can be added later without touching mechanisms.

`Robot.robotPeriodic()` order, with a comment explaining why:

1. `arm.updateInputs()` (all mechanisms, before any logic)
2. `Scheduler.getDefault().run()`
3. record outputs

Commands v3 mechanisms have no `periodic()`; if step 1 is missing, replay
silently breaks.

## Verify before writing (refresh step, report first)

- Commands v3: how a class implements `Mechanism`, how `run` or
  `runRepeatedly` build a command, how a default command is set, and how
  a command ends. Read "Command Mechanisms" and "Creating Commands".
- AdvantageKit: `@AutoLog` usage and the generated class name
  (`ArmIOInputsAutoLogged`), `Logger.processInputs`, `Logger.recordOutput`.
  The annotation processor is already in `build.gradle`.
- WPILib 2027: package and constructor of the single-jointed arm sim and
  `PIDController`; whether they need native libraries in a unit test.
- Phoenix 6 `26.70.0-alpha-2`: `TalonFX` constructor with `CANBus`,
  position control request, and how to read position, velocity, voltage
  and current. See the `2027_alpha7` branch of Phoenix6-Examples.
- Where a time step comes from in sim. Use the fixed loop period, not a
  wall clock.

## Phase 1: IO layer and RunProfile (commit 1)

Create `ArmIO`, `ArmIOSim`, `ArmIOTalonFX`, `ArmConstants`, `RunProfile`.
`ArmIOSim` steps the physics once per `updateInputs` call using the loop
period. `ArmIOTalonFX` uses a placeholder CAN id from `ArmConstants` and
the CANivore bus; mark it clearly as untested on hardware.

Done when: `./gradlew build` passes.
Commit: `Add Arm IO layer and RunProfile`

## Phase 2: mechanism, tests, wiring (commit 2)

- Create `Arm` as designed. Add `public final Arm arm` to `Robot`, built
  from `RunProfile`.
- Fix the `robotPeriodic()` order.
- `DriveTeleop`: on start, schedule a scripted exercise that moves the arm
  between two angles and repeats, so a sim run produces interesting data
  with no gamepad. Use Commands v3 waits, not a wall clock.
- Tests in `src/test/java/frc/robot/mechanisms/arm/ArmTest.java`:
  `clampGoal` at, inside and beyond both limits; `atGoal` at exactly the
  tolerance and just outside it. Pure functions only; no WPILib natives.

Done when:

- `./gradlew build` passes and the new tests ran.
- `./gradlew run` starts without exceptions and the log contains `Arm/`
  inputs (BLOCKED if enabling is needed to see motion; say so).

Commit: `Add Arm mechanism with scripted teleop exercise`

## Phase 3: vendor-import guard and docs (commit 3)

- `build.gradle`: add a `checkVendorImports` task wired into `check`. It
  fails the build if any file under `src/main/java` outside an `io/`
  folder imports `com.ctre`, `yams`, `com.pathplanner` or `org.photonvision`.
  Prove it: add a bad import temporarily, quote the failure, revert.
- `AGENTS.md`: add "Architecture rules" (the six below), "Interfaces that
  exist today" (the Arm's logged signals and commands) and "Known broken
  or stale" (ArmIOTalonFX untested on hardware; placeholder CAN id).
- `docs/robot/arm.md`: what it is, its signals, its commands, its owner.
- `ROADMAP.md`: mark M2 items done.

The six rules:

1. Vendor code never crosses the IO wall.
2. Every mechanism ships with a working sim IO.
3. All inputs update in `Robot.robotPeriodic()` before the scheduler runs.
4. Command logic goes inside the command body, never beside it.
5. Coordination between mechanisms uses the Commands v3 `StateMachine`.
6. Time comes from the logged clock or the loop period only.

Commit: `Guard vendor imports and document the Arm`

## Phase 4: human check (Cohen)

- [ ] Run sim from WPILib VS Code with the Sim GUI enabled. Select and
      enable `DriveTeleop`. In AdvantageScope the arm position follows the
      goal.
- [ ] Stop. Replay that log: `AKIT_LOG_PATH=<log> ./gradlew run`.
- [ ] Open the `_sim` log. `RealOutputs/Arm/GoalRad` and
      `ReplayOutputs/Arm/GoalRad` are identical; same for `AtGoal`.

M2 is done when the last box is ticked. M3 automates this check.
