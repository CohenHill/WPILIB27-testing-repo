package frc.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

/** Queries against a fixture produced by running ArmReachesGoal and replaying its log. */
class LogQueryTest {
  private static final String FIXTURE = "src/test/resources/frc/tools/arm_replay.wpilog";

  private static LogData fixture() throws Exception {
    return LogData.read(FIXTURE);
  }

  @Test
  void keysListsArmInputsWithTypes() throws Exception {
    String out = LogQuery.keys(fixture(), "Arm", 50);
    assertTrue(out.contains("\"name\":\"/Arm/PositionRad\",\"type\":\"double\""), out);
    assertTrue(out.contains("\"name\":\"/Arm/Connected\",\"type\":\"boolean\""), out);
    assertTrue(out.startsWith("{\"total\":5,\"shown\":5"), out);
  }

  @Test
  void keysRespectsLimit() throws Exception {
    String out = LogQuery.keys(fixture(), null, 3);
    assertTrue(out.contains("\"shown\":3"), out);
  }

  @Test
  void keysListsNonNumericTypes() throws Exception {
    String out = LogQuery.keys(fixture(), "ReplayOutputs/Console", 50);
    assertTrue(out.contains("\"type\":\"string\""), out);
  }

  @Test
  void statsOfPositionIsWithinLimits() throws Exception {
    String out = LogQuery.stats(fixture(), "Arm/PositionRad", null, null);
    assertTrue(out.contains("\"count\":148"), out);
    assertTrue(out.contains("\"type\":\"double\""), out);
  }

  @Test
  void statsWindowSelectsFewerSamples() throws Exception {
    String all = LogQuery.stats(fixture(), "Arm/PositionRad", null, null);
    String early = LogQuery.stats(fixture(), "Arm/PositionRad", 0.0, 0.1);
    assertTrue(all.contains("\"count\":148"), all);
    assertTrue(!early.contains("\"count\":148") && !early.contains("\"count\":0,"), early);
  }

  @Test
  void statsOfMissingKeyFails() {
    assertThrows(
        LogQuery.QueryException.class, () -> LogQuery.stats(fixture(), "Nope", null, null));
  }

  @Test
  void statsOfStringSignalIsUnsupported() throws Exception {
    LogQuery.QueryException e =
        assertThrows(
            LogQuery.QueryException.class,
            () -> LogQuery.stats(fixture(), "ReplayOutputs/Console", null, null));
    assertTrue(e.getMessage().contains("unsupported type"), e.getMessage());
  }

  @Test
  void replayedArmOutputsMatchRecordedOutputs() throws Exception {
    assertEquals(
        "{\"key\":\"Arm/GoalRad\",\"diverged\":false,\"samples\":3}",
        LogQuery.diff(fixture(), "Arm/GoalRad"));
    assertTrue(LogQuery.diff(fixture(), "Arm/AtGoal").contains("\"diverged\":false"));
  }

  @Test
  void diffReportsFirstDivergence() throws Exception {
    LogData d = new LogData();
    d.startSeconds = 0;
    LogData.Entry real = entry(d, "/RealOutputs/X", List.of(0.0, 1.0, 2.0), List.of(5.0, 6.0, 7.0));
    LogData.Entry replay =
        entry(d, "/ReplayOutputs/X", List.of(0.0, 1.0, 2.0), List.of(5.0, 9.0, 7.0));
    assertTrue(real.isNumeric() && replay.isNumeric());
    String out = LogQuery.diff(d, "X");
    assertTrue(out.contains("\"diverged\":true") && out.contains("\"index\":1"), out);
    assertTrue(out.contains("\"real\":6") && out.contains("\"replay\":9"), out);
  }

  @Test
  void diffWithoutReplayedSignalFails() throws Exception {
    assertThrows(LogQuery.QueryException.class, () -> LogQuery.diff(fixture(), "Arm/PositionRad"));
  }

  @Test
  void missingFileIsAnError() {
    assertThrows(java.io.IOException.class, () -> LogData.read("no/such/file.wpilog"));
  }

  private static LogData.Entry entry(
      LogData d, String name, List<Double> times, List<Double> values) {
    LogData.Entry e = LogData.newEntry(name, "double");
    e.times.addAll(times);
    e.values.addAll(values);
    d.entries.put(name, e);
    return e;
  }
}
