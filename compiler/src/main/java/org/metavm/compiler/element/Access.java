package org.metavm.compiler.element;

public enum Access {
    PUBLIC(1),
    PACKAGE(2),
    PRIVATE(3),
    PROTECTED(4),
    INTERNAL(5),

    ;

    private final int code;

    Access(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }
}
