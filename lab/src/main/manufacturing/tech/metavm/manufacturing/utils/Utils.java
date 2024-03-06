package tech.metavm.manufacturing.utils;

import tech.metavm.entity.EntityType;
import tech.metavm.lang.NumberUtils;

import javax.annotation.Nullable;
import java.util.Date;
import java.util.function.Predicate;

@EntityType("Utils")
public class Utils {

    public static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }

    public static String randomQrCode(String prefix) {
        return prefix + "_" + (1 + NumberUtils.random(100000000));
    }

    public static <T> @Nullable T find(Iterable<? extends T> iterable, Predicate<? super T> predicate) {
        for (T t : iterable) {
            if (predicate.test(t)) {
                return t;
            }
        }
        return null;
    }

    public static @Nullable Date toDaysNullable(@Nullable Date time) {
        return time == null ? null : toDays(time);
    }

    public static Date toDays(Date time) {
        return new Date(time.getTime() / 86400000 * 86400000);
    }

}
