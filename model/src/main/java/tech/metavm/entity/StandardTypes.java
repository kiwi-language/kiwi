package tech.metavm.entity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.object.type.*;
import tech.metavm.util.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public class StandardTypes {

    public static final Logger logger = LoggerFactory.getLogger(StandardTypes.class);

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
        return holder.getNullableAnyType();
    }

    public static Type getAnyType(boolean nullable) {
        return nullable ? getNullableAnyType() : getAnyType();
    }

    public static Klass getEnumType() {
        return holder.getEnumType();
    }

    public static ClassType getParameterizedEnumType() {
        var pType = ParameterizedTypeImpl.create(
                Enum.class, Enum.class.getTypeParameters()[0]
        );
        return (ClassType) getType(pType);
    }

    public static Klass getEntityType() {
        return holder.getEntityType();
    }

    public static Klass getRecordType() {
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

    public static Field getEnumNameField(Klass classType) {
        return classType.getFieldByCode("name");
    }

    public static Field getEnumOrdinalField(Klass classType) {
        return classType.getFieldByCode("ordinal");
    }

    public static Klass getListType() {
        return holder.getListType();
    }

    public static Klass getReadWriteListType() {
        return holder.getReadWriteListType();
    }

    public static Klass getChildListType() {
        return holder.getChildListType();
    }

    public static Klass getSetType() {
        return holder.getSetType();
    }

    public static Klass getMapType() {
        return holder.getMapType();
    }

    public static Klass getCollectionType() {
        return holder.getCollectionType();
    }

    public static Klass getIterableType() {
        return holder.getIterableType();
    }

    public static Klass getIteratorType() {
        return holder.getIteratorType();
    }

    public static Klass getIteratorImplType() {
        return holder.getIteratorImplType();
    }

    public static Klass getThrowableType() {
        return holder.getThrowableType();
    }

    public static Klass getExceptionType() {
        return holder.getExceptionType();
    }

    public static Klass getRuntimeExceptionType() {
        return holder.getRuntimeExceptionType();
    }

    public static UnionType getNullableThrowableType() {
        return (UnionType) ModelDefRegistry.getType(BiUnion.createNullableType(Throwable.class));
    }

    public static UnionType getNullableStringType() {
        return holder.getNullableStringType();
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

    public static Klass getConsumerType() {
        return holder.getConsumerType();
    }

    public static Klass setConsumerType(Klass type) {
        holder.setConsumerType(type);
        return type;
    }

    public static Klass getPredicateType() {
        return holder.getPredicateType();
    }

    public static Klass setPredicateType(Klass type) {
        holder.setPredicateType(type);
        return type;
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

    public static void setEnumType(Klass enumType) {
        holder.setEnumType(enumType);
    }

    public static void setThrowableType(Klass throwableType) {
        holder.setThrowableType(throwableType);
    }

    public static void setExceptionType(Klass exceptionType) {
        holder.setExceptionType(exceptionType);
    }

    public static void setRuntimeExceptionType(Klass runtimeExceptionType) {
        holder.setRuntimeExceptionType(runtimeExceptionType);
    }

    public static void setIllegalArgumentExceptionType(Klass illegalArgumentExceptionType) {
        holder.setIllegalArgumentExceptionType(illegalArgumentExceptionType);
    }

    public static void setIllegalStateExceptionType(Klass illegalStateExceptionType) {
        holder.setIllegalStateExceptionType(illegalStateExceptionType);
    }

    public static Klass getNullPointerExceptionType() {
        return holder.getNullPointerExceptionType();
    }

    public static void setNullPointerExceptionType(Klass nullPointerExceptionType) {
        holder.setNullPointerExceptionType(nullPointerExceptionType);
    }

    public static Klass getIllegalArgumentExceptionType() {
        return holder.getIllegalArgumentExceptionType();
    }

    public static Klass getIllegalStateExceptionType() {
        return holder.getIllegalStateExceptionType();
    }

    public static void setEntityType(Klass entityType) {
        holder.setEntityType(entityType);
    }

    public static Klass setRecordType(Klass recordType) {
        holder.setRecordType(recordType);
        return recordType;
    }

    public static Klass setCollectionType(Klass collectionType) {
        holder.setCollectionType(collectionType);
        return collectionType;
    }

    public static Klass setSetType(Klass setType) {
        holder.setSetType(setType);
        return setType;
    }

    public static Klass setListType(Klass listType) {
        holder.setListType(listType);
        return listType;
    }

    public static Klass setReadWriteListType(Klass readWriteListType) {
        holder.setReadWriteListType(readWriteListType);
        return readWriteListType;
    }

    public static Klass setChildListType(Klass childListType) {
        holder.setChildListType(childListType);
        return childListType;
    }

    public static Klass setMapType(Klass mapType) {
        holder.setMapType(mapType);
        return mapType;
    }

    public static Klass setIteratorType(Klass iteratorType) {
        holder.setIteratorType(iteratorType);
        return iteratorType;
    }

    public static Klass setIterableType(Klass iterableType) {
        holder.setIterableType(iterableType);
        return iterableType;
    }

    public static Klass setIteratorImplType(Klass iteratorImplType) {
        holder.setIteratorImplType(iteratorImplType);
        return iteratorImplType;
    }

    public static Klass getParameterizedType(Klass template, List<Type> typeArguments) {
        return holder.getParameterizedType(template, typeArguments);
    }

    public static void addParameterizedType(Klass type) {
        holder.addParameterizedType(type);
    }

    public static void clearParameterizedTypes() {
        holder.clearParameterizedTypes();
    }

    public static UnionType getNullableType(Type type) {
        return holder.getNullableType(type);
    }

    public static void addNullableType(UnionType type) {
        holder.addNullableType(type);
    }

    public static void clearNullableTypes() {
        holder.clearNullableTypes();
    }

}
