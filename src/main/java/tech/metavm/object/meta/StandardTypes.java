package tech.metavm.object.meta;

import tech.metavm.entity.Entity;
import tech.metavm.entity.ModelDefRegistry;
import tech.metavm.object.instance.ArrayType;
import tech.metavm.util.Null;
import tech.metavm.util.Password;
import tech.metavm.util.Table;

import java.util.Date;
import java.util.function.Function;

public class StandardTypes {

    private static final Function<Class<?>, Type> getType = ModelDefRegistry::getType;

    public static Type getType(Class<?> javaType) {
        return getType.apply(javaType);
    }

    public static AnyType getObjectType() {
        return (AnyType) ModelDefRegistry.getType(Object.class);
    }

    public static ClassType getEnumType() {
        return (ClassType) ModelDefRegistry.getType(Enum.class);
    }

    public static ClassType getEntityType() {
        return (ClassType) ModelDefRegistry.getType(Entity.class);
    }

    public static ClassType getRecordType() {
        return (ClassType) ModelDefRegistry.getType(Record.class);
    }

    public static PrimitiveType getIntType() {
        return (PrimitiveType) ModelDefRegistry.getType(Integer.class);
    }

    public static PrimitiveType getBoolType() {
        return (PrimitiveType) getType(Boolean.class);
    }

    public static PrimitiveType getLongType() {
        return (PrimitiveType) getType(Long.class);
    }

    public static PrimitiveType getStringType() {
        return (PrimitiveType) getType(String.class);
    }

    public static PrimitiveType getTimeType() {
        return (PrimitiveType) getType(Date.class);
    }

    public static PrimitiveType getNullType() {
        return (PrimitiveType) getType(Null.class);
    }

    public static PrimitiveType getPasswordType() {
        return (PrimitiveType) getType(Password.class);
    }

    public static PrimitiveType getDoubleType() {
        return (PrimitiveType) getType(Double.class);
    }

    public static ArrayType getArrayType() {
        return (ArrayType) getType(Table.class);
    }

    public static Field getEnumNameField() {
        return ModelDefRegistry.getField(Enum.class, "name");
    }

    public static Field getEnumOrdinalField() {
        return ModelDefRegistry.getField(Enum.class, "ordinal");
    }


}
