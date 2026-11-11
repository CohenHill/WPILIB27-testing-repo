package frc.robot.sim;

import frc.robot.Robot;
import frc.robot.SimHook;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import org.littletonrobotics.junction.LoggedOpModeRobot;
import org.littletonrobotics.junction.Logger;
import org.wpilib.hardware.hal.OpModeOption;
import org.wpilib.simulation.DriverStationSim;

/**
 * Runs one scenario headless, then exits the program with 0 (pass) or 1 (fail). Active only in SIM
 * mode when the SIM_SCENARIO environment variable names a scenario.
 */
public final class SimDriver implements SimHook {
  private static final double PERIOD_SEC = LoggedOpModeRobot.DEFAULT_PERIOD;

  /** Loops ignored for overrun counting while the JVM warms up. */
  private static final int WARMUP_LOOPS = 25;

  /** A loop counts as an overrun above this many periods, as in LoggedOpModeRobot's watchdog. */
  private static final double OVERRUN_PERIODS = 4.0;

  private Scenario scenario;
  private List<Check> checks;
  private int loop = 0;
  private int overruns = 0;
  private int firstOverrunLoop = -1;
  private double maxGapMs = 0;
  private long lastLoopNs = 0;

  /** Required by ServiceLoader. */
  public SimDriver() {}

  /**
   * Selects the scenario's OpMode and enables the robot. Call once from the Robot constructor,
   * before the first loop. Setting the Driver Station state before the loop starts keeps the logged
   * state and the robot's view of it identical from the first cycle, so replay matches. Changing it
   * mid-run would make the log lead the robot by one cycle.
   *
   * @return true if SIM_SCENARIO names a scenario (and the robot was enabled for it)
   */
  @Override
  public boolean begin(Robot robot) {
    String name = System.getenv("SIM_SCENARIO");
    if (name == null || name.isBlank()) {
      return false;
    }
    scenario = Scenarios.find(name).orElse(null);
    if (scenario == null) {
      System.err.println("[SimDriver] Unknown scenario: " + name);
      System.exit(2);
    }
    checks = scenario.checks(robot);
    OpModeOption option =
        Arrays.stream(DriverStationSim.getOpModeOptions())
            .filter(o -> o.name.equals(scenario.opMode()))
            .findFirst()
            .orElse(null);
    if (option == null) {
      System.err.println("[SimDriver] No OpMode named " + scenario.opMode());
      System.exit(2);
    }
    DriverStationSim.setDsAttached(true);
    DriverStationSim.setRobotMode(option.getMode());
    DriverStationSim.setOpMode(option.id);
    DriverStationSim.setEnabled(true);
    DriverStationSim.notifyNewData();
    return true;
  }

  /** Call once per loop from simulationPeriodic(). */
  @Override
  public void update() {
    long now = System.nanoTime();
    if (loop >= WARMUP_LOOPS && lastLoopNs != 0) {
      double gapSec = (now - lastLoopNs) / 1e9;
      maxGapMs = Math.max(maxGapMs, gapSec * 1000.0);
      if (gapSec > OVERRUN_PERIODS * PERIOD_SEC) {
        overruns++;
        if (firstOverrunLoop < 0) {
          firstOverrunLoop = loop;
        }
      }
    }
    lastLoopNs = now;

    checks.forEach(c -> c.sample(loop));
    loop++;
    if (loop >= scenario.durationLoops()) {
      finish();
    }
  }

  private void finish() {
    DriverStationSim.setEnabled(false);
    DriverStationSim.notifyNewData();

    Check.Stats stats = new Check.Stats(loop, overruns, firstOverrunLoop, maxGapMs);
    List<Check.Result> results = new ArrayList<>();
    for (Check c : checks) {
      results.add(c.result(stats));
    }
    boolean pass = results.stream().allMatch(Check.Result::pass);

    Logger.end();
    String logPath = newestLog();
    writeSummary(pass, results, stats, logPath);
    System.out.println("[SimDriver] " + scenario.name() + (pass ? " PASS" : " FAIL"));
    System.exit(pass ? 0 : 1);
  }

  private static String newestLog() {
    File[] files = new File("logs").listFiles((d, n) -> n.endsWith(".wpilog"));
    if (files == null || files.length == 0) {
      return "";
    }
    return Arrays.stream(files)
        .max(Comparator.comparingLong(File::lastModified))
        .map(File::getPath)
        .orElse("");
  }

  private void writeSummary(
      boolean pass, List<Check.Result> results, Check.Stats stats, String logPath) {
    StringBuilder sb = new StringBuilder();
    sb.append("{\"scenario\":").append(json(scenario.name()));
    sb.append(",\"pass\":").append(pass);
    sb.append(",\"checks\":[");
    for (int i = 0; i < results.size(); i++) {
      Check.Result r = results.get(i);
      if (i > 0) {
        sb.append(',');
      }
      sb.append("{\"name\":").append(json(r.name()));
      sb.append(",\"pass\":").append(r.pass());
      sb.append(",\"observed\":").append(json(r.observed()));
      sb.append(",\"expected\":").append(json(r.expected())).append('}');
    }
    sb.append("],\"loops\":").append(stats.loops());
    sb.append(",\"loopOverruns\":").append(stats.overruns());
    sb.append(",\"maxLoopGapMs\":").append(Math.round(stats.maxGapMs() * 10) / 10.0);
    sb.append(",\"log\":").append(json(logPath)).append("}\n");
    try {
      Path dir = Path.of("build", "sim", scenario.name());
      Files.createDirectories(dir);
      Files.writeString(dir.resolve("summary.json"), sb.toString());
    } catch (IOException e) {
      System.err.println("[SimDriver] Could not write summary: " + e);
    }
  }

  private static String json(String s) {
    StringBuilder sb = new StringBuilder("\"");
    for (char c : s.toCharArray()) {
      switch (c) {
        case '"' -> sb.append("\\\"");
        case '\\' -> sb.append("\\\\");
        case '\n' -> sb.append("\\n");
        default -> sb.append(c < 0x20 ? " " : String.valueOf(c));
      }
    }
    return sb.append('"').toString();
  }
}
