package com.quant.finance.execution.config;

import javax.sql.DataSource;
import liquibase.integration.spring.SpringLiquibase;

//@Configuration
//@AutoConfigureAfter(DataSourceAutoConfiguration.class)
public class LiquibaseConfig {

  //@Bean
  public SpringLiquibase liquibase(DataSource dataSource) {
    SpringLiquibase liquibase = new SpringLiquibase();
    liquibase.setDataSource(dataSource);
    liquibase.setChangeLog("classpath:/changelog/master.yaml");
    liquibase.setDefaultSchema("engine");
    liquibase.setShouldRun(true);
    return liquibase;
  }
}