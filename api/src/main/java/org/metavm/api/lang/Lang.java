package org.metavm.api.lang;

import javax.annotation.Nullable;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

public class Lang {

    public static Object value;

    private static final Map<String, Object> context = new HashMap<>();

    public static void setContext(String key, Object value) {
        context.put(key, value);
    }

    public static @Nullable Object getContext(String key) {
        return context.get(key);
    }

    public static void print(@Nullable Object message) {
        System.out.println(message);
    }

    public static long random(long bound) {
        return new Random().nextLong(bound);
    }

    public static String formatNumber(String format, long value) {
        return new DecimalFormat(format).format(value);
    }

    public static String secureRandom(int length) {
        return "";
    }

    public static String secureHash(String value, @Nullable String salt) {
        return value;
    }

    public static String getId(Object object) {
        return "";
    }

    public static @Nullable Object getParent(Object object) {
        return null;
    }

    public static Object getRoot(Object object) {
        return value;
    }

    public static String concat(Object o1, Object o2) {
        return Objects.toString(o1) + o2;
    }

}