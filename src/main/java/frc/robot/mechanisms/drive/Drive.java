package frc.robot.mechanisms.drive;

import frc.robot.mechanisms.drive.io.DriveIO;
import frc.robot.mechanisms.drive.io.DriveIOInputsAutoLogged;
import java.util.function.DoubleSupplier;
import org.littletonrobotics.junction.Logger;
import org.wpilib.command3.Command;
import org.wpilib.command3.Mechanism;
import org.wpilib.math.geometry.Pose2d;
import org.wpilib.math.kinematics.SwerveModulePosition;
import org.wpilib.math.kinematics.SwerveModuleVelocity;

/** The swerve drivetrain. All hardware access goes through {@link DriveIO}. */
public class Drive implements Mechanism {
  private final DriveIO io;
  private final DriveIOInputsAutoLogged inputs = new DriveIOInputsAutoLogged();

  public Drive(DriveIO io) {
    this.io = io;
    setDefaultCommand(stop());
  }

  /** Stick deadband with rescaling: 0 inside the deadband, full range outside it. */
  public static double deadband(double value, double deadband) {
    if (Math.abs(value) <= deadband) {
      return 0.0;
    }
    return Math.copySign((Math.abs(value) - deadband) / (1.0 - deadband), value);
  }

  /** Converts a -1..1 stick value to a speed: deadband, square for fine control, then scale. */
  public static double stickToSpeed(double stick, double deadband, double maxSpeed) {
    double d = deadband(Math.clamp(stick, -1.0, 1.0), deadband);
    return Math.copySign(d * d, d) * maxSpeed;
  }

  /** Reads the IO and logs the inputs. Call every loop, before the scheduler runs. */
  public void updateInputs() {
    io.updateInputs(inputs);
    Logger.processInputs("Drive", inputs);
  }

  /** Records the drive outputs. Call every loop, after the scheduler runs. */
  public void recordOutputs() {
    Logger.recordOutput("Drive/Pose", inputs.pose);
    Logger.recordOutput("Drive/PoseX", inputs.pose.getX());
    Logger.recordOutput("Drive/PoseY", inputs.pose.getY());
    Logger.recordOutput("Drive/HeadingRad", inputs.pose.getRotation().getRadians());
    Logger.recordOutput("Drive/ModuleStates", inputs.moduleVelocities);
    Logger.recordOutput("Drive/ModuleTargets", inputs.moduleTargets);
  }

  /** Latest odometry pose. */
  public Pose2d getPose() {
    return inputs.pose;
  }

  /** Latest module positions. */
  public SwerveModulePosition[] getModulePositions() {
    return inputs.modulePositions;
  }

  /** Latest module velocities. */
  public SwerveModuleVelocity[] getModuleVelocities() {
    return inputs.moduleVelocities;
  }

  /** Resets the odometry pose. */
  public void resetPose(Pose2d pose) {
    io.resetPose(pose);
  }

  /** Field-relative teleop drive from -1..1 stick suppliers (x forward, y left, omega CCW). */
  public Command teleopDrive(DoubleSupplier x, DoubleSupplier y, DoubleSupplier omega) {
    return run(coroutine -> {
          while (true) {
            io.driveFieldRelative(
                stickToSpeed(
                    x.getAsDouble(),
                    DriveConstants.STICK_DEADBAND,
                    DriveConstants.MAX_SPEED_METERS_PER_SEC),
                stickToSpeed(
                    y.getAsDouble(),
                    DriveConstants.STICK_DEADBAND,
                    DriveConstants.MAX_SPEED_METERS_PER_SEC),
                stickToSpeed(
                    omega.getAsDouble(),
                    DriveConstants.STICK_DEADBAND,
                    DriveConstants.MAX_ANGULAR_SPEED_RAD_PER_SEC));
            coroutine.yield();
          }
        })
        .named("Drive teleop");
  }

  /** Drives field-relative at fixed velocities (m/s, m/s, rad/s) until interrupted. */
  public Command driveFieldRelative(double vx, double vy, double omega) {
    return run(coroutine -> {
          while (true) {
            io.driveFieldRelative(vx, vy, omega);
            coroutine.yield();
          }
        })
        .named("Drive field-relative");
  }

  /** Stops driving. This is the default command and holds the stop every loop. */
  public Command stop() {
    return run(coroutine -> {
          while (true) {
            io.stop();
            coroutine.yield();
          }
        })
        .named("Drive stop");
  }
}
