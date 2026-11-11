package frc.robot.sim;

/** Prints scenario names, one per line. Needs no simulation. */
public final class ListScenarios {
  private ListScenarios() {}

  public static void main(String[] args) {
    Scenarios.ALL.forEach(s -> System.out.println(s.name()));
  }
}
