package tech.metavm.entity;

import tech.metavm.object.type.Type;

import java.util.function.Function;

public interface GlobalKey {

    String getKey(Function<Type, java.lang.reflect.Type> getJavaType);

}
