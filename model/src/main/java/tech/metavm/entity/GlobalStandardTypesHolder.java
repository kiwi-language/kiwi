package tech.metavm.entity;

import tech.metavm.object.type.*;

public class GlobalStandardTypesHolder implements StandardTypesHolder {

    private PrimitiveType longType;
    private PrimitiveType doubleType;
    private PrimitiveType booleanType;
    private PrimitiveType stringType;
    private PrimitiveType passwordType;
    private PrimitiveType timeType;
    private PrimitiveType nullType;
    private PrimitiveType voidType;
    private NeverType neverType;
    private AnyType anyType;
    private UnionType nullableAnyType;
    private ArrayType anyArrayType;
    private UnionType nullableStringType;
    private ClassType enumType;
    private ClassType throwableType;
    private ClassType exceptionType;
    private ClassType runtimeExceptionType;
    private ClassType entityType;
    private ClassType recordType;
    private ClassType collectionType;
    private ClassType setType;
    private ClassType listType;
    private ClassType mapType;
    private ClassType iteratorType;
    private ClassType iteratorImplType;

    @Override
    public PrimitiveType getLongType() {
        return longType;
    }

    @Override
    public PrimitiveType getDoubleType() {
        return doubleType;
    }

    @Override
    public PrimitiveType getBooleanType() {
        return booleanType;
    }

    @Override
    public PrimitiveType getStringType() {
        return stringType;
    }

    @Override
    public PrimitiveType getTimeType() {
        return timeType;
    }

    @Override
    public PrimitiveType getVoidType() {
        return voidType;
    }

    @Override
    public PrimitiveType getNullType() {
        return nullType;
    }

    @Override
    public NeverType getNeverType() {
        return neverType;
    }

    @Override
    public AnyType getAnyType() {
        return anyType;
    }

    @Override
    public ArrayType getAnyArrayType() {
        return anyArrayType;
    }

    @Override
    public UnionType getNullableAnyType() {
        return nullableAnyType;
    }

    @Override
    public UnionType getNullableStringType() {
        return nullableStringType;
    }

    @Override
    public ClassType getEnumType() {
        return enumType;
    }

    @Override
    public ClassType getThrowableType() {
        return throwableType;
    }

    @Override
    public ClassType getExceptionType() {
        return exceptionType;
    }

    @Override
    public ClassType getRuntimeExceptionType() {
        return runtimeExceptionType;
    }

    @Override
    public ClassType getEntityType() {
        return entityType;
    }

    @Override
    public ClassType getRecordType() {
        return recordType;
    }

    @Override
    public ClassType getCollectionType() {
        return collectionType;
    }

    @Override
    public ClassType getSetType() {
        return setType;
    }

    @Override
    public ClassType getListType() {
        return listType;
    }

    @Override
    public ClassType getMapType() {
        return mapType;
    }

    @Override
    public ClassType getIteratorType() {
        return iteratorType;
    }

    @Override
    public ClassType getIteratorImplType() {
        return iteratorImplType;
    }

    @Override
    public PrimitiveType getPasswordType() {
        return passwordType;
    }

    @Override
    public void setLongType(PrimitiveType type) {
        longType = type;
    }

    @Override
    public void setDoubleType(PrimitiveType type) {
        doubleType = type;
    }

    @Override
    public void setBooleanType(PrimitiveType type) {
        booleanType = type;
    }

    @Override
    public void setStringType(PrimitiveType type) {
        stringType = type;
    }

    @Override
    public void setTimeType(PrimitiveType type) {
        timeType = type;
    }

    @Override
    public void setVoidType(PrimitiveType type) {
        voidType = type;
    }

    @Override
    public void setNullType(PrimitiveType type) {
        nullType = type;
    }

    @Override
    public void setPasswordType(PrimitiveType type) {
        passwordType = type;
    }

    @Override
    public void setNeverType(NeverType type) {
        neverType = type;
    }

    @Override
    public void setAnyType(AnyType type) {
        anyType = type;
    }

    @Override
    public void setAnyArrayType(ArrayType type) {
        anyArrayType = type;
    }

    @Override
    public void setNullableAnyType(UnionType type) {
        nullableAnyType = type;
    }

    @Override
    public void setNullableStringType(UnionType type) {
        nullableStringType = type;
    }

    @Override
    public void setEnumType(ClassType type) {
        enumType = type;
    }

    @Override
    public void setThrowableType(ClassType type) {
        throwableType = type;
    }

    @Override
    public void setExceptionType(ClassType type) {
        exceptionType = type;
    }

    @Override
    public void setRuntimeExceptionType(ClassType type) {
        runtimeExceptionType = type;
    }

    @Override
    public void setEntityType(ClassType type) {
        entityType = type;
    }

    @Override
    public void setRecordType(ClassType type) {
        recordType = type;
    }

    @Override
    public void setCollectionType(ClassType type) {
        collectionType = type;
    }

    @Override
    public void setSetType(ClassType type) {
        setType = type;
    }

    @Override
    public void setListType(ClassType type) {
        listType = type;
    }

    @Override
    public void setMapType(ClassType type) {
        mapType = type;
    }

    @Override
    public void setIteratorType(ClassType type) {
        iteratorType = type;
    }

    @Override
    public void setIteratorImplType(ClassType type) {
        iteratorImplType = type;
    }

}
