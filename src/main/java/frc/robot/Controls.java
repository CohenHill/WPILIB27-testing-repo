package frc.robot;

import frc.robot.mechanisms.arm.Arm;
import frc.robot.opmodes.ArmExercise;
import org.wpilib.command3.button.CommandGamepad;

/** Always-on button bindings and driver inputs. */
public class Controls {
  /** USB port of the driver gamepad. */
  private static final int DRIVER_PORT = 0;

  private final CommandGamepad driver = new CommandGamepad(DRIVER_PORT);

  public Controls(Arm arm) {
    // Hold the south face button (A on an Xbox layout) to run the scripted arm exercise.
    driver.faceDown().whileTrue(ArmExercise.command(arm));
  }

  /**
   * Field-forward stick input, -1..1 (stick up is forward). The gamepad applies its own deadband.
   */
  public double driveForward() {
    return -driver.getGamepad().getLeftY();
  }

  /** Field-left stick input, -1..1 (stick left is left). */
  public double driveLeft() {
    return -driver.getGamepad().getLeftX();
  }

  /** Counter-clockwise rotation stick input, -1..1 (stick left turns left). */
  public double driveTurn() {
    return -driver.getGamepad().getRightX();
  }
}
