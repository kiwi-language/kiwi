package tech.metavm.entity;


import tech.metavm.flow.FlowRT;
import tech.metavm.object.instance.ArrayType;
import tech.metavm.object.meta.ConstraintRT;
import tech.metavm.object.meta.Field;
import tech.metavm.object.meta.UnionType;
import tech.metavm.object.meta.UniqueConstraintRT;
import tech.metavm.util.ParameterizedTypeImpl;
import tech.metavm.util.ReflectUtils;
import tech.metavm.util.Table;

import java.lang.reflect.Type;
import java.util.function.Function;

public record ModelIdentity(
        Type type,
        String name
) {

    public static ModelIdentity classTypeFields(Class<?> javaType) {
        return new ModelIdentity(
                ParameterizedTypeImpl.create(Table.class, Field.class),
                javaType.getName() + ".fields"
        );
    }

    public static ModelIdentity classTypeConstraints(Class<?> javaType) {
        return new ModelIdentity(
                ParameterizedTypeImpl.create(Table.class, ConstraintRT.class),
                javaType.getName() + ".constraints"
        );
    }

    public static ModelIdentity classTypeFlows(Class<?> javaType) {
        return new ModelIdentity(
                ParameterizedTypeImpl.create(Table.class, FlowRT.class),
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
                type.getCanonicalName(getJavaType)
        );
    }

    public static ModelIdentity field(java.lang.reflect.Field javaField) {
        return new ModelIdentity(
                Field.class,
                ReflectUtils.getFieldQualifiedName(javaField)
        );
    }

    public static ModelIdentity table(Table<?> table,
                                      tech.metavm.object.meta.Type type,
                                      Function<tech.metavm.object.meta.Type, Type> getJavaType,
                                      String javaFieldName) {
        return new ModelIdentity(
                table.getGenericType(),
                type.getCanonicalName(getJavaType) + "." + javaFieldName
        );
    }

    public static ModelIdentity uniqueConstraint(java.lang.reflect.Field javaField) {
        return new ModelIdentity(
                UniqueConstraintRT.class,
                ReflectUtils.getFieldQualifiedName(javaField)
        );
    }

    @Override
    public String toString() {
        return "ModelId [" + name + ":" + type.getTypeName() + "]";
    }
}
