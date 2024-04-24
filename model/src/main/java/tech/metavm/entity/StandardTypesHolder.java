package tech.metavm.entity;

import tech.metavm.object.type.*;

import java.util.List;

public interface StandardTypesHolder {
    
    PrimitiveType getLongType();

    PrimitiveType getDoubleType();

    PrimitiveType getBooleanType();

    PrimitiveType getStringType();

    PrimitiveType getTimeType();

    PrimitiveType getVoidType();

    PrimitiveType getNullType();

    NeverType getNeverType();
    
    AnyType getAnyType();

    ArrayType getAnyArrayType();

    ArrayType getNeverArrayType();

    UnionType getNullableAnyType();

    UnionType getNullableStringType();
    
    Klass getEnumType();

    Klass getThrowableType();

    Klass getExceptionType();

    Klass getRuntimeExceptionType();

    Klass getEntityType();

    Klass getRecordType();

    Klass getCollectionType();

    Klass getSetType();

    Klass getListType();

    Klass getMapType();

    Klass getIteratorType();

    Klass getIteratorImplType();

    ArrayType getReadonlyAnyArrayType();
    
    PrimitiveType getPasswordType();

    void setLongType(PrimitiveType type);

    void setDoubleType(PrimitiveType type);

    void setBooleanType(PrimitiveType type);

    void setStringType(PrimitiveType type);

    void setTimeType(PrimitiveType type);

    void setVoidType(PrimitiveType type);

    void setNullType(PrimitiveType type);

    void setPasswordType(PrimitiveType type);

    void setNeverType(NeverType type);

    void setAnyType(AnyType type);

    void setAnyArrayType(ArrayType type);

    void setNeverArrayType(ArrayType type);

    void setNullableAnyType(UnionType type);

    void setNullableStringType(UnionType type);

    void setEnumType(Klass type);

    void setThrowableType(Klass type);

    void setExceptionType(Klass type);

    void setRuntimeExceptionType(Klass type);

    void setEntityType(Klass type);

    void setRecordType(Klass type);

    void setCollectionType(Klass type);

    void setSetType(Klass type);

    void setListType(Klass type);

    void setMapType(Klass type);

    void setIteratorType(Klass type);

    void setIteratorImplType(Klass type);

    void setReadonlyAnyArrayType(ArrayType type);

    void setReadWriteListType(Klass type);

    void setChildListType(Klass type);

    Klass getChildListType();

    Klass getReadWriteListType();

    Klass getIterableType();

    void setIterableType(Klass iterableType);

    Klass getConsumerType();

    void setConsumerType(Klass consumerType);

    Klass getPredicateType();

    void setPredicateType(Klass type);

    void setIllegalArgumentExceptionType(Klass illegalArgumentExceptionType);

    void setIllegalStateExceptionType(Klass illegalStateExceptionType);

    Klass getIllegalArgumentExceptionType();

    Klass getIllegalStateExceptionType();

    Klass getNullPointerExceptionType();

    void setNullPointerExceptionType(Klass nullPointerExceptionType);

    Klass getParameterizedType(Klass template, List<Type> typeArguments);

    void addParameterizedType(Klass type);

    void clearParameterizedTypes();

    void addNullableType(UnionType type);

    UnionType getNullableType(Type type);

    void clearNullableTypes();
}
