package tech.metavm.entity;

import tech.metavm.util.ReflectUtils;

import java.lang.reflect.Field;

public record EntityQueryField(
        String fieldName,
        Object value
) {

}
