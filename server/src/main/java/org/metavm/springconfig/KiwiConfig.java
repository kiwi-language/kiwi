package org.metavm.springconfig;


import org.metavm.util.Utils;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.FileInputStream;
import java.util.Map;
import java.util.Objects;

@Component
public class KiwiConfig {

    public static final String DRIVER_NAME = "org.postgresql.Driver";
    public static final String LOCAL_CONFIG = "config/kiwi.yml";
    public static final String GLOBAL_CONFIG = "/etc/kiwi/kiwi.yml";
    private final DbConfig dbConfig;
    private final ServerConfig serverConfig;

    /** @noinspection unchecked*/
    public KiwiConfig() {
        var config = getConfig();
        dbConfig = buildDbConfig((Map<String, Object>) config.get("datasource"));
        serverConfig = buildServerConfig((Map<String, Object>) config.get("server"));
    }

    private ServerConfig buildServerConfig(Map<String, Object> config) {
        if (config == null)
            throw new RuntimeException("server config is missing");
        var port = (int) config.getOrDefault("port", 8080);
        return new ServerConfig(port);
    }

    private Map<String, Object> getConfig() {
        if (Utils.fileExists(LOCAL_CONFIG))
            return getConfig(LOCAL_CONFIG);
        else if (Utils.fileExists(GLOBAL_CONFIG))
            return getConfig(GLOBAL_CONFIG);
        else
            throw new RuntimeException("No kiwi configuration file found. Please provide a valid path.");
    }

    private Map<String, Object> getConfig(String configPath) {
        var yaml = new Yaml();
        try (var inputStream = new FileInputStream(configPath)) {
            return yaml.load(inputStream);
        } catch (YAMLException e) {
            throw new RuntimeException("Error parsing YAML file: " + configPath, e);
        } catch (Exception e) {
            throw new RuntimeException("An error occurred: " + e.getMessage(), e);
        }
    }

    private DbConfig buildDbConfig(Map<String, Object> config) {
        if (config == null)
            throw new RuntimeException("datasource config is missing");
        var userName = (String) config.get("username");
        var passwd = Objects.toString(config.get("password"));
        var dbName = (String) config.get("database");
        var host = (String) config.getOrDefault("host", "127.0.0.1");
        var port = (int) config.getOrDefault("port", 5432);
        return new DbConfig(userName, passwd, dbName, host, port);
    }

    public DbConfig getDbConfig() {
        return dbConfig;
    }

    public ServerConfig getServerConfig() {
        return serverConfig;
    }

    public record DbConfig(String username, String password, String dbName, String host, int port) {

        String url() {
            return String.format("jdbc:postgresql://%s:%d/%s", host, port, dbName);
        }

    }

    public record ServerConfig(int port) {}


}
