package frc.robot.mechanisms.arm.io;

import frc.robot.mechanisms.arm.ArmConstants;
import org.littletonrobotics.junction.LoggedOpModeRobot;
import org.wpilib.math.controller.PIDController;
import org.wpilib.math.system.DCMotor;
import org.wpilib.simulation.SingleJointedArmSim;

/** Arm physics simulation with a PID position loop. Steps once per updateInputs call. */
public class ArmIOSim implements ArmIO {
  private static final double DT = LoggedOpModeRobot.DEFAULT_PERIOD;

  private final SingleJointedArmSim sim =
      new SingleJointedArmSim(
          DCMotor.getKrakenX60(1),
          ArmConstants.GEAR_RATIO,
          ArmConstants.ARM_MASS_KG
              * ArmConstants.ARM_LENGTH_METERS
              * ArmConstants.ARM_LENGTH_METERS
              / 3.0,
          ArmConstants.ARM_LENGTH_METERS,
          ArmConstants.MIN_ANGLE_RAD,
          ArmConstants.MAX_ANGLE_RAD,
          true,
          ArmConstants.START_ANGLE_RAD);
  private final PIDController pid =
      new PIDController(ArmConstants.SIM_KP, 0.0, ArmConstants.SIM_KD);

  private boolean closedLoop = false;
  private double targetRad = ArmConstants.START_ANGLE_RAD;
  private double openLoopVolts = 0.0;
  private double appliedVolts = 0.0;

  @Override
  public void updateInputs(ArmIOInputs inputs) {
    if (closedLoop) {
      appliedVolts =
          pid.calculate(sim.getAngle(), targetRad) + ArmConstants.SIM_KG * Math.cos(sim.getAngle());
    } else {
      appliedVolts = openLoopVolts;
    }
    appliedVolts = Math.clamp(appliedVolts, -12.0, 12.0);
    sim.setInputVoltage(appliedVolts);
    sim.update(DT);

    inputs.connected = true;
    inputs.positionRad = sim.getAngle();
    inputs.velocityRadPerSec = sim.getVelocity();
    inputs.appliedVolts = appliedVolts;
    inputs.supplyCurrentAmps = Math.abs(sim.getCurrentDraw());
  }

  @Override
  public void setTargetPosition(double positionRad) {
    closedLoop = true;
    targetRad = positionRad;
  }

  @Override
  public void setVoltage(double volts) {
    closedLoop = false;
    openLoopVolts = volts;
  }

  @Override
  public void stop() {
    closedLoop = false;
    openLoopVolts = 0.0;
  }
}
