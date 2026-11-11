---
paths:
  - "src/main/java/frc/robot/mechanisms/**"
---
# Mechanism rules

- A mechanism is a Commands v3 `Mechanism` with no vendor imports. It talks to
  hardware only through its IO interface.
- Inputs are read in `updateInputs()`, called from `Robot.robotPeriodic()`
  before the scheduler runs. Outputs are recorded after the scheduler.
- Command logic goes inside the command body, never beside it. Commands v3
  mechanisms have no `periodic()`.
- Clamp every goal to the soft limits. Keep limits, tolerances and gains in the
  mechanism's constants file.
- Put pure logic in static methods and unit test it (no WPILib natives).
- Coordinating several mechanisms uses the Commands v3 `StateMachine`.
- Update the mechanism card in `docs/robot/` and AGENTS.md when signals or
  commands change.
