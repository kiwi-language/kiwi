package tech.metavm.entity.generic.mocks;

import tech.metavm.entity.Entity;
import tech.metavm.entity.EntityType;

@EntityType(compiled = true)
public class GenericsBase<T> extends Entity implements GenericsInterface<T> {

    T value;

    @Override
    public T getValie() {
        return value;
    }
}
