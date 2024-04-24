package tech.metavm.object.type;

import tech.metavm.util.NncUtils;

public enum ClassKind {
    CLASS(1, TypeCategory.CLASS),
    ENUM(2, TypeCategory.ENUM),
    INTERFACE(3, TypeCategory.INTERFACE),
    VALUE(4, TypeCategory.VALUE)
    ;

    private final int code;

    private final TypeCategory typeCategory;

    ClassKind(int code, TypeCategory typeCategory) {
        this.code = code;
        this.typeCategory = typeCategory;
    }

    public int code() {
        return code;
    }

    public TypeCategory typeCategory() {
        return typeCategory;
    }

    public static ClassKind fromCode(int code) {
        return NncUtils.findRequired(values(), v -> v.code == code);
    }

    public static ClassKind fromTypeCategory(TypeCategory typeCategory) {
        return NncUtils.findRequired(values(), v -> v.typeCategory == typeCategory);
    }

}
