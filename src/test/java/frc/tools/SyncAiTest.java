package frc.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SyncAiTest {
  private static final String RULE =
      "---\npaths:\n  - \"src/a/**\"\n  - \"src/b/**\"\n---\n# Rule\n\n- be good\n";
  private static final String AGENT =
      "---\nname: demo\ndescription: Does a \"thing\". Use often.\ntools: Read, Grep, Glob, Bash, Edit\nmodel: opus\n---\nRead AGENTS.md first.\n";

  private static int run(Path root, boolean check, ByteArrayOutputStream err) {
    return SyncAi.run(
        root, check, new PrintStream(new ByteArrayOutputStream()), new PrintStream(err));
  }

  private static void setup(Path root) throws Exception {
    Files.createDirectories(root.resolve(".claude/rules"));
    Files.createDirectories(root.resolve(".claude/agents"));
    Files.writeString(root.resolve(".claude/rules/demo.md"), RULE);
    Files.writeString(root.resolve(".claude/agents/demo.md"), AGENT);
  }

  @Test
  void rendersInstructionWithApplyToFromPaths() {
    String out = SyncAi.renderInstruction(RULE, ".claude/rules/demo.md");
    assertTrue(out.startsWith("---\napplyTo: \"src/a/**,src/b/**\"\n---\n"), out);
    assertTrue(out.contains(SyncAi.MARKER) && out.contains("# Rule"), out);
  }

  @Test
  void ruleWithoutPathsAppliesEverywhere() {
    assertTrue(SyncAi.renderInstruction("# Just text\n", "x").contains("applyTo: \"**\""));
  }

  @Test
  void rendersAgentWithMappedToolsAndNoModel() {
    String out = SyncAi.renderAgent(AGENT, ".claude/agents/demo.md");
    assertTrue(out.contains("name: demo"), out);
    assertTrue(out.contains("description: \"Does a \\\"thing\\\". Use often.\""), out);
    assertTrue(out.contains("tools: [\"read\", \"search\", \"execute\", \"edit\"]"), out);
    assertFalse(out.contains("model:"), out);
    assertTrue(out.contains("Read AGENTS.md first."), out);
  }

  @Test
  void writeThenCheckPasses(@TempDir Path root) throws Exception {
    setup(root);
    assertEquals(0, run(root, false, new ByteArrayOutputStream()));
    assertTrue(Files.exists(root.resolve(".github/instructions/demo.instructions.md")));
    assertTrue(Files.exists(root.resolve(".github/agents/demo.agent.md")));
    assertEquals(0, run(root, true, new ByteArrayOutputStream()));
  }

  @Test
  void checkFailsWhenGeneratedFilesAreMissing(@TempDir Path root) throws Exception {
    setup(root);
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    assertEquals(1, run(root, true, err));
    assertTrue(err.toString().contains("missing: .github/agents/demo.agent.md"), err.toString());
    assertFalse(Files.exists(root.resolve(".github/agents/demo.agent.md")), "check must not write");
  }

  @Test
  void checkFailsWhenSourceChanges(@TempDir Path root) throws Exception {
    setup(root);
    run(root, false, new ByteArrayOutputStream());
    Files.writeString(root.resolve(".claude/rules/demo.md"), RULE + "- another\n");
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    assertEquals(1, run(root, true, err));
    assertTrue(err.toString().contains("out of date: .github/instructions/demo.instructions.md"));
  }

  @Test
  void checkFailsWhenGeneratedFileIsEditedByHand(@TempDir Path root) throws Exception {
    setup(root);
    run(root, false, new ByteArrayOutputStream());
    Path gen = root.resolve(".github/agents/demo.agent.md");
    Files.writeString(gen, Files.readString(gen) + "hand edit\n");
    assertEquals(1, run(root, true, new ByteArrayOutputStream()));
  }

  @Test
  void orphanedGeneratedFileIsDriftAndRemovedOnWrite(@TempDir Path root) throws Exception {
    setup(root);
    run(root, false, new ByteArrayOutputStream());
    Files.delete(root.resolve(".claude/agents/demo.md"));
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    assertEquals(1, run(root, true, err));
    assertTrue(err.toString().contains("orphan"), err.toString());
    assertEquals(0, run(root, false, new ByteArrayOutputStream()));
    assertFalse(Files.exists(root.resolve(".github/agents/demo.agent.md")));
  }

  @Test
  void handWrittenCopilotFilesAreLeftAlone(@TempDir Path root) throws Exception {
    setup(root);
    Files.createDirectories(root.resolve(".github/instructions"));
    Path mine = root.resolve(".github/instructions/mine.instructions.md");
    Files.writeString(mine, "---\napplyTo: \"**\"\n---\nhand written\n");
    run(root, false, new ByteArrayOutputStream());
    assertEquals(0, run(root, true, new ByteArrayOutputStream()));
    assertTrue(Files.readString(mine).contains("hand written"));
  }
}
