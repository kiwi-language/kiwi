package org.metavm.object.instance.core;

import com.google.common.collect.Iterators;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Consumer;

class RemovalSet implements Collection<DurableInstance> {
    final Set<DurableInstance> views = new HashSet<>();
    final Set<DurableInstance> instances = new HashSet<>();

    public boolean add(DurableInstance instance) {
        if (instance.isView())
            return views.add(instance);
        else
            return instances.add(instance);
    }

    @Override
    public boolean remove(Object o) {
        if (o instanceof DurableInstance i)
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
    public boolean addAll(@NotNull Collection<? extends DurableInstance> c) {
        boolean added = false;
        for (DurableInstance i : c) {
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
        return o instanceof DurableInstance i && (i.isView() ? views.contains(i) : instances.contains(i));
    }

    @NotNull
    @Override
    public Iterator<DurableInstance> iterator() {
        return Iterators.concat(views.iterator(), instances.iterator());
    }

    @NotNull
    @Override
    public Object[] toArray() {
        var array = new Object[size()];
        int i = 0;
        for (DurableInstance view : views) {
            array[i++] = view;
        }
        for (DurableInstance inst : instances) {
            array[i++] = inst;
        }
        return array;
    }

    @NotNull
    @Override
    public <T> T[] toArray(@NotNull T[] a) {
        int i = 0;
        for (DurableInstance view : views) {
            a[i++] = (T) view;
        }
        for (DurableInstance inst : instances) {
            a[i++] = (T) inst;
        }
        return a;
    }

    public void forEach(Consumer<? super DurableInstance> action) {
//          Remove views first otherwise uninitialized views in the removal set may fail to initialize
        views.forEach(action);
        instances.forEach(action);
    }

}
