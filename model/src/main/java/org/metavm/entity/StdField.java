package org.metavm.entity;

import org.metavm.api.Index;
import org.metavm.object.type.Field;

public enum StdField {

    enumName(Enum.class, "name"),
    enumOrdinal(Enum.class, "ordinal"),
    indexName(Index.class, "name"),
    exceptionDetailMessage(Exception.class, "detailMessage"),
    exceptionCause(Exception.class, "cause"),
    ;

    private final Field field;

    StdField(Class<?> javaClass, String fieldName) {
        var klass = StdKlassRegistry.instance.getKlass(javaClass);
        field = klass.getFieldByName(fieldName);

    }

    public Field get() {
        return field;
    }

}
