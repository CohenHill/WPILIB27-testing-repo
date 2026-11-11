package frc.robot.sim.scenarios;

import frc.robot.Robot;
import frc.robot.mechanisms.arm.ArmConstants;
import frc.robot.sim.Check;
import frc.robot.sim.Scenario;
import java.util.List;

/** The arm never leaves its soft limits, and the loop never overruns. */
public class ArmHoldsWithinLimits implements Scenario {
  @Override
  public String name() {
    return "ArmHoldsWithinLimits";
  }

  @Override
  public String opMode() {
    return "Arm Exercise";
  }

  @Override
  public int durationLoops() {
    return 500;
  }

  @Override
  public List<Check> checks(Robot robot) {
    return List.of(new WithinLimitsCheck(robot), new NoOverrunsCheck());
  }

  private static final class WithinLimitsCheck implements Check {
    private final Robot robot;
    private double min = Double.POSITIVE_INFINITY;
    private double max = Double.NEGATIVE_INFINITY;

    WithinLimitsCheck(Robot robot) {
      this.robot = robot;
    }

    @Override
    public void sample(int loop) {
      double p = robot.arm.getPositionRad();
      min = Math.min(min, p);
      max = Math.max(max, p);
    }

    @Override
    public Result result(Stats stats) {
      boolean pass =
          min >= ArmConstants.MIN_ANGLE_RAD - 1e-9 && max <= ArmConstants.MAX_ANGLE_RAD + 1e-9;
      return new Result(
          "ArmWithinLimits",
          pass,
          "min " + min + " rad, max " + max + " rad",
          "within [" + ArmConstants.MIN_ANGLE_RAD + ", " + ArmConstants.MAX_ANGLE_RAD + "] rad");
    }
  }

  private static final class NoOverrunsCheck implements Check {
    @Override
    public void sample(int loop) {}

    @Override
    public Result result(Stats stats) {
      String observed =
          stats.overruns() == 0
              ? "0 overruns, worst loop gap " + Math.round(stats.maxGapMs()) + " ms"
              : stats.overruns()
                  + " overruns, first at loop "
                  + stats.firstOverrunLoop()
                  + ", worst loop gap "
                  + Math.round(stats.maxGapMs())
                  + " ms";
      return new Result("NoLoopOverruns", stats.overruns() == 0, observed, "0");
    }
  }
}
