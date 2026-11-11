# Team 10101 robot code

![Build](https://github.com/CohenHill/WPILIB27-testing-repo/actions/workflows/build.yml/badge.svg)

FRC robot code on WPILib 2027 (alpha) for Systemcore: Java 25, Commands v3,
AdvantageKit logging and replay.

## Quick start

New here? Do `docs/setup.md` first, then `docs/tour.md`.

Run these in the WPILib VS Code terminal. `./frc` finds the WPILib Java 25 for
you (plain `./gradlew` needs `JAVA_HOME` set to it). On Windows use `.\frc.cmd`.

| To | Run |
| --- | --- |
| Build and run the unit tests | `./frc test` |
| See which simulations exist | `./frc sim --list` |
| Run one headless, with a pass or fail | `./frc sim DriveSquare` |
| Replay a log through the current code | `./frc replay <log>` |
| Read a log | `./frc log keys <log> Arm/` then `./frc log stats <log> Arm/PositionRad` |
| Get a driver report | `./frc sim SimMatch`, then `./frc log driver-report <log it printed>` |

**Drive the robot yourself in simulation:** in VS Code run *WPILib: Simulate
Robot Code* and turn on the Sim GUI when it asks. Plug in a gamepad, select
`DriveTeleop`, and enable. Left stick drives, right stick turns, hold A to run
the arm exercise, press B for the zone action. Open the log from `logs/` in
AdvantageScope. (The Sim GUI is off by default because replay needs it off.)

## Where things are

| Folder | What | Goes on the robot? |
| --- | --- | --- |
| `src/main/java` | Robot code: `Robot`, `Controls`, `opmodes/`, `mechanisms/` | Yes |
| `src/main/deploy` | Data files for the robot (`zones.json`, the Elastic layout) | Yes |
| `src/sim/java` | Headless simulation scenarios | No |
| `src/tools/java` | The `frc` command line tool and the AI hooks | No |
| `src/test/java` | Unit tests | No |
| `docs/` | Setup, tour, robot cards (`docs/robot/`), metrics, roadmap | No |

- `AGENTS.md`: project rules and commands, for people and AI tools.
- `CONTRIBUTING.md`: how to branch, commit and open a pull request.
- `docs/reference/2027-notes.md`: links to the 2027 documentation.
