package frc.robot.sim.scenarios;

import frc.robot.Robot;
import frc.robot.context.ZoneActions;
import frc.robot.sim.Check;
import frc.robot.sim.Scenario;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** A scripted match: the robot visits both zones and scores twice. Its log feeds the log tools. */
public class SimMatch implements Scenario {
  /** Number of scores the exercise performs. */
  public static final int EXPECTED_SCORES = 2;

  @Override
  public String name() {
    return "SimMatch";
  }

  @Override
  public String opMode() {
    return "Sim Match";
  }

  @Override
  public int durationLoops() {
    return 760;
  }

  @Override
  public List<Check> checks(Robot robot) {
    return List.of(new MatchCheck(robot));
  }

  private static final class MatchCheck implements Check {
    private final Robot robot;
    private final Set<String> zones = new LinkedHashSet<>();
    private boolean wasHolding = false;
    private int scores = 0;

    MatchCheck(Robot robot) {
      this.robot = robot;
    }

    @Override
    public void sample(int loop) {
      zones.add(robot.context.zone());
      boolean holding = robot.context.holdingPiece();
      if (wasHolding && !holding) {
        scores++;
      }
      wasHolding = holding;
    }

    @Override
    public Result result(Stats stats) {
      boolean pass =
          scores == EXPECTED_SCORES
              && zones.contains(ZoneActions.LEFT)
              && zones.contains(ZoneActions.RIGHT);
      return new Result(
          "MatchHappened",
          pass,
          scores + " scores, zones visited " + zones,
          EXPECTED_SCORES + " scores, zones visited include LEFT and RIGHT");
    }
  }
}
