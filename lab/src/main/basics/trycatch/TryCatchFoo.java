package trycatch;

import org.metavm.api.ChildEntity;
import org.metavm.api.lang.Lang;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.function.BiConsumer;

public class TryCatchFoo<K, V> {

    @ChildEntity
    private final List<Entry<K,V>> entries = new ArrayList<>();

    public void put(K key, V value) {
        entries.add(new Entry<>(key, value));
    }

    public void forEach(BiConsumer<K, V> action) {
        for (var entry : entries) {
            K key;
            V value;
            try {
                key = entry.getKey();
                value = entry.getValue();
            } catch (IllegalStateException | NullPointerException ise) {
                // this usually means the entry is no longer in the map.
                throw new ConcurrentModificationException(ise);
            }
            action.accept(key, value);
        }
    }

    public void print() {
        forEach((k,v) -> Lang.print(k.toString()  + ":" + v.toString()));
    }

    private static class Entry<K,V> {
        private final K key;
        private final V value;

        public Entry(K key, V value) {
            this.key = key;
            this.value = value;
        }

        public K getKey() {
            return key;
        }

        public V getValue() {
            return value;
        }

    }

}
