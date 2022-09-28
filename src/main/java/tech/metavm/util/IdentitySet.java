package tech.metavm.util;

import java.util.IdentityHashMap;

public class IdentitySet<T> {

    private final IdentityHashMap<T, T> map = new IdentityHashMap<>();

    public boolean add(T t) {
        return map.put(t, t) != null;
    }

    public boolean contains(T o) {
        return map.containsKey(o);
    }

    public boolean remove(T t) {
        return map.remove(t) != null;
    }

}
