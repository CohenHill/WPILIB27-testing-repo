package frc.robot.opmodes;

import frc.robot.Robot;
import org.wpilib.command3.Command;
import org.wpilib.command3.Scheduler;
import org.wpilib.opmode.OpMode;
import org.wpilib.opmode.Teleop;

/**
 * Teleop OpMode: field-relative swerve drive from the driver gamepad. Button bindings that work in
 * every mode are in Controls.
 */
@Teleop(name = "DriveTeleop")
public class DriveTeleop implements OpMode {
  private final Command setup;

  public DriveTeleop(Robot robot) {
    // Setting a default command inside a command scopes it to that command: when this command is
    // cancelled in end(), the drive goes back to its stop default.
    setup =
        Command.noRequirements(
                coroutine -> {
                  robot.drive.setDefaultCommand(
                      robot.drive.teleopDrive(
                          robot.controls::driveForward,
                          robot.controls::driveLeft,
                          robot.controls::driveTurn));
                  coroutine.park();
                })
            .named("Teleop drive setup");
  }

  @Override
  public void start() {
    Scheduler.getDefault().schedule(setup);
  }

  @Override
  public void end() {
    Scheduler.getDefault().cancel(setup);
  }
}
