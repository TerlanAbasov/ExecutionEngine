package com.quant.finance.execution.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@ConfigurationProperties(prefix = "application")
@Configuration
public class ApplicationProperties {

  private final Client client = new Client();
  private final Account account = new Account();
  private final Params params = new Params();

  @Getter
  @Setter
  public static class Client {

    private final Telegram telegram = new Telegram();
    private final Gateway gateway = new Gateway();

    @Setter
    @Getter
    public static class Telegram {
      private String url;
      private String token;
      private String chatId;
      private String botUsername;
      private boolean enabled;
    }

    @Setter
    @Getter
    public static class Gateway {
      private String host;
      private Integer port;
      private Integer id;
    }

  }

  @Setter
  @Getter
  public static class Account {
    private String id;
  }

  @Setter
  @Getter
  public static class Params {
    private Long pairTickerThreadSleep;
    private Integer contractFutureTimeout;
    private Long startUpDelay;
    private Long ibNotificationCooldown;
    private Long reconnectTriggerDelay;
  }

}
