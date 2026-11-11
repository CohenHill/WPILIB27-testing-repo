package frc.robot.mechanisms.drive.io;

import static org.wpilib.units.Units.RadiansPerSecond;

import com.ctre.phoenix6.Utils;
import com.ctre.phoenix6.swerve.SwerveDrivetrain.SwerveDriveState;
import com.ctre.phoenix6.swerve.SwerveRequest;
import frc.robot.mechanisms.drive.io.generated.TunerConstants;
import frc.robot.mechanisms.drive.io.generated.TunerConstants.TunerSwerveDrivetrain;
import org.wpilib.framework.RobotBase;
import org.wpilib.math.geometry.Pose2d;
import org.wpilib.math.kinematics.ChassisVelocities;
import org.wpilib.math.linalg.Matrix;
import org.wpilib.math.numbers.N1;
import org.wpilib.math.numbers.N3;
import org.wpilib.system.Notifier;
import org.wpilib.system.RobotController;

/**
 * Wraps the CTRE swerve drivetrain for the real robot and simulation.
 *
 * <p>NOTE: in simulation the CTRE drivetrain is stepped on its own Notifier thread every 4 ms using
 * a wall clock (as in CTRE's example), not on the 20 ms robot loop. Odometry therefore depends on
 * real time. UNTESTED ON HARDWARE.
 */
public class DriveIOCtre implements DriveIO {
  private static final double SIM_LOOP_PERIOD_SEC = 0.004;

  private final TunerSwerveDrivetrain drivetrain = TunerConstants.createDrivetrain();
  private final SwerveRequest.ApplyFieldVelocity fieldRequest =
      new SwerveRequest.ApplyFieldVelocity();
  private final SwerveRequest.ApplyRobotVelocity robotRequest =
      new SwerveRequest.ApplyRobotVelocity();
  private final SwerveRequest.Idle idleRequest = new SwerveRequest.Idle();

  private Notifier simNotifier = null;
  private double lastSimTime;

  public DriveIOCtre() {
    if (RobotBase.isSimulation()) {
      lastSimTime = Utils.getCurrentTimeSeconds();
      simNotifier =
          new Notifier(
              () -> {
                double now = Utils.getCurrentTimeSeconds();
                double dt = now - lastSimTime;
                lastSimTime = now;
                drivetrain.updateSimState(dt, RobotController.getBatteryVoltage());
              });
      simNotifier.startPeriodic(SIM_LOOP_PERIOD_SEC);
    }
  }

  @Override
  public void updateInputs(DriveIOInputs inputs) {
    SwerveDriveState state = drivetrain.getStateCopy();
    inputs.connected = state.ModulePositions != null && state.SuccessfulDaqs > 0;
    inputs.pose = state.Pose;
    inputs.velocity = state.Velocity;
    inputs.modulePositions =
        state.ModulePositions != null ? state.ModulePositions : inputs.modulePositions;
    inputs.moduleVelocities =
        state.ModuleVelocities != null ? state.ModuleVelocities : inputs.moduleVelocities;
    inputs.moduleTargets = state.ModuleTargets != null ? state.ModuleTargets : inputs.moduleTargets;
    inputs.gyroYaw = state.RawHeading;
    inputs.gyroRateRadPerSec =
        drivetrain.getPigeon2().getAngularVelocityZWorld().getValue().in(RadiansPerSecond);
    inputs.odometryPeriodSec = state.OdometryPeriod;
  }

  @Override
  public void driveFieldRelative(double vx, double vy, double omega) {
    drivetrain.setControl(fieldRequest.withVelocity(new ChassisVelocities(vx, vy, omega)));
  }

  @Override
  public void driveRobotRelative(double vx, double vy, double omega) {
    drivetrain.setControl(robotRequest.withVelocity(new ChassisVelocities(vx, vy, omega)));
  }

  @Override
  public void stop() {
    drivetrain.setControl(idleRequest);
  }

  @Override
  public void resetPose(Pose2d pose) {
    drivetrain.resetPose(pose);
  }

  @Override
  public void addVisionMeasurement(Pose2d pose, double timestampSeconds, Matrix<N3, N1> stdDevs) {
    drivetrain.addVisionMeasurement(pose, timestampSeconds, stdDevs);
  }
}
