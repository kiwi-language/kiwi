package org.metavm.entity;

import org.jetbrains.annotations.NotNull;
import org.metavm.util.Utils;

import java.util.Collection;
import java.util.Comparator;
import java.util.function.Predicate;

public class ReadWriteArray<T> extends ReadonlyArray<T> {

    public ReadWriteArray(Class<T> klass) {
        super(klass);
    }

    @NotNull
    public Object[] toArray() {
        return table.toArray();
    }

    @NotNull
    public <T1> T1[] toArray(@NotNull T1[] a) {
        return table.toArray(a);
    }

    public boolean add(T value) {
        return table.add(value);
    }

    public boolean remove(Object value) {
        return table.remove(value);
    }

    public boolean addAll(@NotNull Collection<? extends T> c) {
        return table.addAll(c);
    }

    public boolean addAll(int index, @NotNull Collection<? extends T> c) {
        return table.addAll(index, c);
    }

    public T set(int index, T value) {
        return table.set(index, value);
    }

    public void add(int index, T value) {
        table.add(index, value);
    }

    public T remove(int index) {
        return table.remove(index);
    }

    public void addAll(Iterable<? extends T> values) {
        Utils.listAddAll(table, values);
    }

    public void clear() {
        table.clear();
    }

    public void sort(Comparator<? super T> comparator) {
        table.sort(comparator);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof ReadWriteArray<?> thatArray)
            return table.equals(thatArray.table);
        else
            return false;
    }

    public boolean removeIf(Predicate<? super T> predicate) {
        return table.removeIf(predicate);
    }
}
