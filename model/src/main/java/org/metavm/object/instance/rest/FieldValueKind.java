package org.metavm.object.instance.rest;

import org.metavm.util.NncUtils;

public enum FieldValueKind {

    PRIMITIVE(1, PrimitiveFieldValue.class),
    REFERENCE(2, ReferenceFieldValue.class),
    ARRAY(3, ArrayFieldValue.class),
    INSTANCE_DTO(4, InstanceFieldValue.class),
    EXPRESSION(5, ExpressionFieldValue.class),
    LIST(7, ListFieldValue.class),

    ;

    private final int code;
    private final Class<?> dtoClass;

    FieldValueKind(int code, Class<?> dtoClass) {
        this.code = code;
        this.dtoClass = dtoClass;
    }

    public int code() {
        return code;
    }

    public static FieldValueKind getByCode(int code) {
        return NncUtils.findRequired(values(), v -> v.code == code);
    }

    public static FieldValueKind getByDTOClass(Class<?> dtoClass) {
        return NncUtils.findRequired(values(), v -> v.dtoClass == dtoClass);
    }

}
