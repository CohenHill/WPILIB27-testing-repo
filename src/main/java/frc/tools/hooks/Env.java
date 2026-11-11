package frc.tools.hooks;

import java.util.List;

/** Everything a hook needs from the outside world, so hooks can be tested without it. */
public interface Env {
  /** Result of running a process. */
  record ExecResult(int exit, String output) {}

  /** Environment variable, or null. */
  String getenv(String name);

  /** File contents relative to the project directory (or absolute), or null if missing. */
  String readFile(String path);

  /** File names in a directory relative to the project directory; empty if missing. */
  List<String> listFiles(String dir);

  /** Runs a command in the project directory. */
  ExecResult exec(List<String> command, int timeoutSeconds);

  /** The project directory. */
  String cwd();
}
