package frc.tools;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Event, tracking and driver metrics over a {@link LogData}. Definitions are in docs/metrics.md.
 * Each method returns one line of compact JSON.
 */
public final class LogMetrics {
  private LogMetrics() {}

  /** A loop longer than this is an overrun: 4 periods, as in LoggedOpModeRobot's watchdog. */
  public static final double OVERRUN_MS = 80.0;

  /** The robot is idle below this speed, in meters per second. */
  public static final double IDLE_SPEED_MPS = 0.05;

  /** Arm tolerance for "near goal", in radians (matches ArmConstants.GOAL_TOLERANCE_RAD). */
  public static final double ARM_TOLERANCE_RAD = 0.05;

  private record Event(double t, String type, String detail) {}

  // ---------------------------------------------------------------- events

  /** Time-ordered events, capped at {@code limit}. */
  public static String events(LogData d, int limit) {
    List<Event> events = new ArrayList<>();

    LogData.Entry enabled = d.find("DriverStation/Enabled");
    boolean[] prev = {false};
    boolean[] seen = {false};
    forEachNumeric(
        enabled,
        d,
        (t, v) -> {
          boolean on = v != 0;
          if (!seen[0] ? on : on != prev[0]) {
            events.add(new Event(t, on ? "enabled" : "disabled", ""));
          }
          seen[0] = true;
          prev[0] = on;
        });

    LogData.Entry ds = d.find("DriverStation/DSAttached");
    prev[0] = false;
    seen[0] = false;
    forEachNumeric(
        ds,
        d,
        (t, v) -> {
          boolean on = v != 0;
          if (!seen[0] ? on : on != prev[0]) {
            events.add(new Event(t, on ? "ds-attached" : "ds-detached", ""));
          }
          seen[0] = true;
          prev[0] = on;
        });

    prev[0] = false;
    forEachNumeric(
        d.find("DriverStation/EmergencyStop"),
        d,
        (t, v) -> {
          boolean on = v != 0;
          if (on && !prev[0]) {
            events.add(new Event(t, "estop", ""));
          }
          prev[0] = on;
        });

    LogData.Entry mode = d.find("DriverStation/OpMode");
    if (mode != null) {
      String last = "";
      for (int i = 0; i < mode.texts.size(); i++) {
        String name = mode.texts.get(i).length > 0 ? mode.texts.get(i)[0] : "";
        if (!name.isEmpty() && !name.equals(last)) {
          events.add(new Event(rel(d, mode.textTimes.get(i)), "mode", name));
        }
        last = name;
      }
    }

    for (Map.Entry<String, LogData.Entry> e : d.entries.entrySet()) {
      String name = e.getKey();
      String type = null;
      if (name.startsWith("/RealOutputs/AlertGroups/") && name.endsWith("/errors")) {
        type = "alert-error";
      } else if (name.startsWith("/RealOutputs/AlertGroups/") && name.endsWith("/warnings")) {
        type = "alert-warning";
      }
      if (type == null || !e.getValue().isText()) {
        continue;
      }
      String group = name.substring("/RealOutputs/AlertGroups/".length(), name.lastIndexOf('/'));
      String lastText = "";
      LogData.Entry entry = e.getValue();
      for (int i = 0; i < entry.texts.size(); i++) {
        String text = String.join(" | ", entry.texts.get(i));
        if (!text.isEmpty() && !text.equals(lastText)) {
          events.add(new Event(rel(d, entry.textTimes.get(i)), type, group + ": " + text));
        }
        lastText = text;
      }
    }

    double[] lastCount = {Double.NaN};
    forEachNumeric(
        d.find("SystemStats/FaultCounts/Brownout"),
        d,
        (t, v) -> {
          if (!Double.isNaN(lastCount[0]) && v > lastCount[0]) {
            events.add(new Event(t, "brownout", "count " + Json.num(v)));
          }
          lastCount[0] = v;
        });

    forEachNumeric(
        d.find("RealOutputs/LoggedOpModeRobot/FullCycleMS"),
        d,
        (t, v) -> {
          if (v > OVERRUN_MS) {
            events.add(new Event(t, "loop-overrun", Json.num(Math.round(v * 10) / 10.0) + " ms"));
          }
        });

    events.sort(Comparator.comparingDouble(Event::t));
    StringBuilder sb = new StringBuilder("[");
    int shown = Math.min(limit, events.size());
    for (int i = 0; i < shown; i++) {
      Event ev = events.get(i);
      if (i > 0) {
        sb.append(',');
      }
      sb.append("{\"t\":").append(Json.num(round3(ev.t())));
      sb.append(",\"type\":").append(Json.str(ev.type()));
      sb.append(",\"detail\":").append(Json.str(ev.detail())).append('}');
    }
    sb.append(']');
    return "{\"total\":" + events.size() + ",\"shown\":" + shown + ",\"events\":" + sb + "}";
  }

  // ----------------------------------------------------------------- track

  private record Track(String goalKey, String measuredKey, double tolerance) {}

  private static final Map<String, Track> TRACKS =
      Map.of("Arm", new Track("RealOutputs/Arm/GoalRad", "Arm/PositionRad", ARM_TOLERANCE_RAD));

  /** Goal against measured for a mechanism. */
  public static String track(LogData d, String mechanism) throws LogQuery.QueryException {
    Track t = TRACKS.get(mechanism);
    if (t == null) {
      throw new LogQuery.QueryException(
          "unknown mechanism '"
              + mechanism
              + "' (known: "
              + String.join(", ", TRACKS.keySet())
              + "; others have no numeric goal signal)");
    }
    LogData.Entry goal = d.find(t.goalKey());
    LogData.Entry measured = d.find(t.measuredKey());
    if (goal == null || !goal.isNumeric()) {
      throw new LogQuery.QueryException("no goal signal " + t.goalKey() + " in log");
    }
    if (measured == null || !measured.isNumeric()) {
      throw new LogQuery.QueryException("no measured signal " + t.measuredKey() + " in log");
    }
    int n = 0;
    int near = 0;
    double sumAbs = 0;
    double sumSq = 0;
    double maxAbs = 0;
    double last = Double.NaN;
    int g = -1;
    for (int i = 0; i < measured.values.size(); i++) {
      double time = measured.times.get(i);
      while (g + 1 < goal.times.size() && goal.times.get(g + 1) <= time) {
        g++;
      }
      if (g < 0) {
        continue; // before the first goal sample
      }
      double err = measured.values.get(i) - goal.values.get(g);
      n++;
      sumAbs += Math.abs(err);
      sumSq += err * err;
      maxAbs = Math.max(maxAbs, Math.abs(err));
      if (Math.abs(err) <= t.tolerance()) {
        near++;
      }
      last = err;
    }
    if (n == 0) {
      return "{\"mechanism\":"
          + Json.str(mechanism)
          + ",\"samples\":0,\"reason\":\"no measured samples after the first goal\"}";
    }
    return "{\"mechanism\":"
        + Json.str(mechanism)
        + ",\"samples\":"
        + n
        + ",\"meanAbsError\":"
        + Json.num(sumAbs / n)
        + ",\"rmsError\":"
        + Json.num(Math.sqrt(sumSq / n))
        + ",\"maxAbsError\":"
        + Json.num(maxAbs)
        + ",\"finalError\":"
        + Json.num(last)
        + ",\"fractionNearGoal\":"
        + Json.num((double) near / n)
        + ",\"tolerance\":"
        + Json.num(t.tolerance())
        + "}";
  }

  // ---------------------------------------------------------- driver report

  /** Enabled time, cycles, idle time and time per zone. */
  public static String driverReport(LogData d) {
    List<double[]> enabled = enabledIntervals(d);
    double enabledSeconds = sum(enabled);

    StringBuilder sb = new StringBuilder("{\"enabledSeconds\":" + Json.num(round3(enabledSeconds)));

    // Cycles: HoldingPiece true -> false while enabled
    LogData.Entry holding = d.find("RealOutputs/Context/HoldingPiece");
    if (holding == null || !holding.isNumeric()) {
      sb.append(
          ",\"cycles\":null,\"cycleSeconds\":null,\"cyclesReason\":\"no RealOutputs/Context/HoldingPiece signal\"");
    } else {
      List<Double> scores = new ArrayList<>();
      boolean was = false;
      for (int i = 0; i < holding.values.size(); i++) {
        boolean now = holding.values.get(i) != 0;
        double t = rel(d, holding.times.get(i));
        if (was && !now && isEnabledAt(enabled, t)) {
          scores.add(t);
        }
        was = now;
      }
      sb.append(",\"cycles\":").append(scores.size());
      if (scores.isEmpty() || enabled.isEmpty()) {
        sb.append(",\"cycleSeconds\":null");
      } else {
        double prevT = enabled.get(0)[0];
        double total = 0;
        double min = Double.POSITIVE_INFINITY;
        double max = 0;
        for (double t : scores) {
          double dt = t - prevT;
          total += dt;
          min = Math.min(min, dt);
          max = Math.max(max, dt);
          prevT = t;
        }
        sb.append(",\"cycleSeconds\":{\"mean\":")
            .append(Json.num(round3(total / scores.size())))
            .append(",\"min\":")
            .append(Json.num(round3(min)))
            .append(",\"max\":")
            .append(Json.num(round3(max)))
            .append('}');
      }
    }

    sb.append(
        ",\"timeToAlign\":null,\"timeToAlignReason\":\"no align command exists yet (M8), so nothing is logged\"");

    // Idle time from pose changes
    LogData.Entry px = d.find("RealOutputs/Drive/PoseX");
    LogData.Entry py = d.find("RealOutputs/Drive/PoseY");
    if (px == null || py == null || !px.isNumeric() || !py.isNumeric() || px.times.isEmpty()) {
      sb.append(
          ",\"idleSeconds\":null,\"idleReason\":\"no RealOutputs/Drive/PoseX and PoseY signals\"");
    } else {
      sb.append(",\"idleSeconds\":").append(Json.num(round3(idleSeconds(d, px, py, enabled))));
    }

    // Time per zone
    LogData.Entry zone = d.find("RealOutputs/Context/Zone");
    if (zone == null || !zone.isText() || zone.texts.isEmpty()) {
      sb.append(",\"secondsPerZone\":null,\"zoneReason\":\"no RealOutputs/Context/Zone signal\"");
    } else {
      Map<String, Double> perZone = new LinkedHashMap<>();
      double end = rel(d, d.endSeconds);
      for (int i = 0; i < zone.texts.size(); i++) {
        double a = rel(d, zone.textTimes.get(i));
        double b = i + 1 < zone.texts.size() ? rel(d, zone.textTimes.get(i + 1)) : end;
        String name = zone.texts.get(i).length > 0 ? zone.texts.get(i)[0] : "";
        perZone.merge(name, overlap(enabled, a, b), Double::sum);
      }
      sb.append(",\"secondsPerZone\":{");
      boolean first = true;
      for (Map.Entry<String, Double> e : perZone.entrySet()) {
        if (!first) {
          sb.append(',');
        }
        first = false;
        sb.append(Json.str(e.getKey())).append(':').append(Json.num(round3(e.getValue())));
      }
      sb.append('}');
    }
    return sb.append('}').toString();
  }

  private static double idleSeconds(
      LogData d, LogData.Entry px, LogData.Entry py, List<double[]> enabled) {
    // Merge sample times of X and Y; value at a time is the latest sample at or before it.
    List<Double> times = new ArrayList<>();
    times.addAll(px.times);
    times.addAll(py.times);
    times.sort(Comparator.naturalOrder());
    double idle = 0;
    int ix = 0;
    int iy = 0;
    double x = Double.NaN;
    double y = Double.NaN;
    double prevT = Double.NaN;
    double prevX = Double.NaN;
    double prevY = Double.NaN;
    for (double t : times) {
      while (ix < px.times.size() && px.times.get(ix) <= t) {
        x = px.values.get(ix++);
      }
      while (iy < py.times.size() && py.times.get(iy) <= t) {
        y = py.values.get(iy++);
      }
      if (!Double.isNaN(prevT) && t > prevT && !Double.isNaN(prevX) && !Double.isNaN(prevY)) {
        double speed = Math.hypot(x - prevX, y - prevY) / (t - prevT);
        if (speed < IDLE_SPEED_MPS) {
          idle += overlap(enabled, rel(d, prevT), rel(d, t));
        }
      }
      prevT = t;
      prevX = x;
      prevY = y;
    }
    // After the last pose sample the pose holds: the robot did not move.
    if (!Double.isNaN(prevT)) {
      idle += overlap(enabled, rel(d, prevT), rel(d, d.endSeconds));
    }
    return idle;
  }

  // ---------------------------------------------------------------- helpers

  private static double rel(LogData d, double absoluteSeconds) {
    return absoluteSeconds - d.startSeconds;
  }

  private static double round3(double v) {
    return Math.round(v * 1000.0) / 1000.0;
  }

  private interface Sample {
    void accept(double relTime, double value);
  }

  private static void forEachNumeric(LogData.Entry e, LogData d, Sample s) {
    if (e == null || !e.isNumeric()) {
      return;
    }
    for (int i = 0; i < e.values.size(); i++) {
      s.accept(rel(d, e.times.get(i)), e.values.get(i));
    }
  }

  /** Intervals [start, end] in relative seconds during which the robot is enabled. */
  static List<double[]> enabledIntervals(LogData d) {
    List<double[]> out = new ArrayList<>();
    LogData.Entry en = d.find("DriverStation/Enabled");
    if (en == null || !en.isNumeric()) {
      return out;
    }
    double start = Double.NaN;
    for (int i = 0; i < en.values.size(); i++) {
      boolean on = en.values.get(i) != 0;
      double t = rel(d, en.times.get(i));
      if (on && Double.isNaN(start)) {
        start = t;
      } else if (!on && !Double.isNaN(start)) {
        out.add(new double[] {start, t});
        start = Double.NaN;
      }
    }
    if (!Double.isNaN(start)) {
      out.add(new double[] {start, rel(d, d.endSeconds)});
    }
    return out;
  }

  private static double sum(List<double[]> intervals) {
    double s = 0;
    for (double[] iv : intervals) {
      s += iv[1] - iv[0];
    }
    return s;
  }

  private static boolean isEnabledAt(List<double[]> intervals, double t) {
    for (double[] iv : intervals) {
      if (t >= iv[0] && t <= iv[1]) {
        return true;
      }
    }
    return false;
  }

  /** Length of [a, b] that lies inside the intervals. */
  static double overlap(List<double[]> intervals, double a, double b) {
    double s = 0;
    for (double[] iv : intervals) {
      double lo = Math.max(a, iv[0]);
      double hi = Math.min(b, iv[1]);
      if (hi > lo) {
        s += hi - lo;
      }
    }
    return s;
  }
}
