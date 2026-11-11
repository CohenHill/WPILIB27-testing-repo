package frc.tools.hooks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class MiniJsonTest {
  @Test
  void parsesNestedHookInput() {
    Map<String, Object> m =
        MiniJson.parseObject(
            "{\"tool_name\":\"Bash\",\"tool_input\":{\"command\":\"echo \\\"hi\\\"\\nls\",\"n\":3},\"flag\":true,\"x\":null,\"l\":[1,\"a\",false]}");
    assertEquals("Bash", m.get("tool_name"));
    @SuppressWarnings("unchecked")
    Map<String, Object> input = (Map<String, Object>) m.get("tool_input");
    assertEquals("echo \"hi\"\nls", input.get("command"));
    assertEquals(3.0, input.get("n"));
    assertEquals(true, m.get("flag"));
    assertNull(m.get("x"));
    assertEquals(List.of(1.0, "a", false), m.get("l"));
  }

  @Test
  void parsesUnicodeEscapes() {
    assertEquals("A", MiniJson.parseObject("{\"k\":\"\\u0041\"}").get("k"));
  }

  @Test
  void badInputGivesEmptyMap() {
    assertTrue(MiniJson.parseObject("{oops").isEmpty());
    assertTrue(MiniJson.parseObject(null).isEmpty());
    assertTrue(MiniJson.parseObject("[1,2]").isEmpty());
  }
}
