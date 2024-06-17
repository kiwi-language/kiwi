package org.metavm.api.lang;

import javax.annotation.Nullable;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Lang {

    private static final Map<String, Object> context = new HashMap<>();

    public static void setContext(String key, Object value) {
        context.put(key, value);
    }

    public static Object getContext(String key) {
        return context.get(key);
    }

    public static void delete(Object obj) {}

    public static void print(@Nullable Object message) {
        System.out.println(message);
    }

    public static long random(long bound) {
        return new Random().nextLong(bound);
    }

    public static String formatNumber(String format, long value) {
        return new DecimalFormat(format).format(value);
    }
}