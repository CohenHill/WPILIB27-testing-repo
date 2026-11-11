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
 * robot. In simulation it is "replay" if the AKIT_LOG_PATH environment variable is set to a
 * non-blank path of a .wpilog file, and "sim" otherwise. See {@link RunMode#select}.
 */
public final class Constants {
  public static final RunMode currentMode =
      RunMode.select(RobotBase.isReal(), System.getenv("AKIT_LOG_PATH"));
}
