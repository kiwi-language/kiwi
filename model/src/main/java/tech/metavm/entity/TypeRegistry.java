package tech.metavm.entity;

import tech.metavm.flow.Function;
import tech.metavm.object.type.ClassType;
import tech.metavm.object.type.Type;
import tech.metavm.object.type.TypeDef;
import tech.metavm.object.view.Mapping;

public interface TypeRegistry {

    Type getType(Class<?> javaClass);

    default boolean isTypeDefType(ClassType type) {
        return getType(TypeDef.class).isAssignableFrom(type);
    }

    default boolean isMappingType(ClassType type) {
        return getType(Mapping.class).isAssignableFrom(type);
    }

    default boolean isFunctionType(ClassType type) {
        return getType(Function.class).isAssignableFrom(type);
    }

}
