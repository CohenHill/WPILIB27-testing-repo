# Arm

Reference mechanism: a single-jointed arm driven by one motor. It shows the
IO pattern that every later mechanism copies. No real hardware is behind it
yet.

Owner: unassigned (mentor until a student takes it).

## Files

- `mechanisms/arm/Arm.java`: Commands v3 `Mechanism`. No vendor imports.
- `mechanisms/arm/ArmConstants.java`: limits, tolerance, gear ratio, gains,
  CAN id. Placeholder values.
- `mechanisms/arm/io/ArmIO.java`: the IO interface and logged inputs.
- `mechanisms/arm/io/ArmIOSim.java`: WPILib arm physics with a PID loop.
- `mechanisms/arm/io/ArmIOTalonFX.java`: Phoenix 6. Untested on hardware.

## Signals

| Key | Kind | Meaning |
| --- | --- | --- |
| `Arm/Connected` | input | Motor controller responding |
| `Arm/PositionRad` | input | Arm angle, 0 = horizontal, up positive |
| `Arm/VelocityRadPerSec` | input | Arm angular velocity |
| `Arm/AppliedVolts` | input | Voltage applied to the motor |
| `Arm/SupplyCurrentAmps` | input | Supply current |
| `RealOutputs/Arm/GoalRad` | output | Current goal angle |
| `RealOutputs/Arm/AtGoal` | output | Position within tolerance of the goal |

## Commands

- `moveTo(rad)`: clamps the goal to the soft limits, moves, finishes at goal.
- `hold()`: holds the current goal until interrupted. The default command.
- `stop()`: stops driving the arm until interrupted (the arm is not held, so it can fall).

`DriveTeleop` runs a scripted exercise that alternates between two angles
with one-second pauses, so a sim run produces data without a gamepad.
