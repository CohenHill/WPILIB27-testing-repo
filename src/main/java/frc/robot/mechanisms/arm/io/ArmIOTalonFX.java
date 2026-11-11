package frc.robot.mechanisms.arm.io;

import com.ctre.phoenix6.BaseStatusSignal;
import com.ctre.phoenix6.CANBus;
import com.ctre.phoenix6.StatusSignal;
import com.ctre.phoenix6.configs.Slot0Configs;
import com.ctre.phoenix6.controls.NeutralOut;
import com.ctre.phoenix6.controls.PositionVoltage;
import com.ctre.phoenix6.controls.VoltageOut;
import com.ctre.phoenix6.hardware.TalonFX;
import frc.robot.mechanisms.arm.ArmConstants;
import org.wpilib.units.measure.Angle;
import org.wpilib.units.measure.AngularVelocity;
import org.wpilib.units.measure.Current;
import org.wpilib.units.measure.Voltage;

/**
 * Phoenix 6 TalonFX implementation of the arm IO.
 *
 * <p>UNTESTED ON HARDWARE. The CAN id and bus in ArmConstants are placeholders.
 */
public class ArmIOTalonFX implements ArmIO {
  private static final double TWO_PI = 2.0 * Math.PI;

  private final TalonFX talon = new TalonFX(ArmConstants.CAN_ID, new CANBus(ArmConstants.CAN_BUS));
  private final StatusSignal<Angle> position = talon.getPosition();
  private final StatusSignal<AngularVelocity> velocity = talon.getVelocity();
  private final StatusSignal<Voltage> appliedVolts = talon.getMotorVoltage();
  private final StatusSignal<Current> supplyCurrent = talon.getSupplyCurrent();

  private final PositionVoltage positionRequest = new PositionVoltage(0.0);
  private final VoltageOut voltageRequest = new VoltageOut(0.0);
  private final NeutralOut neutralRequest = new NeutralOut();

  public ArmIOTalonFX() {
    var slot0 = new Slot0Configs();
    slot0.kP = ArmConstants.TALON_KP;
    talon.getConfigurator().apply(slot0);
  }

  @Override
  public void updateInputs(ArmIOInputs inputs) {
    var status = BaseStatusSignal.refreshAll(position, velocity, appliedVolts, supplyCurrent);
    inputs.connected = status.isOK();
    inputs.positionRad = position.getValueAsDouble() / ArmConstants.GEAR_RATIO * TWO_PI;
    inputs.velocityRadPerSec = velocity.getValueAsDouble() / ArmConstants.GEAR_RATIO * TWO_PI;
    inputs.appliedVolts = appliedVolts.getValueAsDouble();
    inputs.supplyCurrentAmps = supplyCurrent.getValueAsDouble();
  }

  @Override
  public void setTargetPosition(double positionRad) {
    talon.setControl(positionRequest.withPosition(positionRad / TWO_PI * ArmConstants.GEAR_RATIO));
  }

  @Override
  public void setVoltage(double volts) {
    talon.setControl(voltageRequest.withOutput(volts));
  }

  @Override
  public void stop() {
    talon.setControl(neutralRequest);
  }
}
