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
- Simulate (writes a log to `logs/`): `./gradlew simulateJava`
- Replay a log: `AKIT_LOG_PATH=<path to .wpilog> ./gradlew simulateJava`
  (PowerShell: `$env:AKIT_LOG_PATH='<path>'; ./gradlew simulateJava`)
- Windows: use `.\gradlew.bat` in place of `./gradlew`.

The build runs Spotless and reformats Java, Gradle, JSON and Markdown.

## Layout

- `src/main/java/first/Main.java`: entry point. Do not change.
- `src/main/java/first/robot/Robot.java`: logging setup, scheduler loop.
- `src/main/java/first/robot/opmodes/`: one class per OpMode.
- `src/main/java/first/robot/Controls.java`: button bindings.
- `src/main/java/first/robot/Constants.java`: run mode selection.

## Rules

1. Look up APIs before using them (see above).
2. No claim that something works without the command you ran and its output.
3. Never deploy to a robot and never push. A human does both.
4. Never edit `vendordeps/`, `.wpilib/`, `gradle/` or `gradlew*`.
5. Anything that reads wall-clock time or unlogged NetworkTables breaks
   replay. Ask before adding either.
6. Keep changes small and explainable. Students must be able to read them.

## Open questions

None yet. Each entry will name an owner. If a task touches one, stop and ask.

## Before finishing any change

- [ ] `./gradlew build` passes; output quoted.
- [ ] New WPILib or vendor calls were checked against source or docs.
- [ ] No edits to protected paths (rule 4).
- [ ] If behavior changed: simulated, or stated as not verified.
