package org.metavm.entity.natives;

public interface ValueHolder<T> {

    T get();

    void set(T value);

}
