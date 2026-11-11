package frc.tools;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Queries over a {@link LogData}. Each method returns one line of compact JSON. */
public final class LogQuery {
  private LogQuery() {}

  /** Thrown when a query cannot be answered (missing key, unsupported type). */
  public static final class QueryException extends Exception {
    public QueryException(String message) {
      super(message);
    }
  }

  /** Lists signal names and types, optionally filtered by prefix, capped at limit entries. */
  public static String keys(LogData data, String prefix, int limit) {
    String p = prefix == null ? "" : (prefix.startsWith("/") ? prefix : "/" + prefix);
    int total = 0;
    StringBuilder sb = new StringBuilder("[");
    for (Map.Entry<String, LogData.Entry> e : data.entries.entrySet()) {
      if (!e.getKey().startsWith(p)) {
        continue;
      }
      if (total < limit) {
        if (total > 0) {
          sb.append(',');
        }
        sb.append("{\"name\":").append(Json.str(e.getKey()));
        sb.append(",\"type\":").append(Json.str(e.getValue().type)).append('}');
      }
      total++;
    }
    sb.append(']');
    return "{\"total\":" + total + ",\"shown\":" + Math.min(total, limit) + ",\"keys\":" + sb + "}";
  }

  /**
   * Count, min, max, mean and last of a numeric signal, optionally within a time window. The window
   * is in seconds from the start of the log.
   */
  public static String stats(LogData data, String key, Double from, Double to)
      throws QueryException {
    LogData.Entry e = numeric(data, key);
    int count = 0;
    double min = Double.POSITIVE_INFINITY;
    double max = Double.NEGATIVE_INFINITY;
    double sum = 0;
    double last = Double.NaN;
    for (int i = 0; i < e.values.size(); i++) {
      double t = e.times.get(i) - data.startSeconds;
      if ((from != null && t < from) || (to != null && t > to)) {
        continue;
      }
      double v = e.values.get(i);
      count++;
      min = Math.min(min, v);
      max = Math.max(max, v);
      sum += v;
      last = v;
    }
    String body =
        count == 0
            ? "\"min\":null,\"max\":null,\"mean\":null,\"last\":null"
            : "\"min\":"
                + Json.num(min)
                + ",\"max\":"
                + Json.num(max)
                + ",\"mean\":"
                + Json.num(sum / count)
                + ",\"last\":"
                + Json.num(last);
    return "{\"key\":"
        + Json.str(e.name)
        + ",\"type\":"
        + Json.str(e.type)
        + ",\"count\":"
        + count
        + ","
        + body
        + "}";
  }

  /**
   * Compares RealOutputs/key with ReplayOutputs/key sample by sample and reports the first
   * divergence, or that there is none.
   */
  public static String diff(LogData data, String key) throws QueryException {
    String k = key.startsWith("/") ? key.substring(1) : key;
    LogData.Entry real = data.find("RealOutputs/" + k);
    LogData.Entry replay = data.find("ReplayOutputs/" + k);
    if (real == null) {
      throw new QueryException("no signal RealOutputs/" + k + " in log");
    }
    if (replay == null) {
      throw new QueryException(
          "no signal ReplayOutputs/" + k + " in log (is this a replayed log, ending in _sim?)");
    }
    if (!real.isNumeric() || !replay.isNumeric()) {
      throw new QueryException("unsupported type: " + real.type);
    }
    int n = Math.min(real.values.size(), replay.values.size());
    for (int i = 0; i < n; i++) {
      double a = real.values.get(i);
      double b = replay.values.get(i);
      boolean sameTime = Math.abs(real.times.get(i) - replay.times.get(i)) < 1e-9;
      if (!sameTime || Math.abs(a - b) > 1e-9) {
        return "{\"key\":"
            + Json.str(k)
            + ",\"diverged\":true,\"index\":"
            + i
            + ",\"time\":"
            + Json.num(real.times.get(i))
            + ",\"real\":"
            + Json.num(a)
            + ",\"replay\":"
            + Json.num(b)
            + "}";
      }
    }
    if (real.values.size() != replay.values.size()) {
      return "{\"key\":"
          + Json.str(k)
          + ",\"diverged\":true,\"index\":"
          + n
          + ",\"realSamples\":"
          + real.values.size()
          + ",\"replaySamples\":"
          + replay.values.size()
          + "}";
    }
    return "{\"key\":" + Json.str(k) + ",\"diverged\":false,\"samples\":" + n + "}";
  }

  private static LogData.Entry numeric(LogData data, String key) throws QueryException {
    LogData.Entry e = data.find(key);
    if (e == null) {
      throw new QueryException("no signal named " + key);
    }
    if (!e.isNumeric()) {
      throw new QueryException("unsupported type: " + e.type);
    }
    return e;
  }

  /** Lists only the numeric key names in a list; used by tests. */
  static List<String> names(LogData data) {
    return List.copyOf(data.entries.keySet());
  }

  static String fmt(double d) {
    return String.format(Locale.ROOT, "%.6g", d);
  }
}
