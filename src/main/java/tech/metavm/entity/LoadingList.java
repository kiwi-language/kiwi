package tech.metavm.entity;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Supplier;

public class LoadingList<E> implements List<E> {

    private List<E> actualList;

    private final Supplier<List<E>> loader;

    public LoadingList(Supplier<List<E>> loader) {
        this.loader = loader;
    }

    private void ensureLoaded() {
        if(!isLoaded()) {
            setActualList(loader.get());
        }
    }

    public boolean isLoaded() {
        return actualList != null;
    }

    public void preload(List<E> actualList) {
        if(isLoaded()) {
            throw new IllegalStateException("Already loaded");
        }
        setActualList(new ArrayList<>(actualList));
    }

    private void setActualList(List<E> actualList) {
        this.actualList = actualList;
    }

    @Override
    public int size() {
        ensureLoaded();
        return actualList.size();
    }

    @Override
    public boolean isEmpty() {
        ensureLoaded();
        return actualList.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        ensureLoaded();
        return actualList.contains(o);
    }

    @NotNull
    @Override
    public Iterator<E> iterator() {
        ensureLoaded();
        return actualList.iterator();
    }

    @NotNull
    @Override
    public Object[] toArray() {
        ensureLoaded();
        return actualList.toArray();
    }

    @NotNull
    @Override
    public <T> T[] toArray(@NotNull T[] a) {
        ensureLoaded();
        return actualList.toArray(a);
    }

    @Override
    public boolean add(E e) {
        ensureLoaded();
        return actualList.add(e);
    }

    @Override
    public boolean remove(Object o) {
        ensureLoaded();
        return actualList.remove(o);
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        ensureLoaded();
        return actualList.containsAll(c);
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends E> c) {
        ensureLoaded();
        return actualList.addAll(c);
    }

    @Override
    public boolean addAll(int index, @NotNull Collection<? extends E> c) {
        ensureLoaded();
        return actualList.addAll(index, c);
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        ensureLoaded();
        return actualList.removeAll(c);
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        ensureLoaded();
        return actualList.retainAll(c);
    }

    @Override
    public void clear() {
        ensureLoaded();
        actualList.clear();
    }

    @Override
    public E get(int index) {
        ensureLoaded();
        return actualList.get(index);
    }

    @Override
    public E set(int index, E element) {
        ensureLoaded();
        return actualList.set(index, element);
    }

    @Override
    public void add(int index, E element) {
        ensureLoaded();
        actualList.add(index, element);
    }

    @Override
    public E remove(int index) {
        ensureLoaded();
        return actualList.remove(index);
    }

    @Override
    public int indexOf(Object o) {
        ensureLoaded();
        return actualList.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        ensureLoaded();
        return actualList.lastIndexOf(o);
    }

    @NotNull
    @Override
    public ListIterator<E> listIterator() {
        ensureLoaded();
        return actualList.listIterator();
    }

    @NotNull
    @Override
    public ListIterator<E> listIterator(int index) {
        ensureLoaded();
        return actualList.listIterator(index);
    }

    @NotNull
    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        ensureLoaded();
        return actualList.subList(fromIndex, toIndex);
    }
}
