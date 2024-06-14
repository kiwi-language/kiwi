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

    void setEnumKlass(Klass klass);

    void setThrowableKlass(Klass klass);

    void setExceptionKlass(Klass klass);

    void setRuntimeExceptionKlass(Klass klass);

    void setEntityKlass(Klass klass);

    void setRecordKlass(Klass klass);

    void setCollectionKlass(Klass klass);

    void setSetKlass(Klass klass);

    void setListKlass(Klass klass);

    void setMapKlass(Klass klass);

    void setIteratorKlass(Klass klass);

    void setIteratorImplKlass(Klass klass);

    void setReadWriteListKlass(Klass klass);

    void setChildListKlass(Klass klass);

    void setValueListKlass(Klass klass);

    Klass getChildListKlass();

    Klass getValueListKlass();

    Klass getReadWriteListKlass();

    Klass getIterableKlass();

    void setIterableKlass(Klass klass);

    Klass getConsumerKlass();

    void setConsumerKlass(Klass klass);

    Klass getPredicateKlass();

    void setPredicateKlass(Klass klass);

    void setIllegalArgumentExceptionKlass(Klass klass);

    void setIllegalStateExceptionKlass(Klass klass);

    Klass getIllegalArgumentExceptionKlass();

    Klass getIllegalStateExceptionKlass();

    Klass getNullPointerExceptionKlass();

    void setNullPointerExceptionKlass(Klass klass);

    Klass getInterceptorKlass();

    void setInterceptorKlass(Klass klass);
}
