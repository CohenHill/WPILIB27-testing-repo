package frc.robot.mechanisms.drive;

/** Drive limits and module ratios. Values are placeholders until the drivebase is built. */
public final class DriveConstants {
  private DriveConstants() {}

  /** SDS MK5i drive ratios (motor turns per wheel turn). Check against the SDS spec sheet. */
  public enum ModuleRatio {
    R1(7.03),
    R2(6.03),
    R3(5.36);

    public final double driveRatio;

    ModuleRatio(double driveRatio) {
      this.driveRatio = driveRatio;
    }

    /** Theoretical top speed in meters per second for a motor free speed and a wheel radius. */
    public double freeSpeedMetersPerSec(double motorFreeSpeedRpm, double wheelRadiusMeters) {
      double wheelRotationsPerSec = motorFreeSpeedRpm / 60.0 / driveRatio;
      return wheelRotationsPerSec * 2.0 * Math.PI * wheelRadiusMeters;
    }
  }

  /** The ratio this robot uses. Must match TunerConstants. */
  public static final ModuleRatio RATIO = ModuleRatio.R2;

  /** Kraken X60 free speed, in rpm. */
  public static final double MOTOR_FREE_SPEED_RPM = 6000.0;

  /** MK5i wheel radius, in meters (4 inch wheel). */
  public static final double WHEEL_RADIUS_METERS = 0.0508;

  /** Fraction of the theoretical free speed that teleop may command. */
  public static final double TELEOP_SPEED_FRACTION = 0.8;

  /** Maximum teleop translation speed, meters per second. */
  public static final double MAX_SPEED_METERS_PER_SEC =
      TELEOP_SPEED_FRACTION
          * RATIO.freeSpeedMetersPerSec(MOTOR_FREE_SPEED_RPM, WHEEL_RADIUS_METERS);

  /** Maximum teleop rotation speed, radians per second. */
  public static final double MAX_ANGULAR_SPEED_RAD_PER_SEC = 2.0 * Math.PI;

  /** Gamepad stick deadband (0 to 1). */
  public static final double STICK_DEADBAND = 0.1;
}
