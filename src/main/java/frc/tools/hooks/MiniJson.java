package frc.tools.hooks;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** A small JSON parser: objects, arrays, strings, numbers, booleans and null. */
public final class MiniJson {
  private final String s;
  private int i = 0;

  private MiniJson(String s) {
    this.s = s;
  }

  /** Parses JSON text. Returns Map, List, String, Double, Boolean or null. */
  public static Object parse(String text) {
    MiniJson p = new MiniJson(text);
    p.ws();
    Object v = p.value();
    p.ws();
    if (p.i != p.s.length()) {
      throw new IllegalArgumentException("Unexpected trailing text at " + p.i);
    }
    return v;
  }

  /** Parses a JSON object; returns an empty map for blank or invalid input. */
  @SuppressWarnings("unchecked")
  public static Map<String, Object> parseObject(String text) {
    if (text == null || text.isBlank()) {
      return new LinkedHashMap<>();
    }
    try {
      Object o = parse(text);
      return o instanceof Map<?, ?> m ? (Map<String, Object>) m : new LinkedHashMap<>();
    } catch (RuntimeException e) {
      return new LinkedHashMap<>();
    }
  }

  private void ws() {
    while (i < s.length() && Character.isWhitespace(s.charAt(i))) {
      i++;
    }
  }

  private Object value() {
    if (i >= s.length()) {
      throw new IllegalArgumentException("Unexpected end of JSON");
    }
    char c = s.charAt(i);
    switch (c) {
      case '{':
        return object();
      case '[':
        return array();
      case '"':
        return string();
      case 't':
        expect("true");
        return Boolean.TRUE;
      case 'f':
        expect("false");
        return Boolean.FALSE;
      case 'n':
        expect("null");
        return null;
      default:
        return number();
    }
  }

  private void expect(String word) {
    if (!s.startsWith(word, i)) {
      throw new IllegalArgumentException("Expected " + word + " at " + i);
    }
    i += word.length();
  }

  private Map<String, Object> object() {
    Map<String, Object> m = new LinkedHashMap<>();
    i++;
    ws();
    if (s.charAt(i) == '}') {
      i++;
      return m;
    }
    while (true) {
      ws();
      String key = string();
      ws();
      if (s.charAt(i++) != ':') {
        throw new IllegalArgumentException("Expected : at " + (i - 1));
      }
      ws();
      m.put(key, value());
      ws();
      char c = s.charAt(i++);
      if (c == '}') {
        return m;
      }
      if (c != ',') {
        throw new IllegalArgumentException("Expected , or } at " + (i - 1));
      }
    }
  }

  private List<Object> array() {
    List<Object> l = new ArrayList<>();
    i++;
    ws();
    if (s.charAt(i) == ']') {
      i++;
      return l;
    }
    while (true) {
      ws();
      l.add(value());
      ws();
      char c = s.charAt(i++);
      if (c == ']') {
        return l;
      }
      if (c != ',') {
        throw new IllegalArgumentException("Expected , or ] at " + (i - 1));
      }
    }
  }

  private String string() {
    if (s.charAt(i) != '"') {
      throw new IllegalArgumentException("Expected string at " + i);
    }
    i++;
    StringBuilder sb = new StringBuilder();
    while (true) {
      char c = s.charAt(i++);
      if (c == '"') {
        return sb.toString();
      }
      if (c != '\\') {
        sb.append(c);
        continue;
      }
      char e = s.charAt(i++);
      switch (e) {
        case 'n' -> sb.append('\n');
        case 't' -> sb.append('\t');
        case 'r' -> sb.append('\r');
        case 'b' -> sb.append('\b');
        case 'f' -> sb.append('\f');
        case 'u' -> {
          sb.append((char) Integer.parseInt(s.substring(i, i + 4), 16));
          i += 4;
        }
        default -> sb.append(e);
      }
    }
  }

  private Double number() {
    int start = i;
    while (i < s.length() && "+-0123456789.eE".indexOf(s.charAt(i)) >= 0) {
      i++;
    }
    if (start == i) {
      throw new IllegalArgumentException("Unexpected character at " + i);
    }
    return Double.parseDouble(s.substring(start, i));
  }
}
