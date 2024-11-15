package org.metavm.object.instance.core;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Consumer;

@Slf4j
class RemovalSet implements Collection<Instance> {
    final Set<Instance> instances = new HashSet<>();

    public boolean add(Instance instance) {
        return instances.add(instance);
    }

    @Override
    public boolean remove(Object o) {
        if (o instanceof Instance i)
            return instances.remove(i);
        else
            return false;
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        for (Object o : c) {
            if (!contains(o))
                return false;
        }
        return true;
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends Instance> c) {
        boolean added = false;
        for (Instance i : c) {
            if (add(i))
                added = true;
        }
        return added;
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        boolean changed = false;
        for (Object o : c) {
            if (remove(o))
                changed = true;
        }
        return changed;
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        return instances.retainAll(c);
    }

    @Override
    public void clear() {
        instances.clear();
        ;
    }

    @Override
    public int size() {
        return instances.size();
    }

    @Override
    public boolean isEmpty() {
        return instances.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return o instanceof Instance i && instances.contains(i);
    }

    @NotNull
    @Override
    public Iterator<Instance> iterator() {
        return instances.iterator();
    }

    @NotNull
    @Override
    public Object[] toArray() {
        return instances.toArray();
    }

    @NotNull
    @Override
    public <T> T[] toArray(@NotNull T[] a) {
        return instances.toArray(a);
    }

    public void forEach(Consumer<? super Instance> action) {
        instances.forEach(action);
    }

}
