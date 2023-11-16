package tech.metavm.springconfig;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.wall.WallConfig;
import com.alibaba.druid.wall.WallFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {

    @Value("${spring.datasource.url}")
    private String url;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Value("${spring.datasource.driver-class-name}")
    private String driverClassName;

    private static final int MAX_ACTIVE = 10;

    private DataSource dataSource;

    private PlatformTransactionManager transactionManager;


    @Bean
    public DataSource getDataSource() {
        if(dataSource == null) {
            DruidDataSource dataSource = new DruidDataSource();
            dataSource.setUsername(username);
            dataSource.setPassword(password);
            dataSource.setMaxActive(MAX_ACTIVE);
            dataSource.setUrl(url);
            dataSource.setDriverClassName(driverClassName);
            this.dataSource = dataSource;
        }
        return dataSource;
    }

    @Bean
    public WallFilter wallFilter() {
        WallFilter wallFilter = new WallFilter();
        wallFilter.setConfig(wallConfig());
        return wallFilter;
    }

    @Bean
    public WallConfig wallConfig() {
        WallConfig wallConfig = new WallConfig();
        wallConfig.setMultiStatementAllow(true);
        return wallConfig;
    }

    @Bean
    public PlatformTransactionManager transactionManager() {
        if(transactionManager == null) {
            transactionManager = new DataSourceTransactionManager(getDataSource());
        }
        return transactionManager;
    }

    @Bean
    public TransactionTemplate transactionTemplate() {
        return new TransactionTemplate(transactionManager());
    }

}
