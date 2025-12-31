package org.metavm;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.metavm.config.ConfigException;
import org.metavm.config.KiwiConfig;
import org.metavm.config.Mode;
import org.metavm.context.ApplicationContext;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.util.Map;
import java.util.Objects;

@Slf4j
public class ObjectApplication {

    public static void main(String[] args) {
        parseArgs(args);
        var mode = getMode().name().toLowerCase();
        log.info("Mode: {}", mode);
        ApplicationContext.start(mode);
    }

    private static Mode getMode() {
        var config = getConfig();
        var mode = (String) config.get("mode");
        return switch (mode) {
            case "memory" -> Mode.MEMORY;
            case null -> Mode.MEMORY;
            case "persistent" -> Mode.PERSISTENT;
            default -> throw new ConfigException("Unknown mode: " + mode);
        };
    }

    @SneakyThrows
    private static Map<String, Object> getConfig() {
        Objects.requireNonNull(KiwiConfig.CONFIG_PATH, "Config path is not specified");
        var yaml = new Yaml();
        try (var inputStream = new FileInputStream(KiwiConfig.CONFIG_PATH)) {
            return yaml.load(inputStream);
        }
    }

    private static void parseArgs(String[] args) {
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-config")) {
                if (i >= args.length - 1) {
                    System.err.println("Invalid options");
                    System.exit(1);
                }
                KiwiConfig.CONFIG_PATH = args[++i];
            }
        }
    }

}
