package frc.robot.mechanisms.arm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ArmTest {
  private static final double MIN = ArmConstants.MIN_ANGLE_RAD;
  private static final double MAX = ArmConstants.MAX_ANGLE_RAD;
  private static final double TOL = ArmConstants.GOAL_TOLERANCE_RAD;

  @Test
  void clampGoalAtLimitsIsUnchanged() {
    assertEquals(MIN, Arm.clampGoal(MIN));
    assertEquals(MAX, Arm.clampGoal(MAX));
  }

  @Test
  void clampGoalInsideLimitsIsUnchanged() {
    assertEquals(0.5, Arm.clampGoal(0.5));
  }

  @Test
  void clampGoalBeyondLimitsIsClamped() {
    assertEquals(MIN, Arm.clampGoal(MIN - 1.0));
    assertEquals(MAX, Arm.clampGoal(MAX + 1.0));
  }

  @Test
  void atGoalWithinToleranceIsTrue() {
    // Goal 0.0 keeps the boundary exact (no floating-point rounding in the difference)
    assertTrue(Arm.atGoal(1.0, 1.0));
    assertTrue(Arm.atGoal(TOL, 0.0));
    assertTrue(Arm.atGoal(-TOL, 0.0));
  }

  @Test
  void atGoalJustOutsideToleranceIsFalse() {
    assertFalse(Arm.atGoal(1.0 + TOL + 0.001, 1.0));
    assertFalse(Arm.atGoal(1.0 - TOL - 0.001, 1.0));
  }
}
