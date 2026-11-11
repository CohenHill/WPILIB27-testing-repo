package frc.robot.sim.scenarios;

import frc.robot.Robot;
import frc.robot.mechanisms.arm.ArmConstants;
import frc.robot.sim.Check;
import frc.robot.sim.Scenario;
import java.util.List;

/** The arm reaches its first goal within a loop budget. */
public class ArmReachesGoal implements Scenario {
  /** Loops allowed between the goal first changing and the arm being at the goal. */
  public static final int MAX_LOOPS_TO_GOAL = 100;

  @Override
  public String name() {
    return "ArmReachesGoal";
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
    return List.of(new ReachesGoalCheck(robot));
  }

  private static final class ReachesGoalCheck implements Check {
    private final Robot robot;
    private int goalSetLoop = -1;
    private int reachedLoop = -1;
    private double finalError = Double.NaN;

    ReachesGoalCheck(Robot robot) {
      this.robot = robot;
    }

    @Override
    public void sample(int loop) {
      if (goalSetLoop < 0 && robot.arm.getGoalRad() != ArmConstants.START_ANGLE_RAD) {
        goalSetLoop = loop;
      }
      if (goalSetLoop >= 0 && reachedLoop < 0 && robot.arm.isAtGoal()) {
        reachedLoop = loop;
        finalError = Math.abs(robot.arm.getPositionRad() - robot.arm.getGoalRad());
      }
    }

    @Override
    public Result result(Stats stats) {
      String expected = "at goal within " + MAX_LOOPS_TO_GOAL + " loops of the goal being set";
      if (goalSetLoop < 0) {
        return new Result("ArmReachesGoal", false, "no goal was ever set", expected);
      }
      if (reachedLoop < 0) {
        return new Result(
            "ArmReachesGoal",
            false,
            "never at goal; goal set at loop "
                + goalSetLoop
                + ", position "
                + robot.arm.getPositionRad()
                + " rad, goal "
                + robot.arm.getGoalRad()
                + " rad",
            expected);
      }
      int took = reachedLoop - goalSetLoop;
      return new Result(
          "ArmReachesGoal",
          took <= MAX_LOOPS_TO_GOAL && finalError <= ArmConstants.GOAL_TOLERANCE_RAD,
          "reached goal after " + took + " loops, error " + finalError + " rad",
          expected);
    }
  }
}
