package org.metavm.context;

import lombok.SneakyThrows;
import org.yaml.snakeyaml.Yaml;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.tools.StandardLocation;
import java.io.StringReader;
import java.util.Map;

public class AppConfig {

    private static final String CONFIG_PATH = "application.yml";

    @SneakyThrows
    public static AppConfig load(ProcessingEnvironment processingEnv) {
        var file = processingEnv.getFiler().getResource(StandardLocation.CLASS_OUTPUT, "", CONFIG_PATH);
        processingEnv.getMessager().printNote("App config last modified: " + file.getLastModified());
        String content = null;
        if (file.getLastModified() > 0)
            content = file.getCharContent(true).toString();
        else {
            var input = AppConfig.class.getResourceAsStream("/" + CONFIG_PATH);
            if (input != null) {
                try (var in = input) {
                    content = new String(in.readAllBytes());
                }
            }
        }
        if (content == null)
            return new AppConfig(Map.of());
        return new AppConfig(new Yaml().load(new StringReader(content)));
    }

    private final Map<String, Object> map;

    private AppConfig(Map<String, Object> map) {
        this.map = map;
    }

    public Object get(String key, Element context) {
        var parts = key.split("\\.");
        Object value = map;
        for (String part : parts) {
            if (value instanceof Map<?,?> subMap)
                value = subMap.get(part);
            else
                throw new ContextConfigException("Config key '" + key + "' not found", context);
        }
        return value;
    }

    public String getString(String key, Element context) {
        var v = get(key, context);
        return v != null ? v.toString() : null;
    }

    public boolean getBoolean(String key, Element context) {
        return (boolean) get(key, context);
    }

    public int getInt(String key, Element context) {
        return (int) get(key, context);
    }

    public long getLong(String key, Element context) {
        return (long) get(key, context);
    }

}
