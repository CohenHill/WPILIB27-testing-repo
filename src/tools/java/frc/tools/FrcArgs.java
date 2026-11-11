package frc.tools;

import java.util.ArrayList;
import java.util.List;

/** Parses the arguments of {@code frc log ...}. Pure code: no files, no simulation. */
public record FrcArgs(String command, String log, String key, Double from, Double to, int limit) {
  /** Default cap on listed items so output stays short. */
  public static final int DEFAULT_LIMIT = 50;

  /** Thrown for bad arguments. The message is meant for the user. */
  public static final class UsageException extends Exception {
    public UsageException(String message) {
      super(message);
    }
  }

  /**
   * Parses arguments after "log": {@code keys <log> [prefix]}, {@code stats <log> <key> [--from s]
   * [--to s]}, {@code diff <log> <key>}, each with an optional {@code --limit n}.
   */
  public static FrcArgs parse(String[] args) throws UsageException {
    if (args.length == 0) {
      throw new UsageException("expected one of: keys, stats, diff");
    }
    String command = args[0];
    if (!List.of("keys", "stats", "diff", "events", "track", "driver-report", "pull")
        .contains(command)) {
      throw new UsageException("unknown log command: " + command);
    }

    List<String> positional = new ArrayList<>();
    Double from = null;
    Double to = null;
    int limit = DEFAULT_LIMIT;
    for (int i = 1; i < args.length; i++) {
      String a = args[i];
      switch (a) {
        case "--from", "--to", "--limit" -> {
          if (i + 1 >= args.length) {
            throw new UsageException(a + " needs a value");
          }
          String value = args[++i];
          try {
            if (a.equals("--from")) {
              from = Double.parseDouble(value);
            } else if (a.equals("--to")) {
              to = Double.parseDouble(value);
            } else {
              limit = Integer.parseInt(value);
            }
          } catch (NumberFormatException e) {
            throw new UsageException(a + " needs a number, got: " + value);
          }
          if (a.equals("--limit") && limit < 1) {
            throw new UsageException("--limit must be at least 1");
          }
        }
        default -> {
          if (a.startsWith("--")) {
            throw new UsageException("unknown option: " + a);
          }
          positional.add(a);
        }
      }
    }
    if ((from != null || to != null) && !command.equals("stats")) {
      throw new UsageException("--from and --to only apply to stats");
    }

    int min =
        switch (command) {
          case "pull" -> 0;
          case "keys", "events", "driver-report" -> 1;
          default -> 2;
        };
    int max =
        switch (command) {
          case "pull" -> 0;
          case "events", "driver-report" -> 1;
          default -> 2;
        };
    if (positional.size() < min || positional.size() > max) {
      throw new UsageException(
          switch (command) {
            case "keys" -> "usage: log keys <log> [prefix]";
            case "stats" -> "usage: log stats <log> <key> [--from s] [--to s]";
            case "events" -> "usage: log events <log> [--limit n]";
            case "track" -> "usage: log track <log> <mechanism>";
            case "driver-report" -> "usage: log driver-report <log>";
            case "pull" -> "usage: log pull";
            default -> "usage: log diff <log> <key>";
          });
    }
    return new FrcArgs(
        command,
        positional.isEmpty() ? null : positional.get(0),
        positional.size() > 1 ? positional.get(1) : null,
        from,
        to,
        limit);
  }
}
