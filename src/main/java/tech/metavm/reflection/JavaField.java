package tech.metavm.reflection;

import tech.metavm.reflection.JavaType;
import tech.metavm.util.ReflectUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Type;

public class JavaField {

    private final Field rawField;
    private final JavaType declaringType;
    private final Type type;

    public JavaField(Field rawField, JavaType declaringType) {
        this.rawField = rawField;
        this.declaringType = declaringType;
        this.type = ReflectUtils.evaluateFieldType(declaringType.getType(), rawField.getGenericType());
        declaringType.addField(this);
    }

    public String getName() {
        return rawField.getName();
    }

    public JavaType getDeclaringType() {
        return declaringType;
    }

    public Type getType() {
        return type;
    }
}
