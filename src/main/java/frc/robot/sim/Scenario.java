package frc.robot.sim;

import frc.robot.Robot;
import java.util.List;

/** A headless simulation scenario: which OpMode to run, for how long, and what to check. */
public interface Scenario {
  String name();

  /** Name of the OpMode to select, as shown on the Driver Station. */
  String opMode();

  /** How many enabled loops to run. */
  int durationLoops();

  /** Creates fresh checks for one run. Checks read mechanisms through public getters only. */
  List<Check> checks(Robot robot);
}
