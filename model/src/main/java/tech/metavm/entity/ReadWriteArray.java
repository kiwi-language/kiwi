package tech.metavm.entity;

import org.jetbrains.annotations.NotNull;
import tech.metavm.util.NncUtils;
import tech.metavm.util.TypeReference;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;

@SuppressWarnings("NullableProblems")
public class ReadWriteArray<T> extends ReadonlyArray<T> implements List<T> {

    public ReadWriteArray(Class<T> klass) {
        super(klass);
    }

    public ReadWriteArray(TypeReference<T> typeRef) {
        super(typeRef);
    }

    public ReadWriteArray(Class<T> klass, Collection<T> data) {
        super(klass, data);
    }

    public ReadWriteArray(TypeReference<T> typeRef, Collection<T> data) {
        super(typeRef, data);
    }

    public ReadWriteArray(Type  type) {
        super(type);
    }

    public ReadWriteArray() {}

    @Override
    protected Class<?> getRawClass() {
        return ReadWriteArray.class;
    }

    @SuppressWarnings("NullableProblems")
    @NotNull
    @Override
    public Object[] toArray() {
        return table.toArray();
    }

    @SuppressWarnings("NullableProblems")
    @NotNull
    @Override
    public <T1> T1[] toArray(@NotNull T1[] a) {
        return table.toArray(a);
    }

    public boolean add(T value) {
        return table.add(value);
    }

    public boolean remove(Object value) {
        return table.remove(value);
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        return table.containsAll(c);
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends T> c) {
        return table.addAll(c);
    }

    @Override
    public boolean addAll(int index, @NotNull Collection<? extends T> c) {
        return table.addAll(index, c);
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        return table.removeAll(c);
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        return table.retainAll(c);
    }

    @Override
    public T set(int index, T value) {
        return table.set(index, value);
    }

    @Override
    public void add(int index, T value) {
        table.add(index, value);
    }

    @Override
    public T remove(int index) {
        return table.remove(index);
    }

    @NotNull
    @Override
    public ListIterator<T> listIterator() {
        return table.listIterator();
    }

    @NotNull
    @Override
    public ListIterator<T> listIterator(int index) {
        return table.listIterator(index);
    }

    public void addAll(Iterable<? extends T> values) {
        NncUtils.listAddAll(table, values);
    }

    @Override
    public void clear() {
        table.clear();
    }

    public void reset(Iterable<? extends T> values) {
        clear();
        addAll(values);
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
}
