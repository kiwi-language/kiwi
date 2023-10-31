package tech.metavm.object.instance.rest;

import tech.metavm.util.NncUtils;

public enum FieldValueKind {

    PRIMITIVE(1, PrimitiveFieldValue.class),
    REFERENCE(2, ReferenceFieldValueDTO.class),
    ARRAY(3, ArrayFieldValueDTO.class),
    INSTANCE_DTO(4, InstanceFieldValueDTO.class),
    EXPRESSION(5, ExpressionFieldValueDTO.class)
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
