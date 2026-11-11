package frc.robot.opmodes;

import static org.wpilib.units.Units.Seconds;

import frc.robot.Robot;
import frc.robot.context.ZoneActions;
import org.wpilib.command3.Command;
import org.wpilib.command3.Scheduler;
import org.wpilib.math.geometry.Pose2d;
import org.wpilib.math.geometry.Rotation2d;
import org.wpilib.opmode.OpMode;
import org.wpilib.opmode.Utility;

/**
 * Scripted exercise: "presses" the zone action once in the LEFT zone and once in the RIGHT zone by
 * moving the odometry pose and running the same command. Used by the ZoneActionDiffers scenario.
 */
@Utility(name = "Zone Action Exercise")
public class ZoneActionExercise implements OpMode {
  private final Command exercise;

  public ZoneActionExercise(Robot robot) {
    exercise =
        Command.noRequirements(
                coroutine -> {
                  robot.drive.resetPose(new Pose2d(4.0, 4.0, Rotation2d.ZERO));
                  coroutine.wait(Seconds.of(0.3));
                  coroutine.await(ZoneActions.command(robot.context, robot.arm));
                  coroutine.wait(Seconds.of(0.3));
                  robot.drive.resetPose(new Pose2d(12.0, 4.0, Rotation2d.ZERO));
                  coroutine.wait(Seconds.of(0.3));
                  coroutine.await(ZoneActions.command(robot.context, robot.arm));
                  coroutine.wait(Seconds.of(0.5));
                })
            .named("Zone action exercise");
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
