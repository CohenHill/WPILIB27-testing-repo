package frc.tools;

import java.io.IOException;
import java.util.Arrays;

/** Entry point for {@code frc log ...}. JSON goes to stdout; errors go to stderr, exit non-zero. */
public final class Frc {
  private Frc() {}

  public static void main(String[] args) {
    if (args.length > 0 && args[0].equals("hook")) {
      System.exit(frc.tools.hooks.HookMain.run(java.util.Arrays.copyOfRange(args, 1, args.length)));
    }
    System.exit(run(args));
  }

  /** Runs one log command and returns the process exit code. */
  static int run(String[] args) {
    try {
      // The wrapper passes "log" first when called directly; Gradle passes only the rest.
      String[] rest =
          args.length > 0 && args[0].equals("log")
              ? Arrays.copyOfRange(args, 1, args.length)
              : args;
      FrcArgs a = FrcArgs.parse(rest);
      LogData data = LogData.read(a.log());
      String out =
          switch (a.command()) {
            case "keys" -> LogQuery.keys(data, a.key(), a.limit());
            case "stats" -> LogQuery.stats(data, a.key(), a.from(), a.to());
            default -> LogQuery.diff(data, a.key());
          };
      System.out.println(out);
      return 0;
    } catch (FrcArgs.UsageException e) {
      System.err.println("frc: " + e.getMessage());
      return 2;
    } catch (LogQuery.QueryException e) {
      System.err.println("frc: " + e.getMessage());
      return 3;
    } catch (IOException e) {
      System.err.println("frc: " + e.getMessage());
      return 2;
    }
  }
}
