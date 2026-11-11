# Drive

Swerve drivetrain on Phoenix 6, behind `DriveIO`. It drives in simulation from
scenarios and a gamepad, and replays.

Owner: unassigned (mentor until a student takes it).

## Hardware assumed (not confirmed)

SDS MK5i R2 (6.03:1 drive, 26:1 steer), Kraken X60 drive, **steering motor:
Kraken X60 assumed, X44 not ruled out (to confirm)**, CANcoder, Pigeon 2,
CANivore. `TunerConstants` is copied from CTRE's Phoenix6-Examples
(`2027_alpha7`) and edited for the above. Every hardware value in it is a
placeholder: CAN ids, encoder offsets, inversions, module positions, coupling
ratio, steer gains and speed at 12 V. Replace the file with real Tuner X
output when the drivebase exists.

## Files

- `mechanisms/drive/Drive.java`: Commands v3 `Mechanism`, no vendor imports.
- `mechanisms/drive/DriveConstants.java`: speeds, deadband, `ModuleRatio` enum.
- `mechanisms/drive/io/DriveIO.java`: the interface and logged inputs.
- `mechanisms/drive/io/DriveIOCtre.java`: wraps the CTRE drivetrain (real and sim).
- `mechanisms/drive/io/generated/TunerConstants.java`: constants (see above).

## Signals

| Key | Kind | Meaning |
| --- | --- | --- |
| `Drive/Connected` | input | Drivetrain has produced data |
| `Drive/Pose` | input | Odometry pose (struct) |
| `Drive/Velocity` | input | Robot-relative chassis velocities (struct) |
| `Drive/ModulePositions`, `ModuleVelocities`, `ModuleTargets` | input | Per module (struct arrays) |
| `Drive/GyroYaw`, `GyroRateRadPerSec` | input | Pigeon 2 |
| `Drive/OdometryPeriodSec` | input | Odometry period |
| `RealOutputs/Drive/Pose`, `ModuleStates`, `ModuleTargets` | output | Struct outputs |
| `RealOutputs/Drive/PoseX`, `PoseY`, `HeadingRad` | output | Numbers, so `frc log diff` can compare them |

## Commands

- `teleopDrive(x, y, omega)`: field-relative from -1..1 stick suppliers
  (deadband, squared, scaled to the max speeds).
- `driveFieldRelative(vx, vy, omega)`: fixed velocities until interrupted.
- `stop()`: the default command; holds the modules idle every loop.

## Teleop and scenarios

`DriveTeleop` sets `teleopDrive` as the drive's default command while it runs
(scoped to a command, so it reverts to `stop` when the OpMode ends). Hold the
driver gamepad's south face button to run the scripted arm exercise.

Scenarios: `DriveSquare`, `DriveRotateInPlace`, `TeleopIdleHoldsStill`. They use
the scripted OpModes `Drive Square` and `Drive Rotate` (Utility mode).

## Things to know

- In simulation the CTRE drivetrain is stepped on its own 4 ms `Notifier`
  thread with a wall clock, not on the 20 ms robot loop (copied from CTRE's
  example). Pose depends on real time, so runs are not bit-identical and the
  `SIM_FAST` option from M3 is not meaningful for the drive.
- `DriveRotateInPlace` is an open-loop timed turn: it coasts about 0.65 rad past
  one full turn, so its heading tolerance is 0.9 rad. Tighten it when heading
  control exists.
- The rotation scenario tolerance and all speeds are from simulation of
  placeholder constants, not a real robot.
- `Drive.addVisionMeasurement` is in the IO but not yet used (M7).
