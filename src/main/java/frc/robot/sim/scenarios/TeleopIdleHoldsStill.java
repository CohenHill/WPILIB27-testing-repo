package frc.robot.sim.scenarios;

import frc.robot.Robot;
import frc.robot.sim.Check;
import frc.robot.sim.Scenario;
import java.util.List;
import org.wpilib.math.geometry.Pose2d;

/** Teleop with no stick input: the robot must not move. Exercises the teleop wiring. */
public class TeleopIdleHoldsStill implements Scenario {
  /** Allowed movement from the start pose, in meters. */
  public static final double DRIFT_LIMIT_METERS = 0.02;

  @Override
  public String name() {
    return "TeleopIdleHoldsStill";
  }

  @Override
  public String opMode() {
    return "DriveTeleop";
  }

  @Override
  public int durationLoops() {
    return 150;
  }

  @Override
  public List<Check> checks(Robot robot) {
    return List.of(new StillCheck(robot));
  }

  private static final class StillCheck implements Check {
    private final Robot robot;
    private Pose2d start = null;
    private double maxDrift = 0.0;
    private int samples = 0;

    StillCheck(Robot robot) {
      this.robot = robot;
    }

    @Override
    public void sample(int loop) {
      Pose2d p = robot.drive.getPose();
      if (start == null) {
        start = p;
      }
      samples++;
      maxDrift = Math.max(maxDrift, p.getTranslation().getDistance(start.getTranslation()));
    }

    @Override
    public Result result(Stats stats) {
      return new Result(
          "TeleopIdleHoldsStill",
          samples > 0 && maxDrift <= DRIFT_LIMIT_METERS,
          "max drift " + maxDrift + " m over " + samples + " loops",
          "max drift <= " + DRIFT_LIMIT_METERS + " m");
    }
  }
}
