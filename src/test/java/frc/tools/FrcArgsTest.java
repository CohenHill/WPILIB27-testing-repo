package frc.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class FrcArgsTest {
  @Test
  void keysWithPrefix() throws Exception {
    FrcArgs a = FrcArgs.parse(new String[] {"keys", "my.wpilog", "Arm"});
    assertEquals("keys", a.command());
    assertEquals("my.wpilog", a.log());
    assertEquals("Arm", a.key());
    assertEquals(FrcArgs.DEFAULT_LIMIT, a.limit());
  }

  @Test
  void keysWithoutPrefix() throws Exception {
    assertNull(FrcArgs.parse(new String[] {"keys", "my.wpilog"}).key());
  }

  @Test
  void statsWithWindowAndLimit() throws Exception {
    FrcArgs a =
        FrcArgs.parse(
            new String[] {"stats", "my.wpilog", "Arm/PositionRad", "--from", "1.5", "--to", "3"});
    assertEquals(1.5, a.from());
    assertEquals(3.0, a.to());
    assertEquals(10, FrcArgs.parse(new String[] {"stats", "l", "k", "--limit", "10"}).limit());
  }

  @Test
  void diffTakesLogAndKey() throws Exception {
    FrcArgs a = FrcArgs.parse(new String[] {"diff", "my.wpilog", "Arm/GoalRad"});
    assertEquals("diff", a.command());
    assertEquals("Arm/GoalRad", a.key());
  }

  @Test
  void rejectsBadInput() {
    assertThrows(FrcArgs.UsageException.class, () -> FrcArgs.parse(new String[] {}));
    assertThrows(FrcArgs.UsageException.class, () -> FrcArgs.parse(new String[] {"nope", "l"}));
    assertThrows(FrcArgs.UsageException.class, () -> FrcArgs.parse(new String[] {"keys"}));
    assertThrows(FrcArgs.UsageException.class, () -> FrcArgs.parse(new String[] {"stats", "l"}));
    assertThrows(
        FrcArgs.UsageException.class, () -> FrcArgs.parse(new String[] {"diff", "l", "k", "x"}));
    assertThrows(
        FrcArgs.UsageException.class,
        () -> FrcArgs.parse(new String[] {"stats", "l", "k", "--from"}));
    assertThrows(
        FrcArgs.UsageException.class,
        () -> FrcArgs.parse(new String[] {"stats", "l", "k", "--from", "abc"}));
    assertThrows(
        FrcArgs.UsageException.class,
        () -> FrcArgs.parse(new String[] {"stats", "l", "k", "--limit", "0"}));
    assertThrows(
        FrcArgs.UsageException.class, () -> FrcArgs.parse(new String[] {"keys", "l", "--bogus"}));
    assertThrows(
        FrcArgs.UsageException.class,
        () -> FrcArgs.parse(new String[] {"keys", "l", "--from", "1"}));
  }
}
