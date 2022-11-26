package tech.metavm.util;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.BiConsumer;

public class KeyValueList<K, V> {

    private final LinkedHashMap<K, V> map = new LinkedHashMap<>();

    public List<KeyValue<K, V>> values() {
        return NncUtils.map(map.entrySet(), entry -> new KeyValue<>(entry.getKey(), entry.getValue()));
    }

    public void add(K key, V value) {
        map.put(key, value);
    }

    public void remove(K key) {
        map.remove(key);
    }

    public void forEach(BiConsumer<? super K, ? super V> action) {
        map.forEach(action);
    }

}

