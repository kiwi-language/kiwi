package tech.metavm.reflection;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class ReflectionStore {

    private final Map<Type, JavaType> map = new HashMap<>();

    public JavaType getJavaType(Type type) {
        JavaType existing = map.get(type);
        if(existing != null) {
            return existing;
        }
        JavaType javaType = parseJavaType(type);
        map.putIfAbsent(type, javaType);
        return map.get(type);
    }

    private JavaType parseJavaType(Type type) {
        return null;
    }

}
