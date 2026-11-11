package frc.tools.hooks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class HooksTest {
  /** A fake environment: canned files, env vars and command results. */
  private static final class FakeEnv implements Env {
    final Map<String, String> files;
    final Map<String, String> env;
    final Map<String, ExecResult> commands;
    final List<String> ran = new ArrayList<>();

    FakeEnv(Map<String, String> files, Map<String, String> env, Map<String, ExecResult> commands) {
      this.files = files;
      this.env = env;
      this.commands = commands;
    }

    @Override
    public String getenv(String name) {
      return env.get(name);
    }

    @Override
    public String readFile(String path) {
      return files.get(path);
    }

    @Override
    public List<String> listFiles(String dir) {
      return files.keySet().stream()
          .filter(k -> k.startsWith(dir + "/"))
          .map(k -> k.substring(dir.length() + 1))
          .toList();
    }

    @Override
    public ExecResult exec(List<String> command, int timeoutSeconds) {
      String key = String.join(" ", command);
      ran.add(key);
      return commands.getOrDefault(key, new ExecResult(0, ""));
    }

    @Override
    public String cwd() {
      return "/proj";
    }
  }

  private static FakeEnv env(Map<String, Env.ExecResult> commands) {
    return new FakeEnv(
        Map.of(
            "build.gradle",
                "plugins {\n    id \"org.wpilib.GradleRIO\" version \"2027.0.0-alpha-8\"\n}",
            "build/test-results/test/TEST-A.xml",
                "<testsuite name=\"A\" tests=\"6\" skipped=\"0\" failures=\"0\" errors=\"0\">",
            "build/test-results/test/TEST-B.xml",
                "<testsuite name=\"B\" tests=\"5\" skipped=\"0\" failures=\"1\" errors=\"0\">"),
        Map.of("FRC_AI_LEVEL", "3"),
        commands);
  }

  @Test
  void sessionStartPrintsVersionBranchLevelAndTests() {
    FakeEnv e =
        env(Map.of("git rev-parse --abbrev-ref HEAD", new Env.ExecResult(0, "feature/x\n")));
    Hooks.Result r = Hooks.run("session-start", "{}", e);
    assertEquals(0, r.exit());
    assertTrue(r.stdout().contains("WPILib 2027.0.0-alpha-8"), r.stdout());
    assertTrue(r.stdout().contains("Git branch: feature/x"), r.stdout());
    assertTrue(r.stdout().contains("AI level: 3"), r.stdout());
    assertTrue(r.stdout().contains("11 tests, 1 failing"), r.stdout());
  }

  @Test
  void sessionStartDefaultsLevelAndHandlesNoResults() {
    FakeEnv e = new FakeEnv(Map.of(), Map.of(), Map.of());
    Hooks.Result r = Hooks.run("session-start", "{}", e);
    assertTrue(r.stdout().contains("level: 1"), r.stdout());
    assertTrue(r.stdout().contains("no test results found"), r.stdout());
  }

  @Test
  void onStopDoesNothingWhenStopHookAlreadyActive() {
    FakeEnv e = env(Map.of());
    Hooks.Result r = Hooks.run("on-stop", "{\"stop_hook_active\": true}", e);
    assertEquals("", r.stdout());
    assertTrue(e.ran.isEmpty(), e.ran.toString());
  }

  @Test
  void onStopDoesNothingWhenSrcMainUnchanged() {
    FakeEnv e = env(Map.of());
    Hooks.Result r = Hooks.run("on-stop", "{\"stop_hook_active\": false}", e);
    assertEquals("", r.stdout());
    assertEquals(List.of("git status --porcelain -- src/main"), e.ran);
  }

  @Test
  void onStopReportsFailingTestsToClaude() {
    FakeEnv e =
        env(
            Map.of(
                "git status --porcelain -- src/main", new Env.ExecResult(0, " M src/main/X.java\n"),
                "./gradlew -q test", new Env.ExecResult(1, "RunModeTest FAILED\n")));
    Hooks.Result r = Hooks.run("on-stop", "{}", e);
    assertEquals(0, r.exit());
    assertTrue(r.stdout().contains("\"additionalContext\""), r.stdout());
    assertTrue(r.stdout().contains("FAILED"), r.stdout());
  }

  @Test
  void onStopReportsPassToTheUserOnly() {
    FakeEnv e =
        env(
            Map.of(
                "git status --porcelain -- src/main",
                new Env.ExecResult(0, "?? src/main/Y.java\n")));
    Hooks.Result r = Hooks.run("on-stop", "{}", e);
    assertTrue(r.stdout().contains("\"systemMessage\""), r.stdout());
    assertTrue(!r.stdout().contains("additionalContext"), r.stdout());
  }

  @Test
  void postEditIgnoresNonJavaFiles() {
    FakeEnv e = env(Map.of());
    Hooks.Result r = Hooks.run("post-edit", "{\"tool_input\":{\"file_path\":\"docs/x.md\"}}", e);
    assertEquals("", r.stdout());
    assertTrue(e.ran.isEmpty());
  }

  @Test
  void postEditReportsCompileFailureButNeverBlocks() {
    FakeEnv e =
        env(
            Map.of(
                "./gradlew -q spotlessApply compileJava",
                new Env.ExecResult(1, "Robot.java:3: error: ';' expected\n")));
    Hooks.Result r =
        Hooks.run(
            "post-edit",
            "{\"tool_input\":{\"file_path\":\"src/main/java/frc/robot/Robot.java\"}}",
            e);
    assertEquals(0, r.exit());
    assertTrue(r.stdout().contains("error: ';' expected"), r.stdout());
  }

  @Test
  void postEditReportsSuccess() {
    FakeEnv e = env(Map.of());
    Hooks.Result r =
        Hooks.run(
            "post-edit",
            "{\"tool_input\":{\"file_path\":\"src/main/java/frc/robot/Robot.java\"}}",
            e);
    assertTrue(r.stdout().contains("compiles"), r.stdout());
  }

  @Test
  void preCompactAsksToKeepPlanFilesAndFailRows() {
    Hooks.Result r = Hooks.run("pre-compact", "{}", env(Map.of()));
    assertTrue(r.stdout().contains("plan") && r.stdout().contains("FAIL"), r.stdout());
  }

  @Test
  void guardsReadHookJsonFromStdin() {
    String deploy =
        "{\"hook_event_name\":\"PreToolUse\",\"tool_name\":\"Bash\",\"tool_input\":{\"command\":\"./gradlew deploy\"}}";
    Hooks.Result r = Hooks.run("guard-shell", deploy, env(Map.of()));
    assertEquals(2, r.exit());
    assertTrue(r.stderr().startsWith("Blocked:"), r.stderr());

    String delTest =
        "{\"tool_name\":\"Bash\",\"tool_input\":{\"command\":\"rm src/test/java/frc/robot/RunModeTest.java\"}}";
    assertEquals(2, Hooks.run("guard-shell", delTest, env(Map.of())).exit());

    String ok = "{\"tool_name\":\"Bash\",\"tool_input\":{\"command\":\"./gradlew build\"}}";
    assertEquals(0, Hooks.run("guard-shell", ok, env(Map.of())).exit());
  }

  @Test
  void guardEditUsesTheExistingFileOnDisk() {
    FakeEnv e =
        new FakeEnv(
            Map.of(
                "src/test/java/frc/robot/RunModeTest.java",
                "@Test void a() { assertTrue(true); }\n@Test void b() { assertTrue(true); }"),
            Map.of(),
            Map.of());
    String input =
        "{\"tool_name\":\"Write\",\"tool_input\":{\"file_path\":\"src/test/java/frc/robot/RunModeTest.java\",\"content\":\"@Test void a() { assertTrue(true); }\"}}";
    Hooks.Result r = Hooks.run("guard-edit", input, e);
    assertEquals(2, r.exit());
    assertTrue(r.stderr().contains("removes a test"), r.stderr());
  }

  @Test
  void unknownHookIsANonBlockingError() {
    Hooks.Result r = Hooks.run("nope", "{}", env(Map.of()));
    assertEquals(1, r.exit());
  }

  @Test
  void malformedInputDoesNotCrashOrBlock() {
    assertEquals(0, Hooks.run("guard-shell", "not json", env(Map.of())).exit());
    assertEquals(0, Hooks.run("guard-edit", "", env(Map.of())).exit());
  }
}
