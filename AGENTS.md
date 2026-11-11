# AGENTS.md

FRC robot code. WPILib 2027 (alpha) on Systemcore, Java 25, Commands v3,
AdvantageKit logging and replay. Read this file before any change.

## Versions (source of truth: build.gradle and vendordeps/)

- GradleRIO / WPILib: see `build.gradle` plugins block
- AdvantageKit: see `vendordeps/AdvantageKit.json`
- Commands v3: see its file in `vendordeps/`

## 2027 is not what you remember

| You may want to write | Correct for 2027 |
| --- | --- |
| `edu.wpi.first...` imports | `org.wpilib...` |
| Commands v2 `Subsystem`, `RobotContainer` | Commands v3 `Mechanism`, OpModes |
| `robotInit()` | `Robot()` constructor |
| `SmartDashboard`, `SendableChooser` | Telemetry and Tunables APIs |
| `motor.set(x)` | `setThrottle(x)` |
| Integer CAN bus arguments | `CANPort` enum |
| `ChassisSpeeds` | `ChassisVelocities` |
| "Test" mode | "Utility" mode |

Never write a WPILib, Commands v3 or vendor call from memory. Use the
`api-check` skill: read the pinned version, open the class source or docs,
then write the call. Links are in `docs/reference/2027-notes.md`.

## Glossary

- Elastic: the Elastic FRC driver dashboard. Never Elasticsearch.
- Scope: AdvantageScope. AK: AdvantageKit.
- OpMode: a class annotated `@Teleop`, `@Autonomous` or `@Utility`.

## Commands

- Build and run tests: `./gradlew build`
- Tests only: `./gradlew test`
- Simulate (writes a log to `logs/`): `./gradlew run`
- Replay a log: `AKIT_LOG_PATH=<path to .wpilog> ./gradlew run`
  (PowerShell: `$env:AKIT_LOG_PATH='<path>'; ./gradlew run`)
- Windows: use `.\gradlew.bat` in place of `./gradlew`.

The build runs Spotless and reformats Java, Gradle, JSON and Markdown.

## Layout

- `src/main/java/frc/Main.java`: entry point. Do not change.
- `src/main/java/frc/robot/Robot.java`: logging setup, runs the Commands v3 scheduler each loop.
- `src/main/java/frc/robot/opmodes/`: one class per OpMode.
- `src/main/java/frc/robot/Controls.java`: button bindings.
- `src/main/java/frc/robot/Constants.java`: holds the current run mode.
- `src/main/java/frc/robot/RunProfile.java`: picks each mechanism's IO.
- `src/main/java/frc/robot/mechanisms/`: one folder per mechanism, with `io/`.
- `src/main/java/frc/robot/RunMode.java`: pure run mode selection (REAL, SIM, REPLAY).
- Tests live in `src/test/java`.

## Rules

1. Look up APIs before using them (see above).
2. No claim that something works without the command you ran and its output.
3. Never deploy to a robot and never push. A human does both.
4. Never edit `vendordeps/`, `.wpilib/`, `gradle/` or `gradlew*`.
5. Anything that reads wall-clock time or unlogged NetworkTables breaks
   replay. Ask before adding either.
6. Keep changes small and explainable. Students must be able to read them.

## Architecture rules

1. Vendor code never crosses the IO wall. Only files under an `io/` folder
   may import `com.ctre`, `yams`, `com.pathplanner` or `org.photonvision`.
   `checkVendorImports` fails the build otherwise.
2. Every mechanism ships with a working sim IO.
3. All inputs update in `Robot.robotPeriodic()` before the scheduler runs.
4. Command logic goes inside the command body, never beside it.
5. Coordination between mechanisms uses the Commands v3 `StateMachine`.
6. Time comes from the logged clock or the loop period only.

## Interfaces that exist today

- `Arm` (`frc.robot.mechanisms.arm`), a Commands v3 `Mechanism`.
  - Commands: `moveTo(rad)` (finishes at goal), `hold()` (default command),
    `stop()`.
  - Logged inputs under `Arm/`: `Connected`, `PositionRad`,
    `VelocityRadPerSec`, `AppliedVolts`, `SupplyCurrentAmps`.
  - Logged outputs under `RealOutputs/Arm/`: `GoalRad`, `AtGoal`.
- `ArmIO`: `updateInputs`, `setTargetPosition`, `setVoltage`, `stop`. An
  empty `new ArmIO() {}` is the replay IO.
- `RunProfile.armIO(RunMode)`: picks the IO (REAL, SIM, REPLAY).
- Details: `docs/robot/arm.md`.

## Known broken or stale

- `ArmIOTalonFX` compiles but has never run on hardware. The CAN id, bus
  name and gains in `ArmConstants` are placeholders.
- Sim gains and arm physics in `ArmConstants` are untuned.
- Nothing enables an OpMode headless yet, so a headless sim run records no
  arm motion (planned for M3).

## Open questions

None yet. Each entry will name an owner. If a task touches one, stop and ask.

## Before finishing any change

- [ ] `./gradlew build` passes; output quoted.
- [ ] New WPILib or vendor calls were checked against source or docs.
- [ ] No edits to protected paths (rule 4).
- [ ] If behavior changed: simulated, or stated as not verified.
- [ ] New logic has a unit test, or the reason it cannot is stated.
