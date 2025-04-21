package org.metavm.springconfig;

import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.metavm.util.Utils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;

import javax.sql.DataSource;
import java.io.FileInputStream;
import java.util.Map;
import java.util.Objects;

@Configuration
@Slf4j
public class DataSourceConfig {


    public static final String DRIVER_NAME = "org.postgresql.Driver";
    public static final String LOCAL_CONFIG = "config/kiwi.yml";
    public static final String GLOBAL_CONFIG = "/etc/kiwi/kiwi.yml";

    private final DbConfig dbConfig;

    public DataSourceConfig() {
        if (Utils.fileExists(LOCAL_CONFIG))
            dbConfig = loadDbConfig(LOCAL_CONFIG);
        else if (Utils.fileExists(GLOBAL_CONFIG))
            dbConfig = loadDbConfig(GLOBAL_CONFIG);
        else
            throw new RuntimeException("No kiwi configuration file found. Please provide a valid path.");
    }

    private DbConfig loadDbConfig(String configPath) {
        Yaml yaml = new Yaml();
        try (var inputStream = new FileInputStream(configPath)) {
            Map<String, Object> data = yaml.load(inputStream);
            //noinspection unchecked
            var config = (Map<String, Object>) data.get("datasource");
            if (config == null) {
                throw new RuntimeException("Database configuration is absent");
            }
            var userName = (String) config.get("username");
            var passwd = Objects.toString(config.get("password"));
            var dbName = (String) config.get("database");
            var host = (String) config.getOrDefault("host", "127.0.0.1");
            var port = (int) config.getOrDefault("port", 5432);
            return new DbConfig(userName, passwd, dbName, host, port);
        } catch (YAMLException e) {
            throw new RuntimeException("Error parsing YAML file: " + configPath, e);
        } catch (Exception e) {
            throw new RuntimeException("An error occurred: " + e.getMessage(), e);
        }
    }

    @Bean
    @Primary
    public DataSource primaryDataSource(
            @Value("${spring.datasource.primary.hikari.maximum-pool-size}") int maxPoolSize,
            @Value("${spring.datasource.primary.hikari.minimum-idle}") int minIdle
    ) {
        return createDataSource(DRIVER_NAME, dbConfig.username, dbConfig.password, dbConfig.url(), maxPoolSize, minIdle);
    }

    @Bean
    public DataSource secondaryDataSource(
            @Value("${spring.datasource.secondary.hikari.maximum-pool-size}") int maxPoolSize,
            @Value("${spring.datasource.secondary.hikari.minimum-idle}") int minIdle
    ) {
        return createDataSource(DRIVER_NAME, dbConfig.username, dbConfig.password, dbConfig.url(), maxPoolSize, minIdle);
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


    private record DbConfig(String username, String password, String dbName, String host, int port) {

        String url() {
            return String.format("jdbc:postgresql://%s:%d/%s", host, port, dbName);
        }

    }
}
