package org.metavm.entity.natives;

public class DirectValueHolder<T> implements ValueHolder<T> {

    private T value;

    @Override
    public T get() {
        return value;
    }

    @Override
    public void set(T value) {
        this.value = value;
    }
}
