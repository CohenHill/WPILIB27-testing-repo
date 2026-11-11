package frc.robot.opmodes;

import static org.wpilib.units.Units.Seconds;

import frc.robot.Robot;
import org.wpilib.command3.Command;
import org.wpilib.command3.Scheduler;
import org.wpilib.opmode.OpMode;
import org.wpilib.opmode.Utility;

/**
 * Scripted exercise: drives field-relative in a square (forward, left, back, right), then stops.
 * Used by the DriveSquare scenario; needs no gamepad.
 */
@Utility(name = "Drive Square")
public class DriveSquareExercise implements OpMode {
  /** Speed along each side, in meters per second. */
  public static final double SPEED = 1.0;

  /** Time spent on each side, in seconds. */
  public static final double SIDE_SECONDS = 1.0;

  private final Command exercise;

  public DriveSquareExercise(Robot robot) {
    exercise =
        Command.noRequirements(
                coroutine -> {
                  double[][] sides = {{SPEED, 0}, {0, SPEED}, {-SPEED, 0}, {0, -SPEED}};
                  for (double[] side : sides) {
                    coroutine.await(
                        robot
                            .drive
                            .driveFieldRelative(side[0], side[1], 0.0)
                            .withTimeout(Seconds.of(SIDE_SECONDS)));
                  }
                })
            .named("Drive square");
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
