package frc.tools.hooks;

import frc.robot.util.MiniJson;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** The {@code frc hook <name>} subcommands. Each reads hook JSON and returns a result. */
public final class Hooks {
  private Hooks() {}

  /** What a hook wants to do: exit code, standard output, standard error. */
  public record Result(int exit, String stdout, String stderr) {
    static Result ok() {
      return new Result(0, "", "");
    }

    static Result out(String text) {
      return new Result(0, text, "");
    }

    static Result block(String reason) {
      return new Result(2, "", reason);
    }
  }

  public static Result run(String name, String stdin, Env env) {
    Map<String, Object> in = MiniJson.parseObject(stdin);
    return switch (name) {
      case "guard-shell" -> guardShell(in);
      case "guard-edit" -> guardEdit(in, env);
      case "session-start" -> sessionStart(env);
      case "post-edit" -> postEdit(in, env);
      case "on-stop" -> onStop(in, env);
      case "pre-compact" -> preCompact();
      default -> new Result(1, "", "frc hook: unknown hook '" + name + "'");
    };
  }

  // ---- guards ----

  static Result guardShell(Map<String, Object> in) {
    Map<String, Object> tool = map(in.get("tool_input"));
    Optional<String> block = GuardShell.check(str(tool.get("command")), str(in.get("agent_type")));
    return block.map(Result::block).orElse(Result.ok());
  }

  static Result guardEdit(Map<String, Object> in, Env env) {
    Map<String, Object> tool = map(in.get("tool_input"));
    String file = str(tool.get("file_path"));
    String existing = file == null ? null : env.readFile(file);
    Optional<String> block = GuardEdit.check(tool, str(in.get("agent_type")), existing);
    return block.map(Result::block).orElse(Result.ok());
  }

  // ---- reporting ----

  static Result sessionStart(Env env) {
    String gradle = env.readFile("build.gradle");
    String wpilib = "unknown";
    if (gradle != null) {
      Matcher m =
          Pattern.compile("org\\.wpilib\\.GradleRIO\"\\s+version\\s+\"([^\"]+)\"").matcher(gradle);
      if (m.find()) {
        wpilib = m.group(1);
      }
    }
    Env.ExecResult branch = env.exec(List.of("git", "rev-parse", "--abbrev-ref", "HEAD"), 10);
    String branchName = branch.exit() == 0 ? branch.output().trim() : "unknown";
    String level = env.getenv("FRC_AI_LEVEL");
    String levelText =
        level == null || level.isBlank()
            ? "1 (FRC_AI_LEVEL is not set; see docs/ai-levels.md)"
            : level.trim();
    return Result.out(
        "Project facts: WPILib "
            + wpilib
            + ". Git branch: "
            + branchName
            + ". Student AI level: "
            + levelText
            + ". Last test result: "
            + lastTests(env)
            + ".\n");
  }

  static String lastTests(Env env) {
    int tests = 0;
    int failures = 0;
    int files = 0;
    Pattern attr =
        Pattern.compile("tests=\"(\\d+)\"[^>]*?failures=\"(\\d+)\"(?:[^>]*?errors=\"(\\d+)\")?");
    for (String f : env.listFiles("build/test-results/test")) {
      if (!f.endsWith(".xml")) {
        continue;
      }
      String xml = env.readFile("build/test-results/test/" + f);
      if (xml == null) {
        continue;
      }
      Matcher m = attr.matcher(xml);
      if (m.find()) {
        files++;
        tests += Integer.parseInt(m.group(1));
        failures += Integer.parseInt(m.group(2));
        if (m.group(3) != null) {
          failures += Integer.parseInt(m.group(3));
        }
      }
    }
    if (files == 0) {
      return "no test results found (run ./gradlew test)";
    }
    return tests + " tests, " + failures + " failing (from build/test-results)";
  }

  static Result postEdit(Map<String, Object> in, Env env) {
    Map<String, Object> tool = map(in.get("tool_input"));
    String file = str(tool.get("file_path"));
    if (file == null || !file.endsWith(".java")) {
      return Result.ok();
    }
    Env.ExecResult r = env.exec(List.of("./gradlew", "-q", "spotlessApply", "compileJava"), 170);
    String message =
        r.exit() == 0
            ? "After editing " + file + ": Spotless applied and the project compiles."
            : "After editing "
                + file
                + ": Spotless or compile failed (exit "
                + r.exit()
                + ").\n"
                + firstLines(r.output(), 25);
    return Result.out(
        "{\"hookSpecificOutput\":{\"hookEventName\":\"PostToolUse\",\"additionalContext\":"
            + quote(message)
            + "}}\n");
  }

  static Result onStop(Map<String, Object> in, Env env) {
    if (Boolean.TRUE.equals(in.get("stop_hook_active"))) {
      return Result.ok(); // report once; never loop
    }
    Env.ExecResult changed =
        env.exec(List.of("git", "status", "--porcelain", "--", "src/main"), 15);
    if (changed.exit() != 0 || changed.output().isBlank()) {
      return Result.ok();
    }
    Env.ExecResult tests = env.exec(List.of("./gradlew", "-q", "test"), 280);
    String summary = lastTests(env);
    if (tests.exit() == 0) {
      return Result.out(
          "{\"systemMessage\":"
              + quote("Tests after src/main changes: PASS (" + summary + ")")
              + "}\n");
    }
    String message =
        "Tests after your src/main changes FAILED ("
            + summary
            + "). Fix them before finishing.\n"
            + firstLines(tests.output(), 25);
    return Result.out(
        "{\"hookSpecificOutput\":{\"hookEventName\":\"Stop\",\"additionalContext\":"
            + quote(message)
            + "}}\n");
  }

  static Result preCompact() {
    return Result.out(
        "When summarizing, keep: the current plan and its checklist, the list of files read and"
            + " changed, the commands run with their key results, and every open FAIL or"
            + " BLOCKED row from verification.\n");
  }

  // ---- helpers ----

  @SuppressWarnings("unchecked")
  private static Map<String, Object> map(Object o) {
    return o instanceof Map<?, ?> m ? (Map<String, Object>) m : Map.of();
  }

  private static String str(Object o) {
    return o instanceof String s ? s : null;
  }

  private static String firstLines(String text, int n) {
    String[] lines = text.split("\\R");
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < Math.min(n, lines.length); i++) {
      sb.append(lines[i]).append('\n');
    }
    return sb.toString().trim();
  }

  static String quote(String s) {
    StringBuilder sb = new StringBuilder("\"");
    for (char c : s.toCharArray()) {
      switch (c) {
        case '"' -> sb.append("\\\"");
        case '\\' -> sb.append("\\\\");
        case '\n' -> sb.append("\\n");
        case '\r' -> sb.append("\\r");
        case '\t' -> sb.append("\\t");
        default -> sb.append(c < 0x20 ? " " : String.valueOf(c));
      }
    }
    return sb.append('"').toString();
  }
}
