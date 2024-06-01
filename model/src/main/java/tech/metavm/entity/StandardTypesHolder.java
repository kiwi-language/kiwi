package tech.metavm.entity;

import tech.metavm.object.type.Klass;

public interface StandardTypesHolder {
    
    Klass getEnumKlass();

    Klass getThrowableKlass();

    Klass getExceptionKlass();

    Klass getRuntimeExceptionKlass();

    Klass getEntityKlass();

    Klass getRecordKlass();

    Klass getCollectionKlass();

    Klass getSetKlass();

    Klass getListKlass();

    Klass getMapKlass();

    Klass getIteratorKlass();

    Klass getIteratorImplKlass();

    void setEnumKlass(Klass type);

    void setThrowableKlass(Klass type);

    void setExceptionKlass(Klass type);

    void setRuntimeExceptionKlass(Klass type);

    void setEntityKlass(Klass type);

    void setRecordKlass(Klass type);

    void setCollectionKlass(Klass type);

    void setSetKlass(Klass type);

    void setListKlass(Klass type);

    void setMapKlass(Klass type);

    void setIteratorKlass(Klass type);

    void setIteratorImplKlass(Klass type);

    void setReadWriteListKlass(Klass type);

    void setChildListKlass(Klass type);

    Klass getChildListKlass();

    Klass getReadWriteListKlass();

    Klass getIterableKlass();

    void setIterableKlass(Klass iterableType);

    Klass getConsumerKlass();

    void setConsumerKlass(Klass consumerType);

    Klass getPredicateKlass();

    void setPredicateKlass(Klass type);

    void setIllegalArgumentExceptionKlass(Klass illegalArgumentExceptionType);

    void setIllegalStateExceptionKlass(Klass illegalStateExceptionType);

    Klass getIllegalArgumentExceptionKlass();

    Klass getIllegalStateExceptionKlass();

    Klass getNullPointerExceptionKlass();

    void setNullPointerExceptionKlass(Klass nullPointerExceptionType);

}
