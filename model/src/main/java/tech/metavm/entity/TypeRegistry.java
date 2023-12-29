package tech.metavm.entity;

import tech.metavm.object.type.Type;

public interface TypeRegistry {

    Type getType(Class<?> javaClass);

}
