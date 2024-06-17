package org.metavm.entity.natives;

public class ThreadLocalValueHolder<T> implements ValueHolder<T> {

    private final ThreadLocal<T> TL = new ThreadLocal<>();

    @Override
    public T get() {
        return TL.get();
    }

    @Override
    public void set(T value) {
        TL.set(value);
    }
}
