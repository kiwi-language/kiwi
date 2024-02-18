package tech.metavm.entity;

import tech.metavm.object.type.*;

public class ThreadLocalStandardTypesHolder implements StandardTypesHolder {

    private final ThreadLocal<GlobalStandardTypesHolder> TL = ThreadLocal.withInitial(GlobalStandardTypesHolder::new);

    @Override
    public PrimitiveType getLongType() {
        return TL.get().getLongType();
    }

    @Override
    public PrimitiveType getDoubleType() {
        return TL.get().getDoubleType();
    }

    @Override
    public PrimitiveType getBooleanType() {
        return TL.get().getBooleanType();
    }

    @Override
    public PrimitiveType getStringType() {
        return TL.get().getStringType();
    }

    @Override
    public PrimitiveType getTimeType() {
        return TL.get().getTimeType();
    }

    @Override
    public PrimitiveType getVoidType() {
        return TL.get().getVoidType();
    }

    @Override
    public PrimitiveType getNullType() {
        return TL.get().getNullType();
    }

    @Override
    public NeverType getNeverType() {
        return TL.get().getNeverType();
    }

    @Override
    public AnyType getAnyType() {
        return TL.get().getAnyType();
    }

    @Override
    public ArrayType getAnyArrayType() {
        return TL.get().getAnyArrayType();
    }

    @Override
    public ArrayType getNeverArrayType() {
        return TL.get().getNeverArrayType();
    }

    @Override
    public UnionType getNullableAnyType() {
        return TL.get().getNullableAnyType();
    }

    @Override
    public UnionType getNullableStringType() {
        return TL.get().getNullableStringType();
    }

    @Override
    public ClassType getEnumType() {
        return TL.get().getEnumType();
    }

    @Override
    public ClassType getThrowableType() {
        return TL.get().getThrowableType();
    }

    @Override
    public ClassType getExceptionType() {
        return TL.get().getExceptionType();
    }

    @Override
    public ClassType getRuntimeExceptionType() {
        return TL.get().getRuntimeExceptionType();
    }

    @Override
    public ClassType getEntityType() {
        return TL.get().getEntityType();
    }

    @Override
    public ClassType getRecordType() {
        return TL.get().getRecordType();
    }

    @Override
    public ClassType getCollectionType() {
        return TL.get().getCollectionType();
    }

    @Override
    public ClassType getSetType() {
        return TL.get().getSetType();
    }

    @Override
    public ClassType getListType() {
        return TL.get().getListType();
    }

    @Override
    public ClassType getMapType() {
        return TL.get().getMapType();
    }

    @Override
    public ClassType getIteratorType() {
        return TL.get().getIteratorType();
    }

    @Override
    public ClassType getIteratorImplType() {
        return TL.get().getIteratorImplType();
    }

    @Override
    public ArrayType getReadonlyAnyArrayType() {
        return TL.get().getReadonlyAnyArrayType();
    }

    @Override
    public PrimitiveType getPasswordType() {
        return TL.get().getPasswordType();
    }

    @Override
    public void setLongType(PrimitiveType type) {
        TL.get().setLongType(type);
    }

    @Override
    public void setDoubleType(PrimitiveType type) {
        TL.get().setDoubleType(type);
    }

    @Override
    public void setBooleanType(PrimitiveType type) {
        TL.get().setBooleanType(type);
    }

    @Override
    public void setStringType(PrimitiveType type) {
        TL.get().setStringType(type);
    }

    @Override
    public void setTimeType(PrimitiveType type) {
        TL.get().setTimeType(type);
    }

    @Override
    public void setVoidType(PrimitiveType type) {
        TL.get().setVoidType(type);
    }

    @Override
    public void setNullType(PrimitiveType type) {
        TL.get().setNullType(type);
    }

    @Override
    public void setPasswordType(PrimitiveType type) {
        TL.get().setPasswordType(type);
    }

    @Override
    public void setNeverType(NeverType type) {
        TL.get().setNeverType(type);
    }

    @Override
    public void setAnyType(AnyType type) {
        TL.get().setAnyType(type);
    }

    @Override
    public void setAnyArrayType(ArrayType type) {
        TL.get().setAnyArrayType(type);
    }

    @Override
    public void setNeverArrayType(ArrayType type) {
        TL.get().setNeverArrayType(type);
    }

    @Override
    public void setNullableAnyType(UnionType type) {
        TL.get().setNullableAnyType(type);
    }

    @Override
    public void setNullableStringType(UnionType type) {
        TL.get().setNullableStringType(type);
    }

    @Override
    public void setEnumType(ClassType type) {
        TL.get().setEnumType(type);
    }

    @Override
    public void setThrowableType(ClassType type) {
        TL.get().setThrowableType(type);
    }

    @Override
    public void setExceptionType(ClassType type) {
        TL.get().setExceptionType(type);
    }

    @Override
    public void setRuntimeExceptionType(ClassType type) {
        TL.get().setRuntimeExceptionType(type);
    }

    @Override
    public void setEntityType(ClassType type) {
        TL.get().setEntityType(type);
    }

    @Override
    public void setRecordType(ClassType type) {
        TL.get().setRecordType(type);
    }

    @Override
    public void setCollectionType(ClassType type) {
        TL.get().setCollectionType(type);
    }

    @Override
    public void setSetType(ClassType type) {
        TL.get().setSetType(type);
    }

    @Override
    public void setListType(ClassType type) {
        TL.get().setListType(type);
    }

    @Override
    public void setMapType(ClassType type) {
        TL.get().setMapType(type);
    }

    @Override
    public void setIteratorType(ClassType type) {
        TL.get().setIteratorType(type);
    }

    @Override
    public void setIteratorImplType(ClassType type) {
        TL.get().setIteratorImplType(type);
    }

    @Override
    public void setReadonlyAnyArrayType(ArrayType type) {
        TL.get().setReadonlyAnyArrayType(type);
    }

    @Override
    public void setReadWriteListType(ClassType type) {
        TL.get().setReadWriteListType(type);
    }

    @Override
    public void setChildListType(ClassType type) {
        TL.get().setChildListType(type);
    }

    @Override
    public ClassType getChildListType() {
        return TL.get().getChildListType();
    }

    @Override
    public ClassType getReadWriteListType() {
        return TL.get().getReadWriteListType();
    }

    @Override
    public ClassType getIterableType() {
        return TL.get().getIterableType();
    }

    @Override
    public void setIterableType(ClassType iterableType) {
        TL.get().setIterableType(iterableType);
    }

    @Override
    public ClassType getConsumerType() {
        return TL.get().getConsumerType();
    }

    @Override
    public void setConsumerType(ClassType consumerType) {
        TL.get().setConsumerType(consumerType);
    }

}
