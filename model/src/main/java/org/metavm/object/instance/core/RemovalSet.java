package org.metavm.object.instance.core;

import com.google.common.collect.Iterators;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Consumer;

@Slf4j
class RemovalSet implements Collection<Instance> {
    final Set<Instance> views = new HashSet<>();
    final Set<Instance> instances = new HashSet<>();

    public boolean add(Instance instance) {
        if (instance.isView())
            return views.add(instance);
        else
            return instances.add(instance);
    }

    @Override
    public boolean remove(Object o) {
        if (o instanceof Instance i)
            return i.isView() ? views.remove(i) : instances.remove(i);
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
        var r = views.retainAll(c);
        return instances.retainAll(c) || r;
    }

    @Override
    public void clear() {
        views.clear();
        instances.clear();
        ;
    }

    @Override
    public int size() {
        return views.size() + instances.size();
    }

    @Override
    public boolean isEmpty() {
        return views.isEmpty() && instances.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return o instanceof Instance i && (i.isView() ? views.contains(i) : instances.contains(i));
    }

    @NotNull
    @Override
    public Iterator<Instance> iterator() {
        return Iterators.concat(views.iterator(), instances.iterator());
    }

    @NotNull
    @Override
    public Object[] toArray() {
        var array = new Object[size()];
        int i = 0;
        for (Instance view : views) {
            array[i++] = view;
        }
        for (Instance inst : instances) {
            array[i++] = inst;
        }
        return array;
    }

    @NotNull
    @Override
    public <T> T[] toArray(@NotNull T[] a) {
        int i = 0;
        for (Instance view : views) {
            a[i++] = (T) view;
        }
        for (Instance inst : instances) {
            a[i++] = (T) inst;
        }
        return a;
    }

    public void forEach(Consumer<? super Instance> action) {
//          Remove views first otherwise uninitialized views in the removal set may fail to initialize
        views.forEach(action);
        instances.forEach(action);
    }

}
