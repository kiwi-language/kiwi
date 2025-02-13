package anonymous_class;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AnonymousClassFoo<K,V> {

    public static <K, V> AnonymousClassFoo<K, V> create(List<EntryDTO<K, V>> entries) {
        var foo = new AnonymousClassFoo<K, V>();
        for (EntryDTO<K, V> entry : entries) {
            foo.new Entry(entry.key(), entry.value());
        }
        return foo;
    }

    private final List<Entry> entries = new ArrayList<>();


    public Iterable<K> keysSnapshot() {
        var a0 = new ArrayList<>(entries);
        return new Iterable<K>() {

            @NotNull
            @Override
            public Iterator<K> iterator() {
                return new Iterator<>() {

                    private final Iterator<Entry> i = a0.iterator();

                    @Override
                    public boolean hasNext() {
                        return i.hasNext();
                    }

                    @Override
                    public K next() {
                        return i.next().key();
                    }
                };
            }
        };
    }

    @SuppressWarnings("StringConcatenationInLoop")
    public String concatKeys() {
        var it = keysSnapshot().iterator();
        var r = "";
        while (it.hasNext()) {
            if(!r.isEmpty())
                r = r + ",";
            r = r + it.next();
        }
        return r;
    }

    public class Entry {
        private final K key;
        private final V value;

        public Entry(K key, V value) {
            this.key = key;
            this.value = value;
            entries.add(this);
        }

        public K key() {
            return key;
        }

        public V value() {
            return value;
        }


    }

}
