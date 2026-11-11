package frc.robot;

/** The runtime mode used by AdvantageKit. */
public enum RunMode {
  /** Running on a real robot. */
  REAL,

  /** Running a physics simulator. */
  SIM,

  /** Replaying from a log file. */
  REPLAY;

  /**
   * Chooses the run mode. A real robot is always {@link #REAL}. Otherwise a non-blank replay log
   * path means {@link #REPLAY}, and anything else means {@link #SIM}.
   *
   * @param isReal true when running on a real robot
   * @param replayLogPath the AKIT_LOG_PATH value, or null if unset
   * @return the run mode
   */
  public static RunMode select(boolean isReal, String replayLogPath) {
    if (isReal) {
      return REAL;
    }
    if (replayLogPath != null && !replayLogPath.isBlank()) {
      return REPLAY;
    }
    return SIM;
  }
}
