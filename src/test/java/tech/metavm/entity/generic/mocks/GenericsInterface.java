package tech.metavm.entity.generic.mocks;

import tech.metavm.util.RuntimeGeneric;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Map;

public interface GenericsInterface<E> extends RuntimeGeneric {

    default Map<TypeVariable<?>, Type> getTypeVariableMap() {
        return Map.of();
    }

    public E getValie();

}
