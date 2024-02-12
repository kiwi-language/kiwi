package tech.metavm.entity;

import tech.metavm.builtin.IndexDef;

import java.util.List;
import java.util.function.Predicate;

public class IndexUtils {

    public static <R, I extends Index<R>> long count(I from, I to) {
        return 0L;
    }

    public static <R, I extends Index<R>> List<R> scan(I from, I to) {
        return List.of();
    }

    public static <R, I extends Index<R>> List<R> select(I key) {
        return List.of();
    }

    public static <R, I extends Index<R>> R selectFirst(I key) {
        return null;
    }

    public static <R, I extends Index<R>> long count(Class<I> klass, Predicate<I> filter) {
        return 0L;
    }

    public static <R, I extends Index<R>> List<R> query(Class<I> klass, Predicate<IndexDef<R>> filter) {
        return List.of();
    }

}
