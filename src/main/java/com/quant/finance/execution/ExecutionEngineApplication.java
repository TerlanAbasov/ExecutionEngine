package com.quant.finance.execution;

import com.quant.finance.execution.client.DiscordClient;
import com.quant.finance.execution.client.TelegramClient;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ThreadLocalRandom;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.core.env.Environment;

@SpringBootApplication
@EnableFeignClients(clients = {DiscordClient.class, TelegramClient.class})
@Slf4j
public class ExecutionEngineApplication {

  public static void main(String[] args) {
    System.setProperty("spring.devtools.restart.enabled", "false");
    SpringApplication app = new SpringApplication(ExecutionEngineApplication.class);
    Environment env = app.run(args).getEnvironment();
    logApplicationStartup(env);
  }

  private static void logApplicationStartup(Environment env) {
    String protocol = "http";
    if (env.getProperty("server.ssl.key-store") != null) {
      protocol = "https";
    }
    String serverPort = env.getProperty("local.server.port");
    String contextPath = env.getProperty("server.servlet.context-path");
    if (StringUtils.isBlank(contextPath)) {
      contextPath = "/";
    }
    String hostAddress = "localhost";
    try {
      hostAddress = InetAddress.getLocalHost().getHostAddress();
    } catch (UnknownHostException e) {
      log.warn("The host name could not be determined, using `localhost` as fallback");
    }
    log.info("Application '{}' is running! Access URLs: Local: {}://localhost:{}{} " +
            "External: {}://{}:{}{} Profile(s): {}",
        env.getProperty("spring.application.name"),
        protocol,
        serverPort,
        contextPath,
        protocol,
        hostAddress,
        serverPort,
        contextPath,
        env.getActiveProfiles());
  }


}
