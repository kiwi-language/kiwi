package org.metavm.autograph;

import org.metavm.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PrimitiveStaticFields {

    private static final Map<Field, Object> map = new HashMap<>();

    static {
        var klasses = List.of(
                Byte.class, Short.class, Integer.class, Long.class,
                Float.class, Double.class, Boolean.class, Character.class
        );

        for (var klass : klasses) {
            for (Field field : klass.getFields()) {
                if(Modifier.isStatic(field.getModifiers()) && field.getType().isPrimitive())
                    map.put(field, ReflectionUtils.get(null, field));
            }
        }
    }

    public static boolean isConstant(Field field) {
        return map.containsKey(field);
    }

    public static Object getConstant(Field field) {
        return map.get(field);
    }

}
