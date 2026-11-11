# Log metrics

Definitions for `frc log events`, `track` and `driver-report`. Times are in
seconds from the first data record in the log. Every metric is computed only
from signals that are in the log; if a signal is missing, the metric says so
instead of guessing.

## Enabled time

The robot is **enabled** while `DriverStation/Enabled` is true. A log with no
`Enabled` samples has zero enabled time. Samples are changes, so each value
holds until the next sample; the last value holds until the last record in the
log. `driver-report` metrics that say "while enabled" use this.

## `frc log events <log>`

A time-ordered list of things that happened. Types:

| Type | Source | Meaning |
| --- | --- | --- |
| `enabled`, `disabled` | `DriverStation/Enabled` changes | Robot enable and disable |
| `ds-attached`, `ds-detached` | `DriverStation/DSAttached` changes | Driver Station connection |
| `estop` | `DriverStation/EmergencyStop` becomes true | Emergency stop |
| `mode` | `DriverStation/OpMode` changes (non-empty) | The selected OpMode changed; detail is its name |
| `alert-error`, `alert-warning` | `RealOutputs/AlertGroups/<group>/errors` and `warnings` | A non-empty alert list appeared or changed; detail is `group: text` |
| `brownout` | `SystemStats/FaultCounts/Brownout` increases | The roboRIO-class controller browned out |
| `loop-overrun` | `RealOutputs/LoggedOpModeRobot/FullCycleMS` > 80 | A robot loop took more than 4 periods (80 ms), the same limit as the loop watchdog |

Output: `{"total": N, "shown": M, "events": [{"t":..., "type":..., "detail":...}]}`,
capped at 50 events unless `--limit` is given.

## `frc log track <log> <mechanism>`

How well a mechanism followed its goal. Mechanisms known to the tool: `Arm`
(goal `RealOutputs/Arm/GoalRad`, measured `Arm/PositionRad`). A mechanism
without a numeric goal signal is reported as "no goal signal".

The goal is a step function: each goal sample holds until the next. For every
measured sample, `error = measured - goal`. Reported: `samples`,
`meanAbsError`, `rmsError`, `maxAbsError`, `finalError` (last sample), and
`fractionNearGoal`, the share of samples with `|error|` no more than the
mechanism's tolerance (Arm: 0.05 rad, the same as `ArmConstants`). Samples
before the first goal sample are ignored.

## `frc log driver-report <log>`

All of these are measured over enabled time only.

| Metric | Definition |
| --- | --- |
| `enabledSeconds` | Total enabled time |
| `cycles` | Number of times `RealOutputs/Context/HoldingPiece` goes true to false (one score) |
| `cycleSeconds` | For each score, the time since the previous score, or since the first enable for the first one. Reported as `mean`, `min`, `max`. Absent if there are no scores |
| `timeToAlign` | Not available yet: no align command exists (M8), so no signal is logged. Reported as `null` with a reason |
| `idleSeconds` | Enabled time when the robot's speed is under 0.05 m/s. Speed is the distance between consecutive `RealOutputs/Drive/PoseX` and `PoseY` samples divided by the time between them |
| `secondsPerZone` | Enabled time spent in each value of `RealOutputs/Context/Zone` |

Limits: pose samples are logged on change, so a long gap between samples is
treated as the robot standing still. A signal that is not in the log makes
its metrics `null` with a reason.

## `frc log pull`

Fetch the newest log from the Systemcore. **BLOCKED** until a robot is
available: the command exists and says so, and does not fetch anything.
