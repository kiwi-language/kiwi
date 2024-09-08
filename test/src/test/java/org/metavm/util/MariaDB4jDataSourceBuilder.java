package org.metavm.util;

import ch.vorburger.exec.ManagedProcessException;
import ch.vorburger.mariadb4j.DB;
import ch.vorburger.mariadb4j.DBConfigurationBuilder;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class MariaDB4jDataSourceBuilder {

    static {
        try {
            DBConfigurationBuilder config = DBConfigurationBuilder.newBuilder();
            config.setPort(3307);
            config.setUnpackingFromClasspath(false);
            config.setLibDir(System.getProperty("java.io.tmpdir") + "/MariaDB4j/no-libs");
            config.setBaseDir("/opt/homebrew");
            DB db = DB.newEmbeddedDB(config.build());
            db.start();

            System.out.println("Data dir: " + db.getConfiguration().getDataDir());
        } catch (ManagedProcessException e) {
            throw new RuntimeException(e);
        }

    }

    public static DataSource getDataSource() {
        var dataSource = new HikariDataSource();
        dataSource.setDriverClassName(com.mysql.cj.jdbc.Driver.class.getName());
        dataSource.setJdbcUrl("jdbc:mysql://localhost");
        dataSource.setUsername("root");
        dataSource.setPassword("85263670");
        return dataSource;
    }

    public static void main(String[] args) throws SQLException {
        DataSource dataSource = getDataSource();
        Connection connection = dataSource.getConnection();
        Statement statement = connection.createStatement();
        statement.execute("use object");

        ResultSet resultSet = statement.executeQuery("show tables ");

        while (resultSet.next()) {
            System.out.println(resultSet.getString(1));
        }
    }

}
