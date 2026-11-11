---
paths:
  - "src/main/java/frc/robot/opmodes/**"
---
# OpMode rules

- An OpMode is a public class implementing `org.wpilib.opmode.OpMode`,
  annotated `@Teleop`, `@Autonomous` or `@Utility`, in a package under
  `frc.robot` so the annotation scan finds it.
- It has a public constructor taking `Robot` (preferred) or no arguments.
- Lifecycle: `start()` once on enable, `periodic()` every enabled loop, `end()`
  on disable. Do not use `PeriodicOpMode.addPeriodic`: AdvantageKit ignores it.
- Schedule commands in `start()` and cancel them in `end()`.
- Time comes from the logged clock or the loop period only. No wall clock, no
  unlogged NetworkTables reads (they break replay).
