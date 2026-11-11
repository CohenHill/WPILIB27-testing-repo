// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

package frc.robot;

import frc.robot.context.RobotContext;
import frc.robot.context.ZoneMap;
import frc.robot.mechanisms.arm.Arm;
import frc.robot.mechanisms.drive.Drive;
import java.util.ServiceLoader;
import org.littletonrobotics.junction.LogFileUtil;
import org.littletonrobotics.junction.LoggedOpModeRobot;
import org.littletonrobotics.junction.Logger;
import org.littletonrobotics.junction.networktables.NT4Publisher;
import org.littletonrobotics.junction.wpilog.WPILOGReader;
import org.littletonrobotics.junction.wpilog.WPILOGWriter;
import org.wpilib.command3.Scheduler;

public class Robot extends LoggedOpModeRobot {
  public final Controls controls;
  public final Arm arm;
  public final Drive drive;
  public final RobotContext context;
  private final SimHook simHook;
  private DriverDashboard dashboard = null;

  public Robot() {
    recordMetadata();
    startLogging();

    // Mechanisms first, then things that use them
    arm = new Arm(RunProfile.armIO(Constants.currentMode));
    drive = new Drive(RunProfile.driveIO(Constants.currentMode));
    context = new RobotContext(drive::getPose, ZoneMap.load());
    controls = new Controls(arm, context);

    simHook = findSimHook();

    // The driver table and layout server run on the robot and in an interactive sim, not in replay
    // and not in headless scenarios.
    if (Constants.currentMode == RunMode.REAL
        || (Constants.currentMode == RunMode.SIM && simHook == null)) {
      dashboard = new DriverDashboard();
    }
  }

  /** Build and git details, so every log says exactly which code made it. */
  private static void recordMetadata() {
    Logger.recordMetadata("ProjectName", BuildConstants.MAVEN_NAME);
    Logger.recordMetadata("BuildDate", BuildConstants.BUILD_DATE);
    Logger.recordMetadata("GitSHA", BuildConstants.GIT_SHA);
    Logger.recordMetadata("GitDate", BuildConstants.GIT_DATE);
    Logger.recordMetadata("GitBranch", BuildConstants.GIT_BRANCH);
    Logger.recordMetadata(
        "GitDirty",
        switch (BuildConstants.DIRTY) {
          case 0 -> "All changes committed";
          case 1 -> "Uncommitted changes";
          default -> "Unknown";
        });
  }

  /** Chooses where the AdvantageKit log goes (or comes from, in replay) and starts the logger. */
  private void startLogging() {
    switch (Constants.currentMode) {
      case REAL, SIM -> {
        // Log to a file (robot: USB stick "/U/logs"; sim: "logs/") and to NetworkTables
        Logger.addDataReceiver(new WPILOGWriter());
        Logger.addDataReceiver(new NT4Publisher());
      }
      case REPLAY -> {
        // Read inputs from a log and write the replayed outputs beside it ("<log>_sim.wpilog")
        setUseTiming(false); // Run as fast as possible
        String logPath = LogFileUtil.findReplayLog();
        Logger.setReplaySource(new WPILOGReader(logPath));
        Logger.addDataReceiver(new WPILOGWriter(LogFileUtil.addPathSuffix(logPath, "_sim")));
      }
    }
    Logger.start();
  }

  /**
   * In simulation, finds a sim-only hook (a headless scenario) if one is on the classpath and wants
   * to run. Hooks live in src/sim, outside the robot code. Returns null otherwise.
   */
  private SimHook findSimHook() {
    if (Constants.currentMode != RunMode.SIM) {
      return null;
    }
    for (SimHook hook : ServiceLoader.load(SimHook.class)) {
      if (hook.begin(this)) {
        if (System.getenv("SIM_FAST") != null) {
          // Opt-in only: Commands v3 waits use the real clock, so fast mode desyncs them from
          // physics
          setUseTiming(false);
        }
        return hook;
      }
    }
    return null;
  }

  /**
   * Runs every loop in every mode.
   *
   * <p>Order matters. Commands v3 mechanisms have no periodic(), so inputs must be read and logged
   * here, before any command logic runs. If step 1 is skipped, replay silently breaks because
   * commands would see unlogged hardware values.
   */
  @Override
  public void robotPeriodic() {
    // 1. Update and log every mechanism's inputs
    arm.updateInputs();
    drive.updateInputs();
    context.update();

    // 2. Run commands
    Scheduler.getDefault().run();

    // 3. Record outputs
    arm.recordOutputs();
    drive.recordOutputs();
    context.recordOutputs();
    if (dashboard != null) {
      dashboard.update(context, arm);
    }
  }

  @Override
  public void simulationPeriodic() {
    if (simHook != null) {
      simHook.update();
    }
  }
}
