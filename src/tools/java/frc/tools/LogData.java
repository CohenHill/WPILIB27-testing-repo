package frc.tools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.wpilib.datalog.DataLogReader;
import org.wpilib.datalog.DataLogRecord;

/** The contents of a .wpilog file: every entry's name, type and (for numeric types) samples. */
public final class LogData {
  /** One logged signal. Samples are kept only for numeric and boolean types. */
  public static final class Entry {
    public final String name;
    public final String type;
    public final List<Double> times = new ArrayList<>();
    public final List<Double> values = new ArrayList<>();

    /** Text samples, for string and string[] signals (a string is a one-element array). */
    public final List<String[]> texts = new ArrayList<>();

    public final List<Double> textTimes = new ArrayList<>();

    Entry(String name, String type) {
      this.name = name;
      this.type = type;
    }

    public boolean isNumeric() {
      return isNumericType(type);
    }

    public boolean isText() {
      return type.equals("string") || type.equals("string[]");
    }
  }

  /** Creates an empty entry. Used by tests. */
  static Entry newEntry(String name, String type) {
    return new Entry(name, type);
  }

  public static boolean isNumericType(String type) {
    return switch (type) {
      case "boolean", "int64", "float", "double" -> true;
      default -> false;
    };
  }

  /** Timestamp of the latest data record, in seconds. */
  public double endSeconds = Double.NaN;

  /** Timestamp of the earliest data record (not entry headers) in the log, in seconds. */
  public double startSeconds = Double.NaN;

  /** Entries by name, sorted. */
  public final Map<String, Entry> entries = new TreeMap<>();

  /**
   * Reads a log file. 2027 log timestamps are in nanoseconds. Throws IOException if it is missing
   * or not a valid wpilog.
   */
  public static LogData read(String path) throws IOException {
    DataLogReader reader = new DataLogReader(path);
    if (!reader.isValid()) {
      throw new IOException("not a valid wpilog file: " + path);
    }
    LogData data = new LogData();
    Map<Integer, Entry> byId = new HashMap<>();
    for (DataLogRecord record : reader) {
      double ts = record.getTimestamp() / 1e9;
      if (!record.isControl() && (Double.isNaN(data.startSeconds) || ts < data.startSeconds)) {
        data.startSeconds = ts;
      }
      if (!record.isControl() && (Double.isNaN(data.endSeconds) || ts > data.endSeconds)) {
        data.endSeconds = ts;
      }
      if (record.isStart()) {
        DataLogRecord.StartRecordData start = record.getStartData();
        Entry e = data.entries.computeIfAbsent(start.name, n -> new Entry(n, start.type));
        byId.put(start.entry, e);
      } else if (!record.isControl()) {
        Entry e = byId.get(record.getEntry());
        if (e != null && e.isText()) {
          try {
            e.texts.add(
                e.type.equals("string")
                    ? new String[] {record.getString()}
                    : record.getStringArray());
            e.textTimes.add(record.getTimestamp() / 1e9);
          } catch (RuntimeException ex) {
            // Skip malformed samples
          }
          continue;
        }
        if (e == null || !e.isNumeric()) {
          continue;
        }
        try {
          double v =
              switch (e.type) {
                case "boolean" -> record.getBoolean() ? 1.0 : 0.0;
                case "int64" -> (double) record.getInteger();
                case "float" -> (double) record.getFloat();
                default -> record.getDouble();
              };
          e.times.add(record.getTimestamp() / 1e9);
          e.values.add(v);
        } catch (RuntimeException ex) {
          // Skip malformed samples
        }
      }
    }
    return data;
  }

  /** Finds an entry by key, with or without a leading slash. */
  public Entry find(String key) {
    String k = key.startsWith("/") ? key : "/" + key;
    Entry e = entries.get(k);
    return e != null ? e : entries.get(key);
  }
}
