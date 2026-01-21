package org.manul.context;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.SneakyThrows;
import org.manul.jdbc.TransactionManager;
import org.manul.jdbc.TransactionTemplate;

import javax.sql.DataSource;

@Configuration(module = "test")
public class TestDataSourceConfig {

    @SneakyThrows
    @Bean
    public DataSource dataSource() {
        var config = new HikariConfig();
        config.setJdbcUrl("jdbc:h2:mem:my_shared_db");
        config.setMinimumIdle(1);
        config.setMaximumPoolSize(8);
        return new HikariDataSource(config);
    }

    @Bean
    public TransactionManager transactionManager(DataSource dataSource) {
        return new TransactionManager(dataSource);
    }

    @Bean
    public TransactionTemplate transactionTemplate(DataSource dataSource) {
        return new TransactionTemplate(dataSource);
    }

}
