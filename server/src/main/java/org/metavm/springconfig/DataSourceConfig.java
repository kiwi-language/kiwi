package org.metavm.springconfig;

import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.metavm.context.sql.TransactionIsolation;
import org.metavm.jdbc.TransactionTemplate;
import org.metavm.context.Qualifier;
import org.metavm.context.Bean;
import org.metavm.context.Configuration;
import org.metavm.context.Primary;

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
