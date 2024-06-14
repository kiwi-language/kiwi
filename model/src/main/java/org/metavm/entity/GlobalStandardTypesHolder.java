package org.metavm.entity;

import org.metavm.object.type.Klass;

public class GlobalStandardTypesHolder implements StandardTypesHolder {

    private Klass enumType;
    private Klass throwableType;
    private Klass exceptionType;
    private Klass runtimeExceptionType;
    private Klass entityType;
    private Klass recordType;
    private Klass collectionType;
    private Klass setType;
    private Klass listType;
    private Klass mapType;
    private Klass iteratorType;
    private Klass iteratorImplType;
    private Klass childListType;
    private Klass valueListKlass;
    private Klass readWriteListType;
    private Klass iterableType;
    private Klass consumerType;
    private Klass predicateType;
    private Klass illegalArgumentExceptionType;
    private Klass illegalStateExceptionType;
    private Klass nullPointerExceptionType;
    private Klass interceptorKlass;

    @Override
    public Klass getEnumKlass() {
        return enumType;
    }

    @Override
    public Klass getThrowableKlass() {
        return throwableType;
    }

    @Override
    public Klass getExceptionKlass() {
        return exceptionType;
    }

    @Override
    public Klass getRuntimeExceptionKlass() {
        return runtimeExceptionType;
    }

    @Override
    public Klass getEntityKlass() {
        return entityType;
    }

    @Override
    public Klass getRecordKlass() {
        return recordType;
    }

    @Override
    public Klass getCollectionKlass() {
        return collectionType;
    }

    @Override
    public Klass getSetKlass() {
        return setType;
    }

    @Override
    public Klass getListKlass() {
        return listType;
    }

    @Override
    public Klass getMapKlass() {
        return mapType;
    }

    @Override
    public Klass getIteratorKlass() {
        return iteratorType;
    }

    @Override
    public Klass getIteratorImplKlass() {
        return iteratorImplType;
    }

    @Override
    public void setEnumKlass(Klass type) {
        enumType = type;
    }

    @Override
    public void setThrowableKlass(Klass type) {
        throwableType = type;
    }

    @Override
    public void setExceptionKlass(Klass type) {
        exceptionType = type;
    }

    @Override
    public void setRuntimeExceptionKlass(Klass type) {
        runtimeExceptionType = type;
    }

    @Override
    public void setEntityKlass(Klass type) {
        entityType = type;
    }

    @Override
    public void setRecordKlass(Klass type) {
        recordType = type;
    }

    @Override
    public void setCollectionKlass(Klass type) {
        collectionType = type;
    }

    @Override
    public void setSetKlass(Klass type) {
        setType = type;
    }

    @Override
    public void setListKlass(Klass type) {
        listType = type;
    }

    @Override
    public void setMapKlass(Klass type) {
        mapType = type;
    }

    @Override
    public void setIteratorKlass(Klass type) {
        iteratorType = type;
    }

    @Override
    public void setIteratorImplKlass(Klass type) {
        iteratorImplType = type;
    }

    @Override
    public void setChildListKlass(Klass type) {
        childListType = type;
    }

    @Override
    public void setValueListKlass(Klass klass) {
        valueListKlass = klass;
    }

    @Override
    public void setReadWriteListKlass(Klass type) {
        readWriteListType = type;
    }

    @Override
    public Klass getChildListKlass() {
        return childListType;
    }

    @Override
    public Klass getValueListKlass() {
        return valueListKlass;
    }

    @Override
    public Klass getReadWriteListKlass() {
        return readWriteListType;
    }

    @Override
    public Klass getIterableKlass() {
        return iterableType;
    }

    @Override
    public void setIterableKlass(Klass iterableType) {
        this.iterableType = iterableType;
    }

    @Override
    public Klass getConsumerKlass() {
        return consumerType;
    }

    @Override
    public void setConsumerKlass(Klass consumerType) {
        this.consumerType = consumerType;
    }

    @Override
    public Klass getPredicateKlass() {
        return predicateType;
    }

    @Override
    public void setPredicateKlass(Klass type) {
        predicateType = type;
    }

    @Override
    public void setIllegalArgumentExceptionKlass(Klass illegalArgumentExceptionType) {
        this.illegalArgumentExceptionType = illegalArgumentExceptionType;
    }

    @Override
    public void setIllegalStateExceptionKlass(Klass illegalStateExceptionType) {
        this.illegalStateExceptionType = illegalStateExceptionType;
    }

    @Override
    public Klass getIllegalArgumentExceptionKlass() {
        return illegalArgumentExceptionType;
    }

    @Override
    public Klass getIllegalStateExceptionKlass() {
        return illegalStateExceptionType;
    }

    @Override
    public Klass getNullPointerExceptionKlass() {
        return nullPointerExceptionType;
    }

    @Override
    public void setNullPointerExceptionKlass(Klass nullPointerExceptionType) {
        this.nullPointerExceptionType = nullPointerExceptionType;
    }

    @Override
    public Klass getInterceptorKlass() {
        return interceptorKlass;
    }

    @Override
    public void setInterceptorKlass(Klass klass) {
        this.interceptorKlass = klass;
    }

}
