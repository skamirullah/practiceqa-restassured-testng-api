package com.practiceqa.api.server;

import com.practiceqa.api.config.Config;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

/**
 * Starts and stops the practice API for a test run. The API is one dependency-free
 * Node.js file, downloaded into .cache/ the first time it is needed.
 */
public final class PracticeApiServer {
  private static final Path FILE = Path.of(".cache", "practiceqa-api.mjs");
  private static final HttpClient HTTP = HttpClient.newHttpClient();
  private static Process process;

  private PracticeApiServer() {}

  public static synchronized void start() {
    if (Config.usesExternalApi() || process != null) return;
    try {
      download();
      process = new ProcessBuilder("node", FILE.toString(), "--port=" + Config.port(), "--quiet")
          .redirectErrorStream(true)
          .redirectOutput(ProcessBuilder.Redirect.DISCARD)
          .start();
      Runtime.getRuntime().addShutdownHook(new Thread(PracticeApiServer::stop));
      waitUntilHealthy(Duration.ofSeconds(15));
    } catch (IOException e) {
      throw new IllegalStateException("Could not start the practice API. Is Node.js 20+ installed?", e);
    }
  }

  public static synchronized void stop() {
    if (process != null) {
      process.destroy();
      process = null;
    }
  }

  private static void download() throws IOException {
    if (Files.exists(FILE)) return;
    Files.createDirectories(FILE.getParent());
    HttpRequest request = HttpRequest.newBuilder(URI.create(Config.downloadUrl())).build();
    try {
      HttpResponse<Path> response = HTTP.send(request, HttpResponse.BodyHandlers.ofFile(FILE));
      if (response.statusCode() != 200) {
        Files.deleteIfExists(FILE);
        throw new IOException("Download failed with HTTP " + response.statusCode());
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IOException("Interrupted while downloading the practice API", e);
    }
  }

  private static void waitUntilHealthy(Duration timeout) {
    long deadline = System.nanoTime() + timeout.toNanos();
    HttpRequest health = HttpRequest.newBuilder(URI.create(Config.baseUrl() + "/api/health")).build();
    while (System.nanoTime() < deadline) {
      try {
        if (HTTP.send(health, HttpResponse.BodyHandlers.discarding()).statusCode() == 200) return;
      } catch (IOException notListeningYet) {
        // keep polling
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        break;
      }
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        break;
      }
    }
    throw new IllegalStateException("The practice API did not become healthy on " + Config.baseUrl());
  }
}
