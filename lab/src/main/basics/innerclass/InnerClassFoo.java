package innerclass;

import org.metavm.api.Entity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class InnerClassFoo<K,V> {

    private final List<Entry<K,V>> entries = new ArrayList<>();

    public void addEntry(K key, V value) {
        entries.add(new Entry<>(key, value));
    }

    public class Entry<K,V> {
        private K key;
        private V value;

        public Entry(K key, V value) {
            this.key = key;
            this.value = value;
        }

        public K getKey() {
            return key;
        }

        public void setKey(K key) {
            this.key = key;
        }

        public V getValue() {
            return value;
        }

        public void setValue(V value) {
            this.value = value;
        }
    }

    public Iterator<Entry<K,V>> iterator() {
        return new MyIterator();
    }

    public Entry<K,V> first() {
        var it = iterator();
        if(it.hasNext())
            return it.next();
        throw new IllegalStateException();
    }

    @Entity(ephemeral = true)
    public class MyIterator implements Iterator<Entry<K,V>> {

        private final Iterator<Entry<K,V>> it;

        public MyIterator() {
            this(entries.iterator());
        }

        public MyIterator(Iterator<Entry<K,V>> it) {
            this.it = it;
        }

        @Override
        public boolean hasNext() {
            return it.hasNext();
        }

        @Override
        public Entry<K, V> next() {
            return it.next();
        }

        public MyIterator copy() {
            return new MyIterator();
        }

    }

}
