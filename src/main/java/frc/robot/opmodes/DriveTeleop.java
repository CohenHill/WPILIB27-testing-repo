package frc.robot.opmodes;

import static org.wpilib.units.Units.Seconds;

import frc.robot.Robot;
import org.littletonrobotics.junction.Logger;
import org.wpilib.command3.Command;
import org.wpilib.command3.Scheduler;
import org.wpilib.opmode.OpMode;
import org.wpilib.opmode.Teleop;

/**
 * Teleop OpMode. Records a heartbeat counter each enabled loop and runs a scripted arm exercise so
 * a sim run produces data without a gamepad.
 */
@Teleop(name = "DriveTeleop")
public class DriveTeleop implements OpMode {
  private static final double LOW_RAD = 0.2;
  private static final double HIGH_RAD = 1.2;

  private final Robot robot;
  private final Command exercise;
  private long loopCount = 0;

  public DriveTeleop(Robot robot) {
    this.robot = robot;
    this.exercise =
        Command.noRequirements(
                coroutine -> {
                  while (true) {
                    coroutine.await(robot.arm.moveTo(HIGH_RAD));
                    coroutine.wait(Seconds.of(1.0));
                    coroutine.await(robot.arm.moveTo(LOW_RAD));
                    coroutine.wait(Seconds.of(1.0));
                  }
                })
            .named("Arm exercise");
  }

  @Override
  public void start() {
    Scheduler.getDefault().schedule(exercise);
  }

  @Override
  public void periodic() {
    Logger.recordOutput("OpMode/TeleopHeartbeat", loopCount++);
  }

  @Override
  public void end() {
    Scheduler.getDefault().cancel(exercise);
  }
}
