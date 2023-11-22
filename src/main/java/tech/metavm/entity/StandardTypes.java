package tech.metavm.entity;

import tech.metavm.object.type.*;
import tech.metavm.util.*;

import java.util.List;
import java.util.Set;
import java.util.function.Function;

public class StandardTypes {

    public static PrimitiveType longType;
    public static PrimitiveType doubleType;
    public static PrimitiveType booleanType;
    public static PrimitiveType stringType;
    public static PrimitiveType passwordType;
    public static PrimitiveType timeType;
    public static PrimitiveType nullType;
    static PrimitiveType voidType;
    static NothingType nothingType;
    static ObjectType objectType;
    static ClassType enumType;
    static ClassType throwableType;
    static ClassType exceptionType;
    static ClassType runtimeExceptionType;
    static ClassType entityType;
    static ClassType recordType;
    static ClassType collectionType;
    static ClassType setType;
    static ClassType listType;
    static ClassType mapType;
    static ClassType iteratorType;
    static ClassType iteratorImplType;

    private static final Function<java.lang.reflect.Type, Type> getType = ModelDefRegistry::getType;

    public static Type getType(java.lang.reflect.Type javaType) {
        return getType.apply(javaType);
    }

    public static ClassType getClassType(java.lang.reflect.Type javaType) {
        return (ClassType) getType.apply(javaType);
    }

    public static ObjectType getObjectType() {
        return objectType;
    }

    public static UnionType getNullableObjectType() {
        return (UnionType) ModelDefRegistry.getType(BiUnion.createNullableType(Object.class));
    }

    public static Type getObjectType(boolean nullable) {
        return nullable ? getNullableObjectType() : getObjectType();
    }

    public static ClassType getEnumType() {
        return enumType;
    }

    public static ClassType getParameterizedEnumType() {
        var pType = ParameterizedTypeImpl.create(
                Enum.class, Enum.class.getTypeParameters()[0]
        );
        return (ClassType) getType(pType);
    }

    public static ClassType getEntityType() {
        return entityType;
    }

    public static ClassType getRecordType() {
        return recordType;
    }

    public static PrimitiveType getBooleanType() {
        return booleanType;
    }

    public static PrimitiveType getLongType() {
        return longType;
    }

    public static PrimitiveType getStringType() {
        return stringType;
    }

    public static PrimitiveType getTimeType() {
        return timeType;
    }

    public static PrimitiveType getNullType() {
        return nullType;
    }

    public static PrimitiveType getVoidType() {
        return voidType;
    }

    public static PrimitiveType getPasswordType() {
        return passwordType;
    }

    public static PrimitiveType getDoubleType() {
        return doubleType;
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
        return listType;
    }

    public static ClassType getSetType() {
        return setType;
    }

    public static ClassType getMapType() {
        return mapType;
    }

    public static ClassType getCollectionType() {
        return collectionType;
    }

    public static ClassType getIteratorType() {
        return iteratorType;
    }

    public static ClassType getIteratorImplType() {
        return iteratorImplType;
    }

    public static ClassType getThrowableType() {
        return throwableType;
    }

    public static ClassType getExceptionType() {
        return exceptionType;
    }

    public static ClassType getRuntimeExceptionType() {
        return runtimeExceptionType;
    }

    public static UnionType getNullableThrowableType() {
        return (UnionType) ModelDefRegistry.getType(BiUnion.createNullableType(Throwable.class));
    }

    public static NothingType getNothingType() {
        return nothingType;
    }

    public static List<PrimitiveType> getPrimitiveTypes() {
        return List.of(
                getLongType(), getStringType(), getTimeType(),
                getDoubleType(), getPasswordType(), getBooleanType(), getBooleanType()
        );
    }

}
