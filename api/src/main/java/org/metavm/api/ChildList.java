package org.metavm.api;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

public class ChildList<T> extends AbstractList<T> {

    private final List<T> list;

    public ChildList() {
        list = new ArrayList<>();
    }

    public ChildList(Collection<? extends T> c) {
        list = new ArrayList<>(c);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        return list.addAll(c);
    }

    @Override
    public boolean isEmpty() {
        return list.isEmpty();
    }

    @Override
    public T get(int index) {
        return list.get(index);
    }

    @Override
    public T set(int index, T element) {
        return list.set(index, element);
    }

    @Override
    public boolean remove(Object o) {
        return list.remove(o);
    }

    @Override
    public T remove(int index) {
        return list.remove(index);
    }

    @Override
    public boolean contains(Object o) {
        return list.contains(o);
    }

    @Override
    public int size() {
        return list.size();
    }

    @Override
    public boolean add(T t) {
        return list.add(t);
    }

    @Override
    public void clear() {
        list.clear();
    }

    public boolean removeIf(Predicate<? super T> filter) {
        return list.removeIf(filter);
    }

    private void writeObject(ObjectOutputStream o) {}

    private void readObject(ObjectInputStream i) {}

}
