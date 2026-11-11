package frc.robot.sim;

import frc.robot.Robot;
import frc.robot.RunMode;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.littletonrobotics.junction.LoggedOpModeRobot;
import org.littletonrobotics.junction.Logger;
import org.wpilib.hardware.hal.OpModeOption;
import org.wpilib.simulation.DriverStationSim;

/**
 * Runs one scenario headless, then exits the program with 0 (pass) or 1 (fail). Active only in SIM
 * mode when the SIM_SCENARIO environment variable names a scenario.
 */
public final class SimDriver {
  private static final double PERIOD_SEC = LoggedOpModeRobot.DEFAULT_PERIOD;

  /** Loops ignored for overrun counting while the JVM warms up. */
  private static final int WARMUP_LOOPS = 10;

  private final Scenario scenario;
  private final List<Check> checks;
  private int loop = 0;
  private boolean started = false;
  private int overruns = 0;
  private long lastLoopNs = 0;

  private SimDriver(Scenario scenario, Robot robot) {
    this.scenario = scenario;
    this.checks = scenario.checks(robot);
  }

  /** Returns a driver if SIM mode and SIM_SCENARIO is set; otherwise empty. */
  public static Optional<SimDriver> fromEnvironment(RunMode mode, Robot robot) {
    String name = System.getenv("SIM_SCENARIO");
    if (mode != RunMode.SIM || name == null || name.isBlank()) {
      return Optional.empty();
    }
    Scenario scenario = Scenarios.find(name).orElse(null);
    if (scenario == null) {
      System.err.println("[SimDriver] Unknown scenario: " + name);
      System.exit(2);
    }
    return Optional.of(new SimDriver(scenario, robot));
  }

  /** Call once per loop from simulationPeriodic(). */
  public void update() {
    if (!started) {
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
      started = true;
      return;
    }

    long now = System.nanoTime();
    if (loop >= WARMUP_LOOPS && lastLoopNs != 0 && (now - lastLoopNs) / 1e9 > 2 * PERIOD_SEC) {
      overruns++;
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

    Check.Stats stats = new Check.Stats(loop, overruns);
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
