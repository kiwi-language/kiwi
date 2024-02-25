package tech.metavm.entity;

import java.util.ArrayList;
import java.util.Collection;

public class ChildList<T> extends ArrayList<T> {

    public ChildList() {
    }

    public ChildList(Collection<? extends T> c) {
        super(c);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        return super.addAll(c);
    }

    @Override
    public boolean isEmpty() {
        return super.isEmpty();
    }

    @Override
    public T get(int index) {
        return super.get(index);
    }

    @Override
    public T set(int index, T element) {
        return super.set(index, element);
    }

    @Override
    public boolean remove(Object o) {
        return super.remove(o);
    }

    @Override
    public T remove(int index) {
        return super.remove(index);
    }

    @Override
    public boolean contains(Object o) {
        return super.contains(o);
    }

    @Override
    public int size() {
        return super.size();
    }

    @Override
    public boolean add(T t) {
        return super.add(t);
    }

    @Override
    public void clear() {
        super.clear();
    }
}
