package com.quant.finance.executionengine;

import com.quant.finance.execution.client.DiscordClient;
import com.quant.finance.execution.client.IBClient;
import com.quant.finance.execution.client.TelegramClient;
import java.net.InetAddress;
import java.net.UnknownHostException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableFeignClients(clients = {DiscordClient.class, TelegramClient.class})
@SpringBootApplication(scanBasePackages = "com.quant.finance.execution")
@Slf4j
public class ExecutionEngineApplication {

  public static void main(String[] args) {
    System.setProperty("spring.devtools.restart.enabled", "false");
    SpringApplication app = new SpringApplication(ExecutionEngineApplication.class);
    Environment env = app.run(args).getEnvironment();
    logApplicationStartup(env);
    IBClient.getInstance();
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
