package org.metavm.util;

import java.util.Iterator;

public class IteratorImpl<E> implements Iterator<E> {
    @Override
    public boolean hasNext() {
        return false;
    }

    @Override
    public E next() {
        return null;
    }
}
