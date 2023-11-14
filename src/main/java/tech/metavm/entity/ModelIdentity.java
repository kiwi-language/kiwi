package tech.metavm.entity;


import tech.metavm.flow.Flow;
import tech.metavm.object.meta.ArrayType;
import tech.metavm.object.meta.Constraint;
import tech.metavm.object.meta.Field;
import tech.metavm.object.meta.Index;
import tech.metavm.object.meta.UnionType;
import tech.metavm.util.Column;
import tech.metavm.util.ParameterizedTypeImpl;
import tech.metavm.util.ReflectUtils;

import java.lang.reflect.Type;
import java.util.function.Function;

public record ModelIdentity(
        Type type,
        String name
) {

    public ModelIdentity(Type type, String name) {
        this.type = type;
        this.name = name;
    }

    public static ModelIdentity classTypeFields(Class<?> javaType) {
        return new ModelIdentity(
                ParameterizedTypeImpl.create(ChildArray.class, Field.class),
                javaType.getName() + ".fields"
        );
    }

    public static ModelIdentity classTypeConstraints(Class<?> javaType) {
        return new ModelIdentity(
                ParameterizedTypeImpl.create(ChildArray.class, Constraint.class),
                javaType.getName() + ".constraints"
        );
    }

    public static ModelIdentity classTypeFlows(Class<?> javaType) {
        return new ModelIdentity(
                ParameterizedTypeImpl.create(ChildArray.class, Flow.class),
                javaType.getName() + ".flows"
        );
    }

    public static ModelIdentity typeNullableType(Class<?> javaType) {
        return new ModelIdentity(
                UnionType.class,
                javaType.getName() + ".nullableType"
        );
    }

    public static ModelIdentity typeArrayType(Class<?> javaType) {
        return new ModelIdentity(
                ArrayType.class,
                javaType.getName() + ".arrayType"
        );
    }

    public static ModelIdentity type(tech.metavm.object.meta.Type type,
                                     Function<tech.metavm.object.meta.Type, Type> getJavaType) {
        return new ModelIdentity(
                type.getClass(),
                type.getKey(getJavaType)
        );
    }

    public static ModelIdentity field(Field field, Function<tech.metavm.object.meta.Type, Type> getJavaType,
                                      Function<Field, java.lang.reflect.Field> getJavaField) {
        String name = field.getDeclaringType().getKey(getJavaType) + "."
                + getJavaField.apply(field).getName();
        return new ModelIdentity(Field.class, name);
    }

    public static ModelIdentity uniqueConstraint(java.lang.reflect.Field javaField) {
        return new ModelIdentity(
                Index.class,
                ReflectUtils.getFieldQualifiedName(javaField)
        );
    }

    public static ModelIdentity column(Column column) {
        return new ModelIdentity(Column.class, column.name());
    }

    @Override
    public String toString() {
        return "ModelId [" + name + ":" + type.getTypeName() + "]";
    }
}
