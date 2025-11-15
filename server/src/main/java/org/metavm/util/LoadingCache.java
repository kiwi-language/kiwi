package org.metavm.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class LoadingCache<K, V> {

    private final Function<K, V> load;
    private final Map<K, V> map = new ConcurrentHashMap<>();

    public LoadingCache(Function<K, V> load) {
        this.load = load;
    }

    public V get(K key) {
        return map.computeIfAbsent(key, this.load);
    }

    public void invalidate(K key) {
        map.remove(key);
    }


}
