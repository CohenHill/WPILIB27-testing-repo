package frc.robot.opmodes;

import static org.wpilib.units.Units.Seconds;

import frc.robot.Robot;
import org.wpilib.command3.Command;
import org.wpilib.command3.Scheduler;
import org.wpilib.opmode.OpMode;
import org.wpilib.opmode.Utility;

/**
 * Scripted exercise: rotates in place one full turn (counter-clockwise), then stops. Used by the
 * DriveRotateInPlace scenario; needs no gamepad.
 */
@Utility(name = "Drive Rotate")
public class DriveRotateExercise implements OpMode {
  /** Rotation speed, in radians per second. */
  public static final double OMEGA = Math.PI;

  /** Time spent rotating, in seconds. One full turn at OMEGA. */
  public static final double SECONDS = 2.0;

  private final Command exercise;

  public DriveRotateExercise(Robot robot) {
    exercise =
        Command.noRequirements(
                coroutine ->
                    coroutine.await(
                        robot
                            .drive
                            .driveFieldRelative(0.0, 0.0, OMEGA)
                            .withTimeout(Seconds.of(SECONDS))))
            .named("Drive rotate");
  }

  @Override
  public void start() {
    Scheduler.getDefault().schedule(exercise);
  }

  @Override
  public void end() {
    Scheduler.getDefault().cancel(exercise);
  }
}
