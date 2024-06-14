package org.metavm.util;

import java.util.ListIterator;

public class PeekableListIterator<E> implements ListIterator<E> {

    private final ListIterator<E> wrapped;

    private E peek;
    private boolean eof;

    public PeekableListIterator(ListIterator<E> wrapped) {
        this.wrapped = wrapped;
    }

    public E peek() {
        return peek;
    }

    @Override
    public boolean hasNext() {
        return peek != null;
    }

    @Override
    public E next() {
        E currentPeek = peek;
        if(wrapped.hasNext())
            currentPeek = wrapped.next();
        else {
            eof = true;
            currentPeek = null;
        }
        return currentPeek;
    }

    @Override
    public boolean hasPrevious() {
        return false;
    }

    @Override
    public E previous() {
        return null;
    }

    @Override
    public int nextIndex() {
        return 0;
    }

    @Override
    public int previousIndex() {
        return 0;
    }

    @Override
    public void remove() {

    }

    @Override
    public void set(E e) {

    }

    @Override
    public void add(E e) {

    }
}
