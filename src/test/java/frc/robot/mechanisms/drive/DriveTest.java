package frc.robot.mechanisms.drive;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class DriveTest {
  @Test
  void deadbandZeroesSmallInputs() {
    assertEquals(0.0, Drive.deadband(0.05, 0.1));
    assertEquals(0.0, Drive.deadband(-0.1, 0.1));
    assertEquals(0.0, Drive.deadband(0.0, 0.1));
  }

  @Test
  void deadbandRescalesOutsideTheBand() {
    assertEquals(1.0, Drive.deadband(1.0, 0.1), 1e-12);
    assertEquals(-1.0, Drive.deadband(-1.0, 0.1), 1e-12);
    assertEquals(0.5, Drive.deadband(0.55, 0.1), 1e-12);
  }

  @Test
  void stickToSpeedIsSquaredAndSigned() {
    assertEquals(0.0, Drive.stickToSpeed(0.05, 0.1, 4.0));
    assertEquals(4.0, Drive.stickToSpeed(1.0, 0.1, 4.0), 1e-12);
    assertEquals(-4.0, Drive.stickToSpeed(-1.0, 0.1, 4.0), 1e-12);
    // 0.55 -> 0.5 after deadband -> 0.25 squared
    assertEquals(1.0, Drive.stickToSpeed(0.55, 0.1, 4.0), 1e-12);
  }

  @Test
  void stickToSpeedClampsOutOfRangeInput() {
    assertEquals(4.0, Drive.stickToSpeed(5.0, 0.1, 4.0), 1e-12);
    assertEquals(-4.0, Drive.stickToSpeed(-5.0, 0.1, 4.0), 1e-12);
  }

  @Test
  void higherRatioIsSlower() {
    double r1 = DriveConstants.ModuleRatio.R1.freeSpeedMetersPerSec(6000, 0.0508);
    double r2 = DriveConstants.ModuleRatio.R2.freeSpeedMetersPerSec(6000, 0.0508);
    double r3 = DriveConstants.ModuleRatio.R3.freeSpeedMetersPerSec(6000, 0.0508);
    assertTrue(r1 < r2 && r2 < r3, r1 + " " + r2 + " " + r3);
  }

  @Test
  void freeSpeedMatchesHandCalculation() {
    // 6000 rpm / 60 / 6.03 = 16.58 wheel rev/s; circumference 2*pi*0.0508 = 0.3192 m
    assertEquals(5.29, DriveConstants.ModuleRatio.R2.freeSpeedMetersPerSec(6000, 0.0508), 0.01);
  }

  @Test
  void teleopMaxSpeedIsBelowTheoreticalMax() {
    double free =
        DriveConstants.RATIO.freeSpeedMetersPerSec(
            DriveConstants.MOTOR_FREE_SPEED_RPM, DriveConstants.WHEEL_RADIUS_METERS);
    assertTrue(DriveConstants.MAX_SPEED_METERS_PER_SEC < free);
    assertTrue(DriveConstants.MAX_SPEED_METERS_PER_SEC > 0);
  }
}
