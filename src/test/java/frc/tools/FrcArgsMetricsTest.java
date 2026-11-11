package frc.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class FrcArgsMetricsTest {
  @Test
  void eventsTakesALogAndAnOptionalLimit() throws Exception {
    FrcArgs a = FrcArgs.parse(new String[] {"events", "x.wpilog", "--limit", "5"});
    assertEquals("events", a.command());
    assertEquals("x.wpilog", a.log());
    assertEquals(5, a.limit());
  }

  @Test
  void trackTakesALogAndAMechanism() throws Exception {
    FrcArgs a = FrcArgs.parse(new String[] {"track", "x.wpilog", "Arm"});
    assertEquals("Arm", a.key());
  }

  @Test
  void driverReportTakesOnlyALog() throws Exception {
    assertEquals("x.wpilog", FrcArgs.parse(new String[] {"driver-report", "x.wpilog"}).log());
    assertThrows(
        FrcArgs.UsageException.class,
        () -> FrcArgs.parse(new String[] {"driver-report", "x.wpilog", "extra"}));
    assertThrows(FrcArgs.UsageException.class, () -> FrcArgs.parse(new String[] {"driver-report"}));
  }

  @Test
  void pullTakesNothing() throws Exception {
    assertNull(FrcArgs.parse(new String[] {"pull"}).log());
    assertThrows(FrcArgs.UsageException.class, () -> FrcArgs.parse(new String[] {"pull", "x"}));
  }

  @Test
  void trackNeedsBothArguments() {
    assertThrows(FrcArgs.UsageException.class, () -> FrcArgs.parse(new String[] {"track", "x"}));
  }
}
