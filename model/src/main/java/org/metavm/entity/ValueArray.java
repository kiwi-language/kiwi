package org.metavm.entity;

import org.metavm.api.ValueObject;

import java.lang.reflect.Type;
import java.util.Collection;

public class ValueArray<T> extends ReadonlyArray<T> implements ValueObject {

    public ValueArray() {
    }

    public ValueArray(Class<T> klass, Collection<? extends T> data) {
        super(klass, data);
    }

    public ValueArray(Type elementType, Collection<? extends T> data) {
        super(elementType);
        secretlyGetTable().addAll(data);
    }

    @Override
    protected Class<?> getRawClass() {
        return ValueArray.class;
    }
}
