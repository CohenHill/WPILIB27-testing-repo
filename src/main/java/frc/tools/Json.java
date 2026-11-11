package frc.tools;

/** Minimal JSON value formatting. */
final class Json {
  private Json() {}

  static String str(String s) {
    StringBuilder sb = new StringBuilder("\"");
    for (char c : s.toCharArray()) {
      switch (c) {
        case '"' -> sb.append("\\\"");
        case '\\' -> sb.append("\\\\");
        default -> sb.append(c < 0x20 ? " " : String.valueOf(c));
      }
    }
    return sb.append('"').toString();
  }

  static String num(double d) {
    if (Double.isNaN(d) || Double.isInfinite(d)) {
      return "null";
    }
    return d == Math.rint(d) && Math.abs(d) < 1e15 ? Long.toString((long) d) : Double.toString(d);
  }
}
