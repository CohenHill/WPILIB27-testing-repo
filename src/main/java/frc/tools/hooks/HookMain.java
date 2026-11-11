package frc.tools.hooks;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/** Entry for {@code frc hook <name>}: reads hook JSON on stdin, writes the result. */
public final class HookMain {
  private HookMain() {}

  /** Runs a hook and returns the process exit code. */
  public static int run(String[] args) {
    if (args.length < 1) {
      System.err.println(
          "usage: frc hook <guard-shell|guard-edit|session-start|post-edit|on-stop|pre-compact>");
      return 1;
    }
    String stdin;
    try {
      stdin = new String(System.in.readAllBytes(), StandardCharsets.UTF_8);
    } catch (IOException e) {
      stdin = "";
    }
    Map<String, Object> in = MiniJson.parseObject(stdin);
    Object cwd = in.get("cwd");
    Env env = new RealEnv(cwd instanceof String s ? s : System.getenv("CLAUDE_PROJECT_DIR"));
    Hooks.Result r = Hooks.run(args[0], stdin, env);
    if (!r.stdout().isEmpty()) {
      System.out.print(r.stdout());
    }
    if (!r.stderr().isEmpty()) {
      System.err.println(r.stderr());
    }
    return r.exit();
  }
}
