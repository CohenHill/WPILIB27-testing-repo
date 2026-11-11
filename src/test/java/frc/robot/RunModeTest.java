package frc.robot;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class RunModeTest {
  @Test
  void realRobotWithoutLogPathIsReal() {
    assertEquals(RunMode.REAL, RunMode.select(true, null));
  }

  @Test
  void realRobotNeverReplays() {
    assertEquals(RunMode.REAL, RunMode.select(true, "/some/log.wpilog"));
  }

  @Test
  void notRealWithLogPathIsReplay() {
    assertEquals(RunMode.REPLAY, RunMode.select(false, "/some/log.wpilog"));
  }

  @Test
  void notRealWithNullLogPathIsSim() {
    assertEquals(RunMode.SIM, RunMode.select(false, null));
  }

  @Test
  void notRealWithEmptyLogPathIsSim() {
    assertEquals(RunMode.SIM, RunMode.select(false, ""));
  }

  @Test
  void notRealWithBlankLogPathIsSim() {
    assertEquals(RunMode.SIM, RunMode.select(false, "   "));
  }
}
