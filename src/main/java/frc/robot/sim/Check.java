package frc.robot.sim;

/** A named condition evaluated over a scenario run. */
public interface Check {
  /** Run statistics supplied by the SimDriver at evaluation time. */
  record Stats(int loops, int overruns) {}

  /** The outcome of one check. */
  record Result(String name, boolean pass, String observed, String expected) {}

  /** Called once per enabled loop, after the scheduler has run. */
  void sample(int loop);

  /** Called once after the run ends. */
  Result result(Stats stats);
}
