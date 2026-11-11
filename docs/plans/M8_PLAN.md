# M8 plan (brief): autos and auto-align

Branch: `auto/first-routines`. Assumes M6 and M7 are merged.
This is a brief. Refresh and expand with Cohen before editing.

## Goal

Two autonomous routines that pass scenarios in CI, and a drive-to-pose
command for auto-alignment.

## Phase 1 is a spike

PathPlannerLib `2027.0.0-alpha-4.1` with Commands v3 is unproven here.
Read the library source for this version and answer:

- Does it provide Commands v3 commands, or only Commands v2?
- If only v2, can a path be sampled directly (trajectory in, target
  states out) so we can follow it from a v3 command ourselves?
- What does it need from the drivetrain (pose, velocities, a drive
  callback, robot config)?

Report and stop. Fallbacks, in order: follow sampled PathPlanner
trajectories from our own v3 command; WPILib's 2027 trajectory API;
Choreo. Cohen picks.

## Design

- `auto/AutoPaths.java`: the only class that imports the path library
  (add `auto/` to the vendor-import guard's allowed folders, or put the
  adapter under an `io/` folder). Exposes `follow(name)` as a v3 command.
- `Drive.driveToPose(pose)`: v3 command with translation and heading
  controllers, finishing within tolerance. Goal poses are data.
- `opmodes/`: two `@Autonomous` classes on a placeholder field, each a
  short sequence using `follow` and the Arm.
- Paths live in `src/main/deploy/`. A human draws them in PathPlanner.

## Deliverables

- Unit tests for `driveToPose` helper math.
- Scenarios: `AutoA`, `AutoB` (final pose and a "scored" stand-in such as
  the Arm reaching its goal at the right place), `AlignToPose` (position
  error, heading error, time to align).
- Every `@Autonomous` OpMode must have a scenario; add a test that fails
  if one does not.

## Done when

All three scenarios pass in CI and both autos replay.
