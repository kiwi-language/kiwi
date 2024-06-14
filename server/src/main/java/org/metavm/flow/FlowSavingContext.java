package org.metavm.flow;

import org.metavm.util.InternalException;

import java.io.Closeable;
import java.util.HashMap;
import java.util.Map;

public class FlowSavingContext {

    public static final Key<Boolean> KEY_SKIP_PREPROCESSING = new Key<>("skipPreprocessing", false);

    private static final ThreadLocal<Config> CONFIG_LOCAL = new ThreadLocal<>();

    public static class Config implements Closeable {
        final Map<String, Object> map = new HashMap<>();

        public void setBoolean(Key<Boolean> key, boolean value) {
            map.put(key.name, value);
        }

        public boolean getBoolean(Key<Boolean> key) {
            return (boolean) map.getOrDefault(key.name, key.defaultValue);
        }

        public <T> T get(Key<T> key) {
            //noinspection unchecked
            return (T) map.getOrDefault(key.name, key.defaultValue);
        }

        @Override
        public void close() {
            CONFIG_LOCAL.remove();
        }
    }

    public static boolean skipPreprocessing() {
        return getConfigItem(KEY_SKIP_PREPROCESSING);
    }

    public static void skipPreprocessing(boolean value) {
        getConfig().setBoolean(KEY_SKIP_PREPROCESSING, value);
    }

    private static <T> T getConfigItem(Key<T> key) {
        return getConfig().get(key);
    }

    private static Config getConfig() {
        var config = CONFIG_LOCAL.get();
        if(config == null) {
            throw new InternalException("Config is not initialized");
        }
        return config;
    }

    public static void initConfig() {
        Config config = new Config();
        CONFIG_LOCAL.set(config);
    }

    public static void clearConfig() {
        CONFIG_LOCAL.remove();
    }

    public static class Key<T> {

        final String name;
        final T defaultValue;

        private Key(String name, T defaultValue) {
            this.name = name;
            this.defaultValue = defaultValue;
        }
    }


}
