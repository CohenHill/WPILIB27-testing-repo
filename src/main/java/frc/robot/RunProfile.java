package frc.robot;

import frc.robot.mechanisms.arm.io.ArmIO;
import frc.robot.mechanisms.arm.io.ArmIOSim;
import frc.robot.mechanisms.arm.io.ArmIOTalonFX;
import frc.robot.mechanisms.arm.io.ArmIOYams;

/**
 * Picks the IO implementation for each mechanism. For now the choice comes from the run mode only.
 * Add one factory method per mechanism; bench profiles can be added here later.
 */
public final class RunProfile {
  private RunProfile() {}

  /** Arm IO for the current environment: ARM_IO=yams|plain (default plain). */
  public static ArmIO armIO(RunMode mode) {
    return armIO(mode, System.getenv("ARM_IO"));
  }

  /**
   * Arm IO for a mode and an IO choice. {@code choice} is "yams" or "plain"; null, blank or
   * anything else means plain. Replay always uses the empty IO.
   */
  public static ArmIO armIO(RunMode mode, String choice) {
    boolean yams = "yams".equalsIgnoreCase(choice == null ? "" : choice.trim());
    return switch (mode) {
      case REAL -> yams ? new ArmIOYams() : new ArmIOTalonFX();
      case SIM -> yams ? new ArmIOYams() : new ArmIOSim();
      case REPLAY -> new ArmIO() {};
    };
  }
}
