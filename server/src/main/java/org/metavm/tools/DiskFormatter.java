package org.metavm.tools;

import com.alibaba.druid.pool.DruidDataSource;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.MatchAllQueryBuilder;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.metavm.util.Constants;
import org.metavm.util.InternalException;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

import static org.metavm.util.NncUtils.requireNonNull;

public class DiskFormatter {

    private static final String CONFIG_HOST = "host";
    private static final String CONFIG_ES_PORT = "es_port";
    private static final String CONFIG_REDIS_PORT = "redis_port";
    private static final String CONFIG_DB_USER = "db_user";
    private static final String CONFIG_DB_DRIVER = "db_driver";
    private static final String CONFIG_DB_PASSWORD = "db_password";
    private static final String CONFIG_JDBC_URL = "db_jdbc_url";
    private static final String CONFIG_DELETE_ID_FILES = "delete_id_files";
    private static final String CONFIG_CLEAR_DB = "delete_clear_db";
    private static final String CONFIG_REBOOT = "reboot";

    public static final Map<String, Object> DEV_CONFIG = Map.of(
            CONFIG_HOST, "127.0.0.1",
            CONFIG_ES_PORT, 7002,
            CONFIG_REDIS_PORT, 7003,
            CONFIG_DELETE_ID_FILES, false,
            CONFIG_CLEAR_DB, true,
            CONFIG_DB_USER, "postgres",
            CONFIG_DB_PASSWORD, "85263670",
            CONFIG_JDBC_URL, "jdbc:postgresql://127.0.0.1:7001/object",
            CONFIG_DB_DRIVER, "org.postgresql.Driver",
            CONFIG_REBOOT, false
    );

    public static final Map<String, Object> LOCAL_CONFIG = Map.of(
            CONFIG_HOST, "127.0.0.1",
            CONFIG_ES_PORT, 9200,
            CONFIG_REDIS_PORT, 6379,
            CONFIG_DELETE_ID_FILES, false,
            CONFIG_CLEAR_DB, true,
            CONFIG_DB_USER, "postgres",
            CONFIG_DB_PASSWORD, "85263670",
            CONFIG_JDBC_URL, "jdbc:postgresql://127.0.0.1:5432/object",
            CONFIG_DB_DRIVER, "org.postgresql.Driver",
            CONFIG_REBOOT, false
    );

    public static final Map<String, Object> CONFIG = LOCAL_CONFIG;

//    public static final String HOST = "localhost";

    private static String host() {
        return (String) CONFIG.get(CONFIG_HOST);
    }

    private static int esPort() {
        return (int) CONFIG.get(CONFIG_ES_PORT);
    }

    private static boolean shouldDeleteIdFiles() {
        return (boolean) CONFIG.get(CONFIG_DELETE_ID_FILES);
    }

    private static boolean shouldReboot() {
        return (boolean) CONFIG.get(CONFIG_REBOOT);
    }

    private static void clearDataBases() {
        if ((boolean) CONFIG.get(CONFIG_CLEAR_DB)) {
            try (DruidDataSource dataSource = new DruidDataSource()) {
                dataSource.setDriverClassName((String) CONFIG.get(CONFIG_DB_DRIVER));
                dataSource.setUrl((String) CONFIG.get(CONFIG_JDBC_URL));
                dataSource.setUsername((String) CONFIG.get(CONFIG_DB_USER));
                dataSource.setPassword((String) CONFIG.get(CONFIG_DB_PASSWORD));
                dataSource.setMaxActive(1);

                try (Connection connection = dataSource.getConnection();
                     Statement statement = connection.createStatement()) {
                    statement.execute("delete from id_block");
//                statement.execute("delete from id_region");
                    statement.execute("delete from instance");
                    statement.execute("delete from reference");
                    statement.execute("delete from index_entry");
                    statement.execute("delete from files");
                    statement.execute("delete from id_sequence");
                } catch (SQLException e) {
                    throw new InternalException("SQL Error", e);
                }
            }
        }
    }

    private static void clearRedis() {
        if (CONFIG.containsKey(CONFIG_REDIS_PORT)) {
            var config = new RedisStandaloneConfiguration(
                    (String) CONFIG.get(CONFIG_HOST),
                    (int) CONFIG.get(CONFIG_REDIS_PORT)
            );
            var connectionFactory = new JedisConnectionFactory(config);
            connectionFactory.afterPropertiesSet();
            try (var connection = connectionFactory.getConnection()) {
                connection.serverCommands().flushAll();
            }
        }
    }

    private static void clearEs() {
        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials("elastic", "85263670"));

        RestClientBuilder builder = RestClient.builder(new HttpHost(host(), esPort()))
                .setHttpClientConfigCallback(b -> b.setDefaultCredentialsProvider(credentialsProvider));
        //noinspection deprecation
        try (RestHighLevelClient client = new RestHighLevelClient(builder)) {
            DeleteByQueryRequest request = new DeleteByQueryRequest("instance");
            request.setQuery(new MatchAllQueryBuilder());
            BulkByScrollResponse response = client.deleteByQuery(request, RequestOptions.DEFAULT);
            System.out.println("Deleted documents: " + response.getCreated());
        } catch (IOException e) {
            throw new InternalException("Elasticsearch error", e);
        }
    }

    private static void deleteIdFiles() {
        var dirs = List.of(Constants.RESOURCE_CP_ROOT, Constants.RESOURCE_TARGET_CP_ROOT);
        for (String dir : dirs) {
            String idDirPath = dir + "/id";
            File idDir = new File(idDirPath);
            if (idDir.exists()) {
                for (String idFile : requireNonNull(idDir.list())) {
                    if (!new File(idDirPath + "/" + idFile).delete()) {
                        System.err.println("Fail to delete id file '" + idFile + "'");
                    }
                }
            }
        }
    }

    private static void clearColumnFile() {
        String file = Constants.RESOURCE_CP_ROOT + "/column/columns.properties";
        try {
            new PrintWriter(file).close();
        } catch (IOException e) {
            throw new RuntimeException("Fail to clear column file", e);
        }
    }

    private static void clearTypeTagsFile() {
        String file = Constants.RESOURCE_CP_ROOT + "/typeTags/typeTags.properties";
        try {
            new PrintWriter(file).close();
        } catch (IOException e) {
            throw new RuntimeException("Fail to clear type tags file", e);
        }
    }


    public static void main(String[] args) {
        clearEs();
        clearRedis();
        clearDataBases();
        if (shouldDeleteIdFiles()) {
            deleteIdFiles();
            clearColumnFile();
            clearTypeTagsFile();
        }
        if (shouldReboot()) {
            System.out.println("Rebooting...");
            Rebooter.reboot();
            System.out.println("Rebooted");
        }
    }

}
