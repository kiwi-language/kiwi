package org.metavm.util;

public class ThreadConfigs {

    private static final ThreadLocal<Config> TL = ThreadLocal.withInitial(Config::new);

    public static boolean sharedParameterizedElements() {
        return config().sharedParameterizedElements;
    }

    public static void sharedParameterizedElements(boolean b) {
        config().sharedParameterizedElements = b;
    }

    private static Config config() {
        return TL.get();
    }

    private static class Config {
        private boolean sharedParameterizedElements = false;

    }
}
