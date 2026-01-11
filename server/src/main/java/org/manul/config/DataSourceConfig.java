package org.manul.config;

import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.manul.context.Bean;
import org.manul.context.Configuration;
import org.manul.context.Primary;
import org.manul.context.Qualifier;
import org.manul.context.sql.TransactionIsolation;
import org.manul.jdbc.TransactionTemplate;

import javax.sql.DataSource;

@Configuration(module = "persistent")
@Slf4j
public class DataSourceConfig {

    public static final String DRIVER_NAME = "org.postgresql.Driver";

    private final ManulConfig.DbConfig dbConfig;

    public DataSourceConfig(ManulConfig config) {
        this.dbConfig = config.getDbConfig();
    }

    @Bean
    @Primary
    public DataSource primaryDataSource() {
        return createDataSource(DRIVER_NAME, dbConfig.username(), dbConfig.password(), dbConfig.url(), 10, 0);
    }

    @Bean
    public DataSource secondaryDataSource() {
        return createDataSource(DRIVER_NAME, dbConfig.username(), dbConfig.password(), dbConfig.url(), 1, 0);
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
    public TransactionTemplate primaryTransactionTemplate(@Qualifier("primaryDataSource") DataSource dataSource) {
        var tp = new TransactionTemplate(dataSource);
        tp.setIsolationLevel(TransactionIsolation.SERIALIZABLE);
        return tp;
    }

    @Bean
    public TransactionTemplate secondaryTransactionTemplate(@Qualifier("secondaryDataSource") DataSource dataSource) {
        return new TransactionTemplate(dataSource);
    }


}
