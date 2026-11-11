package frc.robot.opmodes;

import static org.wpilib.units.Units.Seconds;

import frc.robot.Robot;
import org.wpilib.command3.Command;
import org.wpilib.command3.Scheduler;
import org.wpilib.opmode.OpMode;
import org.wpilib.opmode.Utility;

/**
 * Scripted "match": idle, pick up a piece, drive across the field into the other zone, score, idle,
 * then do it again in reverse. Gives the log tools something realistic to report on.
 */
@Utility(name = "Sim Match")
public class SimMatchExercise implements OpMode {
  /** Driving speed, in meters per second. */
  public static final double SPEED = 3.0;

  private final Command match;

  public SimMatchExercise(Robot robot) {
    match =
        Command.noRequirements(
                coroutine -> {
                  coroutine.wait(Seconds.of(1.0));
                  robot.context.setHoldingPiece(true);
                  coroutine.await(
                      robot.drive.driveFieldRelative(SPEED, 0.0, 0.0).withTimeout(Seconds.of(5.0)));
                  robot.context.setHoldingPiece(false);
                  coroutine.wait(Seconds.of(1.0));
                  robot.context.setHoldingPiece(true);
                  coroutine.await(
                      robot
                          .drive
                          .driveFieldRelative(-SPEED, 0.0, 0.0)
                          .withTimeout(Seconds.of(5.0)));
                  robot.context.setHoldingPiece(false);
                  coroutine.wait(Seconds.of(1.0));
                })
            .named("Sim match");
  }

  @Override
  public void start() {
    Scheduler.getDefault().schedule(match);
  }

  @Override
  public void end() {
    Scheduler.getDefault().cancel(match);
  }
}
