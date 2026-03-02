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
  private final Contract contract = new Contract();
  private final Account account = new Account();

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
      private int port;
      private int id;

    }

  }

  @Setter
  @Getter
  public static class Contract {

    private int count;

  }

  @Setter
  @Getter
  public static class Account {

    private String id;

  }

}
