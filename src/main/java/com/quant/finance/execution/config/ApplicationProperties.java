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

    @Getter
    @Setter
    public static class Client {

        private final ExperianPco experianPco = new ExperianPco();
        private final ExperianPcoBulk experianPcoBulk = new ExperianPcoBulk();


        @Setter
        @Getter
        public static class ExperianPco {

            private String username;
            private String password;

        }

        @Setter
        @Getter
        public static class ExperianPcoBulk {

            private String username;
            private String password;

        }

    }

}
