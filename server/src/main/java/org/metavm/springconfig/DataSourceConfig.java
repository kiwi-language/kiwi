package org.metavm.springconfig;

import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;

@Configuration
@Slf4j
public class DataSourceConfig {

    public static final String DRIVER_NAME = "org.postgresql.Driver";

    private final KiwiConfig.DbConfig dbConfig;

    public DataSourceConfig(KiwiConfig config) {
        this.dbConfig = config.getDbConfig();
    }

    @Bean
    @Primary
    public DataSource primaryDataSource(
            @Value("${spring.datasource.primary.hikari.maximum-pool-size}") int maxPoolSize,
            @Value("${spring.datasource.primary.hikari.minimum-idle}") int minIdle
    ) {
        return createDataSource(DRIVER_NAME, dbConfig.username(), dbConfig.password(), dbConfig.url(), maxPoolSize, minIdle);
    }

    @Bean
    public DataSource secondaryDataSource(
            @Value("${spring.datasource.secondary.hikari.maximum-pool-size}") int maxPoolSize,
            @Value("${spring.datasource.secondary.hikari.minimum-idle}") int minIdle
    ) {
        return createDataSource(DRIVER_NAME, dbConfig.username(), dbConfig.password(), dbConfig.url(), maxPoolSize, minIdle);
    }

    public DataSource createDataSource(String driverClassName, String userName, String password, String url, int maxPoolSize, int minIdle) {
        var dataSource = new HikariDataSource();
        dataSource.setDriverClassName(driverClassName);
        dataSource.setUsername(userName);
        dataSource.setPassword(password);
        dataSource.setJdbcUrl(url);
        dataSource.setMaximumPoolSize(maxPoolSize);
        dataSource.setMinimumIdle(minIdle);
        return dataSource;
    }

    @Bean
    @Primary
    public PlatformTransactionManager primaryTransactionManager(@Qualifier("primaryDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean
    public PlatformTransactionManager secondaryTransactionManager(@Qualifier("secondaryDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean
    @Primary
    public TransactionTemplate primaryTransactionTemplate(@Qualifier("primaryTransactionManager") PlatformTransactionManager transactionManager) {
        var tp = new TransactionTemplate(transactionManager);
        tp.setIsolationLevel(TransactionDefinition.ISOLATION_SERIALIZABLE);
        return tp;
    }

    @Bean
    public TransactionTemplate secondaryTransactionTemplate(@Qualifier("secondaryTransactionManager") PlatformTransactionManager transactionManager) {
        return new TransactionTemplate(transactionManager);
    }


}
