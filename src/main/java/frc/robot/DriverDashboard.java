package frc.robot;

import frc.robot.context.RobotContext;
import frc.robot.mechanisms.arm.Arm;
import org.wpilib.net.WebServer;
import org.wpilib.system.Filesystem;
import org.wpilib.telemetry.Telemetry;

/**
 * The small table the driver sees in Elastic, separate from the AdvantageKit stream. It only writes
 * (never reads) NetworkTables, so it cannot affect replay. It also serves the deploy folder on port
 * 5800 so Elastic can download its layout from the robot (File > Load Layout From Robot).
 */
public class DriverDashboard {
  /** Port Elastic downloads the layout from. */
  public static final int WEB_PORT = 5800;

  public DriverDashboard() {
    WebServer.start(WEB_PORT, Filesystem.getDeployDirectory().getPath());
  }

  /** Publishes the driver table. Call every loop, after the scheduler. */
  public void update(RobotContext context, Arm arm) {
    Telemetry.log("Driver/Zone", context.zone());
    Telemetry.log("Driver/Alliance", context.alliance());
    Telemetry.log("Driver/HoldingPiece", context.holdingPiece());
    Telemetry.log("Driver/ArmAtGoal", arm.isAtGoal());
  }
}
