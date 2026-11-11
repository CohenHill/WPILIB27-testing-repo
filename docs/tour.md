# Tour: the Arm, bottom to top

One session, about an hour. Read each file, then answer the question under it
before you look at the next one. Everything is in
`src/main/java/frc/robot/mechanisms/arm/` unless noted. The idea to take away:
**all hardware sits behind one interface, so the same robot code runs on a real
robot, in simulation, and in replay.**

## 1. The interface: `io/ArmIO.java`

The Arm talks to hardware only through `ArmIO`. It has logged **inputs**
(position, velocity, volts, current, connected) and methods to command the arm.
Every method has an empty default body.

> Question: why do the methods have empty bodies? (Hint: what is `new ArmIO() {}`
> good for?)

## 2. The simulation: `io/ArmIOSim.java`

Physics (a motor, gearing and an arm) plus a PID loop. It is stepped once per
robot loop. There is no hardware here.

> Question: where does the simulation's time step come from, and why must it not
> be a wall clock?

## 3. The hardware: `io/ArmIOTalonFX.java` (and `ArmIOYams.java`)

The real motor controller version. It compiles but has never run on a robot.
Vendor libraries (`com.ctre`, `yams`) are only allowed in files under an `io/`
folder, and the build fails otherwise (`checkVendorImports`).

> Question: what stops someone importing a vendor class straight into `Arm.java`?

## 4. The mechanism: `Arm.java`

A Commands v3 `Mechanism`. It holds the IO, logs the inputs in `updateInputs()`,
and has commands: `moveTo`, `hold` (the default) and `stop`. Pure helpers
(`clampGoal`, `atGoal`) are static so they can be unit tested.

> Question: why are `clampGoal` and `atGoal` static methods instead of using
> fields?

## 5. The command

Look at `moveTo`. The logic lives **inside** the command body. A command ends
when its body returns, here when the arm is at its goal.

> Question: what ends `moveTo`? What would happen if it never ended?

## 6. The wiring: `Robot.java` (`frc/robot/`)

`robotPeriodic()` runs three steps in a fixed order: update and log every
mechanism's inputs, run the scheduler (commands), record outputs.

> Question: what would break in replay if the order were scheduler first?

## 7. The test: `src/test/java/.../ArmTest.java`

Unit tests of the pure helpers. They need no robot and no WPILib natives.

> Question: which boundary values are tested, and why those?

## 8. The scenario: `src/sim/java/frc/robot/sim/scenarios/ArmReachesGoal.java`

A headless simulation with a pass or fail. Run it:

```
./frc sim ArmReachesGoal
```

Read the JSON: each check has `observed` and `expected`.

> Question: what would make this scenario fail? Try it (change a gain in
> `ArmConstants.java`, run, then undo).

## 9. Replay

```
./frc replay <log from the scenario>
./frc log diff <replayed log> Arm/GoalRad
```

Replay feeds the logged inputs back through the same code. If the outputs match,
the logic is deterministic.

> Question: why does replay use an empty `ArmIO`?

## Done

You can now explain every layer. Next: `docs/first-task.md`.
