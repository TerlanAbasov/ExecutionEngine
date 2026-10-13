package com.quant.finance.execution.service;

import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

@Slf4j
public class EngineService {
  public static void restartEngine() {
    try {
      ProcessBuilder pb = new ProcessBuilder(
          "sudo", "systemctl", "restart", "execution-engine"
      );

      Process process = getProcess(pb);

      int exitCode = process.waitFor();

      if (exitCode == 0) {
        System.out.println("✅ ExecutionEngine restarted successfully");
      } else {
        System.out.println("❌ Restart failed. Exit code: " + exitCode);
      }

    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }
  }

  public static void stopEngine() {
    try {
      ProcessBuilder pb = new ProcessBuilder(
          "sudo", "systemctl", "stop", "engine"
      );

      Process process = getProcess(pb);

      int exitCode = process.waitFor();

      if (exitCode == 0) {
        log.info("✅ ExecutionEnginestopped successfully");
      } else {
        log.info("❌ Stop failed. Exit code: {}", exitCode);
      }

    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }
  }

  public static void startEngine() {
    try {
      ProcessBuilder pb = new ProcessBuilder(
          "sudo", "systemctl", "start", "engine"
      );

      Process process = getProcess(pb);

      int exitCode = process.waitFor();

      if (exitCode == 0) {
        log.info("✅ ExecutionEnginestarted successfully");
      } else {
        log.info("❌ Start failed. Exit code: {}", exitCode);
      }

    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }
  }

  @NotNull
  private static Process getProcess(ProcessBuilder pb) throws IOException {
    pb.redirectErrorStream(true);
    Process process = pb.start();

    // Read output (optional but useful for logs)
    try (java.io.BufferedReader reader =
             new java.io.BufferedReader(
                 new java.io.InputStreamReader(process.getInputStream()))) {

      String line;
      while ((line = reader.readLine()) != null) {
        log.info("[SYSTEMCTL] {}", line);
      }
    }
    return process;
  }
}
