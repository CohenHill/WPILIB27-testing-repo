package frc.tools.hooks;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

/** Decides whether a shell command may run. Returns a block reason, or empty to allow. */
public final class GuardShell {
  private GuardShell() {}

  private static final Pattern SEGMENT_SPLIT = Pattern.compile("&&|\\|\\||;|\\||\\r?\\n");
  private static final Pattern ENV_ASSIGN = Pattern.compile("^[A-Za-z_][A-Za-z0-9_]*=.*");
  private static final Pattern RECURSIVE_FLAG = Pattern.compile("^-[A-Za-z]*[rR][A-Za-z]*$");
  private static final List<String> WRAPPERS = List.of("env", "sudo", "command", "nohup", "time");

  /**
   * Checks a command line.
   *
   * @param command the shell command
   * @param agentType the subagent type that issued it, or null for the main agent
   */
  public static Optional<String> check(String command, String agentType) {
    if (command == null) {
      return Optional.empty();
    }
    for (String segment : SEGMENT_SPLIT.split(command)) {
      List<String> t = tokens(segment);
      if (t.isEmpty()) {
        continue;
      }
      if ("log-analyst".equals(agentType)) {
        Optional<String> r = logAnalystRule(t);
        if (r.isPresent()) {
          return r;
        }
      }
      Optional<String> r = generalRules(t);
      if (r.isPresent()) {
        return r;
      }
    }
    return Optional.empty();
  }

  private static Optional<String> logAnalystRule(List<String> t) {
    String cmd = base(t.get(0));
    boolean frc = cmd.equals("frc") || cmd.equals("frc.cmd");
    if (frc && t.size() >= 2 && (t.get(1).equals("log") || t.get(1).equals("replay"))) {
      return Optional.empty();
    }
    return Optional.of(
        "Blocked: the log-analyst may only run `frc log ...` or `frc replay ...`. Got: "
            + String.join(" ", t));
  }

  private static Optional<String> generalRules(List<String> t) {
    String cmd = base(t.get(0));
    List<String> lower = t.stream().map(x -> x.toLowerCase(Locale.ROOT)).toList();

    boolean gradle = cmd.equals("gradlew") || cmd.equals("gradlew.bat") || cmd.equals("gradle");
    if (gradle) {
      for (String a : lower) {
        if (a.equals("deploy")
            || a.equals("eventdeploy")
            || a.endsWith(":deploy")
            || a.endsWith(":eventdeploy")) {
          return Optional.of(
              "Blocked: deploying to a robot is a human action. A person runs `deploy`.");
        }
      }
      for (int i = 0; i < lower.size(); i++) {
        String a = lower.get(i);
        if ((a.equals("-x") || a.equals("--exclude-task")) && i + 1 < lower.size()) {
          String target = lower.get(i + 1);
          if (target.contains("test") || target.contains("check")) {
            return Optional.of(
                "Blocked: excluding the test or check tasks deselects tests. Run the tests.");
          }
        }
        if (a.contains("excludetests") || a.contains("exclude-tests")) {
          return Optional.of("Blocked: a test filter that deselects tests is not allowed.");
        }
      }
    }

    if (cmd.equals("git") && lower.contains("push")) {
      return Optional.of("Blocked: pushing is a human action. A person runs `git push`.");
    }

    boolean deleter =
        List.of("rm", "rmdir", "unlink", "del", "erase", "remove-item", "mv", "move", "move-item")
            .contains(cmd.replace(".exe", ""));
    boolean gitRemover = cmd.equals("git") && (lower.contains("rm") || lower.contains("mv"));

    if (cmd.equals("rm") || cmd.equals("rmdir")) {
      for (String a : t.subList(1, t.size())) {
        if (RECURSIVE_FLAG.matcher(a).matches() || a.equals("--recursive")) {
          return Optional.of("Blocked: recursive delete is not allowed.");
        }
      }
    }
    if (cmd.equals("remove-item") && lower.stream().anyMatch(a -> a.startsWith("-recurse"))) {
      return Optional.of("Blocked: recursive delete is not allowed.");
    }
    if (cmd.equals("find") && lower.contains("-delete")) {
      return Optional.of("Blocked: `find -delete` is not allowed.");
    }

    if (deleter || gitRemover) {
      for (String a : t.subList(1, t.size())) {
        String p = a.replace('\\', '/');
        if (p.contains("src/test/")
            || p.contains("/sim/scenarios/")
            || p.contains(".github/workflows/")) {
          return Optional.of(
              "Blocked: deleting or moving tests, scenarios or CI files is not allowed ("
                  + a
                  + "). Ask a human.");
        }
      }
    }
    return Optional.empty();
  }

  private static String base(String token) {
    String p = token.replace('\\', '/');
    String name = p.substring(p.lastIndexOf('/') + 1);
    return name.toLowerCase(Locale.ROOT);
  }

  /** Splits a segment into tokens, drops quotes, env assignments and wrapper commands. */
  static List<String> tokens(String segment) {
    List<String> out = new ArrayList<>();
    StringBuilder cur = new StringBuilder();
    char quote = 0;
    boolean any = false;
    for (char c : segment.toCharArray()) {
      if (quote != 0) {
        if (c == quote) {
          quote = 0;
        } else {
          cur.append(c);
        }
      } else if (c == '"' || c == '\'') {
        quote = c;
        any = true;
      } else if (Character.isWhitespace(c)) {
        if (cur.length() > 0 || any) {
          out.add(cur.toString());
          cur.setLength(0);
          any = false;
        }
      } else {
        cur.append(c);
      }
    }
    if (cur.length() > 0 || any) {
      out.add(cur.toString());
    }
    int start = 0;
    while (start < out.size()
        && (ENV_ASSIGN.matcher(out.get(start)).matches()
            || WRAPPERS.contains(out.get(start).toLowerCase(Locale.ROOT)))) {
      start++;
    }
    return new ArrayList<>(out.subList(start, out.size()));
  }
}
