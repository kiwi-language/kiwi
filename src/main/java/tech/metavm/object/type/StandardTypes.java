package tech.metavm.object.type;

import tech.metavm.entity.Entity;
import tech.metavm.entity.ModelDefRegistry;
import tech.metavm.util.*;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public class StandardTypes {

    private static final Function<java.lang.reflect.Type, Type> getType = ModelDefRegistry::getType;

    public static Type getType(java.lang.reflect.Type javaType) {
        return getType.apply(javaType);
    }

    public static ClassType getClassType(java.lang.reflect.Type javaType) {
        return (ClassType) getType.apply(javaType);
    }

    public static ObjectType getObjectType() {
        return (ObjectType) ModelDefRegistry.getType(Object.class);
    }

    public static UnionType getNullableObjectType() {
        return ModelDefRegistry.getDefContext().getUnionType(Set.of(getObjectType(), getNullType()));
    }

    public static Type getObjectType(boolean nullable) {
        return nullable ? getNullableObjectType() : getObjectType();
    }

    public static ClassType getEnumType() {
        return (ClassType) ModelDefRegistry.getType(Enum.class);
    }

    public static ClassType getParameterizedEnumType() {
        var pType = ParameterizedTypeImpl.create(
                Enum.class, Enum.class.getTypeParameters()[0]
        );
        return (ClassType) getType(pType);
    }

    public static ClassType getEntityType() {
        return (ClassType) ModelDefRegistry.getType(Entity.class);
    }

    public static ClassType getRecordType() {
        return (ClassType) ModelDefRegistry.getType(Record.class);
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

    public static PrimitiveType getVoidType() {
        return (PrimitiveType) getType(Void.class);
    }

    public static PrimitiveType getPasswordType() {
        return (PrimitiveType) getType(Password.class);
    }

    public static PrimitiveType getDoubleType() {
        return (PrimitiveType) getType(Double.class);
    }

    public static ArrayType getObjectArrayType() {
        return ModelDefRegistry.getDefContext().getArrayType(getObjectType(), ArrayKind.READ_WRITE);
    }

    public static ArrayType getReadOnlyObjectArrayType() {
        return ModelDefRegistry.getDefContext().getArrayType(getObjectType(), ArrayKind.READ_ONLY);
    }

    public static ArrayType getObjectChildArrayType() {
        return ModelDefRegistry.getDefContext().getArrayType(getObjectType(), ArrayKind.CHILD);
    }

    public static Field getEnumNameField(ClassType classType) {
        return classType.getFieldByCode("name");
    }

    public static Field getEnumOrdinalField(ClassType classType) {
        return classType.getFieldByCode("ordinal");
    }

    public static ClassType getListType() {
        return getClassType(MetaList.class);
    }

    public static ClassType getSetType() {
        return getClassType(MetaSet.class);
    }

    public static ClassType getMapType() {
        return getClassType(MetaMap.class);
    }

    public static ClassType getCollectionType() {
        return getClassType(Collection.class);
    }

    public static ClassType getIteratorType() {
        return getClassType(MetaIterator.class);
    }

    public static ClassType getIteratorImplType() {
        return getClassType(IteratorImpl.class);
    }

    public static ClassType getThrowableType() {
        return getClassType(Throwable.class);
    }

    public static ClassType getExceptionType() {
        return getClassType(Exception.class);
    }

    public static ClassType getRuntimeExceptionType() {
        return getClassType(RuntimeException.class);
    }

    public static UnionType getNullableThrowableType() {
        return (UnionType) ModelDefRegistry.getType(BiUnion.createNullableType(Throwable.class));
    }

    public static NothingType getNothingType() {
        return (NothingType) getType(Nothing.class);
    }

    public static List<PrimitiveType> getPrimitiveTypes() {
        return List.of(
                getLongType(), getStringType(), getTimeType(),
                getDoubleType(), getPasswordType(), getBoolType(), getBoolType()
        );
    }

}
