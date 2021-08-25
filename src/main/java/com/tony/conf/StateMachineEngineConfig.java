package com.tony.conf;

import java.io.IOException;
import java.util.concurrent.ThreadPoolExecutor;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.alibaba.druid.pool.DruidDataSource;

import io.seata.saga.engine.config.DbStateMachineConfig;
import io.seata.saga.engine.impl.ProcessCtrlStateMachineEngine;
import io.seata.saga.rm.StateMachineEngineHolder;

@Configuration
public class StateMachineEngineConfig {

  private static final Logger LOGGER = LoggerFactory.getLogger(StateMachineEngineConfig.class);

  @Bean
  public StateMachineEngineHolder stateMachineEngineHolder() {
    StateMachineEngineHolder holder = new StateMachineEngineHolder();
    holder.setStateMachineEngine(processCtrlStateMachineEngine());
    return holder;
  }

  @Bean
  public ProcessCtrlStateMachineEngine processCtrlStateMachineEngine() {
    ProcessCtrlStateMachineEngine engine = new ProcessCtrlStateMachineEngine();
    engine.setStateMachineConfig(dbStateMachineConfig());
    return engine;
  }

  @Bean
  public DbStateMachineConfig dbStateMachineConfig() {
    DbStateMachineConfig config = new DbStateMachineConfig();
    config.setDataSource(sagaDS());
    try {
      config.setResources(new PathMatchingResourcePatternResolver().getResources("statelang/*.json"));
    } catch (IOException e) {
      LOGGER.error(e.getMessage(), e);
    }
    config.setEnableAsync(true);
    config.setThreadPoolExecutor(threadPoolExecutor());
    config.setApplicationId("seata_saga");
    config.setTxServiceGroup("sagaSrv");
    return config;
  }

  @Bean
  @Primary
  @ConfigurationProperties("spring.datasource")
  public DataSource sagaDS() {
    return new DruidDataSource();
  }

  @Bean
  public ThreadPoolExecutor threadPoolExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(1);
    executor.setMaxPoolSize(20);
    executor.setQueueCapacity(99999);
    executor.setThreadNamePrefix("SAGA_ASYNC_EXEC_");
    executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
    executor.initialize();
    return executor.getThreadPoolExecutor();
  }

}
