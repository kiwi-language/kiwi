package anonymous_class;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.ChildEntity;
import org.metavm.api.ChildList;

import java.util.ArrayList;
import java.util.Iterator;

public record AnonymousClassFoo<K,V>(@ChildEntity ChildList<Entry<K,V>> entries) {

    public Iterable<K> keysSnapshot() {
        var a0 = new ArrayList<>(entries);
        return new Iterable<K>() {

            @NotNull
            @Override
            public Iterator<K> iterator() {
                return new Iterator<>() {

                    private final Iterator<Entry<K,V>> i = a0.iterator();

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

    public record Entry<K,V>(K key, V value) {
    }

}
