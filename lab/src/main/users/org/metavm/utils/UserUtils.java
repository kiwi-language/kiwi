package org.metavm.utils;

import org.metavm.api.Entity;

import java.util.function.Predicate;

@Entity
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
