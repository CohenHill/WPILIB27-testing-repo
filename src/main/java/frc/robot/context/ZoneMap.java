package frc.robot.context;

import frc.robot.util.MiniJson;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.wpilib.system.Filesystem;

/** Named field zones (polygons) from {@code deploy/zones.json}, and a point-in-zone lookup. */
public final class ZoneMap {
  /** One named polygon. Points are {x, y} in meters. */
  public record Zone(String name, double[][] polygon) {}

  private final List<Zone> zones;

  public ZoneMap(List<Zone> zones) {
    this.zones = List.copyOf(zones);
  }

  /** An empty map: every point is in no zone. */
  public static ZoneMap empty() {
    return new ZoneMap(List.of());
  }

  /** Loads {@code deploy/zones.json}. A missing or bad file gives an empty map and a message. */
  public static ZoneMap load() {
    try {
      var file = Filesystem.getDeployDirectory().toPath().resolve("zones.json");
      return parse(Files.readString(file));
    } catch (IOException | RuntimeException e) {
      System.err.println("[ZoneMap] Could not load zones.json: " + e);
      return empty();
    }
  }

  /** Parses the zones JSON. Throws IllegalArgumentException if the shape is wrong. */
  @SuppressWarnings("unchecked")
  public static ZoneMap parse(String json) {
    Object root = MiniJson.parse(json);
    if (!(root instanceof Map<?, ?> m) || !(m.get("zones") instanceof List<?> list)) {
      throw new IllegalArgumentException("zones.json needs a \"zones\" list");
    }
    List<Zone> out = new ArrayList<>();
    for (Object o : list) {
      Map<String, Object> z = (Map<String, Object>) o;
      Object name = z.get("name");
      Object poly = z.get("polygon");
      if (!(name instanceof String n) || !(poly instanceof List<?> pts) || pts.size() < 3) {
        throw new IllegalArgumentException("each zone needs a name and a polygon of 3+ points");
      }
      double[][] polygon = new double[pts.size()][2];
      for (int i = 0; i < pts.size(); i++) {
        List<Object> p = (List<Object>) pts.get(i);
        polygon[i][0] = ((Number) p.get(0)).doubleValue();
        polygon[i][1] = ((Number) p.get(1)).doubleValue();
      }
      out.add(new Zone(n, polygon));
    }
    return new ZoneMap(out);
  }

  /** The first zone containing the point, if any. */
  public Optional<String> zoneAt(double x, double y) {
    for (Zone z : zones) {
      if (contains(z.polygon(), x, y)) {
        return Optional.of(z.name());
      }
    }
    return Optional.empty();
  }

  /** Zone names in file order. */
  public List<String> names() {
    return zones.stream().map(Zone::name).toList();
  }

  /** True if the point is inside the polygon or on its boundary. Works for concave polygons. */
  public static boolean contains(double[][] poly, double x, double y) {
    int n = poly.length;
    boolean inside = false;
    for (int i = 0, j = n - 1; i < n; j = i++) {
      double xi = poly[i][0];
      double yi = poly[i][1];
      double xj = poly[j][0];
      double yj = poly[j][1];
      if (onSegment(xi, yi, xj, yj, x, y)) {
        return true;
      }
      boolean crosses = (yi > y) != (yj > y) && x < (xj - xi) * (y - yi) / (yj - yi) + xi;
      if (crosses) {
        inside = !inside;
      }
    }
    return inside;
  }

  private static boolean onSegment(
      double x1, double y1, double x2, double y2, double px, double py) {
    double cross = (px - x1) * (y2 - y1) - (py - y1) * (x2 - x1);
    if (Math.abs(cross) > 1e-9) {
      return false;
    }
    return px >= Math.min(x1, x2) - 1e-9
        && px <= Math.max(x1, x2) + 1e-9
        && py >= Math.min(y1, y2) - 1e-9
        && py <= Math.max(y1, y2) + 1e-9;
  }
}
