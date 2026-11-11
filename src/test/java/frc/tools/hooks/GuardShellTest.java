package frc.tools.hooks;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class GuardShellTest {
  private static boolean blocked(String command) {
    return GuardShell.check(command, null).isPresent();
  }

  @Test
  void blocksDeploy() {
    assertTrue(blocked("./gradlew deploy"));
    assertTrue(blocked("./gradlew eventDeploy"));
    assertTrue(blocked("gradlew.bat deploy"));
    assertTrue(blocked("./gradlew :deploy"));
    assertTrue(blocked("./gradlew build && ./gradlew deploy"));
    assertTrue(blocked("JAVA_HOME=/x ./gradlew -q deploy"));
  }

  @Test
  void blocksPush() {
    assertTrue(blocked("git push"));
    assertTrue(blocked("git push origin main"));
    assertTrue(blocked("git -C repo push --force"));
    assertTrue(blocked("echo hi; git push"));
  }

  @Test
  void blocksRecursiveDelete() {
    assertTrue(blocked("rm -rf build"));
    assertTrue(blocked("rm -r logs"));
    assertTrue(blocked("rm -fr logs"));
    assertTrue(blocked("rm --recursive logs"));
    assertTrue(blocked("Remove-Item -Recurse logs"));
    assertTrue(blocked("find . -name '*.class' -delete"));
  }

  @Test
  void blocksTestFiltersThatDeselect() {
    assertTrue(blocked("./gradlew build -x test"));
    assertTrue(blocked("./gradlew build --exclude-task test"));
    assertTrue(blocked("./gradlew build -x check"));
    assertTrue(blocked("./gradlew build -PexcludeTests=true"));
  }

  @Test
  void blocksDeletingOrMovingTestsScenariosAndCi() {
    assertTrue(blocked("rm src/test/java/frc/robot/RunModeTest.java"));
    assertTrue(blocked("git rm src/test/java/frc/robot/RunModeTest.java"));
    assertTrue(blocked("mv src/test/java/frc/robot/RunModeTest.java /tmp/"));
    assertTrue(blocked("rm src/main/java/frc/robot/sim/scenarios/ArmReachesGoal.java"));
    assertTrue(blocked("rm .github/workflows/build.yml"));
    assertTrue(blocked("del src\\test\\java\\frc\\robot\\RunModeTest.java"));
  }

  @Test
  void allowsNormalWork() {
    assertFalse(blocked("./gradlew build"));
    assertFalse(blocked("./gradlew test --tests frc.robot.RunModeTest"));
    assertFalse(blocked("./frc sim ArmReachesGoal"));
    assertFalse(blocked("git status && git diff"));
    assertFalse(blocked("git commit -m 'deploy docs'"));
    assertFalse(blocked("rm build/frc-sim.log"));
    assertFalse(blocked("echo deploy"));
    assertFalse(blocked("ls src/test/java"));
  }

  @Test
  void logAnalystMayOnlyRunFrcLogAndReplay() {
    assertFalse(GuardShell.check("./frc log keys x.wpilog Arm", "log-analyst").isPresent());
    assertFalse(GuardShell.check("./frc replay x.wpilog", "log-analyst").isPresent());
    assertTrue(GuardShell.check("ls", "log-analyst").isPresent());
    assertTrue(GuardShell.check("./gradlew build", "log-analyst").isPresent());
    assertTrue(GuardShell.check("./frc test", "log-analyst").isPresent());
    assertTrue(GuardShell.check("./frc log keys x; ls", "log-analyst").isPresent());
    assertFalse(GuardShell.check("ls", "verifier").isPresent());
  }

  @Test
  void blockReasonIsClear() {
    String reason = GuardShell.check("./gradlew deploy", null).orElseThrow();
    assertTrue(reason.startsWith("Blocked:") && reason.contains("human"), reason);
  }
}
