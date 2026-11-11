package frc.robot.opmodes;

import static org.wpilib.units.Units.Seconds;

import frc.robot.Robot;
import frc.robot.mechanisms.arm.Arm;
import org.wpilib.command3.Command;
import org.wpilib.command3.Scheduler;
import org.wpilib.opmode.OpMode;
import org.wpilib.opmode.Utility;

/**
 * Scripted exercise: moves the arm between two angles and repeats. Used by the Arm scenarios; needs
 * no gamepad. Teleop runs the same exercise while a button is held.
 */
@Utility(name = "Arm Exercise")
public class ArmExercise implements OpMode {
  private static final double LOW_RAD = 0.2;
  private static final double HIGH_RAD = 1.2;

  private final Command exercise;

  public ArmExercise(Robot robot) {
    exercise = command(robot.arm);
  }

  /** The scripted arm exercise: up, wait, down, wait, forever. */
  public static Command command(Arm arm) {
    return Command.noRequirements(
            coroutine -> {
              while (true) {
                coroutine.await(arm.moveTo(HIGH_RAD));
                coroutine.wait(Seconds.of(1.0));
                coroutine.await(arm.moveTo(LOW_RAD));
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
  public void end() {
    Scheduler.getDefault().cancel(exercise);
  }
}
