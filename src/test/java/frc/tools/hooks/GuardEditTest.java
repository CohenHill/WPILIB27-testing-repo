package frc.tools.hooks;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import org.junit.jupiter.api.Test;

class GuardEditTest {
  private static final String TEST_FILE = "src/test/java/frc/robot/RunModeTest.java";

  private static boolean edit(String file, String oldS, String newS, String agent) {
    return GuardEdit.check(
            Map.of("file_path", file, "old_string", oldS, "new_string", newS), agent, null)
        .isPresent();
  }

  private static boolean write(String file, String content, String existing) {
    return GuardEdit.check(Map.of("file_path", file, "content", content), null, existing)
        .isPresent();
  }

  @Test
  void blocksProtectedPaths() {
    assertTrue(edit("vendordeps/AdvantageKit.json", "a", "b", null));
    assertTrue(edit("/Users/x/repo/.wpilib/wpilib_preferences.json", "a", "b", null));
    assertTrue(edit("gradle/wrapper/gradle-wrapper.properties", "a", "b", null));
    assertTrue(edit("gradlew", "a", "b", null));
    assertTrue(write("vendordeps/New.json", "{}", null));
    assertFalse(edit("build.gradle", "a", "b", null));
  }

  @Test
  void blocksRemovingATest() {
    assertTrue(edit(TEST_FILE, "@Test\n void a() { assertEquals(1, 1); }", "", null));
  }

  @Test
  void blocksDisablingATest() {
    assertTrue(edit(TEST_FILE, "@Test\n void a() {}", "@Test @Disabled\n void a() {}", null));
  }

  @Test
  void blocksRemovingAssertions() {
    assertTrue(edit(TEST_FILE, "assertEquals(1, x);", "x++;", null));
  }

  @Test
  void blocksOverwritingATestWithFewerTests() {
    String existing = "@Test void a() { assertTrue(true); }\n@Test void b() { assertTrue(true); }";
    assertTrue(write(TEST_FILE, "@Test void a() { assertTrue(true); }", existing));
    assertFalse(write(TEST_FILE, existing + "\n@Test void c() { assertTrue(true); }", existing));
  }

  @Test
  void allowsAddingATest() {
    assertFalse(edit(TEST_FILE, "}\n", "@Test\n void c() { assertTrue(true); }\n}\n", null));
  }

  @Test
  void blocksLooseningScenarioCriteria() {
    String f = "src/main/java/frc/robot/sim/scenarios/ArmReachesGoal.java";
    assertTrue(edit(f, "MAX_LOOPS_TO_GOAL = 100", "MAX_LOOPS_TO_GOAL = 10000", null));
    assertTrue(edit(f, "took <= MAX", "true || took <= MAX", null));
    assertTrue(write(f, "class X {}", "class ArmReachesGoal {}"));
    assertFalse(edit(f, "// old comment", "// new comment", null));
    assertFalse(write("src/main/java/frc/robot/sim/scenarios/NewOne.java", "class N {}", null));
  }

  @Test
  void blocksRemovingCiSteps() {
    String f = ".github/workflows/build.yml";
    assertTrue(edit(f, "      - name: Run\n        run: ./frc sim X", "", null));
    assertTrue(edit(f, "run: ./frc sim X", "run: ./frc sim X\n    continue-on-error: true", null));
    assertFalse(edit(f, "run: a", "run: a\n      - run: b", null));
  }

  @Test
  void docsSyncMayOnlyEditDocs() {
    assertFalse(edit("docs/robot/arm.md", "a", "b", "docs-sync"));
    assertFalse(edit("AGENTS.md", "a", "b", "docs-sync"));
    assertTrue(edit("src/main/java/frc/robot/Robot.java", "a", "b", "docs-sync"));
    assertTrue(edit("src/test/java/frc/robot/ArmTest.java", "a", "b", "docs-sync"));
    assertFalse(edit("src/main/java/frc/robot/Robot.java", "a", "b", null));
  }

  @Test
  void blockReasonNamesTheFile() {
    String reason =
        GuardEdit.check(Map.of("file_path", "vendordeps/X.json", "content", "{}"), null, null)
            .orElseThrow();
    assertTrue(reason.contains("vendordeps/X.json") && reason.contains("protected"), reason);
  }
}
