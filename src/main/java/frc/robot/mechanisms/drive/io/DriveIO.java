package frc.robot.mechanisms.drive.io;

import org.littletonrobotics.junction.AutoLog;
import org.wpilib.math.geometry.Pose2d;
import org.wpilib.math.geometry.Rotation2d;
import org.wpilib.math.kinematics.ChassisVelocities;
import org.wpilib.math.kinematics.SwerveModulePosition;
import org.wpilib.math.kinematics.SwerveModuleVelocity;
import org.wpilib.math.linalg.Matrix;
import org.wpilib.math.numbers.N1;
import org.wpilib.math.numbers.N3;

/** Hardware boundary for the swerve drivetrain. {@code new DriveIO() {}} is the replay IO. */
public interface DriveIO {
  @AutoLog
  public static class DriveIOInputs {
    public boolean connected = false;
    public Pose2d pose = new Pose2d();

    /** Robot-relative chassis velocities. */
    public ChassisVelocities velocity = new ChassisVelocities();

    public SwerveModulePosition[] modulePositions = new SwerveModulePosition[0];
    public SwerveModuleVelocity[] moduleVelocities = new SwerveModuleVelocity[0];
    public SwerveModuleVelocity[] moduleTargets = new SwerveModuleVelocity[0];
    public Rotation2d gyroYaw = new Rotation2d();
    public double gyroRateRadPerSec = 0.0;
    public double odometryPeriodSec = 0.0;
  }

  /** Reads the latest values into the inputs. */
  default void updateInputs(DriveIOInputs inputs) {}

  /** Drive with field-relative velocities (m/s, m/s, rad/s). */
  default void driveFieldRelative(double vx, double vy, double omega) {}

  /** Drive with robot-relative velocities (m/s, m/s, rad/s). */
  default void driveRobotRelative(double vx, double vy, double omega) {}

  /** Stop driving (modules idle). */
  default void stop() {}

  /** Reset the odometry pose. */
  default void resetPose(Pose2d pose) {}

  /**
   * Add a vision pose measurement (used in M7). Timestamp: Phoenix time,
   * Utils.getCurrentTimeSeconds().
   */
  default void addVisionMeasurement(Pose2d pose, double timestampSeconds, Matrix<N3, N1> stdDevs) {}
}
