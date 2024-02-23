package tech.metavm.utils;

import java.util.function.Predicate;

public class UserUtils {

    public static <T> boolean nonMatch(Iterable<T> iterable, Predicate<T> predicate) {
        for (T item : iterable) {
            if (predicate.test(item)) {
                return false;
            }
        }
        return true;
    }

}
