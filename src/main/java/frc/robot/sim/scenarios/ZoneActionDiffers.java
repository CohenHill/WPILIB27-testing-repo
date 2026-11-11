package frc.robot.sim.scenarios;

import frc.robot.Robot;
import frc.robot.mechanisms.arm.ArmConstants;
import frc.robot.sim.Check;
import frc.robot.sim.Scenario;
import java.util.ArrayList;
import java.util.List;

/** The same action sends the arm to different goals depending on the zone the robot is in. */
public class ZoneActionDiffers implements Scenario {
  @Override
  public String name() {
    return "ZoneActionDiffers";
  }

  @Override
  public String opMode() {
    return "Zone Action Exercise";
  }

  @Override
  public int durationLoops() {
    return 300;
  }

  @Override
  public List<Check> checks(Robot robot) {
    return List.of(new GoalByZoneCheck(robot));
  }

  private static final class GoalByZoneCheck implements Check {
    private final Robot robot;
    private final List<String> changes = new ArrayList<>();
    private double lastGoal = Double.NaN;

    GoalByZoneCheck(Robot robot) {
      this.robot = robot;
    }

    @Override
    public void sample(int loop) {
      double goal = robot.arm.getGoalRad();
      if (!Double.isNaN(lastGoal) && goal != lastGoal) {
        changes.add(robot.context.zone() + ":" + goal);
      }
      lastGoal = goal;
    }

    @Override
    public Result result(Stats stats) {
      List<String> expected =
          List.of("LEFT:" + ArmConstants.PRESET_LOW_RAD, "RIGHT:" + ArmConstants.PRESET_HIGH_RAD);
      return new Result(
          "GoalByZone",
          changes.equals(expected),
          "goal changes (zone:goal) " + changes,
          "goal changes (zone:goal) " + expected);
    }
  }
}
