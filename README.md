# Team 10101 robot code

![Build](https://github.com/CohenHill/WPILIB27-testing-repo/actions/workflows/build.yml/badge.svg)

FRC robot code on WPILib 2027 (alpha) for Systemcore: Java 25, Commands v3,
AdvantageKit logging and replay.

## Quick start

1. Install WPILib 2027 (see `AGENTS.md` for the version in use).
2. Open this folder in the WPILib VS Code.
3. Build and test: `./gradlew build` (Windows: `.\gradlew.bat build`)
4. Simulate: `./gradlew run`. A log is written to `logs/`.
5. Replay a log: `AKIT_LOG_PATH=<path to .wpilog> ./gradlew run`

## Where things are

- `docs/setup.md`, `docs/tour.md`, `docs/first-task.md`: new student path.
- `AGENTS.md`: project rules and commands, for people and AI tools.
- `CONTRIBUTING.md`: how to branch, commit and open a pull request.
- `docs/ROADMAP.md`: what is being built and in what order.
- `docs/reference/2027-notes.md`: links to the 2027 documentation.
