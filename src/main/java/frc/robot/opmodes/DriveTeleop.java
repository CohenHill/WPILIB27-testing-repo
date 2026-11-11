package frc.robot.opmodes;

import frc.robot.Robot;
import org.littletonrobotics.junction.Logger;
import org.wpilib.opmode.OpMode;
import org.wpilib.opmode.Teleop;

/** Teleop OpMode. Records a heartbeat counter each enabled loop. */
@Teleop(name = "DriveTeleop")
public class DriveTeleop implements OpMode {
  private final Robot robot;
  private long loopCount = 0;

  public DriveTeleop(Robot robot) {
    this.robot = robot;
  }

  @Override
  public void periodic() {
    Logger.recordOutput("OpMode/TeleopHeartbeat", loopCount++);
  }
}
