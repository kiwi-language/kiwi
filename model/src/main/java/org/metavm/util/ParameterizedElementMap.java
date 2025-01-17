package org.metavm.util;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.WeakHashMap;
import java.util.function.BiConsumer;

public class ParameterizedElementMap<K, V> {

    private final InternalMap<K, V> sharedMap = new InternalMap<>();

    private final ThreadLocal<InternalMap<K, V>> TL = ThreadLocal.withInitial(InternalMap::new);

    public V get(K key) {
        var value = map().get(key);
        return value != null ? value : sharedMap.get(key);
    }

    public V put(K key, V value) {
        if(ThreadConfigs.sharedParameterizedElements())
            return sharedMap.put(key, value);
        else {
            var oldValue = map().put(key, value);
            return oldValue != null ? oldValue : sharedMap.get(key);
        }
    }

    public int size() {
        return sharedMap.size() + map().size();
    }

    public Collection<V> values() {
        var values = new ArrayList<>(sharedMap.values());
        values.addAll(map().values());
        return values;
    }

    public void forEach(BiConsumer<? super K, ? super V> action) {
        sharedMap.forEach(action);
        TL.get().forEach(action);
    }

    private InternalMap<K, V> map() {
        return TL.get();
    }

    private static class InternalMap<K, V> {

        private final WeakHashMap<K, WeakReference<V>> weakMap = new WeakHashMap<>();

        V get(K key) {
            var r = weakMap.get(key);
            return r != null ? r.get() : null;
        }

        V put(K key, V value) {
            var r = weakMap.put(key, new WeakReference<>(value));
            return r != null ? r.get() : null;
        }

        V remove(K key) {
            var r = weakMap.remove(key);
            return r != null ? r.get() : null;
        }

        boolean containsKey(K key) {
            return weakMap.containsKey(key);
        }

        Collection<V> values() {
            return Utils.mapAndFilter(weakMap.values(), Reference::get, Objects::nonNull);
        }

        int size() {
            return weakMap.size();
        }

        void clear() {
            weakMap.clear();
        }

        void forEach(BiConsumer<? super K, ? super V> acton) {
            weakMap.forEach((k, ref) -> {
                var v = ref.get();
                if(v != null)
                    acton.accept(k, v);
            });
        }

    }

}
