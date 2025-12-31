package org.metavm.config;


import lombok.Getter;
import lombok.SneakyThrows;
import org.metavm.context.Component;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.util.Map;
import java.util.Objects;

@Getter
@Component
public class KiwiConfig {

    public static String CONFIG_PATH;
    private final DbConfig dbConfig;
    private final EsConfig esConfig;
    private final ServerConfig serverConfig;

    /** @noinspection unchecked*/
    public KiwiConfig() {
        var config = getConfig();
        dbConfig = buildDbConfig((Map<String, Object>) config.get("datasource"));
        serverConfig = buildServerConfig((Map<String, Object>) config.get("server"));
        esConfig = buildEsConfig((Map<String, Object>) config.get("es"));
    }

    private EsConfig buildEsConfig(Map<String, Object> config) {
        if (config == null)
            return null;
        var host = (String) config.get("host");
        var port = (int) config.get("port");
        var user = (String) config.get("user");
        var password = Objects.toString(config.get("password"));
        return new EsConfig(host, port, user, password);
    }

    private ServerConfig buildServerConfig(Map<String, Object> config) {
        Objects.requireNonNull(config, "Server config is missing");
        var port = (int) config.getOrDefault("port", 8080);
        return new ServerConfig(port);
    }

    @SneakyThrows
    private Map<String, Object> getConfig() {
        Objects.requireNonNull(CONFIG_PATH, "Config path is not specified");
        var yaml = new Yaml();
        try (var inputStream = new FileInputStream(CONFIG_PATH)) {
            return yaml.load(inputStream);
        }
    }

    private DbConfig buildDbConfig(Map<String, Object> config) {
        if (config == null)
            return null;
        var userName = (String) config.get("username");
        var passwd = Objects.toString(config.get("password"));
        var dbName = (String) config.get("database");
        var host = (String) config.get("host");
        var port = (int) config.get("port");
        return new DbConfig(userName, passwd, dbName, host, port);
    }

    public record DbConfig(String username, String password, String dbName, String host, int port) {

        String url() {
            return String.format("jdbc:postgresql://%s:%d/%s", host, port, dbName);
        }

    }

    public record ServerConfig(int port) {}

    public record EsConfig(String host, int port, String user, String password) {
    }


}
