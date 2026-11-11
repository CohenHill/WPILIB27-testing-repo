package frc.tools.hooks;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/** The real environment: files, processes and environment variables. */
public final class RealEnv implements Env {
  private final String cwd;

  public RealEnv(String cwd) {
    this.cwd = cwd == null || cwd.isBlank() ? System.getProperty("user.dir") : cwd;
  }

  @Override
  public String getenv(String name) {
    return System.getenv(name);
  }

  @Override
  public String readFile(String path) {
    try {
      Path p = Path.of(path);
      if (!p.isAbsolute()) {
        p = Path.of(cwd).resolve(path);
      }
      return Files.exists(p) ? Files.readString(p, StandardCharsets.UTF_8) : null;
    } catch (IOException | RuntimeException e) {
      return null;
    }
  }

  @Override
  public List<String> listFiles(String dir) {
    File[] files = Path.of(cwd).resolve(dir).toFile().listFiles();
    if (files == null) {
      return List.of();
    }
    return Arrays.stream(files).map(File::getName).sorted().toList();
  }

  @Override
  public ExecResult exec(List<String> command, int timeoutSeconds) {
    try {
      List<String> cmd = new ArrayList<>(command);
      boolean windows = System.getProperty("os.name", "").toLowerCase().contains("win");
      if (!cmd.isEmpty() && cmd.get(0).equals("./gradlew")) {
        cmd.set(0, windows ? "gradlew.bat" : "./gradlew");
      }
      ProcessBuilder pb =
          new ProcessBuilder(cmd).directory(new File(cwd)).redirectErrorStream(true);
      if (System.getenv("JAVA_HOME") == null) {
        // This JVM is the JDK the frc wrapper chose, so Gradle should use it too.
        pb.environment().put("JAVA_HOME", System.getProperty("java.home"));
      }
      Process proc = pb.start();
      byte[] out = proc.getInputStream().readAllBytes();
      if (!proc.waitFor(timeoutSeconds, TimeUnit.SECONDS)) {
        proc.destroyForcibly();
        return new ExecResult(124, new String(out, StandardCharsets.UTF_8) + "\n(timed out)");
      }
      return new ExecResult(proc.exitValue(), new String(out, StandardCharsets.UTF_8));
    } catch (IOException e) {
      return new ExecResult(127, "could not run " + command + ": " + e.getMessage());
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return new ExecResult(130, "interrupted");
    }
  }

  @Override
  public String cwd() {
    return cwd;
  }
}
