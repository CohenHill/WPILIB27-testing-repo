# M6 plan: swerve behind `DriveIO`

Branch: `drive/swerve`. Assumes M3 is merged.

## Goal

A Phoenix 6 swerve drivetrain that drives in simulation from scenarios and
a gamepad, behind a single `DriveIO`, and replays.

## Hardware assumed

SDS MK5i (probably), R2 drive ratio 6.03:1, steering 26:1, Kraken X60
drive, Kraken X44 or X60 steer (confirm), CANcoder, Pigeon 2, CANivore.

## Out of scope

Vision fusion (M7), path following and auto-align (M8), YAMS or YAGSL.

## Phase 0: human (Cohen)

- [ ] Generate the swerve project with Tuner X Preview for Phoenix 6
      `26.70.0-alpha-2`. This normally needs the real modules on the CAN
      bus. If the drivebase is not ready, copy the generated-constants
      file from the `2027_alpha7` branch of CTRE's Phoenix6-Examples and
      edit it for the hardware above.
- [ ] Put the generated files under
      `src/main/java/frc/robot/mechanisms/drive/io/generated/` and commit
      them unchanged.
- [ ] Decide the steering motor and record it in `docs/robot/drive.md`.

## Design

```
mechanisms/drive/
  Drive.java            Mechanism. No vendor imports
  DriveConstants.java   max speeds, module ratio enum (R1, R2, R3)
  io/
    DriveIO.java        interface + @AutoLog inputs
    DriveIOCtre.java    wraps the generated drivetrain (real and sim)
    generated/          Tuner X output, untouched
```

`DriveIO` inputs: `connected`, odometry `pose`, robot-relative chassis
velocities, per-module positions and target states, gyro yaw and rate,
odometry period.
Methods: `driveFieldRelative(vx, vy, omega)`,
`driveRobotRelative(vx, vy, omega)`, `stop()`, `resetPose(pose)`,
`addVisionMeasurement(pose, timestamp, stdDevs)` (used in M7).

`Drive`: command methods `teleopDrive(suppliers)`, `stop()`; default
command is stop. Records `Drive/Pose`, `Drive/ModuleStates`,
`Drive/ModuleTargets`.

## Verify before writing (refresh step, report first)

- What Tuner X generated: class names, how the drivetrain is constructed,
  how requests are applied, how simulation is stepped.
- Whether the generated simulation runs on its own thread with a wall
  clock. If it does, report it: that affects determinism and the fast-sim
  option from M3. Do not rewrite it without asking.
- Names of the 2027 geometry and kinematics types (`ChassisVelocities`,
  module state and position types) and AdvantageKit's support for logging
  them.
- Gamepad API in 2027 (`Gamepad`, the Commands v3 gamepad class, default
  deadband) and how gamepad input is logged for replay.

## Phase 1: IO and mechanism (commit 1)

Build the design. `RunProfile` gets a `drive()` factory. Add
`drive.updateInputs()` to `Robot.robotPeriodic()` before the scheduler.

Done when: build passes; unit tests cover any pure helpers (deadband,
speed scaling, ratio enum).
Commit: `Add Drive mechanism behind DriveIO`

## Phase 2: scenarios (commit 2)

- `DriveSquare`: scripted field-relative velocities trace a square; final
  pose within tolerance of the start.
- `DriveRotateInPlace`: 360 degrees; final heading within tolerance;
  translation drift under a limit.

Done when: both pass via `frc sim`; replay then `frc log diff` matches on
the Drive outputs, or the mismatch is explained and reported.
Commit: `Add drive scenarios`

## Phase 3: teleop (commit 3)

`Controls.java` gets the driver gamepad. `DriveTeleop` sets
`drive.teleopDrive(...)` as the mode's default command. Keep the Arm
exercise behind a button.

Done when: build passes. Driving with a real gamepad is a human check.
Commit: `Add field-relative teleop drive`

## Phase 4: docs

`docs/robot/drive.md`, `AGENTS.md` interfaces table, `ROADMAP.md`.
If replay did not match, add the reason to "Known broken or stale" and
write `docs/decisions/0002-drive-io.md` on whether to fall back to
per-module IO.

## Phase 5: human check (Cohen)

- [ ] Drive in sim with a gamepad; pose in AdvantageScope looks right.
- [ ] BLOCKED until hardware: modules on blocks, then on carpet.
