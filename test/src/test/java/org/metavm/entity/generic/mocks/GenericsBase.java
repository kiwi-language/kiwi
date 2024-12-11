package org.metavm.entity.generic.mocks;

import org.metavm.api.Entity;

@Entity(compiled = true)
public class GenericsBase<T> extends org.metavm.entity.Entity implements GenericsInterface<T> {

    T value;

    @Override
    public T getValie() {
        return value;
    }
}
