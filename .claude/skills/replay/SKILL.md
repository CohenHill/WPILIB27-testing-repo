---
name: replay
description: Replay a simulation or robot log and compare replayed outputs with recorded ones. Use after changing logged inputs, mechanism logic or the loop order, or when asked whether replay still matches.
---
1. Get a log. Use the one given, or make one: `./frc sim <scenario>` prints a
   `log` path in its summary.
2. Replay it: `./frc replay <log>`. It prints `{"replayLog": "<path>"}`.
3. List the outputs to compare: `./frc log keys <replayLog> RealOutputs/`.
   Compare the numeric and boolean ones that matter for the change.
4. For each key (without the `RealOutputs/` prefix):
   `./frc log diff <replayLog> <key>`.
5. Report per key: `diverged` false, or the first divergence (`index`, `time`,
   `real`, `replay`). Struct and other non-numeric keys say "unsupported
   type": list them as not compared.

If the first samples diverge, check that the Driver Station state did not
change mid-run (see "Known broken or stale" in AGENTS.md). Never report replay
as matching without the diff lines.
