package tech.metavm.object.instance;

import tech.metavm.object.meta.Type;

public enum ReferenceKind {

    STRONG(1),
    WEAK(2),

    ;

    private final int code;

    ReferenceKind(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }

    public static ReferenceKind getFromType(Type type) {
        return type.isUnionNullable() ? WEAK : STRONG;
    }
}
