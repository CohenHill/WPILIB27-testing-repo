# Golden log

`match.wpilog` is a log of the `SimMatch` scenario. CI replays it with the
current code and fails if the replayed outputs differ from the recorded ones
(`Arm/GoalRad`, `Arm/AtGoal`, `Drive/PoseX`, `Drive/PoseY`, `Drive/HeadingRad`).

A divergence means a code change altered how the robot reacts to the same
inputs. If that is intended, regenerate the log and commit it with a
sentence in the pull request saying why:

```
./frc sim SimMatch          # prints the new log path
cp <that log> logs/golden/match.wpilog
./frc replay logs/golden/match.wpilog   # then check ./frc log diff shows none
```

Replaying writes `match_sim.wpilog` here; it is git-ignored.
