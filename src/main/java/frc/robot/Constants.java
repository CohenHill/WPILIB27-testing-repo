// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

package frc.robot;

import org.wpilib.framework.RobotBase;

/**
 * This class defines the runtime mode used by AdvantageKit. The mode is "real" when running on a
 * robot. In simulation it is "replay" if the AKIT_LOG_PATH environment variable is set (to the path
 * of a .wpilog file), and "sim" otherwise. No source edit is needed to switch.
 */
public final class Constants {
  private static final Mode simMode =
      System.getenv("AKIT_LOG_PATH") != null ? Mode.REPLAY : Mode.SIM;
  public static final Mode currentMode = RobotBase.isReal() ? Mode.REAL : simMode;

  public static enum Mode {
    /** Running on a real robot. */
    REAL,

    /** Running a physics simulator. */
    SIM,

    /** Replaying from a log file. */
    REPLAY
  }
}
