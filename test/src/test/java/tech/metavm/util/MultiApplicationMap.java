package tech.metavm.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class MultiApplicationMap<K, V> {

    private final Map<Long, Map<K, V>> map = new HashMap<>();

    public Map<K, V> getMap(long appId) {
        return map.computeIfAbsent(appId, k -> new HashMap<>());
    }

    public boolean containsKey(long appId, K key) {
        return getMap(appId).containsKey(key);
    }

    public V get(long appId, K key) {
        return getMap(appId).get(key);
    }

    public V put(long appId, K key, V value) {
        return getMap(appId).put(key, value);
    }

    public V remove(long appId, K key) {
        return getMap(appId).remove(key);
    }

    public Collection<V> values(long appId) {
        return getMap(appId).values();
    }

    public void clear() {
        map.clear();
    }

    public void putAll(MultiApplicationMap<K, V> instanceMap) {
        map.putAll(instanceMap.map);
    }
}
