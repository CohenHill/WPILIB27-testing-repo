package frc.robot.context;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.junit.jupiter.api.Test;

class ZoneMapTest {
  private static final double[][] SQUARE = {{0, 0}, {4, 0}, {4, 4}, {0, 4}};
  // An L shape: the notch at the top right is outside.
  private static final double[][] L_SHAPE = {{0, 0}, {4, 0}, {4, 2}, {2, 2}, {2, 4}, {0, 4}};

  @Test
  void insideAndOutsideASquare() {
    assertTrue(ZoneMap.contains(SQUARE, 2, 2));
    assertFalse(ZoneMap.contains(SQUARE, 5, 2));
    assertFalse(ZoneMap.contains(SQUARE, -0.1, 2));
    assertFalse(ZoneMap.contains(SQUARE, 2, 4.1));
  }

  @Test
  void boundaryAndCornersCountAsInside() {
    assertTrue(ZoneMap.contains(SQUARE, 0, 2));
    assertTrue(ZoneMap.contains(SQUARE, 4, 4));
    assertTrue(ZoneMap.contains(SQUARE, 2, 0));
  }

  @Test
  void concavePolygonNotchIsOutside() {
    assertTrue(ZoneMap.contains(L_SHAPE, 1, 3));
    assertTrue(ZoneMap.contains(L_SHAPE, 3, 1));
    assertFalse(ZoneMap.contains(L_SHAPE, 3, 3));
  }

  @Test
  void firstMatchingZoneWins() {
    ZoneMap m =
        new ZoneMap(
            java.util.List.of(new ZoneMap.Zone("A", SQUARE), new ZoneMap.Zone("B", SQUARE)));
    assertEquals(Optional.of("A"), m.zoneAt(1, 1));
    assertEquals(Optional.empty(), m.zoneAt(9, 9));
  }

  @Test
  void parsesZonesJson() {
    ZoneMap m =
        ZoneMap.parse(
            "{\"zones\":[{\"name\":\"L\",\"polygon\":[[0,0],[1,0],[1,1],[0,1]]},"
                + "{\"name\":\"R\",\"polygon\":[[2,0],[3,0],[3,1],[2,1]]}]}");
    assertEquals(java.util.List.of("L", "R"), m.names());
    assertEquals(Optional.of("L"), m.zoneAt(0.5, 0.5));
    assertEquals(Optional.of("R"), m.zoneAt(2.5, 0.5));
    assertEquals(Optional.empty(), m.zoneAt(1.5, 0.5));
  }

  @Test
  void badJsonIsRejected() {
    assertThrows(IllegalArgumentException.class, () -> ZoneMap.parse("{}"));
    assertThrows(
        IllegalArgumentException.class,
        () -> ZoneMap.parse("{\"zones\":[{\"name\":\"x\",\"polygon\":[[0,0],[1,1]]}]}"));
    assertThrows(
        IllegalArgumentException.class,
        () -> ZoneMap.parse("{\"zones\":[{\"polygon\":[[0,0],[1,0],[1,1]]}]}"));
  }

  @Test
  void shippedZonesFileParsesAndCoversBothSidesOfTheField() throws Exception {
    String json =
        java.nio.file.Files.readString(java.nio.file.Path.of("src/main/deploy/zones.json"));
    ZoneMap m = ZoneMap.parse(json);
    assertEquals(Optional.of("LEFT"), m.zoneAt(4, 4));
    assertEquals(Optional.of("RIGHT"), m.zoneAt(12, 4));
    assertEquals(Optional.empty(), m.zoneAt(-5, 4));
  }
}
