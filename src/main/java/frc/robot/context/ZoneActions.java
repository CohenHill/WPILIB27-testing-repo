package frc.robot.context;

import frc.robot.mechanisms.arm.Arm;
import frc.robot.mechanisms.arm.ArmConstants;
import org.wpilib.command3.Command;

/** Actions whose behavior depends on the zone. */
public final class ZoneActions {
  private ZoneActions() {}

  /** Zone names this action reacts to (must match zones.json). */
  public static final String LEFT = "LEFT";

  public static final String RIGHT = "RIGHT";

  /**
   * One button, two behaviors: in LEFT the arm goes to its low preset, in RIGHT to its high preset,
   * anywhere else nothing happens. The zone is read when the command runs.
   */
  public static Command command(RobotContext context, Arm arm) {
    return Command.noRequirements(
            coroutine -> {
              String zone = context.zone();
              if (zone.equals(LEFT)) {
                coroutine.await(arm.moveTo(ArmConstants.PRESET_LOW_RAD));
              } else if (zone.equals(RIGHT)) {
                coroutine.await(arm.moveTo(ArmConstants.PRESET_HIGH_RAD));
              }
            })
        .named("Zone action");
  }
}
