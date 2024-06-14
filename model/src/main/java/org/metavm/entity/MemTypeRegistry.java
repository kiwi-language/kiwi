package org.metavm.entity;

import org.metavm.object.type.Type;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MemTypeRegistry implements TypeRegistry {

    private final Map<Class<?>, Type> map = new ConcurrentHashMap<>();

    @Override
    public Type getType(Class<?> javaClass) {
        return map.get(javaClass);
    }


    public void putType(Class<?> javaClass, Type type) {
        map.put(javaClass, type);
    }
}
