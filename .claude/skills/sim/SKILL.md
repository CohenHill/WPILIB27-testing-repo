---
name: sim
description: Run headless simulation scenarios for the current change and quote the results. Use after changing mechanism, IO, command or OpMode code, or when asked to simulate or run a scenario.
---
1. List scenarios: `./frc sim --list`.
2. Pick the scenarios that cover the change. Arm code (`mechanisms/arm/`, its
   IO, `DriveTeleop`): `ArmReachesGoal` and `ArmHoldsWithinLimits`. If no
   scenario covers the change, say so; do not claim coverage.
3. Run each: `./frc sim <scenario>`. It prints one JSON summary line and exits
   0 for pass. To run the other Arm IO: `ARM_IO=yams ./frc sim <scenario>`.
4. Quote, per scenario: `pass`, each check's `name`, `observed` and
   `expected`, `loops` and `loopOverruns`. The full result is in
   `build/sim/<scenario>/summary.json`.
5. A failure with `NoLoopOverruns` and one large loop gap can be a machine
   stall: run the scenario once more before reporting a real failure, and say
   you did.

Never say a scenario passed without its summary line. Do not edit a scenario
or a check to make it pass.
