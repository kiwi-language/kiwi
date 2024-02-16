package tech.metavm.entity;

import tech.metavm.object.type.*;
import tech.metavm.util.*;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public class StandardTypes {

    private static StandardTypesHolder holder = new GlobalStandardTypesHolder();

    public static void setHolder(StandardTypesHolder holder) {
        StandardTypes.holder = holder;
    }

    private static final Function<java.lang.reflect.Type, Type> getType = ModelDefRegistry::getType;

    public static Type getType(java.lang.reflect.Type javaType) {
        return getType.apply(javaType);
    }

    public static ClassType getClassType(java.lang.reflect.Type javaType) {
        return (ClassType) getType.apply(javaType);
    }

    public static AnyType getAnyType() {
        return holder.getAnyType();
    }

    public static UnionType getNullableAnyType() {
        return holder.getNullableStringType();
    }

    public static Type getAnyType(boolean nullable) {
        return nullable ? getNullableAnyType() : getAnyType();
    }

    public static ClassType getEnumType() {
        return holder.getEnumType();
    }

    public static ClassType getParameterizedEnumType() {
        var pType = ParameterizedTypeImpl.create(
                Enum.class, Enum.class.getTypeParameters()[0]
        );
        return (ClassType) getType(pType);
    }

    public static ClassType getEntityType() {
        return holder.getEntityType();
    }

    public static ClassType getRecordType() {
        return holder.getRecordType();
    }

    public static PrimitiveType getBooleanType() {
        return holder.getBooleanType();
    }

    public static PrimitiveType getLongType() {
        return holder.getLongType();
    }

    public static PrimitiveType getStringType() {
        return holder.getStringType();
    }

    public static PrimitiveType getTimeType() {
        return holder.getTimeType();
    }

    public static PrimitiveType getNullType() {
        return holder.getNullType();
    }

    public static PrimitiveType getVoidType() {
        return holder.getVoidType();
    }

    public static PrimitiveType getPasswordType() {
        return holder.getPasswordType();
    }

    public static PrimitiveType getDoubleType() {
        return holder.getDoubleType();
    }

    public static ArrayType getAnyArrayType() {
        return holder.getAnyArrayType();
    }

    public static ArrayType getNeverArrayType() {
        return holder.getNeverArrayType();
    }

    public static ArrayType getReadOnlyAnyArrayType() {
        return holder.getReadonlyAnyArrayType();
    }

    public static void setReadonlyAnyArrayType(ArrayType type) {
        holder.setReadonlyAnyArrayType(type);
    }

    public static ArrayType getObjectChildArrayType() {
        return ModelDefRegistry.getDefContext().getArrayType(getAnyType(), ArrayKind.CHILD);
    }

    public static Field getEnumNameField(ClassType classType) {
        return classType.getFieldByCode("name");
    }

    public static Field getEnumOrdinalField(ClassType classType) {
        return classType.getFieldByCode("ordinal");
    }

    public static ClassType getListType() {
        return holder.getListType();
    }

    public static ClassType getReadWriteListType() {
        return holder.getReadWriteListType();
    }

    public static ClassType getChildListType() {
        return holder.getChildListType();
    }

    public static ClassType getSetType() {
        return holder.getSetType();
    }

    public static ClassType getMapType() {
        return holder.getMapType();
    }

    public static ClassType getCollectionType() {
        return holder.getCollectionType();
    }

    public static ClassType getIteratorType() {
        return holder.getIteratorType();
    }

    public static ClassType getIteratorImplType() {
        return holder.getIteratorImplType();
    }

    public static ClassType getThrowableType() {
        return holder.getThrowableType();
    }

    public static ClassType getExceptionType() {
        return holder.getExceptionType();
    }

    public static ClassType getRuntimeExceptionType() {
        return holder.getRuntimeExceptionType();
    }

    public static UnionType getNullableThrowableType() {
        return (UnionType) ModelDefRegistry.getType(BiUnion.createNullableType(Throwable.class));
    }

    public static UnionType getNullableStringType() {
        return holder.getNullableStringType();
    }

    public static NeverType getNothingType() {
        return getNeverType();
    }

    private static volatile Map<PrimitiveKind, PrimitiveType> primitiveTypes;

    public static PrimitiveType getPrimitiveType(PrimitiveKind kind) {
        if(primitiveTypes == null) {
            synchronized (StandardTypes.class) {
                if(primitiveTypes == null) {
                    primitiveTypes = Map.of(
                            PrimitiveKind.BOOLEAN, Objects.requireNonNull(getBooleanType()),
                            PrimitiveKind.LONG, Objects.requireNonNull(getLongType()),
                            PrimitiveKind.DOUBLE, Objects.requireNonNull(getDoubleType()),
                            PrimitiveKind.STRING, Objects.requireNonNull(getStringType()),
                            PrimitiveKind.PASSWORD, Objects.requireNonNull(getPasswordType()),
                            PrimitiveKind.TIME, Objects.requireNonNull(getTimeType()),
                            PrimitiveKind.NULL, Objects.requireNonNull(getNullType()),
                            PrimitiveKind.VOID, Objects.requireNonNull(getVoidType())
                    );
                }
            }
        }
        return Objects.requireNonNull(primitiveTypes.get(kind), () -> "Unknown primitive kind: " + kind);
    }

    public static void setLongType(PrimitiveType longType) {
        holder.setLongType(longType);
    }

    public static void setDoubleType(PrimitiveType doubleType) {
        holder.setDoubleType(doubleType);
    }

    public static void setBooleanType(PrimitiveType booleanType) {
        holder.setBooleanType(booleanType);
    }

    public static void setStringType(PrimitiveType stringType) {
        holder.setStringType(stringType);
    }

    public static void setPasswordType(PrimitiveType passwordType) {
        holder.setPasswordType(passwordType);
    }

    public static void setTimeType(PrimitiveType timeType) {
        holder.setTimeType(timeType);
    }

    public static void setNullType(PrimitiveType nullType) {
        holder.setNullType(nullType);
    }

    public static void setVoidType(PrimitiveType voidType) {
        holder.setVoidType(voidType);
    }

    public static NeverType getNeverType() {
        return holder.getNeverType();
    }

    public static void setNeverType(NeverType neverType) {
        holder.setNeverType(neverType);
    }

    public static void setNeverArrayType(ArrayType neverArrayType) {
        holder.setNeverArrayType(neverArrayType);
    }

    public static void setAnyType(AnyType anyType) {
        holder.setAnyType(anyType);
    }

    public static void setNullableAnyType(UnionType nullableAnyType) {
        holder.setNullableAnyType(nullableAnyType);
    }

    public static void setAnyArrayType(ArrayType anyArrayType) {
        holder.setAnyArrayType(anyArrayType);
    }

    public static void setNullableStringType(UnionType nullableStringType) {
        holder.setNullableStringType(nullableStringType);
    }

    public static void setEnumType(ClassType enumType) {
        holder.setEnumType(enumType);
    }

    public static void setThrowableType(ClassType throwableType) {
        holder.setThrowableType(throwableType);
    }

    public static void setExceptionType(ClassType exceptionType) {
        holder.setExceptionType(exceptionType);
    }

    public static void setRuntimeExceptionType(ClassType runtimeExceptionType) {
        holder.setRuntimeExceptionType(runtimeExceptionType);
    }

    public static void setEntityType(ClassType entityType) {
        holder.setEntityType(entityType);
    }

    public static ClassType setRecordType(ClassType recordType) {
        holder.setRecordType(recordType);
        return recordType;
    }

    public static ClassType setCollectionType(ClassType collectionType) {
        holder.setCollectionType(collectionType);
        return collectionType;
    }

    public static ClassType setSetType(ClassType setType) {
        holder.setSetType(setType);
        return setType;
    }

    public static ClassType setListType(ClassType listType) {
        holder.setListType(listType);
        return listType;
    }

    public static ClassType setReadWriteListType(ClassType readWriteListType) {
        holder.setReadWriteListType(readWriteListType);
        return readWriteListType;
    }

    public static ClassType setChildListType(ClassType childListType) {
        holder.setChildListType(childListType);
        return childListType;
    }

    public static ClassType setMapType(ClassType mapType) {
        holder.setMapType(mapType);
        return mapType;
    }

    public static ClassType setIteratorType(ClassType iteratorType) {
        holder.setIteratorType(iteratorType);
        return iteratorType;
    }

    public static ClassType setIteratorImplType(ClassType iteratorImplType) {
        holder.setIteratorImplType(iteratorImplType);
        return iteratorImplType;
    }
}
