package frc.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class LogMetricsTest {
  /** An empty in-memory log lasting 10 seconds. */
  private static LogData log() {
    LogData d = new LogData();
    d.startSeconds = 0;
    d.endSeconds = 10;
    return d;
  }

  private static void numeric(LogData d, String name, String type, double[] t, double[] v) {
    LogData.Entry e = LogData.newEntry("/" + name, type);
    for (int i = 0; i < t.length; i++) {
      e.times.add(t[i]);
      e.values.add(v[i]);
    }
    d.entries.put("/" + name, e);
  }

  private static void text(LogData d, String name, String type, double[] t, String[][] v) {
    LogData.Entry e = LogData.newEntry("/" + name, type);
    for (int i = 0; i < t.length; i++) {
      e.textTimes.add(t[i]);
      e.texts.add(v[i]);
    }
    d.entries.put("/" + name, e);
  }

  @Test
  void eventsListsEnableModeAlertBrownoutAndOverrunInTimeOrder() {
    LogData d = log();
    numeric(d, "DriverStation/Enabled", "boolean", new double[] {0, 1, 3}, new double[] {0, 1, 0});
    numeric(d, "DriverStation/DSAttached", "boolean", new double[] {0}, new double[] {1});
    text(d, "DriverStation/OpMode", "string", new double[] {0, 1}, new String[][] {{""}, {"Auto"}});
    text(
        d,
        "RealOutputs/AlertGroups/Drive/errors",
        "string[]",
        new double[] {0, 2, 4},
        new String[][] {{}, {"motor 3 stalled"}, {}});
    numeric(
        d,
        "SystemStats/FaultCounts/Brownout",
        "int64",
        new double[] {0, 2.5, 5},
        new double[] {0, 1, 1});
    numeric(
        d,
        "RealOutputs/LoggedOpModeRobot/FullCycleMS",
        "double",
        new double[] {0, 1.5, 2},
        new double[] {10, 95, 10});

    String out = LogMetrics.events(d, 50);
    assertTrue(out.startsWith("{\"total\":7,"), out);
    int enabled = out.indexOf("\"type\":\"enabled\"");
    int ds = out.indexOf("\"type\":\"ds-attached\"");
    int mode = out.indexOf("\"type\":\"mode\",\"detail\":\"Auto\"");
    int overrun = out.indexOf("\"type\":\"loop-overrun\"");
    int alert = out.indexOf("\"detail\":\"Drive: motor 3 stalled\"");
    int brownout = out.indexOf("\"type\":\"brownout\"");
    int disabled = out.indexOf("\"type\":\"disabled\"");
    assertTrue(enabled >= 0 && ds >= 0 && mode > 0 && overrun > mode, out);
    assertTrue(alert > overrun && brownout > alert && disabled > 0, out);
  }

  @Test
  void eventsRespectsLimit() {
    LogData d = log();
    numeric(d, "DriverStation/Enabled", "boolean", new double[] {0, 1, 2}, new double[] {1, 0, 1});
    String out = LogMetrics.events(d, 2);
    assertTrue(out.startsWith("{\"total\":3,\"shown\":2,"), out);
  }

  @Test
  void eventsOnAnEmptyLogIsEmpty() {
    assertEquals("{\"total\":0,\"shown\":0,\"events\":[]}", LogMetrics.events(log(), 50));
  }

  @Test
  void trackMeasuresErrorAgainstAStepGoal() throws Exception {
    LogData d = log();
    numeric(d, "RealOutputs/Arm/GoalRad", "double", new double[] {0, 2}, new double[] {0.0, 1.0});
    numeric(d, "Arm/PositionRad", "double", new double[] {1, 3, 4}, new double[] {0.0, 0.9, 1.0});
    String out = LogMetrics.track(d, "Arm");
    assertTrue(out.contains("\"samples\":3"), out);
    assertTrue(
        out.contains("\"maxAbsError\":0.09999999999999998") || out.contains("\"maxAbsError\":0.1"),
        out);
    assertTrue(out.contains("\"finalError\":0"), out);
    // |error| = 0, 0.1, 0 with a 0.05 tolerance: 2 of 3 samples are near the goal
    assertTrue(out.contains("\"fractionNearGoal\":0.6666"), out);
  }

  @Test
  void trackRejectsUnknownMechanismsAndMissingSignals() {
    assertThrows(LogQuery.QueryException.class, () -> LogMetrics.track(log(), "Drive"));
    assertThrows(LogQuery.QueryException.class, () -> LogMetrics.track(log(), "Arm"));
  }

  @Test
  void driverReportComputesCyclesIdleAndZones() {
    LogData d = log();
    numeric(d, "DriverStation/Enabled", "boolean", new double[] {0}, new double[] {1});
    // Two scores: at t=3 (3 s after enable) and t=8 (5 s after the first)
    numeric(
        d,
        "RealOutputs/Context/HoldingPiece",
        "boolean",
        new double[] {0, 1, 3, 4, 8},
        new double[] {0, 1, 0, 1, 0});
    // Still 0..2 s (idle 2 s), then moves 3 m in 1 s, then holds still to the end (7 s idle)
    numeric(d, "RealOutputs/Drive/PoseX", "double", new double[] {0, 2, 3}, new double[] {0, 0, 3});
    numeric(d, "RealOutputs/Drive/PoseY", "double", new double[] {0, 2, 3}, new double[] {0, 0, 0});
    text(
        d,
        "RealOutputs/Context/Zone",
        "string",
        new double[] {0, 5},
        new String[][] {{"A"}, {"B"}});

    String out = LogMetrics.driverReport(d);
    assertTrue(out.contains("\"enabledSeconds\":10"), out);
    assertTrue(out.contains("\"cycles\":2"), out);
    assertTrue(out.contains("\"cycleSeconds\":{\"mean\":4,\"min\":3,\"max\":5}"), out);
    assertTrue(out.contains("\"idleSeconds\":9"), out);
    assertTrue(out.contains("\"secondsPerZone\":{\"A\":5,\"B\":5}"), out);
    assertTrue(out.contains("\"timeToAlign\":null"), out);
  }

  @Test
  void driverReportCountsOnlyEnabledTime() {
    LogData d = log();
    // Enabled only from 2 to 6; zone A the whole log
    numeric(d, "DriverStation/Enabled", "boolean", new double[] {0, 2, 6}, new double[] {0, 1, 0});
    text(d, "RealOutputs/Context/Zone", "string", new double[] {0}, new String[][] {{"A"}});
    String out = LogMetrics.driverReport(d);
    assertTrue(out.contains("\"enabledSeconds\":4"), out);
    assertTrue(out.contains("\"secondsPerZone\":{\"A\":4}"), out);
  }

  @Test
  void driverReportSaysWhenSignalsAreMissing() {
    String out = LogMetrics.driverReport(log());
    assertTrue(out.contains("\"enabledSeconds\":0"), out);
    assertTrue(out.contains("\"cycles\":null"), out);
    assertTrue(out.contains("\"idleSeconds\":null"), out);
    assertTrue(out.contains("\"secondsPerZone\":null"), out);
  }

  @Test
  void overlapClipsToIntervals() {
    List<double[]> iv = List.of(new double[] {2, 4}, new double[] {6, 9});
    assertEquals(2.0, LogMetrics.overlap(iv, 0, 5), 1e-12);
    assertEquals(0.0, LogMetrics.overlap(iv, 4, 6), 1e-12);
    assertEquals(5.0, LogMetrics.overlap(iv, 0, 10), 1e-12);
  }

  @Test
  void realMatchLogReportsTwoScoresAndBothZones() throws Exception {
    LogData d = LogData.read("src/test/resources/frc/tools/match.wpilog");
    String report = LogMetrics.driverReport(d);
    assertTrue(report.contains("\"cycles\":2"), report);
    assertTrue(report.contains("\"LEFT\":") && report.contains("\"RIGHT\":"), report);
    assertTrue(LogMetrics.events(d, 50).contains("\"detail\":\"Sim Match\""));
    assertTrue(LogMetrics.track(d, "Arm").contains("\"samples\":"));
  }
}
