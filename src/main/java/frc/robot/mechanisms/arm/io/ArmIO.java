package frc.robot.mechanisms.arm.io;

import org.littletonrobotics.junction.AutoLog;

/**
 * Hardware boundary for the arm. An empty implementation ({@code new ArmIO() {}}) is the replay IO.
 */
public interface ArmIO {
  @AutoLog
  public static class ArmIOInputs {
    public boolean connected = false;
    public double positionRad = 0.0;
    public double velocityRadPerSec = 0.0;
    public double appliedVolts = 0.0;
    public double supplyCurrentAmps = 0.0;
  }

  /** Reads the latest values into the inputs. */
  default void updateInputs(ArmIOInputs inputs) {}

  /** Closed-loop move to an angle, in radians. */
  default void setTargetPosition(double positionRad) {}

  /** Open-loop voltage. */
  default void setVoltage(double volts) {}

  /** Stops driving the arm. */
  default void stop() {}
}
