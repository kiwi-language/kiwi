package tech.metavm.entity;

import tech.metavm.util.ReflectUtils;

import java.lang.reflect.Field;

public record EntityQueryField(
        Field field,
        Object value
) {

    public static EntityQueryField create(Class<?> klass, String fieldName, Object value) {
        return new EntityQueryField(ReflectUtils.getField(klass, fieldName), value);
    }

}
