package frc.robot.context;

import java.util.function.Supplier;
import org.littletonrobotics.junction.Logger;
import org.wpilib.command3.Trigger;
import org.wpilib.driverstation.MatchState;
import org.wpilib.math.geometry.Pose2d;

/**
 * Where the robot is and what it holds, as Commands v3 triggers and logged outputs. Updated every
 * loop from the (logged) drive pose, so it replays.
 */
public class RobotContext {
  /** The zone name when the robot is in no zone. */
  public static final String NO_ZONE = "NONE";

  private final Supplier<Pose2d> pose;
  private final ZoneMap zones;
  private String zone = NO_ZONE;
  private boolean holdingPiece = false;

  public RobotContext(Supplier<Pose2d> pose, ZoneMap zones) {
    this.pose = pose;
    this.zones = zones;
  }

  /** Recomputes the zone from the current pose. Call every loop, after the inputs update. */
  public void update() {
    Pose2d p = pose.get();
    zone = zones.zoneAt(p.getX(), p.getY()).orElse(NO_ZONE);
  }

  /** The current zone name, or {@link #NO_ZONE}. */
  public String zone() {
    return zone;
  }

  /** Whether the robot holds a game piece. Mechanisms set this when they gain or lose one. */
  public boolean holdingPiece() {
    return holdingPiece;
  }

  public void setHoldingPiece(boolean holding) {
    holdingPiece = holding;
  }

  /** The alliance from the Driver Station (logged, so it replays), or "UNKNOWN". */
  public String alliance() {
    return MatchState.getAlliance().map(Object::toString).orElse("UNKNOWN");
  }

  /** A trigger that is true while the robot is in the named zone. */
  public Trigger inZone(String name) {
    return new Trigger(() -> zone.equals(name));
  }

  /** A trigger that is true while the robot holds a piece. */
  public Trigger holding() {
    return new Trigger(() -> holdingPiece);
  }

  /** Records the context outputs. Call every loop, after the scheduler. */
  public void recordOutputs() {
    Logger.recordOutput("Context/Zone", zone);
    Logger.recordOutput("Context/HoldingPiece", holdingPiece);
    Logger.recordOutput("Context/Alliance", alliance());
  }
}
