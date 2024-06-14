package org.metavm.util;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Set;

public class IdentitySet<T> implements Set<T> {

    public static <T> IdentitySet<T> of(T t1) {
        IdentitySet<T> set = new IdentitySet<>();
        set.add(t1);
        return set;
    }

    public static <T> IdentitySet<T> of(T t1, T t2) {
        IdentitySet<T> set = new IdentitySet<>();
        set.add(t1);
        set.add(t2);
        return set;
    }

    public static <T> IdentitySet<T> of(T t1, T t2, T t3) {
        IdentitySet<T> set = new IdentitySet<>();
        set.add(t1);
        set.add(t2);
        set.add(t3);
        return set;
    }

    public static <T> IdentitySet<T> of(T t1, T t2, T t3, T t4) {
        IdentitySet<T> set = new IdentitySet<>();
        set.add(t1);
        set.add(t2);
        set.add(t3);
        set.add(t4);
        return set;
    }

    public static <T> IdentitySet<T> of(T t1, T t2, T t3, T t4, T t5) {
        IdentitySet<T> set = new IdentitySet<>();
        set.add(t1);
        set.add(t2);
        set.add(t3);
        set.add(t4);
        set.add(t5);
        return set;
    }

    private final IdentityHashMap<T, T> map = new IdentityHashMap<>();

    public IdentitySet() {
    }

    public IdentitySet(Iterable<? extends T> iterable) {
        iterable.forEach(this::add);
    }

    @Override
    public boolean add(T t) {
        return map.put(t, t) == null;
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return map.containsKey(o);
    }

    @NotNull
    @Override
    public Iterator<T> iterator() {
        return map.keySet().iterator();
    }

    @NotNull
    @Override
    public Object[] toArray() {
        return map.keySet().toArray();
    }

    @NotNull
    @Override
    public <T1> T1[] toArray(@NotNull T1[] a) {
        return map.keySet().toArray(a);
    }

    public boolean remove(Object t) {
        return map.remove(t) != null;
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        return map.keySet().containsAll(c);
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends T> c) {
        boolean changed = false;
        for (T t : c) {
            boolean added = add(t);
            if(added && !changed) {
                changed = true;
            }
        }
        return changed;
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        return map.keySet().retainAll(c);
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        return map.keySet().removeAll(c);
    }

    @Override
    public void clear() {
        map.clear();
    }

}
