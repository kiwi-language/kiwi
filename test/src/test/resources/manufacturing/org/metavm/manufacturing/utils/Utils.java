package org.metavm.manufacturing.utils;

import org.metavm.api.Entity;
import org.metavm.api.lang.Lang;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

@Entity
public class Utils {

    public static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }

    public static String randomQrCode(String prefix) {
        return prefix + "_" + (1 + Lang.random(100000000));
    }

    public static <T> T findRequired(Iterable<? extends T> iterable, Predicate<? super T> predicate) {
        Lang.print(iterable);
        T t = find(iterable, predicate);
        if (t != null) {
            return t;
        }
        throw new IllegalArgumentException("Not found");
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

    public static <T> T requireNonNull(@Nullable T object) {
        if(object == null)
            throw new NullPointerException();
        return object;
    }

    public static <T> void setChildren(List<T> list, List<T> newList) {
        list.clear();
        list.addAll(newList);
    }

    public static <T,R> List<R> map(Collection<T> collection, Function<T, R> mapper) {
        var result = new ArrayList<R>();
        for (T t : collection) {
            result.add(mapper.apply(t));
        }
        return result;
    }

}
