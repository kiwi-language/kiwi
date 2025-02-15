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
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;

@Configuration
@Slf4j
public class DataSourceConfig {

    @Bean
    @Primary
    public DataSource primaryDataSource(
            @Value("${spring.datasource.primary.driver-class-name}") String driverClassName,
            @Value("${spring.datasource.primary.username}") String userName,
            @Value("${spring.datasource.primary.password}") String password,
            @Value("${spring.datasource.primary.url}") String url,
            @Value("${spring.datasource.primary.hikari.maximum-pool-size}") int maxPoolSize,
            @Value("${spring.datasource.primary.hikari.minimum-idle}") int minIdle
    ) {
        return createDataSource(driverClassName, userName, password, url, maxPoolSize, minIdle);
    }

    @Bean
    public DataSource secondaryDataSource(
            @Value("${spring.datasource.secondary.driver-class-name}") String driverClassName,
            @Value("${spring.datasource.secondary.username}") String userName,
            @Value("${spring.datasource.secondary.password}") String password,
            @Value("${spring.datasource.secondary.url}") String url,
            @Value("${spring.datasource.secondary.hikari.maximum-pool-size}") int maxPoolSize,
            @Value("${spring.datasource.secondary.hikari.minimum-idle}") int minIdle
    ) {
        return createDataSource(driverClassName, userName, password, url, maxPoolSize, minIdle);
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
        return new TransactionTemplate(transactionManager);
    }

    @Bean
    public TransactionTemplate secondaryTransactionTemplate(@Qualifier("secondaryTransactionManager") PlatformTransactionManager transactionManager) {
        return new TransactionTemplate(transactionManager);
    }

}
