package frc.robot.sim;

import frc.robot.sim.scenarios.ArmHoldsWithinLimits;
import frc.robot.sim.scenarios.ArmReachesGoal;
import java.util.List;
import java.util.Optional;

/** All known scenarios. Add new scenarios here. */
public final class Scenarios {
  private Scenarios() {}

  public static final List<Scenario> ALL =
      List.of(new ArmReachesGoal(), new ArmHoldsWithinLimits());

  public static Optional<Scenario> find(String name) {
    return ALL.stream().filter(s -> s.name().equals(name)).findFirst();
  }
}
