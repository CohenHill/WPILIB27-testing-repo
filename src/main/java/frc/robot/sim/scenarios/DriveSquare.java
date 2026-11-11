package frc.robot.sim.scenarios;

import frc.robot.Robot;
import frc.robot.sim.Check;
import frc.robot.sim.Scenario;
import java.util.List;
import org.wpilib.math.geometry.Pose2d;

/** Scripted field-relative velocities trace a square; the robot ends near where it started. */
public class DriveSquare implements Scenario {
  /** Allowed distance between the final and start positions, in meters. */
  public static final double RETURN_TOLERANCE_METERS = 0.3;

  /** The robot must get at least this far from the start, so a dead drivetrain fails. */
  public static final double MIN_TRAVEL_METERS = 0.5;

  @Override
  public String name() {
    return "DriveSquare";
  }

  @Override
  public String opMode() {
    return "Drive Square";
  }

  @Override
  public int durationLoops() {
    return 300;
  }

  @Override
  public List<Check> checks(Robot robot) {
    return List.of(new SquareCheck(robot));
  }

  private static final class SquareCheck implements Check {
    private final Robot robot;
    private Pose2d start = null;
    private Pose2d last = null;
    private double maxDistance = 0.0;

    SquareCheck(Robot robot) {
      this.robot = robot;
    }

    @Override
    public void sample(int loop) {
      Pose2d p = robot.drive.getPose();
      if (start == null) {
        start = p;
      }
      last = p;
      maxDistance = Math.max(maxDistance, p.getTranslation().getDistance(start.getTranslation()));
    }

    @Override
    public Result result(Stats stats) {
      double returnError = last.getTranslation().getDistance(start.getTranslation());
      boolean pass = maxDistance >= MIN_TRAVEL_METERS && returnError <= RETURN_TOLERANCE_METERS;
      return new Result(
          "DriveSquare",
          pass,
          "max distance from start "
              + maxDistance
              + " m, final distance from start "
              + returnError
              + " m",
          "max distance >= "
              + MIN_TRAVEL_METERS
              + " m and final distance <= "
              + RETURN_TOLERANCE_METERS
              + " m");
    }
  }
}
