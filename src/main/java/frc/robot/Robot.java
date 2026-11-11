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
  private SimHook simHook = null;

  public Robot() {
    // Record metadata
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

    // Set up data receivers & replay source
    switch (Constants.currentMode) {
      case REAL:
        // Running on a real robot, log to a USB stick ("/U/logs")
        Logger.addDataReceiver(new WPILOGWriter());
        Logger.addDataReceiver(new NT4Publisher());
        break;

      case SIM:
        // Running a physics simulator, log to a file ("logs/") and NT
        Logger.addDataReceiver(new WPILOGWriter());
        Logger.addDataReceiver(new NT4Publisher());
        break;

      case REPLAY:
        // Replaying a log, set up replay source
        setUseTiming(false); // Run as fast as possible
        String logPath = LogFileUtil.findReplayLog();
        Logger.setReplaySource(new WPILOGReader(logPath));
        Logger.addDataReceiver(new WPILOGWriter(LogFileUtil.addPathSuffix(logPath, "_sim")));
        break;
    }

    // Start AdvantageKit logger
    Logger.start();

    arm = new Arm(RunProfile.armIO(Constants.currentMode));
    drive = new Drive(RunProfile.driveIO(Constants.currentMode));
    context = new RobotContext(drive::getPose, ZoneMap.load());
    controls = new Controls(arm, context);

    if (Constants.currentMode == RunMode.SIM) {
      // Sim-only hooks (headless scenarios) live outside the robot code and are found at runtime.
      for (SimHook hook : ServiceLoader.load(SimHook.class)) {
        if (hook.begin(this)) {
          simHook = hook;
          break;
        }
      }
    }
    if (simHook != null && System.getenv("SIM_FAST") != null) {
      // Opt-in only: Commands v3 waits use the real clock, so fast mode desyncs them from physics
      setUseTiming(false);
    }
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
  }

  @Override
  public void simulationPeriodic() {
    if (simHook != null) {
      simHook.update();
    }
  }
}
