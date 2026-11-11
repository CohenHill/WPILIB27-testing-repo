package frc.robot.mechanisms.arm.io;

import static org.wpilib.units.Units.Amps;
import static org.wpilib.units.Units.Kilograms;
import static org.wpilib.units.Units.Meters;
import static org.wpilib.units.Units.Radians;
import static org.wpilib.units.Units.RadiansPerSecond;
import static org.wpilib.units.Units.Volts;

import com.ctre.phoenix6.CANBus;
import com.ctre.phoenix6.hardware.TalonFX;
import frc.robot.mechanisms.arm.ArmConstants;
import org.wpilib.command3.Mechanism;
import org.wpilib.framework.RobotBase;
import org.wpilib.math.controller.ArmFeedforward;
import org.wpilib.math.system.DCMotor;
import yams.commands3.config.SmartMotorControllerConfig;
import yams.commands3.mechanisms.Arm;
import yams.core.mechanisms.config.ArmConfig;
import yams.core.motorcontrollers.SmartMotorController;
import yams.core.motorcontrollers.enums.ControlMode;
import yams.core.motorcontrollers.remote.TalonFXWrapper;

/**
 * YAMS-backed arm IO for the real robot and simulation.
 *
 * <p>YAMS needs a Commands v3 {@link Mechanism} in its config (it uses it only to register live
 * tuning commands). This IO does not own a mechanism, so it passes an unregistered placeholder.
 * UNTESTED ON HARDWARE, like {@link ArmIOTalonFX}.
 */
public class ArmIOYams implements ArmIO {
  private final TalonFX talon = new TalonFX(ArmConstants.CAN_ID, new CANBus(ArmConstants.CAN_BUS));
  private final SmartMotorController smc;
  private final Arm arm;

  public ArmIOYams() {
    SmartMotorControllerConfig config =
        new SmartMotorControllerConfig(new Mechanism() {})
            .withControlMode(ControlMode.CLOSED_LOOP)
            .withGearing(ArmConstants.GEAR_RATIO)
            .withClosedLoopController(ArmConstants.YAMS_KP, 0.0, ArmConstants.YAMS_KD)
            .withFeedforward(new ArmFeedforward(0.0, ArmConstants.SIM_KG, 0.0))
            .withMomentOfInertia(
                Meters.of(ArmConstants.ARM_LENGTH_METERS), Kilograms.of(ArmConstants.ARM_MASS_KG))
            .withStartingPosition(Radians.of(ArmConstants.START_ANGLE_RAD));
    smc = new TalonFXWrapper(talon, DCMotor.getKrakenX60(1), config);
    arm =
        new Arm(
            new ArmConfig()
                .withLength(Meters.of(ArmConstants.ARM_LENGTH_METERS))
                .withHardLimits(
                    Radians.of(ArmConstants.MIN_ANGLE_RAD), Radians.of(ArmConstants.MAX_ANGLE_RAD)),
            smc);
  }

  @Override
  public void updateInputs(ArmIOInputs inputs) {
    if (RobotBase.isSimulation()) {
      arm.simIterate();
    }
    inputs.connected = talon.getPosition().getStatus().isOK();
    inputs.positionRad = smc.getMechanismPosition().in(Radians);
    inputs.velocityRadPerSec = smc.getMechanismVelocity().in(RadiansPerSecond);
    inputs.appliedVolts = smc.getVoltage().in(Volts);
    inputs.supplyCurrentAmps = smc.getSupplyCurrent().map(c -> c.in(Amps)).orElse(0.0);
  }

  @Override
  public void setTargetPosition(double positionRad) {
    smc.setPosition(Radians.of(positionRad));
  }

  @Override
  public void setVoltage(double volts) {
    smc.stopClosedLoopController();
    smc.setVoltage(Volts.of(volts));
  }

  @Override
  public void stop() {
    smc.stopClosedLoopController();
    smc.setVoltage(Volts.of(0.0));
  }
}
