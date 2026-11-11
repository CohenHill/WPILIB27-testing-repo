package frc.robot;

import frc.robot.mechanisms.arm.io.ArmIO;
import frc.robot.mechanisms.arm.io.ArmIOSim;
import frc.robot.mechanisms.arm.io.ArmIOTalonFX;

/**
 * Picks the IO implementation for each mechanism. For now the choice comes from the run mode only.
 * Add one factory method per mechanism; bench profiles can be added here later.
 */
public final class RunProfile {
  private RunProfile() {}

  public static ArmIO armIO(RunMode mode) {
    return switch (mode) {
      case REAL -> new ArmIOTalonFX();
      case SIM -> new ArmIOSim();
      case REPLAY -> new ArmIO() {};
    };
  }
}
