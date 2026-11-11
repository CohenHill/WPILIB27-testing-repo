package frc.robot.sim.scenarios;

import frc.robot.Robot;
import frc.robot.sim.Check;
import frc.robot.sim.Scenario;
import java.util.List;
import org.wpilib.math.geometry.Pose2d;
import org.wpilib.math.geometry.Rotation2d;

/** One full turn in place: the heading returns to the start and the robot barely translates. */
public class DriveRotateInPlace implements Scenario {
  /**
   * Allowed difference between the final and start headings, in radians. The turn is open loop and
   * timed, so the robot coasts past one full turn after the command stops (about 0.65 rad measured
   * in simulation). Tighten this when heading control exists (M8).
   */
  public static final double HEADING_TOLERANCE_RAD = 0.9;

  /** Allowed translation away from the start, in meters. */
  public static final double DRIFT_LIMIT_METERS = 0.15;

  /** The robot must turn at least this far (radians), so a dead drivetrain fails. */
  public static final double MIN_TURN_RAD = 2.0 * Math.PI - 0.5;

  /** The robot must not turn more than this far (radians), so a spinning drivetrain fails. */
  public static final double MAX_TURN_RAD = 2.0 * Math.PI + 1.0;

  @Override
  public String name() {
    return "DriveRotateInPlace";
  }

  @Override
  public String opMode() {
    return "Drive Rotate";
  }

  @Override
  public int durationLoops() {
    return 200;
  }

  @Override
  public List<Check> checks(Robot robot) {
    return List.of(new RotateCheck(robot));
  }

  private static final class RotateCheck implements Check {
    private final Robot robot;
    private Pose2d start = null;
    private Pose2d last = null;
    private double turned = 0.0;
    private double maxDrift = 0.0;

    RotateCheck(Robot robot) {
      this.robot = robot;
    }

    @Override
    public void sample(int loop) {
      Pose2d p = robot.drive.getPose();
      if (start == null) {
        start = p;
      } else {
        Rotation2d step = p.getRotation().minus(last.getRotation());
        turned += step.getRadians();
      }
      last = p;
      maxDrift = Math.max(maxDrift, p.getTranslation().getDistance(start.getTranslation()));
    }

    @Override
    public Result result(Stats stats) {
      double headingError = Math.abs(last.getRotation().minus(start.getRotation()).getRadians());
      boolean pass =
          turned >= MIN_TURN_RAD
              && turned <= MAX_TURN_RAD
              && headingError <= HEADING_TOLERANCE_RAD
              && maxDrift <= DRIFT_LIMIT_METERS;
      return new Result(
          "DriveRotateInPlace",
          pass,
          "turned "
              + turned
              + " rad, final heading error "
              + headingError
              + " rad, max drift "
              + maxDrift
              + " m",
          MIN_TURN_RAD
              + " <= turned <= "
              + MAX_TURN_RAD
              + " rad, heading error <= "
              + HEADING_TOLERANCE_RAD
              + " rad, drift <= "
              + DRIFT_LIMIT_METERS
              + " m");
    }
  }
}
