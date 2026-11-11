package frc.robot;

/**
 * A simulation-only extension point. Implementations live outside the robot code (in the sim source
 * set) and are found with {@link java.util.ServiceLoader}, so the robot jar that is deployed to the
 * robot contains none of them.
 */
public interface SimHook {
  /**
   * Called once from the Robot constructor, after the mechanisms exist and before the first loop.
   *
   * @return true if this hook is active and wants {@link #update()} calls
   */
  boolean begin(Robot robot);

  /** Called once per loop from simulationPeriodic() while active. */
  void update();
}
