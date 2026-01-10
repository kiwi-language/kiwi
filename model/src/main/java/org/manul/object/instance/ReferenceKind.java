package org.manul.object.instance;

import org.manul.object.type.Type;

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
        return STRONG;//type.isNullable() ? WEAK : STRONG;
    }
}
