package tech.metavm.util;

import tech.metavm.object.instance.Instance;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class MultiTenantMap<K, V> {

    private final Map<Long, Map<K, V>> map = new HashMap<>();

    public Map<K, V> getMap(long tenantId) {
        return map.computeIfAbsent(tenantId, k -> new HashMap<>());
    }

    public boolean containsKey(long tenantId, K key) {
        return getMap(tenantId).containsKey(key);
    }

    public V get(long tenantId, K key) {
        return getMap(tenantId).get(key);
    }

    public V put(long tenantId, K key, V value) {
        return getMap(tenantId).put(key, value);
    }

    public V remove(long tenantId, K key) {
        return getMap(tenantId).remove(key);
    }

    public Collection<V> values(long tenantId) {
        return getMap(tenantId).values();
    }
}
