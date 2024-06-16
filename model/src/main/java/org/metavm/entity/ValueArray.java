package org.metavm.entity;

import org.metavm.api.Value;

import java.lang.reflect.Type;
import java.util.Collection;

public class ValueArray<T> extends ReadonlyArray<T> implements Value {

    public ValueArray() {
    }

    public ValueArray(Class<T> klass, Collection<T> data) {
        super(klass, data);
    }

    public ValueArray(Type elementType, Collection<T> data) {
        super(elementType);
        secretlyGetTable().addAll(data);
    }

    @Override
    protected Class<?> getRawClass() {
        return ValueArray.class;
    }
}
