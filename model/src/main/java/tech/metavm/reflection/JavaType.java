package tech.metavm.reflection;

import tech.metavm.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class JavaType {
    private final JavaType superType;
    private final Type type;
    private final List<JavaField> fields = new ArrayList<>();

    public JavaType(Type type, JavaType superType) {
        this.superType = superType;
        this.type = type;
        for (Field rawField : ReflectionUtils.getDeclaredRawFields(type)) {
            new JavaField(rawField, this);
        }
    }

    public JavaType getSuperType() {
        return superType;
    }

    public Type getType() {
        return type;
    }

    public List<JavaField> getFields() {
        return Collections.unmodifiableList(fields);
    }

    void addField(JavaField field) {
        fields.add(field);
    }

}
