package frc.robot.mechanisms.arm;

/** Limits, tolerances, gains and ids for the arm. Values are placeholders until it is built. */
public final class ArmConstants {
  private ArmConstants() {}

  /** Soft limits of the arm, in radians (0 = horizontal, positive = up). */
  public static final double MIN_ANGLE_RAD = -0.2;

  public static final double MAX_ANGLE_RAD = 1.6;

  /** Preset goal angles, in radians. */
  public static final double PRESET_LOW_RAD = 0.2;

  public static final double PRESET_HIGH_RAD = 1.2;

  /** How close to the goal counts as "at goal", in radians. */
  public static final double GOAL_TOLERANCE_RAD = 0.05;

  /** Motor rotations per arm rotation. */
  public static final double GEAR_RATIO = 100.0;

  /** Physical model used by the simulation. */
  public static final double ARM_LENGTH_METERS = 0.5;

  public static final double ARM_MASS_KG = 3.0;

  /** Starting angle in simulation, in radians. */
  public static final double START_ANGLE_RAD = 0.0;

  /** Simulation control gains. Volts per radian, volts per radian per second, volts. */
  public static final double SIM_KP = 40.0;

  public static final double SIM_KD = 0.2;
  public static final double SIM_KG = 0.12;

  /** Placeholder CAN id and bus name. Not wired to hardware. */
  public static final int CAN_ID = 20;

  public static final String CAN_BUS = "canivore";

  /** YAMS closed-loop gains (volts per mechanism rotation, and per rotation per second). */
  public static final double YAMS_KP = 15 * (2.0 * Math.PI);

  public static final double YAMS_KD = 0.0;

  /** Real hardware position gain, in volts per motor rotation. Untuned placeholder. */
  public static final double TALON_KP = 10.0;
}
