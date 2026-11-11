# M7 plan (brief): vision and localization

Branch: `vision/localization`. Assumes M6 is merged.
This is a brief. Do the refresh step thoroughly and expand it into phases
with Cohen before editing.

## Goal

Fuse AprilTag observations from any number of sources into the drive pose,
and measure how good the result is.

## Phase 0: human

- [ ] Install the PhotonLib 2027 vendor library and commit it.
- [ ] Record camera names and mounting positions in `docs/robot/vision.md`
      (placeholders are fine until the robot exists).

## Design

```
mechanisms/vision/
  Vision.java               not hardware; consumes observations
  VisionConstants.java      camera transforms, rejection thresholds
  io/
    VisionIO.java           interface + @AutoLog inputs
    VisionIOPhoton.java
    VisionIOPhotonSim.java
```

- One `VisionIO` per camera. Inputs: `connected`, and a list of pose
  observations (timestamp, pose, ambiguity, tag count, average tag
  distance, tag ids).
- `Vision` applies the same rejection rules to every source (ambiguity,
  distance, off-field, tilt), computes standard deviations, and calls
  `drive.addVisionMeasurement(...)`.
- Logged per observation: accepted or rejected with the reason, and the
  residual against the current pose.
- QuestNav: leave a documented empty slot (`VisionIOQuestNav`), no code.

## Verify first

PhotonLib 2027 API for pose estimation and simulation; the 2027 AprilTag
field layout API (`Fields`); how AdvantageKit logs arrays of structs; how
the simulator's true pose can be read for scoring.

## Deliverables

- The design above, with `RunProfile` wiring and input updates in
  `Robot.robotPeriodic()`.
- Unit tests for the rejection rules (pure functions).
- Scenario `LocalizeWhileDriving`: drive past tags with odometry noise
  injected in sim; estimated pose stays within a set error of true pose.
- Sim-only output `Vision/PoseErrorMeters`.
- `frc log stats` on that signal documented in the `log-triage` skill.

## Done when

The scenario passes in CI, and `frc log stats <log> Vision/PoseErrorMeters`
reports the error for any sim run.
