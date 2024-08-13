package org.metavm.entity.natives;

public interface ValueHolder<T> {

    T get();

    void set(T value);

    default void setLocal(T value) {
        throw new UnsupportedOperationException();
    }

    default void clearLocal() {
        throw new UnsupportedOperationException();
    }

}
