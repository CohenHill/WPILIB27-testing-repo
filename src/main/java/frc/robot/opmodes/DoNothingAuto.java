package frc.robot.opmodes;

import frc.robot.Robot;
import org.littletonrobotics.junction.Logger;
import org.wpilib.opmode.Autonomous;
import org.wpilib.opmode.OpMode;

/** Autonomous OpMode that does nothing except record that it started. */
@Autonomous(name = "Do Nothing")
public class DoNothingAuto implements OpMode {
  private final Robot robot;

  public DoNothingAuto(Robot robot) {
    this.robot = robot;
  }

  @Override
  public void start() {
    Logger.recordOutput("OpMode/AutoStarted", true);
  }
}
