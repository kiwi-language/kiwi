package org.metavm.entity.generic.mocks;

import org.metavm.entity.Entity;
import org.metavm.api.EntityType;

@EntityType(compiled = true)
public class GenericsBase<T> extends Entity implements GenericsInterface<T> {

    T value;

    @Override
    public T getValie() {
        return value;
    }
}
