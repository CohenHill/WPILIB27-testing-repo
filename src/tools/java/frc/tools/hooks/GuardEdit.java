package frc.tools.hooks;

import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Decides whether a file edit may happen. Returns a block reason, or empty to allow. */
public final class GuardEdit {
  private GuardEdit() {}

  private static final Pattern CRITERIA =
      Pattern.compile(
          "[<>]=?|==|!=|Math\\.abs|TOL|MAX_|MIN_|LIMIT|durationLoops|expected|\\bpass\\b");

  /**
   * Checks an Edit or Write.
   *
   * @param toolInput the tool_input object from the hook input
   * @param agentType the subagent type, or null for the main agent
   * @param existing the current content of the file, or null if it does not exist
   */
  public static Optional<String> check(
      Map<String, Object> toolInput, String agentType, String existing) {
    String file = str(toolInput.get("file_path"));
    if (file == null || file.isBlank()) {
      return Optional.empty();
    }
    String p = "/" + file.replace('\\', '/');

    if (p.contains("/vendordeps/")
        || p.contains("/.wpilib/")
        || p.contains("/gradle/")
        || p.endsWith("/gradlew")
        || p.endsWith("/gradlew.bat")) {
      return Optional.of(
          "Blocked: "
              + file
              + " is a protected path (vendordeps, .wpilib, gradle, gradlew). A human edits it.");
    }

    if ("docs-sync".equals(agentType) && !(p.contains("/docs/") || p.endsWith("/AGENTS.md"))) {
      return Optional.of("Blocked: docs-sync may only edit files under docs/ and AGENTS.md.");
    }

    String oldText = str(toolInput.get("old_string"));
    String newText = str(toolInput.get("new_string"));
    String content = str(toolInput.get("content"));
    boolean isEdit = oldText != null && newText != null;
    boolean isWrite = content != null;
    if (!isEdit && !isWrite) {
      return Optional.empty();
    }
    String before = isEdit ? oldText : existing;
    String after = isEdit ? newText : content;

    if (p.contains("/src/test/") && before != null) {
      if (count(after, "@Test") < count(before, "@Test")) {
        return Optional.of("Blocked: this edit removes a test (" + file + "). Never delete tests.");
      }
      if (count(after, "@Disabled") > count(before, "@Disabled")
          || count(after, "assumeTrue(false)") > count(before, "assumeTrue(false)")) {
        return Optional.of("Blocked: this edit disables a test (" + file + ").");
      }
      if (count(after, "assert") < count(before, "assert")) {
        return Optional.of("Blocked: this edit removes assertions from " + file + ".");
      }
    }

    boolean scenarioFile =
        p.contains("/frc/robot/sim/scenarios/") || p.endsWith("/frc/robot/sim/Check.java");
    if (scenarioFile && before != null) {
      if (isWrite) {
        return Optional.of(
            "Blocked: overwriting an existing scenario file needs a human ("
                + file
                + "). Edit it in small steps or ask.");
      }
      if (CRITERIA.matcher(before).find() || CRITERIA.matcher(after).find()) {
        return Optional.of(
            "Blocked: this edit changes scenario pass criteria in "
                + file
                + ". Loosening a check needs a human decision.");
      }
    }

    if (p.contains("/.github/workflows/") && before != null) {
      if (count(after, "run:") + count(after, "uses:")
          < count(before, "run:") + count(before, "uses:")) {
        return Optional.of("Blocked: this edit removes a CI step from " + file + ".");
      }
      if (count(after, "continue-on-error: true") > count(before, "continue-on-error: true")
          || count(after, "if: false") > count(before, "if: false")) {
        return Optional.of("Blocked: this edit weakens or disables a CI step in " + file + ".");
      }
    }
    return Optional.empty();
  }

  static int count(String text, String needle) {
    if (text == null) {
      return 0;
    }
    int n = 0;
    Matcher m = Pattern.compile(Pattern.quote(needle)).matcher(text);
    while (m.find()) {
      n++;
    }
    return n;
  }

  private static String str(Object o) {
    return o instanceof String s ? s : null;
  }
}
