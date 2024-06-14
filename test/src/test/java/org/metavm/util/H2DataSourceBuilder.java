package org.metavm.util;

import com.alibaba.druid.pool.DruidDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class H2DataSourceBuilder {

    public static DataSource getH2DataSource() {
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setDriverClassName(org.h2.Driver.class.getName());
        dataSource.setUrl("jdbc:h2:mem:myDb;DB_CLOSE_DELAY=-1");
        return dataSource;
    }

    public static void main(String[] args) throws SQLException {
        DataSource dataSource = getH2DataSource();
        Connection connection = dataSource.getConnection();

        Statement statement = connection.createStatement();
        statement.execute("""
                CREATE TABLE `instance` (
                  `id` bigint NOT NULL,
                  `app_id` bigint NOT NULL,
                  `type_id` bigint NOT NULL,
                  `title` varchar(64) DEFAULT NULL,
                  `data` text DEFAULT NULL,
                  `version` bigint NOT NULL DEFAULT '0',
                  `sync_version` bigint NOT NULL DEFAULT '0',
                  `deleted_at` bigint NOT NULL DEFAULT '0',
                  PRIMARY KEY (`id`)
                )""");

        statement.executeUpdate("""
                insert into instance (id, app_id, type_id, title, `data`) values 
                (1, -1, 100, 'Big Foo', '{s0: "Big Foo"}')
        """);

        ResultSet rs = statement.executeQuery("select * from instance where id = 1");

        while (rs.next()) {
            System.out.println(rs.getString("title"));
        }
    }

}
