package tech.metavm.entity;

import tech.metavm.flow.Function;
import tech.metavm.object.type.Type;
import tech.metavm.object.view.Mapping;

public interface TypeRegistry {

    Type getType(Class<?> javaClass);

    default boolean isTypeType(Type type) {
        return getType(Type.class).isAssignableFrom(type, null);
    }

    default boolean isMappingType(Type type) {
        return getType(Mapping.class).isAssignableFrom(type, null);
    }

    default boolean isFunctionType(Type type) {
        return getType(Function.class).isAssignableFrom(type, null);
    }

}
