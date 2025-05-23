package org.metavm.api;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Function;

public class Index<K, V> {

    private static Object value;
    private final String name;

    public Index(boolean unique, Function<V, K> keyComputer) {
        this("<unknown>", unique, keyComputer);
    }

    public Index(String name, boolean unique, Function<V, K> keyComputer) {
        this.name = name;
    }

    public List<V> query(K min, K max) {
        return List.of();
    }

    public long count(K min, K max) {
        return 0L;
    }

    public List<V> get(K key) {
        return List.of();
    }

    public @Nullable V getFirst(K key) {
        return (V) value;
    }

    public boolean isUnique() {
        return false;
    }
}
