package tech.metavm.entity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ChildList<T> implements Iterable<T> {

    private final List<T> list = new ArrayList<>();

    public T get(int index) {
        return list.get(index);
    }

    public boolean remove(Object o) {
        return list.remove(o);
    }

    public T remove(int index) {
        return list.remove(index);
    }

    public void add(T value) {
        list.add(value);
    }

    public void add(int index, T value) {
        list.add(index, value);
    }

    @Override
    public Iterator<T> iterator() {
        return list.iterator();
    }
}
