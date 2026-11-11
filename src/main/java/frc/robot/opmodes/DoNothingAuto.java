package frc.robot.opmodes;

import org.littletonrobotics.junction.Logger;
import org.wpilib.opmode.Autonomous;
import org.wpilib.opmode.OpMode;

/** Autonomous OpMode that does nothing except record that it started. */
@Autonomous(name = "Do Nothing")
public class DoNothingAuto implements OpMode {
  @Override
  public void start() {
    Logger.recordOutput("OpMode/AutoStarted", true);
  }
}
