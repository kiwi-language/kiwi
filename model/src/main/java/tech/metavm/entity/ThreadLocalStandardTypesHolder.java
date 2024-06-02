package tech.metavm.entity;

import tech.metavm.object.type.Klass;

public class ThreadLocalStandardTypesHolder implements StandardTypesHolder {

    private final ThreadLocal<GlobalStandardTypesHolder> TL = ThreadLocal.withInitial(GlobalStandardTypesHolder::new);

    @Override
    public Klass getEnumKlass() {
        return TL.get().getEnumKlass();
    }

    @Override
    public Klass getThrowableKlass() {
        return TL.get().getThrowableKlass();
    }

    @Override
    public Klass getExceptionKlass() {
        return TL.get().getExceptionKlass();
    }

    @Override
    public Klass getRuntimeExceptionKlass() {
        return TL.get().getRuntimeExceptionKlass();
    }

    @Override
    public Klass getEntityKlass() {
        return TL.get().getEntityKlass();
    }

    @Override
    public Klass getRecordKlass() {
        return TL.get().getRecordKlass();
    }

    @Override
    public Klass getCollectionKlass() {
        return TL.get().getCollectionKlass();
    }

    @Override
    public Klass getSetKlass() {
        return TL.get().getSetKlass();
    }

    @Override
    public Klass getListKlass() {
        return TL.get().getListKlass();
    }

    @Override
    public Klass getMapKlass() {
        return TL.get().getMapKlass();
    }

    @Override
    public Klass getIteratorKlass() {
        return TL.get().getIteratorKlass();
    }

    @Override
    public Klass getIteratorImplKlass() {
        return TL.get().getIteratorImplKlass();
    }

    @Override
    public void setEnumKlass(Klass type) {
        TL.get().setEnumKlass(type);
    }

    @Override
    public void setThrowableKlass(Klass type) {
        TL.get().setThrowableKlass(type);
    }

    @Override
    public void setExceptionKlass(Klass type) {
        TL.get().setExceptionKlass(type);
    }

    @Override
    public void setRuntimeExceptionKlass(Klass type) {
        TL.get().setRuntimeExceptionKlass(type);
    }

    @Override
    public void setEntityKlass(Klass type) {
        TL.get().setEntityKlass(type);
    }

    @Override
    public void setRecordKlass(Klass type) {
        TL.get().setRecordKlass(type);
    }

    @Override
    public void setCollectionKlass(Klass type) {
        TL.get().setCollectionKlass(type);
    }

    @Override
    public void setSetKlass(Klass type) {
        TL.get().setSetKlass(type);
    }

    @Override
    public void setListKlass(Klass type) {
        TL.get().setListKlass(type);
    }

    @Override
    public void setMapKlass(Klass type) {
        TL.get().setMapKlass(type);
    }

    @Override
    public void setIteratorKlass(Klass type) {
        TL.get().setIteratorKlass(type);
    }

    @Override
    public void setIteratorImplKlass(Klass type) {
        TL.get().setIteratorImplKlass(type);
    }

    @Override
    public void setReadWriteListKlass(Klass type) {
        TL.get().setReadWriteListKlass(type);
    }

    @Override
    public void setChildListKlass(Klass type) {
        TL.get().setChildListKlass(type);
    }

    @Override
    public void setValueListKlass(Klass klass) {
        TL.get().setValueListKlass(klass);
    }

    @Override
    public Klass getChildListKlass() {
        return TL.get().getChildListKlass();
    }

    @Override
    public Klass getValueListKlass() {
        return TL.get().getValueListKlass();
    }

    @Override
    public Klass getReadWriteListKlass() {
        return TL.get().getReadWriteListKlass();
    }

    @Override
    public Klass getIterableKlass() {
        return TL.get().getIterableKlass();
    }

    @Override
    public void setIterableKlass(Klass iterableType) {
        TL.get().setIterableKlass(iterableType);
    }

    @Override
    public Klass getConsumerKlass() {
        return TL.get().getConsumerKlass();
    }

    @Override
    public void setConsumerKlass(Klass consumerType) {
        TL.get().setConsumerKlass(consumerType);
    }

    @Override
    public Klass getPredicateKlass() {
        return TL.get().getPredicateKlass();
    }

    @Override
    public void setPredicateKlass(Klass type) {
        TL.get().setPredicateKlass(type);
    }

    @Override
    public void setIllegalArgumentExceptionKlass(Klass illegalArgumentExceptionType) {
        TL.get().setIllegalArgumentExceptionKlass(illegalArgumentExceptionType);
    }

    @Override
    public void setIllegalStateExceptionKlass(Klass illegalStateExceptionType) {
        TL.get().setIllegalStateExceptionKlass(illegalStateExceptionType);
    }

    @Override
    public Klass getIllegalArgumentExceptionKlass() {
        return TL.get().getIllegalArgumentExceptionKlass();
    }

    @Override
    public Klass getIllegalStateExceptionKlass() {
        return TL.get().getIllegalStateExceptionKlass();
    }

    @Override
    public Klass getNullPointerExceptionKlass() {
        return TL.get().getNullPointerExceptionKlass();
    }

    @Override
    public void setNullPointerExceptionKlass(Klass nullPointerExceptionType) {
        TL.get().setNullPointerExceptionKlass(nullPointerExceptionType);
    }

}
