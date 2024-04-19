package tech.metavm.entity;

import tech.metavm.object.type.*;

import java.util.List;
import java.util.Map;

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
    
    ClassType getEnumType();

    ClassType getThrowableType();

    ClassType getExceptionType();

    ClassType getRuntimeExceptionType();

    ClassType getEntityType();

    ClassType getRecordType();

    ClassType getCollectionType();

    ClassType getSetType();

    ClassType getListType();

    ClassType getMapType();

    ClassType getIteratorType();

    ClassType getIteratorImplType();

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

    void setEnumType(ClassType type);

    void setThrowableType(ClassType type);

    void setExceptionType(ClassType type);

    void setRuntimeExceptionType(ClassType type);

    void setEntityType(ClassType type);

    void setRecordType(ClassType type);

    void setCollectionType(ClassType type);

    void setSetType(ClassType type);

    void setListType(ClassType type);

    void setMapType(ClassType type);

    void setIteratorType(ClassType type);

    void setIteratorImplType(ClassType type);

    void setReadonlyAnyArrayType(ArrayType type);

    void setReadWriteListType(ClassType type);

    void setChildListType(ClassType type);

    ClassType getChildListType();

    ClassType getReadWriteListType();

    ClassType getIterableType();

    void setIterableType(ClassType iterableType);

    ClassType getConsumerType();

    void setConsumerType(ClassType consumerType);

    ClassType getPredicateType();

    void setPredicateType(ClassType type);

    void setIllegalArgumentExceptionType(ClassType illegalArgumentExceptionType);

    void setIllegalStateExceptionType(ClassType illegalStateExceptionType);

    ClassType getIllegalArgumentExceptionType();

    ClassType getIllegalStateExceptionType();

    ClassType getNullPointerExceptionType();

    void setNullPointerExceptionType(ClassType nullPointerExceptionType);

    ClassType getParameterizedType(ClassType template, List<Type> typeArguments);

    void addParameterizedType(ClassType type);

    void clearParameterizedTypes();

    void addNullableType(UnionType type);

    UnionType getNullableType(Type type);

    void clearNullableTypes();
}
