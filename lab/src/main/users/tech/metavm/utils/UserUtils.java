package tech.metavm.utils;

import tech.metavm.entity.EntityType;

import java.util.function.Predicate;

@EntityType("用户工具")
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
