package org.metavm.entity;

import org.metavm.flow.Function;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Type;
import org.metavm.object.type.TypeDef;

public interface TypeRegistry {

    Type getType(Class<?> javaClass);

    default boolean isTypeDefType(ClassType type) {
        return getType(TypeDef.class).isAssignableFrom(type);
    }

    default boolean isFunctionType(ClassType type) {
        return getType(Function.class).isAssignableFrom(type);
    }

}
