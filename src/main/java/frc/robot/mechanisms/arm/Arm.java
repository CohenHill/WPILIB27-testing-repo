package frc.robot.mechanisms.arm;

import frc.robot.mechanisms.arm.io.ArmIO;
import frc.robot.mechanisms.arm.io.ArmIOInputsAutoLogged;
import org.littletonrobotics.junction.Logger;
import org.wpilib.command3.Command;
import org.wpilib.command3.Mechanism;

/** A single-jointed arm. All hardware access goes through {@link ArmIO}. */
public class Arm implements Mechanism {
  private final ArmIO io;
  private final ArmIOInputsAutoLogged inputs = new ArmIOInputsAutoLogged();
  private double goalRad = ArmConstants.START_ANGLE_RAD;

  public Arm(ArmIO io) {
    this.io = io;
    setDefaultCommand(hold());
  }

  /** Clamps a goal to the soft limits. */
  public static double clampGoal(double rad) {
    return Math.clamp(rad, ArmConstants.MIN_ANGLE_RAD, ArmConstants.MAX_ANGLE_RAD);
  }

  /** True when the position is within tolerance of the goal. */
  public static boolean atGoal(double positionRad, double goalRad) {
    return Math.abs(positionRad - goalRad) <= ArmConstants.GOAL_TOLERANCE_RAD;
  }

  /** Latest logged arm angle, in radians. */
  public double getPositionRad() {
    return inputs.positionRad;
  }

  /** Current goal angle, in radians. */
  public double getGoalRad() {
    return goalRad;
  }

  /** True when the arm is within tolerance of its goal. */
  public boolean isAtGoal() {
    return atGoal(inputs.positionRad, goalRad);
  }

  /** Reads the IO and logs the inputs. Call every loop, before the scheduler runs. */
  public void updateInputs() {
    io.updateInputs(inputs);
    Logger.processInputs("Arm", inputs);
  }

  /** Records the arm's outputs. Call every loop, after the scheduler runs. */
  public void recordOutputs() {
    Logger.recordOutput("Arm/GoalRad", goalRad);
    Logger.recordOutput("Arm/AtGoal", atGoal(inputs.positionRad, goalRad));
  }

  /** Moves to an angle (clamped to the limits) and finishes when there. */
  public Command moveTo(double rad) {
    return run(coroutine -> {
          goalRad = clampGoal(rad);
          io.setTargetPosition(goalRad);
          coroutine.waitUntil(() -> atGoal(inputs.positionRad, goalRad));
        })
        .named("Arm moveTo");
  }

  /** Holds the current goal until interrupted. */
  public Command hold() {
    return run(coroutine -> {
          while (true) {
            io.setTargetPosition(goalRad);
            coroutine.yield();
          }
        })
        .named("Arm hold");
  }

  /**
   * Stops driving the arm until interrupted. The arm is not held, so it can fall under gravity. (It
   * must loop: a command that ends at once would hand the arm straight back to hold().)
   */
  public Command stop() {
    return run(coroutine -> {
          while (true) {
            io.stop();
            coroutine.yield();
          }
        })
        .named("Arm stop");
  }
}
